uniform float u_Size;
uniform float deltaTime;
uniform float startX;

attribute vec4 a_Position;
attribute vec4 aColor;
attribute vec4 aVelocity;
varying vec4 vColor;

//normalize(a_Position)

float random(vec2 st)
{
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}



void main() {


    float startOffset = a_Position.x - startX;
    float timeCurve = max(0., deltaTime - (1. + a_Position.x + startOffset/1000.)/3.);

    float direction = random(a_Position.xy + aColor.xy) * (3.14159265 * 2.0);
    float velocity = (0.1 + random(a_Position.xy) * (0.2 - 0.1)) * 420.0;

    vec4 velocityVector = vec4(cos(direction) * velocity * timeCurve/100., sin(direction) * velocity * timeCurve/100., 0., 0.);

    float lifetime = 0.7 + random(a_Position.xy) * (0.7);//todo

    vec4 globalVector = vec4(0.4, 0.9, 0., 0.);

    gl_PointSize = u_Size;
    float random = abs(random(a_Position.xy))+1.;
    gl_Position = a_Position + velocityVector;
    vColor = aColor - vec4(0., 0., 0., timeCurve*random);
}