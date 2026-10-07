#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 0) out vec3 texCoord0;

void main() {
    gl_Position = ProjMat * vec4(Position, -1.0) * vec4(-1.0,-1.0,-1.0,0.5);


    texCoord0 = Position;
}