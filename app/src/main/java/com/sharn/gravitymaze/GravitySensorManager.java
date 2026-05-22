package com.sharn.gravitymaze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

/**
 * 重力感測管理器
 * 結合加速度計與陀螺儀進行 Sensor Fusion
 */
public class GravitySensorManager implements SensorEventListener {
    
    private static final String TAG = "GravitySensor";
    private final SensorManager sensorManager;
    private final Vibrator vibrator;
    
    // 感測器
    private Sensor accelerometer;
    private Sensor gyroscope;
    
    // 重力方向向量
    private float gravityX = 0, gravityY = 0;
    private float smoothedGravityX = 0, smoothedGravityY = 0;
    private static final float SMOOTHING_FACTOR = 0.15f; // 低通濾波係數
    
    // 陀螺儀角速度
    private float gyroX = 0, gyroZ = 0;
    
    // 回調介面
    public interface GravityCallback {
        void onGravityChanged(float x, float y, float tiltAngle);
    }
    private GravityCallback callback;
    
    public GravitySensorManager(Context context, GravityCallback callback) {
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    
    public void start() {
        if (accelerometer != null) {
            // 以 SENSOR_DELAY_GAME (20ms) 獲得流暢的遊戲更新率
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
        Log.d(TAG, "感測器已啟動");
    }
    
    public void stop() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "感測器已停止");
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 原始加速度計值
            float rawX = event.values[0]; // 手機左右傾斜
            float rawY = event.values[1]; // 手機前後傾斜
            
            // 低通濾波平滑處理
            smoothedGravityX = smoothedGravityX * (1 - SMOOTHING_FACTOR) + rawX * SMOOTHING_FACTOR;
            smoothedGravityY = smoothedGravityY * (1 - SMOOTHING_FACTOR) + rawY * SMOOTHING_FACTOR;
            
            // 正規化為重力加速度比例
            float magnitude = (float) Math.sqrt(smoothedGravityX * smoothedGravityX + 
                                               smoothedGravityY * smoothedGravityY);
            if (magnitude > 0.1f) {
                gravityX = smoothedGravityX / 9.8f; // 正規化到 g 單位
                gravityY = smoothedGravityY / 9.8f;
            }
            
            // 計算傾斜角度
            float tiltAngle = (float) Math.atan2(smoothedGravityY, smoothedGravityX);
            
            if (callback != null) {
                callback.onGravityChanged(gravityX, gravityY, tiltAngle);
            }
            
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // 陀螺儀讀取旋轉角速度
            gyroX = event.values[0]; // X軸旋轉
            gyroZ = event.values[2]; // Z軸旋轉（手機平面旋轉）
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "感測器精度變更: " + accuracy);
    }
    
    /**
     * 觸發撞牆震動回饋
     */
    public void triggerHapticFeedback() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // 短促震動 (10ms)
            vibrator.vibrate(10);
        }
    }
    
    /**
     * 傳送門特效震動
     */
    public void triggerPortalEffect() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // 較長震動效果 (50ms)
            vibrator.vibrate(50);
        }
    }
}
