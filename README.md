# 融合广告 Demo

这是一个 Android 广告接入示例工程，包名为 `com.union_test.toutiao`，用于演示穿山甲广告 SDK 及聚合广告能力的初始化、启动和多广告样式展示。

工程包含开屏、信息流、Draw、Banner、插屏、激励视频、全屏视频、贴片广告等示例，同时集成了多个广告网络的 AAR 依赖和聚合 Adapter。

## 项目概览

- 应用名称：融合Demo
- 应用包名：`com.union_test.toutiao`
- 当前版本：`4.0.0.0`
- 最低系统版本：Android 5.0，`minSdkVersion 21`
- 目标系统版本：Android 11，`targetSdkVersion 30`
- 编译 SDK：`compileSdkVersion 30`
- 构建工具：Android Gradle Plugin `4.0.1`，Gradle `6.5`
- 主模块：`:app`

## 功能特性

- 穿山甲 SDK 初始化与启动流程示例
- 穿山甲广告 Demo 入口
- 聚合广告 Demo 入口
- Java 和 Kotlin 示例 Activity
- 支持多种广告形式：
  - 开屏广告
  - 信息流广告
  - Draw 广告
  - Banner 广告
  - 插屏广告
  - 新插屏广告
  - 激励视频广告
  - 全屏视频广告
  - 贴片广告
  - 自定义广告聚合
- 集成多个广告平台相关依赖：
  - 穿山甲
  - AdMob
  - 百度
  - 优量汇/GDT
  - 快手
  - Klevin
  - Mintegral
  - Sigmob
  - Unity Ads

## 目录结构

```text
.
├── app/                         # Android 应用模块
│   ├── libs/                    # 本地 AAR/JAR 依赖
│   │   ├── adn/                 # 第三方广告平台 SDK
│   │   └── adapter/             # 聚合 Adapter
│   ├── release/                 # 已生成的 Release APK
│   ├── src/
│   │   ├── androidTest/         # Android 测试代码
│   │   └── main/
│   │       ├── assets/          # SDK 配置资源
│   │       ├── java/            # Java/Kotlin 示例代码
│   │       └── res/             # 布局、图片、字符串等资源
│   ├── build.gradle             # app 模块构建配置
│   ├── open_ad_sdk.keystore     # Demo 签名文件
│   └── proguard-rules.pro       # 混淆配置
├── gradle/wrapper/              # Gradle Wrapper
├── build.gradle                 # 根工程构建配置
├── gradle.properties            # Gradle 配置
├── settings.gradle              # 模块声明
├── gradlew                      # Linux/macOS 构建脚本
└── gradlew.bat                  # Windows 构建脚本
```

## 环境要求

- Android Studio 4.x 或兼容版本
- JDK 8
- Android SDK Platform 30
- Android Build Tools 30.0.2
- Gradle Wrapper 会自动使用 Gradle 6.5

首次打开项目时，请确认 `local.properties` 中的 `sdk.dir` 指向本机 Android SDK 路径。

## 快速开始

### 1. 克隆或打开项目

使用 Android Studio 打开当前目录：

```text
c:\Users\Welays\Downloads\T1\demo
```

等待 Gradle Sync 完成。

### 2. 配置 SDK 与广告参数

穿山甲 SDK 初始化配置位于：

```text
app/src/main/java/com/union_test/toutiao/config/TTAdManagerHolder.java
```

关键配置示例：

```java
new TTAdConfig.Builder()
        .appId("5001121")
        .appName("APP测试媒体")
        .debug(true)
        .useMediation(true)
        .build();
```

接入正式业务时，请替换为平台申请的真实 `appId`、广告位 ID 和应用名称。上线前建议关闭 `debug`。

### 3. 构建 Debug 包

Windows：

```powershell
.\gradlew.bat assembleDebug
```

Linux/macOS：

```bash
./gradlew assembleDebug
```

构建产物通常位于：

```text
app/build/outputs/apk/debug/
```

### 4. 构建 Release 包

Windows：

```powershell
.\gradlew.bat assembleRelease
```

Linux/macOS：

```bash
./gradlew assembleRelease
```

当前仓库已包含一个 Release 包：

```text
app/release/app-release.apk
```

## 运行流程

应用启动入口为：

```text
app/src/main/java/com/union_test/toutiao/activity/StartActivity.java
```

启动后流程：

1. 点击“初始化穿山甲SDK”。
2. 点击“开始展示广告”。
3. 进入 `SelectActivity`。
4. 选择穿山甲广告 Demo 或聚合广告 Demo。

相关入口：

- `StartActivity`：SDK 初始化与启动页
- `SelectActivity`：选择穿山甲 Demo 或聚合 Demo
- `MainActivity`：穿山甲广告示例入口
- `MediationMainActivity`：聚合广告示例入口

## 关键配置

### Gradle 配置

根工程配置：

```text
build.gradle
```

主要内容：

- Android Gradle Plugin：`com.android.tools.build:gradle:4.0.1`
- Kotlin Gradle Plugin：`org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20`
- Maven 仓库包含阿里云、Google、JCenter、JitPack、Mintegral、Pangle 等源

app 模块配置：

```text
app/build.gradle
```

主要内容：

- `compileSdkVersion 30`
- `minSdkVersion 21`
- `targetSdkVersion 30`
- `multiDexEnabled true`
- 本地 AAR/JAR 依赖通过 `flatDir` 引入
- Debug 和 Release 均使用 Demo 签名配置

### Manifest 配置

Manifest 文件位于：

```text
app/src/main/AndroidManifest.xml
```

包含内容：

- 网络、存储、定位、安装包查询等权限声明
- SDK 相关 Activity、Provider、Service 声明
- AdMob、百度、GDT、Klevin、Mintegral、Sigmob 等平台组件声明
- 应用入口 Activity：`.activity.StartActivity`

## 测试

运行 Android Instrumentation Test：

```powershell
.\gradlew.bat connectedAndroidTest
```

测试代码目录：

```text
app/src/androidTest/
```

## 注意事项

- 当前项目包含 Demo 用广告位、应用 ID 和签名配置，正式上线前请替换为生产配置。
- `TTAdConfig.Builder().debug(true)` 仅适合调试阶段，上线前应关闭。
- 使用聚合功能时，`.useMediation(true)` 必须开启。
- 部分权限涉及隐私合规，例如定位、设备信息、应用安装列表等，请在隐私政策和权限弹窗中明确说明用途。
- `QUERY_ALL_PACKAGES` 权限在 Android 11 及以上有合规要求，正式发布前需确认应用商店审核规则。
- 本地 AAR/JAR 依赖存放在 `app/libs/`，缺失或版本不匹配可能导致编译失败。

## 常见问题

### Gradle Sync 失败

请检查：

- Android SDK 是否安装 Platform 30 和 Build Tools 30.0.2
- JDK 是否为 8 或兼容版本
- Maven 仓库网络是否可访问
- `local.properties` 中的 `sdk.dir` 是否正确

### 编译提示找不到 AAR

请确认依赖文件存在于：

```text
app/libs/
app/libs/adn/
app/libs/adapter/
```

`app/build.gradle` 通过 `flatDir` 引入这些本地依赖。

### 广告无法展示

请检查：

- 是否先完成 SDK 初始化和启动
- `appId` 和广告位 ID 是否正确
- 设备网络是否可用
- 对应广告平台 SDK 或 Adapter 是否完整
- 测试设备、流量分组和后台配置是否生效

## 免责声明

本项目为广告 SDK 接入 Demo，仅用于学习、调试和验证接入流程。正式上线前请根据业务需求完成广告位替换、隐私合规、权限说明、混淆规则、签名配置和稳定性测试。
