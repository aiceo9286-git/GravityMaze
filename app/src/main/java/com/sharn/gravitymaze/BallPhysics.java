package com.sharn.gravitymaze;

import android.graphics.RectF;
import android.util.Log;

/**
 * 鋼珠物理引擎
 * 實現重力加速度、慣性、摩擦力、彈性碰撞
 */
public class BallPhysics {
    
    private static final String TAG = "BallPhysics";
    
    // ⚠️ 修復：物理參數調整 - 降低門檻讓球更容易滾動
    private static final float GRAVITY_SCALE = 800.0f;     // 大幅增加重力倍率（從15提升到800）
    private static final float FRICTION_COEFFICIENT = 0.95f;  // 摩擦力降低（從0.92提升到0.95）
    private static final float WALL_FRICTION = 0.9f;      // 牆面摩擦也降低
    private static final float RESTITUTION = 0.6f;        // 彈性係數
    private static final float MIN_VELOCITY = 0.1f;       // 更低的最小速度
    private static final float MAX_VELOCITY = 300.0f;     // 提高最大速度
    
    // 鋼珠屬性
    private float x, y;           // 位置
    private float vx, vy;       // 速度向量
    private float radius;         // 半徑
    private float mass = 1.0f;  // 質量
    
    // 狀態
    private boolean isMoving = false;
    private int wallCollisionCount = 0;  // 撞牆次數統計
    
    public BallPhysics(float startX, float startY, float radius) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
    }
    
    /**
     * 更新物理狀態
     * @param gravityX 重力 X 分量 (正規化 -1~1)
     * @param gravityY 重力 Y 分量 (正規化 -1~1)
     * @param deltaTime 時間步長 (秒)
     */
    public void update(float gravityX, float gravityY, float deltaTime) {
        // ⚠️ 核心物理：重力加速度
        // F = m*a = m*g*sin(θ)，這裡 gravityX/Y 已經是 sin(θ) 分量
        float ax = gravityX * GRAVITY_SCALE;
        float ay = gravityY * GRAVITY_SCALE;
        
        // 速度積分：v = v0 + a*t
        vx += ax * deltaTime;
        vy += ay * deltaTime;
        
        // 限制最大速度
        float speed = (float) Math.sqrt(vx * vx + vy * vy);
        if (speed > MAX_VELOCITY) {
            float scale = MAX_VELOCITY / speed;
            vx *= scale;
            vy *= scale;
        }
        
        // 位置積分：p = p0 + v*t
        x += vx * deltaTime;
        y += vy * deltaTime;
        
        // 摩擦力（動能衰減）
        vx *= FRICTION_COEFFICIENT;
        vy *= FRICTION_COEFFICIENT;
        
        // 靜止檢測
        if (speed < MIN_VELOCITY) {
            vx = 0;
            vy = 0;
            isMoving = false;
        } else {
            isMoving = true;
        }
    }
    
    /**
     * 處理牆壁碰撞
     * @param wall 牆壁邊界
     * @return 是否發生碰撞
     */
    public boolean handleWallCollision(RectF wall) {
        boolean collided = false;
        
        // 檢查鋼珠圓心與牆壁的最近點
        float closestX = clamp(x, wall.left, wall.right);
        float closestY = clamp(y, wall.top, wall.bottom);
        
        // 計算距離
        float dx = x - closestX;
        float dy = y - closestY;
        float distanceSquared = dx * dx + dy * dy;
        
        if (distanceSquared < radius * radius) {
            // 發生碰撞！
            collided = true;
            wallCollisionCount++;
            
            // 計算碰撞法向量
            float distance = (float) Math.sqrt(distanceSquared);
            float normalX = dx / distance;
            float normalY = dy / distance;
            
            // 如果圓心在牆壁內，將鋼珠推出
            if (distance == 0) {
                normalX = 1;
                normalY = 0;
            }
            
            // 將鋼珠移出牆壁
            float overlap = radius - distance;
            x += normalX * overlap;
            y += normalY * overlap;
            
            // 反射速度：v' = v - (1+e)(v·n)n
            float dotProduct = vx * normalX + vy * normalY;
            
            if (dotProduct < 0) { // 只有朝向牆壁的速度需要反射
                float reflectionScale = -(1 + RESTITUTION);
                vx += reflectionScale * dotProduct * normalX;
                vy += reflectionScale * dotProduct * normalY;
                
                // 牆面摩擦
                vx *= WALL_FRICTION;
                vy *= WALL_FRICTION;
            }
            
            Log.d(TAG, "撞牆！碰撞次數: " + wallCollisionCount);
        }
        
        return collided;
    }
    
    /**
     * 檢查是否掉入黑洞
     * @param holeX 黑洞中心 X
     * @param holeY 黑洞中心 Y
     * @param holeRadius 黑洞半徑
     * @return 是否掉入
     */
    public boolean checkBlackHole(float holeX, float holeY, float holeRadius) {
        float dx = x - holeX;
        float dy = y - holeY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < holeRadius;
    }
    
    /**
     * 檢查是否進入傳送門
     * @param portalX 傳送門中心 X
     * @param portalY 傳送門中心 Y
     * @param portalRadius 傳送門半徑
     * @return 是否進入傳送門
     */
    public boolean checkPortal(float portalX, float portalY, float portalRadius) {
        float dx = x - portalX;
        float dy = y - portalY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < portalRadius;
    }
    
    /**
     * 瞬移到指定位置（傳送門效果）
     */
    public void teleport(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        // 保持速度（慣性延續）
        Log.d(TAG, "傳送！新位置: (" + newX + ", " + newY + ")");
    }
    
    /**
     * 檢查是否到達終點
     */
    public boolean checkGoal(float goalX, float goalY, float goalRadius) {
        float dx = x - goalX;
        float dy = y - goalY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < goalRadius;
    }
    
    // Getter methods
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public boolean isMoving() { return isMoving; }
    public int getWallCollisionCount() { return wallCollisionCount; }
    public void resetCollisionCount() { wallCollisionCount = 0; }
    
    /**
     * 取得鋼珠邊界框（用於碰撞檢測）
     */
    public RectF getBounds() {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
