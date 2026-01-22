package com.example.cyclographart

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * MyGLSurfaceView là một View tùy chỉnh (Custom View).
 * Nó kế thừa từ GLSurfaceView để cung cấp một bề mặt vẽ chuyên dụng cho OpenGL.
 * * Lưu ý: Chúng ta cần constructor có (Context, AttributeSet) để có thể
 * đặt View này vào trong file giao diện XML (activity_main.xml).
 */
class MyGLSurfaceView(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {

    // Khai báo Renderer là public để MainActivity có thể truy cập
    // và thay đổi các biến R, r, d bên trong nó.
    val renderer: MyGLRenderer

    init {
        // 1. Tạo OpenGL ES 3.0 context
        // Đây là bước bắt buộc để khớp với cấu hình trong AndroidManifest
        setEGLContextClientVersion(3)

        // 2. Khởi tạo Renderer (Động cơ vẽ)
        renderer = MyGLRenderer(context)

        // 3. Gán Renderer cho View này
        setRenderer(renderer)

        // 4. Cấu hình chế độ Render
        // RENDERMODE_CONTINUOUSLY: Vẽ lại liên tục (60fps).
        // Phù hợp cho chế độ "Vẽ động" (Animation) mà chúng ta đang làm.
        // Nếu chỉ vẽ hình tĩnh, có thể dùng RENDERMODE_WHEN_DIRTY để tiết kiệm pin.
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}