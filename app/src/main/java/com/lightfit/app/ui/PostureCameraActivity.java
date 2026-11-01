package com.lightfit.app.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import com.lightfit.app.R;
import com.lightfit.app.store.SecureStore;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.Locale;

/**
 * 端侧 AI 体态扫描：CameraX + ML Kit 姿态检测（离线模型）。
 * 隐私：帧只在本机推理，不落盘、不上传；仅保存两个匿名数值指标。
 */
@ExperimentalGetImage
public class PostureCameraActivity extends AppCompatActivity {

    private static final int REQ_CAM = 101;

    private PreviewView previewView;
    private PostureOverlayView overlay;
    private TextView tvMetrics;
    private PoseDetector detector;

    private float lastTiltDeg = -1f;
    private float lastHeadRatio = -1f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture_camera);
        previewView = findViewById(R.id.preview);
        overlay = findViewById(R.id.overlay);
        tvMetrics = findViewById(R.id.tv_metrics);
        Button save = findViewById(R.id.btn_save_scan);
        save.setOnClickListener(v -> saveAndClose());

        detector = PoseDetection.getClient(
                new PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAM);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyze);

                provider.unbindAll();
                provider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, R.string.lf_scan_need_cam, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyze(ImageProxy imageProxy) {
        try {
            if (imageProxy.getImage() != null) {
                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(),
                        imageProxy.getImageInfo().getRotationDegrees());
                detector.process(image)
                        .addOnSuccessListener(pose -> onPose(pose,
                                imageProxy.getWidth(), imageProxy.getHeight()))
                        .addOnFailureListener(e -> { });
            }
        } catch (Exception ignored) {
        } finally {
            imageProxy.close();
        }
    }

    private void onPose(Pose pose, int w, int h) {
        overlay.setPose(pose, w, h);
        PoseLandmark ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
        if (ls == null || rs == null || nose == null) return;

        float dx = rs.getPosition().x - ls.getPosition().x;
        float dy = rs.getPosition().y - ls.getPosition().y;
        float shoulderW = (float) Math.hypot(dx, dy);
        if (shoulderW < 1f) return;

        float tilt = (float) Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(dx)));
        float midY = (ls.getPosition().y + rs.getPosition().y) / 2f;
        float headRatio = (midY - nose.getPosition().y) / shoulderW;
        lastTiltDeg = tilt;
        lastHeadRatio = headRatio;

        runOnUiThread(() -> tvMetrics.setText(
                getString(R.string.lf_scan_tilt) + ": "
                        + String.format(Locale.US, "%.1f", tilt)
                        + "   " + getString(R.string.lf_scan_head) + ": "
                        + String.format(Locale.US, "%.2f", headRatio)));
    }

    private void saveAndClose() {
        if (lastTiltDeg < 0) {
            Toast.makeText(this, R.string.lf_scan_no_pose, Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("tilt", lastTiltDeg);
            o.put("head", lastHeadRatio);
            o.put("t", System.currentTimeMillis());
        } catch (Exception ignored) { }
        new SecureStore(this).put(this, "posture_scan", o.toString());
        Toast.makeText(this, R.string.lf_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAM && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, R.string.lf_scan_need_cam, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) detector.close();
    }
}
