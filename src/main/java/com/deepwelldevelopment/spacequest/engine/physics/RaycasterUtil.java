package com.deepwelldevelopment.spacequest.engine.physics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.deepwelldevelopment.spacequest.engine.scene.Camera;
import com.deepwelldevelopment.spacequest.engine.scene.Projection;

public class RaycasterUtil {
    
    public static Ray getRayFromCamera(Camera camera, Projection projection, float mouseX, float mouseY, float windowWidth, float windowHeight) {
        // Convert mouse coordinates to normalized device coordinates (-1 to 1)
        float ndcX = (2.0f * mouseX) / windowWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY) / windowHeight;
        
        // Create clip space coordinates
        Vector4f clipCoords = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);
        
        // Convert to eye space coordinates
        Matrix4f projectionMatrix = projection.getProjectionMatrix();
        Matrix4f invertedProjection = new Matrix4f(projectionMatrix).invert();
        Vector4f eyeCoords = invertedProjection.transform(clipCoords);
        eyeCoords.set(eyeCoords.x, eyeCoords.y, -1.0f, 0.0f);
        
        // Convert to world space coordinates
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f invertedView = new Matrix4f(viewMatrix).invert();
        Vector4f worldCoords = invertedView.transform(eyeCoords);
        
        Vector3f rayDirection = new Vector3f(worldCoords.x, worldCoords.y, worldCoords.z).normalize();
        Vector3f rayOrigin = new Vector3f(camera.getPosition());
        
        return new Ray(rayOrigin, rayDirection);
    }
    
    public static Ray getForwardRay(Camera camera) {
        Vector3f direction = new Vector3f();
        camera.getViewMatrix().positiveZ(direction).negate().normalize();
        return new Ray(new Vector3f(camera.getPosition()), direction);
    }
}
