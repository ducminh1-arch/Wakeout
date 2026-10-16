# ⚡ Wakeout! - Ứng Dụng Vận Động & Giãn Cơ Tại Bàn Làm Việc

> **Tạm biệt mệt mỏi, uể oải khi ngồi bàn làm việc.** Trò chơi vận động 1 phút qua camera, thử thách thăng bằng hàng ngày, hơn 3.000 bài tập giãn cơ công sở và nhắc nhở cử động nhẹ nhàng. Lấy cảm hứng từ ứng dụng đạt giải *Apple App of the Year* - **Wakeout!**.

---

## 📱 Hệ Sinh Thái Đa Nền Tảng

Wakeout được xây dựng đồng bộ trên cả 3 nền tảng với thiết kế giao diện, cơ chế nhận diện chuyển động và luồng chơi game nhất quán:

| Nền tảng | Công nghệ | Trạng thái & File cài đặt |
| :--- | :--- | :--- |
| **🤖 Android** | Kotlin, Jetpack Compose, CameraX, Google ML Kit Pose, Rung Haptic | **Cài đặt ngay**: [BreakPulse-debug.apk](file:///d:/Wakeout/BreakPulse-debug.apk) *(90.8 MB)* |
| **🍏 iOS** | Swift 5.9, SwiftUI, AVFoundation, Apple Vision (ANE), Core Haptics | **Chạy trên Xcode**: [Wakeout.xcodeproj](file:///d:/Wakeout/ios/Wakeout.xcodeproj) *(Chạy 1-Click)* |
| **🌐 Web / PWA** | Vanilla HTML5 Canvas, CSS Glassmorphism, Web Audio, Node.js | **Chạy máy chủ cục bộ**: `http://localhost:3000` qua [server.js](file:///d:/Wakeout/server.js) |

---

## ✨ 5 Tab Chức Năng Chính

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ⚡ HỆ THỐNG WAKEOUT                             │
├─────────────┬─────────────┬──────────────┬─────────────┬───────────────┤
│ 🎮 1-Min    │ 🏆 Thử      │ 🧘 Giãn cơ   │ ⏰ Nhắc nhở │ ⚡ Nhật ký    │
│    Game     │   thách     │   (Routines) │    vận động │    Watts      │
└─────────────┴─────────────┴──────────────┴─────────────┴───────────────┘
```

### 1. 🎮 Trò Chơi 1 Phút (*Watt Invaders*)
- **Mini-game giải lao tại chỗ**: Đợt vận động kéo dài đúng 60 giây, có thể ngồi chơi ngay tại ghế làm việc hoặc đứng dậy.
- **Nhận diện chuyển động On-Device**: Camera trước bắt chuyển động nghiêng đầu và cơ thể của bạn để điều khiển tàu né đạn, né thiên thạch và phi thuyền người ngoài hành tinh theo thời gian thực.
- **Phím cảm ứng mượt mà**: Phím điều hướng trái/phải (`◀️`, `▶️`) kết hợp nút **💥 PUNCH!** bắn laser kèm hiệu ứng rung phản hồi chân thật.
- **Bảng điểm chuẩn Wakeout**: Hiển thị điểm combo (`56 x28`), đợt quái (`WAVE 6`), đếm ngược `60s` và bảng vinh danh tích lũy điểm Watts khi hoàn thành.

### 2. 🏆 Thử Thách Hàng Ngày (*Daily Challenge*)
- **Camera hướng dẫn tư thế**: Hiển thị khung hình camera góc trước trực tiếp trên màn hình, giúp bạn tự theo dõi dáng tập của mình.
- **3 bài tập rèn luyện cốt lõi**:
  - `🦩 Đứng thăng bằng 1 chân (Single-Leg Balance)`: 45 giây, thưởng +50 Watts – Kích hoạt cơ bụng sâu, cơ đùi và tăng khả năng tập trung của não bộ.
  - `💪 Chống đẩy tại bàn (Desk Push-Ups)`: 30 giây, thưởng +40 Watts – Giúp săn chắc cơ ngực, vai và bắp tay sau.
  - `🪑 Ngồi xổm trên ghế (Chair Squats)`: 40 giây, thưởng +50 Watts – Tăng cường lưu thông máu vùng chân và hông.
- **Thẻ điều khiển nổi bật**: Thẻ trắng bo góc hiển thị số giây đếm ngược, số Watts đạt được và nút **Start / Pause / Reset**.

### 3. 🧘 Bài Tập Giãn Cơ (*Desk Stretches*)
- **12 bài tập giãn cơ công sở chuyên sâu** chia theo 5 vùng cơ thể:
  - `🙆 CỔ & VAI`: Nghiêng cổ kéo giãn cơ thang, Xoay khớp bả vai, Thu cằm chỉnh dáng gù cổ.
  - `🙏 CỔ TAY & BÀN TAY`: Ép cổ tay cầu nguyện, Nắm mở ngón tay giải phóng khớp.
  - `🪑 LƯNG & CỘT SỐNG`: Vặn mình trên ghế, Tư thế Mèo - Bò ngồi ghế, Vươn tay thẳng lên trần nhà.
  - `👀 MẮT & HƠI THỞ`: Quy tắc nghỉ mắt 20-20-20, Hít thở hộp (Box Breathing) thư giãn hệ thần kinh.
  - `🦵 CHÂN & TUẦN HOÀN`: Co gối chạm ngực khi ngồi, Nhón gót chân bơm máu tĩnh mạch.
- **Trình phát tập luyện**: Giao diện đếm giờ hình tròn kèm hướng dẫn từng bước cụ thể và chuông báo hoàn thành.

### 4. ⏰ Nhắc Nhở Cử Động Nhẹ (*Wiggle Reminder*)
- **Hình thành thói quen lành mạnh**: Không còn tình trạng ngồi lì nhiều giờ liên tục gây đau mỏi lưng cổ.
- **Thẻ thông báo Toast**: `Time to wiggle! Let's play a game!` (Đến giờ vận động rồi!).
- **Đồng hồ đếm ngược lớn**: Vòng tròn đồng hồ hiển thị trực quan thời gian còn lại đến lượt cử động tiếp theo.
- **Tùy chọn chu kỳ nhắc nhở**: Chọn nhanh khoảng thời gian `20 phút`, `30 phút`, `45 phút` hoặc `60 phút`.
- **Nút ⚡ WIGGLE NOW!**: Bấm để vận động nhanh ngay lập tức, kích hoạt rung phản hồi và nhận ngay +10 Watts.

### 5. ⚡ Nhật Ký Năng Lượng Watts
- **Gamification năng lượng vận động**: Mọi cử động, trò chơi và bài tập đều được quy đổi sang đơn vị **Watts**.
- **Chỉ số tổng quan**:
  - Tiến độ hôm nay: `⚡ 318 / 1.100 Watts` kèm vòng tròn hiển thị % mục tiêu.
  - Thành tích: `Chuỗi 3 ngày liên tiếp (Streak)`, `1.203 lần tập`, `29 lần vượt thử thách`.
  - `TỔNG NĂNG LƯỢNG TRỌN ĐỜI: ⚡ 971.388 Watts 🏆`.
- **Lịch Heatmap tháng 10/2026**: Nhấp vào từng ngày để xem lịch sử tích lũy năng lượng.
- **Nhật ký hoạt động (Activity Log 📋)**: Ghi lại chi tiết từng mốc thời gian hoàn thành bài tập.

---

## 🎨 Bộ Nhận Diện Thương Hiệu & Logo

- **Ý tưởng thiết kế**: Tia chớp 3D vàng kim (`Energy Pulse`) kết hợp đường sóng nhịp tim, tượng trưng cho nguồn năng lượng sạc nhanh, xua tan căng thẳng mệt mỏi trong tích tắc.
- **Bảng màu chủ đạo**:
  - **Cam Wakeout**: `#FF5B00` ➔ `#FF8800` (Năng động, tràn đầy nhiệt huyết)
  - **Xanh lá Wakeout**: `#58CC02` (Hành động, nút bắt đầu, cảm giác tích cực)
  - **Vàng kim**: `#FFC800` (Năng lượng Watts, tia lửa điện, combo điểm cao)
  - **Tím Wave**: `#8B5CF6` (Đợt sóng game, huy hiệu đặc biệt)
- **Xem trước Logo gốc**: [app_logo_512.png](file:///d:/Wakeout/app_logo_512.png) *(512x512)*

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### 🤖 Bản Android (Cài đặt file APK trực tiếp)
1. **Cài đặt file APK đã đóng gói sẵn**:
   Tải và cài trực tiếp file **[BreakPulse-debug.apk](file:///d:/Wakeout/BreakPulse-debug.apk)** vào điện thoại hoặc máy tính bảng Android (hỗ trợ Android 7.0+ trở lên).
2. **Tự biên dịch từ mã nguồn bằng Gradle**:
   ```powershell
   cd d:\Wakeout\android
   .\gradlew.bat assembleDebug
   ```
   File APK sau khi build nằm tại: `android/app/build/outputs/apk/debug/app-debug.apk`.

### 🍏 Bản iOS (Mở và chạy trên Xcode)
1. Mở file dự án Xcode trên macOS:
   ```bash
   open ios/Wakeout.xcodeproj
   ```
2. Chọn máy ảo chạy (**iPhone Simulator** như *iPhone 16 Pro*, *iPhone 15*) hoặc **Thiết bị thật (iPhone / iPad)**.
3. Bấm **`Cmd + R`** (hoặc bấm nút ▶️ **Run** trên thanh công cụ).
4. Ứng dụng sẽ tự động biên dịch, hiển thị logo khởi động và các tính năng tương tác đầy đủ.

### 🌐 Bản Web / PWA (Trải nghiệm trên trình duyệt)
1. Khởi chạy máy chủ Node.js:
   ```bash
   node server.js
   ```
2. Mở trình duyệt web tại địa chỉ:
   ```
   http://localhost:3000
   ```
3. Để thử nghiệm trên điện thoại cùng mạng Wi-Fi: Mở Safari/Chrome trên điện thoại theo địa chỉ `http://<IP_MÁY_TÍNH>:3000`, sau đó chọn **Thêm vào Màn hình chính (Add to Home Screen)** để chạy toàn màn hình như app native.

---

## 📂 Sơ Đồ Cấu Trúc Dự Án

```
d:\Wakeout
├── BreakPulse-debug.apk         # Bản cài đặt Android APK hoàn chỉnh, đã sửa lỗi
├── app_logo_512.png             # File logo chính thức độ phân giải cao 512x512
├── generate_icons.py            # Script tự động tạo icon cho Android, iOS và Web
├── generate_xcode_proj.py       # Script tự động dựng file cấu hình Wakeout.xcodeproj
├── server.js                    # Server HTTP cục bộ phục vụ bản Web
│
├── android/                     # 🤖 Ứng dụng Android Native (Jetpack Compose)
│   ├── app/src/main/
│   │   ├── AndroidManifest.xml  # Khai báo quyền (Camera, Rung VIBRATE, Thông báo)
│   │   ├── java/com/breakpulse/app/
│   │   │   ├── MainActivity.kt  # Activity chính điều hướng 5 tab
│   │   │   ├── core/            # HapticManager (chống crash rung), SoundManager, MotionEngine
│   │   │   └── ui/screens/      # GameScreen, ChallengeScreen, Routines, Reminder, Watts
│   │   └── res/
│   │       ├── drawable/        # ic_launcher_foreground.png
│   │       ├── mipmap-*/        # Đầy đủ icon vuông & tròn mọi độ phân giải (mdpi đến xxxhdpi)
│   │       └── mipmap-anydpi-v26/ # Cấu hình Adaptive Icon cho Android 8+
│   └── build.gradle.kts
│
├── ios/                         # 🍏 Ứng dụng iOS Native (SwiftUI + Apple Vision)
│   ├── Wakeout.xcodeproj/       # File dự án mở trực tiếp trên Xcode (có sẵn Scheme)
│   ├── App/
│   │   └── MoveDeskApp.swift    # Điểm khởi chạy ứng dụng SwiftUI với 5 tab
│   ├── Core/
│   │   ├── AppState.swift       # Quản lý trạng thái lưu trữ Watts, Streak, hoạt động
│   │   ├── Theme.swift          # Bảng màu chuẩn Wakeout
│   │   ├── Camera/              # MotionDetector.swift & CameraPreviewView.swift
│   │   └── Motion/              # GestureEngine, định nghĩa khớp cơ thể & OneEuroFilter
│   ├── Features/
│   │   ├── Games/               # GameView.swift (Trò chơi Watt Invaders)
│   │   ├── Challenge/           # ChallengeView.swift (Thử thách thăng bằng kèm camera)
│   │   ├── Routines/            # RoutinesView.swift (Trình tập giãn cơ tại chỗ)
│   │   ├── Reminder/            # ReminderView.swift (Đếm ngược nhắc nhở vận động)
│   │   └── Watts/               # WattsView.swift (Thống kê năng lượng & Heatmap)
│   ├── Assets.xcassets/         # Bộ AppIcon đầy đủ mọi kích cỡ cho iPhone/iPad/App Store
│   ├── LaunchScreen.storyboard  # Màn hình chờ khởi động màu cam thương hiệu
│   └── Info.plist               # Tên hiển thị Wakeout & mô tả quyền Camera
│
└── web/                         # 🌐 Ứng dụng Web / Progressive Web App (PWA)
    ├── index.html               # Giao diện responsive 2 chế độ hiển thị
    ├── style.css                # Hiệu ứng kính glassmorphism, màu sắc và typography
    ├── app.js                   # Xử lý sự kiện điều khiển và chuyển đổi các tab
    ├── games.js                 # Engine game 60 FPS chạy trên HTML5 Canvas
    ├── motion.js                # Nhận diện cử chỉ qua camera trên web
    ├── routines.js              # Cơ sở dữ liệu 12 bài tập giãn cơ
    ├── audio.js                 # Bộ phát âm thanh Web Audio
    └── favicon.png, logo.png    # Icon và logo hiển thị trên thanh trình duyệt
```

---

## 🔒 Bảo Mật & Quyền Riêng Tư

- **Quyền Camera (`CAMERA` / `NSCameraUsageDescription`)**: Chỉ được dùng cho việc nhận diện chuyển động cơ thể theo thời gian thực trực tiếp trên chip xử lý của máy (Apple Neural Engine trên iOS, Google ML Kit trên Android, MediaPipe trên Web). **Tuyệt đối không lưu lại bất kỳ hình ảnh, video nào vào bộ nhớ máy và không gửi bất kỳ dữ liệu hình ảnh nào qua mạng Internet.**
- **Quyền Rung (`android.permission.VIBRATE`)**: Đã được khai báo an toàn và bọc cơ chế xử lý ngoại lệ `try-catch`, đảm bảo phản hồi bấm nút và rung chuông báo không bao giờ gây lỗi hay văng ứng dụng.
- **Hoạt động Ngoại tuyến (Offline 100%)**: Toàn bộ trò chơi, bộ đếm giờ, bài tập và âm thanh đều chạy độc lập ngay trên thiết bị mà không cần kết nối mạng.

---

*Được thiết kế và phát triển với tinh thần mang lại sức khỏe, sự tỉnh táo và năng lượng tích cực cho người làm việc văn phòng!*
