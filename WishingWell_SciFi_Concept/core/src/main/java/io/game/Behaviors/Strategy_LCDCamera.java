package io.game.Behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.rblgame.GameContext;
import io.rebelengine.Components.CameraComponent;
import io.rebelengine.Components.MouseLookComponent;
import io.rebelengine.Components.PhysicsBodyComponent;
import io.rebelengine.Components.StrategyLogic;
import io.rebelengine.Components.TransformComponent;

public class Strategy_LCDCamera implements StrategyLogic {
    private final MouseLookComponent mouseLook;

    // Last look angles (degrees). Kept so the camera holds still while locked.
    private float baseYaw, basePitch;

    // Shake state
    private float shakeIntensity = 0f;   // max offset in degrees
    private float shakeDuration = 0f;
    private float shakeTimer = 0f;

    public Strategy_LCDCamera(TransformComponent tc, CameraComponent cc, MouseLookComponent ml){
        this.mouseLook = ml;

        Vector3 toTarget = new Vector3(tc.currentPosition).sub(cc.camera.position).nor();
        float targetPitch = MathUtils.asin(MathUtils.clamp(toTarget.y, -1f, 1f)) * MathUtils.radiansToDegrees;
        float cosP = MathUtils.cos(targetPitch * MathUtils.degreesToRadians);
        float targetYaw = 0f;
        if (Math.abs(cosP) > 0.0001f) {
            targetYaw = MathUtils.atan2(toTarget.x, -toTarget.z) * MathUtils.radiansToDegrees;
        }
        mouseLook.accumulatedDeltaX = targetYaw / mouseLook.sensitivity;
        mouseLook.accumulatedDeltaY = -targetPitch / mouseLook.sensitivity;

        baseYaw = mouseLook.accumulatedDeltaX * mouseLook.sensitivity;
        basePitch = -mouseLook.accumulatedDeltaY * mouseLook.sensitivity;

        applyAngles(cc, baseYaw, basePitch);
    }

    /**
     * Shakes the camera.
     * @param intensity max rotation offset in degrees (try 0.3 subtle, 1 strong, 3 violent)
     * @param duration  seconds until the shake has faded out
     */
    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = Math.max(duration, 0.001f);
        this.shakeTimer = duration;
    }

    @Override
    public void update(PhysicsBodyComponent pbc, TransformComponent tc, CameraComponent cc) {
        if (mouseLook == null) return;

        // Only follow the mouse when unlocked; otherwise keep the last base angles
        if (!GameContext.MOUSELOCK) {
            mouseLook.accumulatedDeltaX = MathUtils.clamp(
                mouseLook.accumulatedDeltaX,
                170f / mouseLook.sensitivity,
                190f / mouseLook.sensitivity
            );
            mouseLook.accumulatedDeltaY = MathUtils.clamp(
                mouseLook.accumulatedDeltaY,
                -10f / mouseLook.sensitivity,
                10f / mouseLook.sensitivity
            );
            baseYaw = mouseLook.accumulatedDeltaX * mouseLook.sensitivity;
            basePitch = -mouseLook.accumulatedDeltaY * mouseLook.sensitivity;
        }

        float yaw = baseYaw;
        float pitch = basePitch;

        // Shake offset, fading out over its duration
        if (shakeTimer > 0f) {
            shakeTimer -= Gdx.graphics.getDeltaTime();
            float strength = shakeIntensity * Math.max(shakeTimer, 0f) / shakeDuration;
            yaw   += MathUtils.random(-1f, 1f) * strength;
            pitch += MathUtils.random(-1f, 1f) * strength;
        }

        applyAngles(cc, yaw, pitch);
    }

    private void applyAngles(CameraComponent cc, float yawDeg, float pitchDeg) {
        float yawRad = yawDeg * MathUtils.degreesToRadians;
        float pitchRad = pitchDeg * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitchRad);

        cc.camera.direction.set(
            cosPitch * MathUtils.sin(yawRad),
            MathUtils.sin(pitchRad),
            -cosPitch * MathUtils.cos(yawRad)
        ).nor();
        cc.camera.up.set(Vector3.Y);
    }
}