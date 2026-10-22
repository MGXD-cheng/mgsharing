# MGsharing 📲

> 局域网文件分享 —— 手机多选文件，浏览器扫码即下。

同一 Wi-Fi 下，把手机上的文件（图片 / 视频 / 音频 / APK / 文档 / 压缩包）一键分享给同网络的任何设备：**无需流量、无需注册、打开浏览器就能下载**。

## ✨ 功能特性

- 📁 **多选分享** —— 系统文件选择器多选文件，自动去重，7 类文件图标识别
- 🌐 **Web 下载页** —— 内置 HTTP 服务，Material 风格下载页（深浅色自适应），流式下载不占内存
- 🔐 **密码保护** —— 全局统一密码 + Cookie 会话鉴权（HttpOnly / SameSite=Strict），防止局域网路人乱下载
- 📱 **二维码连接** —— 一键生成链接二维码，同网设备扫码直达，免手输 IP
- 📋 **剪贴板复制** —— 链接一键复制，随口粘给任何人
- 📤 **接收外部分享** —— 系统分享面板选 MGsharing，其他 App 的文件直接进分享列表
- 🕘 **分享历史** —— 时间线记录每次分享的文件、大小与时长
- 🎨 **Material 3** —— Teal 主题 + Material You 动态取色（Android 12+）+ 深色模式三态
- 🔔 **前台服务保活** —— 分享期间常驻通知，附"停止分享"快捷按钮

## 📥 下载

| 渠道 | 说明 |
|---|---|
| [GitHub Releases](https://github.com/MGXD-cheng/mgsharing/releases) | 直连下载 |
| 国内镜像 | 在上面 Release 链接前拼接：`https://gh-proxy.com/https://github.com/...` |

## 🛠️ 构建

```bash
git clone https://github.com/MGXD-cheng/mgsharing.git
cd mgsharing
./gradlew assembleDebug
# 产出：app/build/outputs/apk/debug/app-debug.apk
```

环境要求：JDK 17 + Android SDK Platform 35（ARM64 Linux 构建需替换 AAPT2）。

## 📖 使用方法

1. 打开 MGsharing，点击「选择文件」多选要分享的文件
2. （可选）在「设置」里设定访问密码与端口
3. 点击「开始分享」，复制链接或展示二维码
4. 同一 Wi-Fi 下的设备用浏览器打开链接 → 输密码 → 下载

## 🏗️ 技术架构

| 层级 | 方案 |
|---|---|
| 语言 / UI | Kotlin 2.3 · Jetpack Compose · Material 3 |
| HTTP 服务 | NanoHTTPD 2.3.1（GET / POST /login / GET /download/{id}） |
| 二维码 | ZXing 3.5.3 |
| 架构 | Single-Activity · Navigation Compose · ViewModel · DataStore |
| 状态 | ServerState 单一数据源（StateFlow 驱动 UI） |

```
com.lingxi.mgsharing
├── data/        文件模型 · 设置存储 · 历史记录
├── server/      NanoHTTPD 服务 · Web 页面 · 前台服务
├── viewmodel/   Share / Settings / History 三个 ViewModel
└── ui/          主题 · 组件四件套 · 三个页面 · 导航
```

## 📄 License

本项目以 [MIT License](LICENSE) 开源。

---

MGXD 出品 · 一人一码 🐱