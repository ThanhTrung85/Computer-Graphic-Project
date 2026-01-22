package com.example.cyclographart

import android.content.Context
import android.opengl.GLES30 // Sử dụng thư viện OpenGL ES 3.0
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * MyGLRenderer: "Nhạc trưởng" điều khiển GPU.
 */
class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // --- CÁC BIẾN DỮ LIỆU ĐIỀU KHIỂN (Public để MainActivity chỉnh sửa) ---
    // @Volatile: Đảm bảo biến được cập nhật tức thì giữa các luồng (UI Thread & Render Thread)
    @Volatile var radiusR: Float = 0.5f   // Bán kính tĩnh
    @Volatile var radiusr: Float = 0.2f   // Bán kính động
    @Volatile var distanceD: Float = 0.3f // Khoảng cách bút
    @Volatile var isAnimationMode: Boolean = true

    // --- CÁC BIẾN QUẢN LÝ OPENGL ---
    private var programId: Int = 0 // ID của chương trình Shader sau khi biên dịch

    // Dữ liệu mảng góc theta (t) nằm ở RAM (Bộ nhớ máy)
    private lateinit var tDataBuffer: FloatBuffer

    // [VBO/VAO]: Các biến lưu trữ ID của Buffer trên GPU
    private var vboId: Int = 0 // Vertex Buffer Object: Kho chứa dữ liệu thô trên GPU
    private var vaoId: Int = 0 // Vertex Array Object: Bảng cấu hình ghi nhớ cách đọc dữ liệu

    // --- CÁC BIẾN ĐỊA CHỈ (HANDLES) TRONG SHADER ---
    // Dùng để liên kết code Kotlin với biến trong file .glsl
    private var uMatrixLocation = 0
    private var uRLocation = 0
    private var urLocation = 0
    private var udLocation = 0
    private var uColorLocation = 0
    private var aThetaLocation = 0

    // Ma trận chiếu (Projection Matrix) để xử lý tỷ lệ màn hình (chống méo)
    private val projectionMatrix = FloatArray(16)

    // --- CÁC THAM SỐ VẼ ---
    private val totalPoints = 5000      // Tổng số điểm mẫu
    private var currentDrawCount = 0    // Số điểm hiện tại được phép vẽ (Animation)
    private val drawSpeed = 5           // Tốc độ vẽ (số điểm thêm vào mỗi khung hình)

    /**
     * 1. HÀM KHỞI TẠO (Chạy 1 lần duy nhất khi Surface được tạo)
     * Tại đây ta làm những việc nặng nhọc nhất: Nạp Shader, Tạo dữ liệu, Gửi lên GPU.
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Đặt màu nền xóa màn hình là màu ĐEN
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // --- BƯỚC A: Nạp & Biên dịch Shader ---
        val vertexCode = ShaderHelper.readShaderFromResource(context, R.raw.cyclo_vertex)
        val fragmentCode = ShaderHelper.readShaderFromResource(context, R.raw.cyclo_fragment)

        // Tạo chương trình (Link vertex + fragment)
        programId = ShaderHelper.createProgram(vertexCode, fragmentCode)
        // Kích hoạt chương trình để sử dụng
        GLES30.glUseProgram(programId)

        // --- BƯỚC B: Lấy địa chỉ biến (Location) ---
        // Hỏi GPU xem các biến này nằm ở ngăn nhớ số mấy
        uMatrixLocation = GLES30.glGetUniformLocation(programId, "u_Matrix")
        uRLocation = GLES30.glGetUniformLocation(programId, "u_R")
        urLocation = GLES30.glGetUniformLocation(programId, "u_r")
        udLocation = GLES30.glGetUniformLocation(programId, "u_d")
        uColorLocation = GLES30.glGetUniformLocation(programId, "u_Color")
        aThetaLocation = GLES30.glGetAttribLocation(programId, "a_Theta")

        // --- BƯỚC C: Tạo dữ liệu mảng t (Theta) tại RAM ---
        val tData = FloatArray(totalPoints)
        val cycles = 35.0f // Số vòng quay

        // Tính toán các giá trị góc tăng dần
        for (i in 0 until totalPoints) {
            tData[i] = (i.toFloat() / totalPoints) * (2 * Math.PI.toFloat() * cycles)
        }

        // Đóng gói mảng float vào ByteBuffer (Bộ nhớ Native) để chuẩn bị gửi đi
        tDataBuffer = ByteBuffer.allocateDirect(tData.size * 4) // 1 float = 4 bytes
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(tData)
        tDataBuffer.position(0) // Reset con trỏ về đầu

        // --- BƯỚC D: KHỞI TẠO VAO & VBO ---
        // Gọi hàm riêng để xử lý việc đẩy dữ liệu lên GPU
        initVboVao()
    }

    /**
     * Hàm phụ trợ: Khởi tạo và Cấu hình VBO/VAO
     */
    private fun initVboVao() {
        // 1. Tạo ID cho các buffer
        val buffers = IntArray(1)
        val arrays = IntArray(1)

        GLES30.glGenBuffers(1, buffers, 0)       // Sinh ra 1 cái tên cho VBO
        GLES30.glGenVertexArrays(1, arrays, 0)   // Sinh ra 1 cái tên cho VAO

        vboId = buffers[0]
        vaoId = arrays[0]

        // 2. Bắt đầu "Ghi hình" cấu hình vào VAO
        // Bind VAO: Từ giờ, mọi cài đặt về buffer sẽ được lưu vào cái VAO này
        GLES30.glBindVertexArray(vaoId)

        // 3. Đưa dữ liệu vào kho VBO
        // Bind VBO: Chọn cái VBO này làm mục tiêu thao tác hiện tại
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)

        // Gửi dữ liệu: Copy từ RAM (tDataBuffer) sang VRAM (GPU Memory)
        // GL_STATIC_DRAW: Báo hiệu dữ liệu này ít khi thay đổi, GPU hãy tối ưu cho việc đọc
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, tDataBuffer.capacity() * 4, tDataBuffer, GLES30.GL_STATIC_DRAW)

        // 4. Chỉ dẫn GPU cách đọc dữ liệu (Vertex Attribute Pointer)
        GLES30.glEnableVertexAttribArray(aThetaLocation) // Mở cổng thuộc tính a_Theta

        // Chỉ dẫn GPU cách đọc dữ liệu:
        // Tham số cuối cùng là 0 (offset), KHÔNG phải là tDataBuffer
        // Nghĩa là: "Hãy đọc từ đầu của cái VBO đang được Bind, chứ không đọc từ RAM nữa"
        GLES30.glVertexAttribPointer(aThetaLocation, 1, GLES30.GL_FLOAT, false, 0, 0)

        // 5. Kết thúc "Ghi hình": Unbind VAO
        // Trả lại trạng thái tự do để tránh các hàm khác vô tình làm hỏng cấu hình
        GLES30.glBindVertexArray(0)

        // Unbind VBO (Tùy chọn, vì khi unbind VAO thì VBO gắn với nó cũng thôi không được track nữa)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    /**
     * 2. HÀM THAY ĐỔI KÍCH THƯỚC (Chạy khi xoay màn hình)
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Thiết lập khung nhìn toàn màn hình
        GLES30.glViewport(0, 0, width, height)

        // Tính tỷ lệ màn hình (Aspect Ratio)
        val aspectRatio = width.toFloat() / height.toFloat()

        // Tạo ma trận trực giao:
        // Chiều ngang (X) sẽ đi từ -ratio đến +ratio
        // Chiều dọc (Y) đi từ -1 đến 1
        // -> Đảm bảo 1 đơn vị X dài bằng 1 đơn vị Y (Hình tròn không bị méo thành elip)
        Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
    }

    /**
     * 3. HÀM VẼ (Vòng lặp vô tận, ~60 lần/giây)
     * Tại đây GPU thực hiện công việc vẽ thực sự.
     */
    override fun onDrawFrame(gl: GL10?) {
        // A. Xóa màn hình (đổ màu đen lên)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // B. Chọn chương trình shader
        GLES30.glUseProgram(programId)

        // C. Cập nhật các biến Uniform (Tham số thay đổi liên tục)
        // Gửi ma trận chiếu
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)
        // Gửi các bán kính R, r, d từ thanh trượt
        GLES30.glUniform1f(uRLocation, radiusR)
        GLES30.glUniform1f(urLocation, radiusr)
        GLES30.glUniform1f(udLocation, distanceD)

        // D. Kích hoạt VAO
        // Chỉ cần 1 dòng lệnh này, toàn bộ cấu hình (VBO nào, đọc ra sao) được khôi phục lại
        // Không cần gọi glVertexAttribPointer dài dòng nữa.
        GLES30.glBindVertexArray(vaoId)

        // E. Thiết lập màu vẽ (Vàng)
        GLES30.glUniform4f(uColorLocation, 1.0f, 0.8f, 0.0f, 1.0f)

        // F. Logic hoạt hình (Tính toán số lượng điểm cần vẽ)
        if (isAnimationMode) {
            if (currentDrawCount < totalPoints) {
                currentDrawCount += drawSpeed // Tăng dần số điểm
                if (currentDrawCount > totalPoints) currentDrawCount = totalPoints
            }
        } else {
            currentDrawCount = totalPoints // Vẽ hết ngay lập tức
        }

        // G. Lệnh vẽ chính (Draw Call)
        // GL_LINE_STRIP: Nối các điểm thành đường liền mạch
        GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, currentDrawCount)

        // H. Vẽ thêm cái đầu bút màu đỏ (Tùy chọn)
        if (isAnimationMode && currentDrawCount > 0 && currentDrawCount < totalPoints) {
            // Đổi màu sang đỏ
            GLES30.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
            // Vẽ 1 điểm (GL_POINTS) tại vị trí cuối cùng
            GLES30.glDrawArrays(GLES30.GL_POINTS, currentDrawCount - 1, 1)
        }

        // I. Dọn dẹp: Unbind VAO sau khi vẽ xong
        GLES30.glBindVertexArray(0)
    }

    /**
     * Hàm reset hoạt hình khi người dùng kéo thanh trượt
     */
    fun resetAnimation() {
        if (isAnimationMode) {
            currentDrawCount = 0 // Vẽ lại từ đầu
        }
    }
}