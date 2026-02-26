#version 450

layout(location = 0) in vec2 inTexCoords;
layout(location = 0) out vec4 outFragColor;

void main()
{
    outFragColor = vec4(inTexCoords.x, inTexCoords.y, 0.0, 1.0);
}
