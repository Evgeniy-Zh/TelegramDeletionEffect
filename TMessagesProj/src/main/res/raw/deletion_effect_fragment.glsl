precision mediump float;
uniform sampler2D u_Texture;
uniform vec4 u_Color;
varying vec4 vColor;

void main() {
    float alpha = texture2D(u_Texture, gl_PointCoord).a;
    vec4 rgba = u_Color;
    rgba.a = alpha;
    gl_FragColor = vColor;
}