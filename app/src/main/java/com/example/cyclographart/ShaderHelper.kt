package com.example.cyclographart

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Một đối tượng (Singleton) chứa các hàm tiện ích để làm việc với OpenGL Shaders.
 * Giúp code trong Renderer gọn gàng và dễ đọc hơn.
 */
object ShaderHelper {
    private const val TAG = "ShaderHelper"

    /**
     * Nhiệm vụ: Đọc nội dung file text (.glsl) trong thư mục res/raw
     * và chuyển đổi nó thành một chuỗi String để gửi cho GPU.
     */
    fun readShaderFromResource(context: Context, resourceId: Int): String {
        val stringBuilder = StringBuilder()
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var nextLine: String?
            while (bufferedReader.readLine().also { nextLine = it } != null) {
                stringBuilder.append(nextLine)
                stringBuilder.append('\n')
            }
        } catch (e: IOException) {
            throw RuntimeException("Không thể đọc tài nguyên shader ID: $resourceId", e)
        }
        return stringBuilder.toString()
    }

    /**
     * Nhiệm vụ: Gửi mã nguồn shader (String) xuống GPU và yêu cầu biên dịch.
     * Trả về: ID của shader (nếu thành công) hoặc 0 (nếu thất bại).
     */
    fun compileShader(type: Int, shaderCode: String): Int {
        // 1. Tạo một đối tượng shader rỗng
        // type có thể là GL_VERTEX_SHADER hoặc GL_FRAGMENT_SHADER
        val shaderId = GLES30.glCreateShader(type)

        if (shaderId == 0) {
            Log.w(TAG, "Không thể tạo shader mới. Có thể do hết bộ nhớ GPU.")
            return 0
        }

        // 2. Nạp mã nguồn vào và biên dịch
        GLES30.glShaderSource(shaderId, shaderCode)
        GLES30.glCompileShader(shaderId)

        // 3. Kiểm tra xem việc biên dịch có thành công không
        // Đây là bước debug cực kỳ quan trọng. Nếu viết sai cú pháp GLSL,
        // log lỗi sẽ hiện ra ở đây.
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            // Nếu thất bại, ghi log lỗi chi tiết ra Logcat
            Log.e(TAG, "Lỗi biên dịch Shader: \n" + GLES30.glGetShaderInfoLog(shaderId))
            GLES30.glDeleteShader(shaderId) // Xóa đi cho đỡ rác bộ nhớ
            return 0
        }

        return shaderId
    }

    /**
     * Nhiệm vụ: Dán Vertex Shader và Fragment Shader lại với nhau thành một Program hoàn chỉnh.
     * Trả về: ID của chương trình (programId).
     */
    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        // Biên dịch từng cái lẻ
        val vertexShaderId = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShaderId = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Nếu một trong hai cái bị lỗi thì dừng luôn
        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0
        }

        // Tạo một chương trình rỗng
        val programId = GLES30.glCreateProgram()
        if (programId == 0) {
            Log.w(TAG, "Không thể tạo OpenGL Program.")
            return 0
        }

        // Đính kèm (Attach) và Liên kết (Link)
        GLES30.glAttachShader(programId, vertexShaderId)
        GLES30.glAttachShader(programId, fragmentShaderId)
        GLES30.glLinkProgram(programId)

        // Kiểm tra trạng thái liên kết
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            Log.e(TAG, "Lỗi liên kết Program: \n" + GLES30.glGetProgramInfoLog(programId))
            GLES30.glDeleteProgram(programId)
            return 0
        }

        return programId
    }
}