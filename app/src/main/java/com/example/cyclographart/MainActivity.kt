package com.example.cyclographart

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Tham chiếu đến khung hiển thị OpenGL
    private lateinit var glSurfaceView: MyGLSurfaceView

    // Tham chiếu trực tiếp đến Renderer để gửi lệnh cho nhanh
    private lateinit var renderer: MyGLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nạp giao diện từ file XML
        setContentView(R.layout.activity_main)

        // 1. Ánh xạ View từ XML
        glSurfaceView = findViewById(R.id.gl_surface_view)

        // Lấy đối tượng renderer từ bên trong SurfaceView ra để dễ thao tác
        renderer = glSurfaceView.renderer

        // 2. Thiết lập các thanh trượt (SeekBars)
        setupControlR()
        setupControlSmallR()
        setupControlD()

        // 3. Thiết lập công tắc chế độ (Switch)
        setupSwitchMode()
    }

    // --- Cấu hình thanh trượt Bán kính tĩnh (R) ---
    private fun setupControlR() {
        val seekBarR = findViewById<SeekBar>(R.id.seekBarR)
        seekBarR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // SeekBar trả về 0-100. Ta chia 100 để lấy giá trị float 0.0 - 1.0
                // Nhân thêm hệ số đê hình to hơn (ví dụ * 0.8)
                renderer.radiusR = (progress / 100.0f) * 0.8f

                // Nếu đang ở chế độ động, reset để vẽ lại từ đầu với hình dáng mới
                renderer.resetAnimation()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // --- Cấu hình thanh trượt Bán kính động (r) ---
    private fun setupControlSmallR() {
        val seekBar_r = findViewById<SeekBar>(R.id.seekBar_r)
        seekBar_r.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Giá trị r quyết định số cánh hoa
                renderer.radiusr = (progress / 100.0f) * 0.5f
                renderer.resetAnimation()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // --- Cấu hình thanh trượt Khoảng cách bút (d) ---
    private fun setupControlD() {
        val seekBarD = findViewById<SeekBar>(R.id.seekBarD)
        seekBarD.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Giá trị d quyết định độ phồng/nhọn của cánh hoa
                renderer.distanceD = (progress / 100.0f) * 0.5f
                renderer.resetAnimation()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // --- Cấu hình công tắc Chế độ (Animation) ---
    private fun setupSwitchMode() {
        val switchMode = findViewById<Switch>(R.id.switchMode)

        // Cập nhật trạng thái ban đầu
        renderer.isAnimationMode = switchMode.isChecked

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            // Gửi lệnh thay đổi chế độ xuống Renderer
            renderer.isAnimationMode = isChecked

            // Vẽ lại từ đầu để người dùng thấy sự thay đổi rõ ràng
            renderer.resetAnimation()
        }
    }

    // --- QUẢN LÝ VÒNG ĐỜI (LIFECYCLE) ---
    // Đây là yêu cầu bắt buộc khi dùng GLSurfaceView để tiết kiệm pin
    // và tránh lỗi khi người dùng thoát app ra ngoài.

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume() // Tiếp tục vẽ khi quay lại app
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause() // Dừng vẽ khi thoát app/tắt màn hình
    }
}