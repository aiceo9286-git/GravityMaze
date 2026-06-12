package com.sharn.gravitymaze;

import android.graphics.RectF;
import android.util.Log;

/**
 * 鋼珠物理引擎
 * 實現重力加速度、慣性、摩擦力、彈性碰撞
 */
public class BallPhysics {
    
    private static final String TAG = "BallPhysics";
    
    // ✅ 修復：物理參數調整到合理範圍
    private static final float GRAVITY_SCALE = 500.0f;    // 重力倍率（從800調整到500）
    private static final float FRICTION_COEFFICIENT = 0.92f;  // 地面摩擦係數
    private static final float WALL_FRICTION = 0.7f;       // 牆面摩擦（撞牆後減速）
    private static final float RESTITUTION = 0.6f;         // 彈性係數（反彈力）
    private static final float MIN_VELOCITY = 5.0f;        // 最小速度（避免浮點誤差）
    private static final float MAX_VELOCITY = 250.0f;      // 最大速度限制
    private static final float COLLISION_EPSILON = 0.0001f;
    private static final float SEPARATION_EPSILON = 0.01f;
    
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
        this.radius = Math.max(1f, radius);
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
        if (!Float.isFinite(deltaTime) || deltaTime <= 0f) {
            return;
        }
        if (!Float.isFinite(gravityX)) {
            gravityX = 0f;
        }
        if (!Float.isFinite(gravityY)) {
            gravityY = 0f;
        }
        deltaTime = Math.min(deltaTime, 0.05f);

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
        if (!Float.isFinite(speed) || speed < MIN_VELOCITY) {
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
        if (wall == null || wall.isEmpty() || !isFiniteWall(wall) ||
                !Float.isFinite(x) || !Float.isFinite(y) ||
                !Float.isFinite(vx) || !Float.isFinite(vy) ||
                !Float.isFinite(radius) || radius <= 0f) {
            return false;
        }

        // 檢查鋼珠圓心與牆壁的最近點
        float closestX = clamp(x, wall.left, wall.right);
        float closestY = clamp(y, wall.top, wall.bottom);
        
        // 計算距離
        float dx = x - closestX;
        float dy = y - closestY;
        float distanceSquared = dx * dx + dy * dy;

        if (!Float.isFinite(distanceSquared) || distanceSquared >= radius * radius) {
            return false;
        }

        float normalX;
        float normalY;
        float penetration;

        if (distanceSquared > COLLISION_EPSILON * COLLISION_EPSILON) {
            float distance = (float) Math.sqrt(distanceSquared);
            normalX = dx / distance;
            normalY = dy / distance;
            penetration = radius - distance;
        } else {
            // 圓心在牆內或剛好落在牆邊時，推向最近牆邊並加上半徑。
            float leftDistance = x - wall.left;
            float rightDistance = wall.right - x;
            float topDistance = y - wall.top;
            float bottomDistance = wall.bottom - y;
            float minDistance = Math.min(Math.min(leftDistance, rightDistance), Math.min(topDistance, bottomDistance));

            if (!Float.isFinite(minDistance)) {
                return false;
            }

            if (Math.abs(vx) > Math.abs(vy) && Math.abs(vx) > COLLISION_EPSILON) {
                if (vx > 0f) {
                    normalX = -1f;
                    normalY = 0f;
                    minDistance = leftDistance;
                } else {
                    normalX = 1f;
                    normalY = 0f;
                    minDistance = rightDistance;
                }
            } else if (Math.abs(vy) > COLLISION_EPSILON) {
                if (vy > 0f) {
                    normalX = 0f;
                    normalY = -1f;
                    minDistance = topDistance;
                } else {
                    normalX = 0f;
                    normalY = 1f;
                    minDistance = bottomDistance;
                }
            } else if (minDistance == leftDistance) {
                normalX = -1f;
                normalY = 0f;
            } else if (minDistance == rightDistance) {
                normalX = 1f;
                normalY = 0f;
            } else if (minDistance == topDistance) {
                normalX = 0f;
                normalY = -1f;
            } else {
                normalX = 0f;
                normalY = 1f;
            }
            penetration = radius + Math.max(0f, minDistance);
        }

        if (!Float.isFinite(penetration) || penetration <= 0f) {
            return false;
        }

        // 將鋼珠移出牆壁；額外留一點間隙，避免下一輪因浮點誤差重複碰撞。
        x += normalX * (penetration + SEPARATION_EPSILON);
        y += normalY * (penetration + SEPARATION_EPSILON);

        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            vx = 0f;
            vy = 0f;
            return false;
        }

        // 反射速度：v' = v - (1+e)(v·n)n
        float dotProduct = vx * normalX + vy * normalY;

        if (Float.isFinite(dotProduct) && dotProduct < 0f) { // 只有朝向牆壁的速度需要反射
            float reflectionScale = -(1f + RESTITUTION);
            vx += reflectionScale * dotProduct * normalX;
            vy += reflectionScale * dotProduct * normalY;

            // 牆面摩擦
            vx *= WALL_FRICTION;
            vy *= WALL_FRICTION;
        }

        if (!Float.isFinite(vx) || !Float.isFinite(vy)) {
            vx = 0f;
            vy = 0f;
        }

        wallCollisionCount++;
        Log.d(TAG, "撞牆！碰撞次數: " + wallCollisionCount);
        return true;
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
        if (!Float.isFinite(newX) || !Float.isFinite(newY)) {
            Log.w(TAG, "忽略無效傳送位置: (" + newX + ", " + newY + ")");
            return;
        }
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
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private boolean isFiniteWall(RectF wall) {
        return Float.isFinite(wall.left) && Float.isFinite(wall.top) &&
                Float.isFinite(wall.right) && Float.isFinite(wall.bottom);
    }
}
