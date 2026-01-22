# 🎨 Cyclograph Art Generator - OpenGL ES 3.0

> **Dự án Đồ họa Máy tính:** Ứng dụng Android mô phỏng vẽ hình Cyclograph (Epitrochoid) sử dụng OpenGL ES 3.0 với kỹ thuật tối ưu hóa VBO/VAO.

![OpenGL ES 3.0](https://img.shields.io/badge/OpenGL%20ES-3.0-green.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## 📖 Giới thiệu

**Cyclograph Art Generator** là một ứng dụng di động minh họa cách xây dựng hệ thống đồ họa 2D hiệu năng cao trên Android. Ứng dụng cho phép người dùng tạo ra các hình hoa văn toán học (Cyclograph/Spirograph) và tương tác với chúng trong thời gian thực.

Điểm nổi bật về mặt kỹ thuật là việc chuyển đổi từ kiến trúc cũ (Client-side Arrays) sang kiến trúc hiện đại **Server-side Rendering** sử dụng **Vertex Buffer Objects (VBO)** và **Vertex Array Objects (VAO)**, giúp tối ưu hóa băng thông CPU-GPU.

## ✨ Tính năng chính

* **Vẽ hình học tham số:** Sử dụng phương trình Epitrochoid để tạo hình dựa trên các tham số toán học.
* **Chế độ hiển thị linh hoạt:**
    * **Tĩnh (Static):** Hiển thị trọn vẹn hình vẽ ngay lập tức.
    * **Động (Animation):** Hiệu ứng "nét bút vẽ dần" (Progressive Drawing) theo thời gian thực.
* **Tương tác thời gian thực:**
    * Điều chỉnh bán kính tĩnh ($R$).
    * Điều chỉnh bán kính động ($r$).
    * Điều chỉnh khoảng cách bút vẽ ($d$).
* **Tối ưu hóa hiệu năng:** Sử dụng `GL_STATIC_DRAW` với VBO và quản lý trạng thái render bằng VAO trên nền tảng OpenGL ES 3.0.

## 🛠 Yêu cầu hệ thống

* **Android Studio:** Phiên bản Ladybug (hoặc mới hơn).
* **Android SDK:** Min SDK 24 (Android 7.0 Nougat) trở lên.
* **Thiết bị/Emulator:** Phải hỗ trợ phần cứng OpenGL ES 3.0.

## 🚀 Hướng dẫn Cài đặt & Sử dụng

Làm theo các bước sau để tải và chạy dự án trên máy của bạn:

### Bước 1: Clone dự án
Mở Terminal hoặc Git Bash và chạy lệnh sau:

```bash
git clone [https://github.com/ThanhTrung85/Computer-Graphic-Project.git](https://github.com/ThanhTrung85/Computer-Graphic-Project.git)
```

### Bước 2: Mở trong Android Studio
1.  Khởi động **Android Studio**.
2.  Chọn **File > Open**.
3.  Tìm đến thư mục `Computer-Graphic-Project` vừa clone và nhấn **OK**.
4.  Đợi Gradle đồng bộ hóa (Sync) các thư viện.

### Bước 3: Cấu hình thiết bị (Nếu dùng máy ảo)
Nếu bạn dùng Emulator, hãy đảm bảo cấu hình OpenGL ES được hỗ trợ:
* Mở **Device Manager**.
* Chỉnh sửa (Edit) thiết bị ảo của bạn.
* Trong phần **Emulated Performance > Graphics**, chọn **"Hardware - GLES 2.0"** (hệ thống sẽ tự động hỗ trợ 3.0 nếu GPU máy tính của bạn đủ mạnh) hoặc để **Automatic**.

### Bước 4: Chạy ứng dụng
* Kết nối thiết bị thật hoặc bật máy ảo.
* Nhấn nút **Run (▶)** trên thanh công cụ của Android Studio.

## 📂 Cấu trúc Dự án

```text
MyCyclographApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml      # Cấu hình glEsVersion 3.0
│   │       ├── java/.../cyclographart/
│   │       │   ├── MainActivity.kt      # Xử lý Logic UI & SeekBars
│   │       │   ├── MyGLSurfaceView.kt   # Khởi tạo Context OpenGL ES 3.0
│   │       │   ├── MyGLRenderer.kt      # Logic Render chính (VBO/VAO implementation)
│   │       │   └── ShaderHelper.kt      # Tiện ích đọc và biên dịch Shader
│   │       └── res/
│   │           ├── layout/activity_main.xml  # Giao diện người dùng (XML)
│   │           └── raw/
│   │               ├── cyclo_vertex.glsl     # GLSL 3.00 Vertex Shader (Logic toán học)
│   │               └── cyclo_fragment.glsl   # GLSL 3.00 Fragment Shader (Màu sắc)
└── build.gradle.kts                     # Quản lý thư viện và dependencies
```

## 📐 Nguyên lý Toán học & Kỹ thuật

### 1. Phương trình Epitrochoid
Tọa độ đỉnh $(x, y)$ được tính toán trong Vertex Shader dựa trên tham số góc $\theta$ (được truyền vào từ VBO) và các uniform $R, r, d$:

$$ x(\theta) = (R + r)\cos(\theta) - d\cos\left(\frac{R + r}{r}\theta\right) $$
$$ y(\theta) = (R + r)\sin(\theta) - d\sin\left(\frac{R + r}{r}\theta\right) $$

### 2. Kiến trúc Render (OpenGL ES 3.0)
* **VBO (Vertex Buffer Object):** Dữ liệu tham số góc $\theta$ được tính toán 1 lần và nạp vào bộ nhớ VRAM của GPU.
* **VAO (Vertex Array Object):** Ghi nhớ cấu hình attribute pointers, giảm thiểu code lặp lại và overhead trong vòng lặp render.
* **Draw Call Manipulation:** Hiệu ứng động được thực hiện bằng cách thay đổi tham số `count` trong hàm `glDrawArrays(GL_LINE_STRIP, 0, count)` thay vì nạp lại dữ liệu mỗi khung hình.

## 🤝 Đóng góp (Contributing)
Mọi đóng góp đều được hoan nghênh! Nếu bạn muốn cải thiện dự án:
1.  Fork dự án.
2.  Tạo nhánh tính năng mới (`git checkout -b feature/NewFeature`).
3.  Commit thay đổi của bạn (`git commit -m 'Add some NewFeature'`).
4.  Push lên nhánh (`git push origin feature/NewFeature`).
5.  Mở một Pull Request.

## 📝 License
Dự án này được thực hiện cho mục đích học tập và nghiên cứu.
