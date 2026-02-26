#version 450

const int MAX_TEXTURES = 100;

layout(location = 0) in vec2 inTexCoords;

layout(location = 0) out vec4 outFragColor;

struct Material {
    vec4 diffuseColor;
    uint hasTexture;
    uint textureIndex;
    uint padding[2];
};

layout(set = 1, binding = 0) readonly buffer MaterialUniform {
    Material materials[];
} matUniform;

layout(set = 2, binding = 0) uniform sampler2D texSampler[MAX_TEXTURES];

layout(push_constant) uniform PushConstants {
    layout(offset = 64) uint materialIndex;
} push_constants;

void main()
{
    Material material = matUniform.materials[push_constants.materialIndex];
    if (material.hasTexture == 1) {
        outFragColor = texture(texSampler[material.textureIndex], inTexCoords);
    } else {
        outFragColor = material.diffuseColor;
    }
}
