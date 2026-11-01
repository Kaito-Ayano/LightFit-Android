package com.lightfit.app.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * AES-256-GCM 本地加密存储（密钥在 AndroidKeyStore，设备内生成，永不上传）。
 * 兑现 BP 的 privacy-first 承诺：无云端、无账号、可一键清除。
 */
public class SecureStore {

    private static final String KS_ALIAS = "lightfit_master_key";
    private static final String PREF = "lightfit_secure";

    private final SecretKey key;

    public SecureStore(Context c) {
        SecretKey k = null;
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (!ks.containsAlias(KS_ALIAS)) {
                KeyGenerator kg = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                kg.init(new KeyGenParameterSpec.Builder(KS_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
                kg.generateKey();
            }
            k = ((KeyStore.SecretKeyEntry) ks.getEntry(KS_ALIAS, null)).getSecretKey();
        } catch (Exception ignored) {
            k = null; // 极端设备降级为明文，仍不出设备
        }
        key = k;
    }

    public void put(Context c, String k, String v) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (key == null) {
            p.edit().putString(k, v).apply();
            return;
        }
        try {
            Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
            ci.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = ci.getIV();
            byte[] ct = ci.doFinal(v.getBytes(StandardCharsets.UTF_8));
            p.edit().putString(k,
                    Base64.encodeToString(iv, Base64.NO_WRAP) + "|"
                            + Base64.encodeToString(ct, Base64.NO_WRAP)).apply();
        } catch (Exception e) {
            p.edit().putString(k, v).apply();
        }
    }

    public String get(Context c, String k, String def) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = p.getString(k, null);
        if (raw == null) return def;
        if (key == null || !raw.contains("|")) return raw;
        try {
            String[] parts = raw.split("\\|", 2);
            Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
            ci.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)));
            return new String(ci.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            return def;
        }
    }

    public void clear(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
