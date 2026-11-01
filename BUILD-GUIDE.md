# LightFit Android · 装机与构建指南（傻瓜式）

> 从零到手机上跑起来，照顺序做，每步都有“看到什么算成功”。

## 第 0 步：安装 Android Studio（一次，约 1.5GB）

1. 浏览器打开 https://developer.android.com/studio
2. 点大大的 **Download Android Studio** 按钮，勾选同意条款 → 下载（约 1GB）
3. 双击安装包 → 一直「Next」→ 选 Standard 安装 → Finish
4. 首次打开会提示 **Downloading Components**（下载 Android SDK）→ 等它完成
   - 看到欢迎页（Welcome to Android Studio）即成功

## 第 1 步：打开工程

1. Welcome 页点 **Open**（或 File → Open）
2. 选择文件夹：`D:\2026实习&秋招\UN远程实习\项目故事\github\LightFit-Android`
   - ⚠️ 选**这个文件夹本身**（里面有 settings.gradle），不要进到 app 子目录
3. 弹窗 **Trust Project** → Trust
4. 右下角出现 Gradle 进度条（首次 3-10 分钟）
   - 若弹 **Install missing SDK Platform 34** → Accept → Install
   - 若弹 **Gradle sync failed** → 把红色错误发我，不要自己乱改

## 第 2 步：Build APK

1. 顶部菜单 **Build → Build APK(s)**
2. 成功后右下角绿条：**APK(s) generated successfully**
   - 产物路径：`github\LightFit-Android\app\build\outputs\apk\debug\app-debug.apk`

## 第 3 步：装到手机

任选一种：
- **微信**：把 app-debug.apk 发到「文件传输助手」→ 手机微信里点开 → 允许安装未知来源
- **USB 线**：手机开「开发者选项→USB 调试」→ 插电脑 → Android Studio 里 Build → 直接点 ▶ 绿色三角形跑真机

## 常见报错速查

| 报错 | 处理 |
|---|---|
| Could not resolve ... gradle:8.2.2 | 网络问题，菜单 File → Sync Project with Gradle Files 重试 1-2 次 |
| SDK location not found | 第 0 步没装完 SDK，重开 AS 等它装完 |
| `:app:compileDebugJavaWithJavac FAILED` | 把红色代码错误**整段发我**，我改代码 |
| 手机上装不上（解析包错误） | 重新拷贝 APK（传输损坏），确认是 app-debug.apk |

## 第 4 步：验收

装好后打开 APP，按仓库根目录 **`TEST-PLAN.md`** 逐项验收（语言/计划/运动库/体态 AI/隐私）。
每项勾选通过或按格式报 bug。