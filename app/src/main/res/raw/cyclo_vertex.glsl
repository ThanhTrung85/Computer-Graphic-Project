#version 300 es
// Khai báo phiên bản GLSL 3.0 (Bắt buộc dòng đầu tiên)

// 'attribute' đã bị loại bỏ, thay bằng 'in'
in float a_Theta;

// Các uniform giữ nguyên
uniform float u_R;
uniform float u_r;
uniform float u_d;
uniform mat4 u_Matrix;

void main() {
    float sumR = u_R + u_r;
    float ratio = sumR / u_r;

    float x = sumR * cos(a_Theta) - u_d * cos(ratio * a_Theta);
    float y = sumR * sin(a_Theta) - u_d * sin(ratio * a_Theta);

    gl_Position = u_Matrix * vec4(x, y, 0.0, 1.0);
    gl_PointSize = 10.0;
}