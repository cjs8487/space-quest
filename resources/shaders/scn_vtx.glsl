#version 450

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inTexCoords;

layout(location = 0) out vec2 outTexCoords;

layout(set = 0, binding = 0) uniform ProjUniform {
    mat4 matrix;
} projUniform;

layout(push_constant) uniform matrices {
    mat4 modelMatrix;
} push_constants;

void main()
{
    gl_Position = projUniform.matrix * push_constants.modelMatrix * vec4(inPos, 1.0);
    outTexCoords = inTexCoords;
}