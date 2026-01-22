#version 300 es
precision mediump float;

uniform vec4 u_Color;

// Trong GLSL 3.0, không còn gl_FragColor định sẵn.
// Ta phải tự khai báo một biến output để xuất màu ra màn hình.
out vec4 fragColor;

void main() {
    // Gán màu vào biến output của mình
    fragColor = u_Color;
}