package com.sharn.gravitymaze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

/**
 * 重力感測管理器 - 修復版
 * 修正坐標軸對應，確保手機傾斜時正確控制
 */
public class GravitySensorManager implements SensorEventListener {
    
    private static final String TAG = "GravitySensor";
    private final SensorManager sensorManager;
    private final Vibrator vibrator;
    
    // 感測器
    private Sensor accelerometer;
    private Sensor gyroscope;
    
    // ⚠️ 修復：正確的重力方向
    // Android 加速度計：
    // values[0] = X軸 (手機左右，向右為正)
    // values[1] = Y軸 (手機上下，向前為正)
    // values[2] = Z軸 (垂直向上，向上為正)
    // 重力方向：當手機平放時，Z = -9.8
    
    private float gravityX = 0, gravityY = 0;
    private float smoothedGravityX = 0, smoothedGravityY = 0;
    private static final float SMOOTHING_FACTOR = 0.1f; // 平滑係數
    static final float GRAVITY_THRESHOLD = 0.5f; // 閾值降低，更容易觸發
    
    // 陀螺儀角速度（保留但暫不使用）
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
            // SENSOR_DELAY_GAME = 20ms，適合遊戲
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
            // ⚠️ 修復：正確解析加速度計數據
            // 這裡直接使用原始值，通過低通濾波平滑
            // X軸：手機左右傾斜（向右傾斜為正）
            // Y軸：手機前後傾斜（向後傾斜為正）
            
            float rawX = event.values[0]; // X軸傾斜
            float rawY = event.values[1]; // Y軸傾斜
            
            // ⚠️ 調試日誌（首次顯示）
            boolean[] logged = {false};
            if (!logged[0]) {
                Log.d(TAG, "加速度計: x=" + rawX + ", y=" + rawY + ", z=" + event.values[2]);
                logged[0] = true;
            }
            
            // 低通濾波平滑處理
            smoothedGravityX = smoothedGravityX * (1 - SMOOTHING_FACTOR) + rawX * SMOOTHING_FACTOR;
            smoothedGravityY = smoothedGravityY * (1 - SMOOTHING_FACTOR) + rawY * SMOOTHING_FACTOR;
            
            // 計算總重力大小
            float magnitude = (float) Math.sqrt(smoothedGravityX * smoothedGravityX + 
                                               smoothedGravityY * smoothedGravityY +
                                               event.values[2] * event.values[2]);
            
            // ⚠️ 修復：即使小幅度傾斜也更新，避免靜止狀態無重力
            if (magnitude > 1.0f) {
                // 正規化到 -1 ~ 1 範圍（除以總重力約9.8）
                gravityX = smoothedGravityX / magnitude;
                gravityY = smoothedGravityY / magnitude;
                
                // ⚠️ 修正：Y軸方向需要反轉，因為屏幕的"上"對應加速度計的"前"
                // 當手機向上傾斜（頂部抬高）時，球應該向上滾（-Y方向）
                gravityY = -gravityY;
                
                // 計算傾斜角度（弧度）
                float tiltAngle = (float) Math.atan2(gravityY, gravityX);
                
                if (callback != null) {
                    callback.onGravityChanged(gravityX, gravityY, tiltAngle);
                }
            }
            
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // 陀螺儀數據暫不使用
            gyroX = event.values[0];
            gyroZ = event.values[2];
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
            vibrator.vibrate(10);
        }
    }
    
    /**
     * 傳送門特效震動
     */
    public void triggerPortalEffect() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
    }
}
