package com.sharn.gravitymaze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 遊戲畫面渲染視圖
 * 處理迷宮繪製、鋼珠渲染、特效顯示、視角跟隨
 */
public class GameView extends View {
    
    private static final String TAG = "GameView";
    
    // 視角參數
    private float cameraOffsetX = 0, cameraOffsetY = 0;
    private float targetCameraX = 0, targetCameraY = 0;
    private static final float CAMERA_SMOOTH = 0.08f; // Smooth Damp 係數
    private static final int MAX_WALL_COLLISION_PASSES = 6;
    
    // 渲染參數
    private Paint wallPaint;
    private Paint ballPaint;
    private Paint blackHolePaint;
    private Paint portalPaint;
    private Paint goalPaint;
    private Paint badgePaint;
    private Paint textPaint;
    private Paint bgPaint;
    
    // 遊戲物件
    private BallPhysics ball;
    private LevelManager.Level currentLevel;
    private List<LevelManager.Badge> collectedBadges = new ArrayList<>();
    
    // 遊戲狀態
    private boolean gameRunning = false;
    private float elapsedTime = 0;
    private int currentLevelId = 1;
    
    // 畫面尺寸
    private int screenWidth, screenHeight;
    
    // 音效管理器
    private SoundManager soundManager;
    
    // 回調介面
    public interface GameCallback {
        void onLevelComplete(List<LevelManager.Badge> badges);
        void onGameOver(String reason);
        void onBadgeCollected(LevelManager.Badge badge);
        void onCheatUnlocked(String cheatCode);
    }
    private GameCallback callback;
    
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }
    
    private void initPaints() {
        // 迷幻時空風格背景漸層
        bgPaint = new Paint();
        
        // 牆壁畫筆 - 發光效果
        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setColor(Color.rgb(100, 50, 150)); // 深紫色
        wallPaint.setStrokeWidth(5);
        wallPaint.setStyle(Paint.Style.FILL);
        
        // 鋼珠畫筆 - 金屬球體漸層
        ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ballPaint.setStyle(Paint.Style.FILL);
        
        // 黑洞 - 旋渦效果
        blackHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackHolePaint.setStyle(Paint.Style.FILL);
        
        // 傳送門 - 發光青色
        portalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        portalPaint.setColor(Color.CYAN);
        portalPaint.setStyle(Paint.Style.FILL);
        portalPaint.setAlpha(180);
        
        // 終點 - 金色光暈
        goalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goalPaint.setColor(Color.rgb(255, 215, 0)); // 金色
        goalPaint.setStyle(Paint.Style.FILL);
        
        // 徽章 - 彩虹色
        badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint.setStyle(Paint.Style.FILL);
        
        // 文字
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
    }
    
    /**
     * 初始化新關卡
     */
    public void startLevel(LevelManager.Level level) {
        if (level == null) {
            Log.e(TAG, "Cannot start null level");
            gameRunning = false;
            currentLevel = null;
            ball = null;
            invalidate();
            return;
        }

        currentLevel = level;
        currentLevelId = level.id;
        
        // 創建鋼珠
        ball = new BallPhysics(level.startX, level.startY, 25);
        ball.resetCollisionCount();
        
        // 重置視角
        cameraOffsetX = 0;
        cameraOffsetY = 0;
        targetCameraX = 0;
        targetCameraY = 0;
        
        // 重置徽章收集狀態
        if (level.badges != null) {
            for (LevelManager.Badge badge : level.badges) {
                if (badge != null) {
                    badge.collected = false;
                }
            }
        }
        collectedBadges.clear();
        
        elapsedTime = 0;
        gameRunning = true;
        
        Log.d(TAG, "開始關卡 " + level.id + ": " + level.name);
        invalidate();
    }
    
    /**
     * 更新遊戲邏輯（每幀呼叫）
     */
    public void update(float deltaTime, float gravityX, float gravityY) {
        if (!gameRunning || ball == null || currentLevel == null) return;
        
        elapsedTime += deltaTime;
        
        // 檢查時間限制
        if (elapsedTime > currentLevel.timeLimit) {
            gameRunning = false;
            if (callback != null) {
                callback.onGameOver("時間耗盡");
            }
            return;
        }
        
        // 更新鋼珠物理
        ball.update(gravityX, gravityY, deltaTime);
        
        // 檢查邊界碰撞 - 改用球體的teleport避免重置物理狀態
        if (!hasWalls() && screenWidth > 0 && screenHeight > 0 &&
                currentLevel.mazeWidth <= screenWidth && currentLevel.mazeHeight <= screenHeight) {
            // 小迷宮：限制在邊界內
            float r = ball.getRadius();
            float bx = ball.getX();
            float by = ball.getY();
            
            if (bx < r) bx = r;
            if (bx > currentLevel.mazeWidth - r) bx = currentLevel.mazeWidth - r;
            if (by < r) by = r;
            if (by > currentLevel.mazeHeight - r) by = currentLevel.mazeHeight - r;
            
            // 只有位置真的超出邊界才需要調整
            if (bx != ball.getX() || by != ball.getY()) {
                ball.teleport(bx, by);
            }
        }
        
        // 牆壁碰撞檢測
        if (resolveWallCollisions()) {
            // 撞牆震動與音效
            if (soundManager != null) {
                soundManager.playCollision();
            }
        }

        if (currentLevel.blackHoles != null) {
            for (LevelManager.BlackHole hole : currentLevel.blackHoles) {
                if (hole != null && ball.checkBlackHole(hole.x, hole.y, hole.radius)) {
                    gameRunning = false;
                    if (soundManager != null) {
                        soundManager.playGameOver();
                    }
                    if (callback != null) {
                        callback.onGameOver("掉入黑洞");
                    }
                    return;
                }
            }
        }

        if (currentLevel.portals != null) {
            for (LevelManager.Portal portal : currentLevel.portals) {
                if (portal != null && ball.checkPortal(portal.x1, portal.y1, portal.radius)) {
                    ball.teleport(portal.x2, portal.y2);
                    if (soundManager != null) {
                        soundManager.playPortal();
                    }
                }
            }
        }

        if (currentLevel.badges != null) {
            for (LevelManager.Badge badge : currentLevel.badges) {
                if (badge != null && !badge.collected) {
                    float dx = ball.getX() - badge.x;
                    float dy = ball.getY() - badge.y;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < ball.getRadius() + badge.radius) {
                        badge.collected = true;
                        collectedBadges.add(badge);
                        if (soundManager != null) {
                            soundManager.playBadge();
                        }
                        if (callback != null) {
                            callback.onBadgeCollected(badge);
                        }
                        // 檢查是否解鎖密技
                        if (!currentLevel.cheatUnlocked && collectedBadges.size() >= currentLevel.badges.size()) {
                            currentLevel.cheatUnlocked = true;
                            if (callback != null) {
                                callback.onCheatUnlocked(currentLevel.cheatCode);
                            }
                        }
                    }
                }
            }
        }

        // 終點檢測
        if (ball.checkGoal(currentLevel.goalX, currentLevel.goalY, 60)) {
            gameRunning = false;
            if (soundManager != null) {
                soundManager.playWin();
            }
            if (callback != null) {
                callback.onLevelComplete(new ArrayList<>(collectedBadges));
            }
        }

        // 更新視角跟隨（Camera Smooth Damp）
        updateCamera();

        invalidate(); // 請求重繪
    }

    /**
     * Camera Smooth Damp - 平滑視角跟隨
     */
    private void updateCamera() {
        if (ball == null || screenWidth <= 0 || screenHeight <= 0) return;
        
        // 計算目標相機位置（讓鋼珠保持在螢幕中心）
        targetCameraX = ball.getX() - screenWidth / 2f;
        targetCameraY = ball.getY() - screenHeight / 2f;
        
        // 限制相機範圍（不超出迷宮邊界）
        if (currentLevel != null) {
            targetCameraX = clamp(targetCameraX, 0, Math.max(0, currentLevel.mazeWidth - screenWidth));
            targetCameraY = clamp(targetCameraY, 0, Math.max(0, currentLevel.mazeHeight - screenHeight));
        }
        
        // Smooth Damp 插值
        cameraOffsetX += (targetCameraX - cameraOffsetX) * CAMERA_SMOOTH;
        cameraOffsetY += (targetCameraY - cameraOffsetY) * CAMERA_SMOOTH;
    }

    private boolean hasWalls() {
        return currentLevel != null && currentLevel.walls != null && !currentLevel.walls.isEmpty();
    }

    private boolean resolveWallCollisions() {
        if (ball == null || currentLevel == null || currentLevel.walls == null) {
            return false;
        }

        boolean collidedAtLeastOnce = false;
        for (int pass = 0; pass < MAX_WALL_COLLISION_PASSES; pass++) {
            boolean collidedThisPass = false;

            for (RectF wall : currentLevel.walls) {
                if (wall != null && ball.handleWallCollision(wall)) {
                    collidedThisPass = true;
                    collidedAtLeastOnce = true;
                }
            }

            if (!collidedThisPass) {
                break;
            }

            if (pass == MAX_WALL_COLLISION_PASSES - 1) {
                Log.w(TAG, "牆壁碰撞解析達到上限，跳出避免無限循環");
            }
        }

        return collidedAtLeastOnce;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 繪製迷幻時空背景
        drawBackground(canvas);
        
        if (currentLevel == null) return;
        
        // 儲存畫布狀態
        canvas.save();
        
        // 應用相機偏移（視角跟隨）
        canvas.translate(-cameraOffsetX, -cameraOffsetY);
        
        // 繪製迷宮牆壁
        if (currentLevel.walls != null) {
            for (RectF wall : currentLevel.walls) {
                if (wall != null) {
                    // 發光效果
                    wallPaint.setShadowLayer(10, 0, 0, Color.rgb(150, 100, 200));
                    canvas.drawRect(wall, wallPaint);
                }
            }
        }
        
        // 繪製黑洞
        if (currentLevel.blackHoles != null) {
            for (LevelManager.BlackHole hole : currentLevel.blackHoles) {
                if (hole != null) {
                    drawBlackHole(canvas, hole);
                }
            }
        }
        
        // 繪製傳送門
        if (currentLevel.portals != null) {
            for (LevelManager.Portal portal : currentLevel.portals) {
                if (portal != null) {
                    drawPortal(canvas, portal);
                }
            }
        }
        
        // 繪製徽章
        if (currentLevel.badges != null) {
            for (LevelManager.Badge badge : currentLevel.badges) {
                if (badge != null && !badge.collected) {
                    drawBadge(canvas, badge);
                }
            }
        }
        
        // 繪製終點
        drawGoal(canvas);
        
        // 繪製鋼珠
        if (ball != null) {
            drawBall(canvas);
        }
        
        // 恢復畫布狀態
        canvas.restore();
        
        // 繪製 UI（不受視角影響）
        drawUI(canvas);
    }
    
    private void drawBackground(Canvas canvas) {
        int width = screenWidth > 0 ? screenWidth : getWidth();
        int height = screenHeight > 0 ? screenHeight : getHeight();
        if (width <= 0 || height <= 0) {
            canvas.drawColor(Color.rgb(30, 10, 60));
            return;
        }

        // 迷幻時空風格：深紫色到藍色的徑向漸層
        RadialGradient gradient = new RadialGradient(
            width / 2f, height / 2f,
            Math.max(1f, Math.max(width, height) / 1.5f),
            new int[]{Color.rgb(30, 10, 60), Color.rgb(80, 20, 120), Color.rgb(20, 5, 40)},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        bgPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, bgPaint);
    }
    
    private void drawBlackHole(Canvas canvas, LevelManager.BlackHole hole) {
        // 外圈光暈
        RadialGradient outerGlow = new RadialGradient(
            hole.x, hole.y, hole.radius * 2,
            new int[]{Color.rgb(150, 50, 200), Color.TRANSPARENT},
            new float[]{0f, 1f},
            Shader.TileMode.CLAMP
        );
        blackHolePaint.setShader(outerGlow);
        canvas.drawCircle(hole.x, hole.y, hole.radius * 2, blackHolePaint);
        
        // 黑洞本體 - 旋渦漸層
        RadialGradient blackHoleGrad = new RadialGradient(
            hole.x, hole.y, hole.radius,
            new int[]{Color.BLACK, Color.rgb(40, 0, 60)},
            new float[]{0.3f, 1f},
            Shader.TileMode.CLAMP
        );
        blackHolePaint.setShader(blackHoleGrad);
        canvas.drawCircle(hole.x, hole.y, hole.radius, blackHolePaint);
    }
    
    private void drawPortal(Canvas canvas, LevelManager.Portal portal) {
        // 傳送門發光效果
        portalPaint.setAlpha((int) (150 + 50 * Math.sin(System.currentTimeMillis() / 200.0)));
        portalPaint.setShadowLayer(15, 0, 0, Color.CYAN);
        canvas.drawCircle(portal.x1, portal.y1, portal.radius, portalPaint);
        
        // 出口標記
        portalPaint.setAlpha(100);
        canvas.drawCircle(portal.x2, portal.y2, portal.radius * 0.7f, portalPaint);
    }
    
    private void drawBadge(Canvas canvas, LevelManager.Badge badge) {
        // 彩虹色動態效果
        float hue = (System.currentTimeMillis() / 20) % 360;
        badgePaint.setColor(Color.HSVToColor(new float[]{hue, 1f, 1f}));
        badgePaint.setShadowLayer(10, 0, 0, badgePaint.getColor());
        
        // 脈動效果
        float pulse = (float) (1 + 0.2 * Math.sin(System.currentTimeMillis() / 150.0));
        canvas.drawCircle(badge.x, badge.y, badge.radius * pulse, badgePaint);
    }
    
    private void drawGoal(Canvas canvas) {
        if (currentLevel == null) return;
        
        // 終點光暈動畫
        float pulse = (float) (1 + 0.3 * Math.sin(System.currentTimeMillis() / 100.0));
        goalPaint.setColor(Color.rgb(255, 215, 0));
        goalPaint.setShadowLayer(20 * pulse, 0, 0, Color.rgb(255, 215, 0));
        
        canvas.drawCircle(currentLevel.goalX, currentLevel.goalY, 50 * pulse, goalPaint);
        
        // 內圓
        goalPaint.setColor(Color.WHITE);
        canvas.drawCircle(currentLevel.goalX, currentLevel.goalY, 25, goalPaint);
    }
    
    private void drawBall(Canvas canvas) {
        if (ball == null) return;
        
        float x = ball.getX();
        float y = ball.getY();
        float r = ball.getRadius();
        
        // 金屬質感漸層
        RadialGradient ballGrad = new RadialGradient(
            x - r * 0.3f, y - r * 0.3f, r * 1.5f,
            new int[]{Color.WHITE, Color.rgb(200, 200, 220), Color.rgb(100, 100, 120)},
            new float[]{0f, 0.4f, 1f},
            Shader.TileMode.CLAMP
        );
        ballPaint.setShader(ballGrad);
        canvas.drawCircle(x, y, r, ballPaint);
        
        // 光澤高光
        ballPaint.setColor(Color.WHITE);
        ballPaint.setAlpha(200);
        canvas.drawCircle(x - r * 0.3f, y - r * 0.3f, r * 0.4f, ballPaint);
        ballPaint.setAlpha(255);
    }
    
    private void drawUI(Canvas canvas) {
        if (currentLevel == null) return;
        
        // 時間顯示
        int remaining = (int) (currentLevel.timeLimit - elapsedTime);
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        
        // 時間警告色
        if (remaining < 10) {
            textPaint.setColor(Color.RED);
        } else {
            textPaint.setColor(Color.WHITE);
        }
        
        canvas.drawText(timeText, screenWidth / 2f, 60, textPaint);
        
        // 徽章收集進度
        textPaint.setColor(Color.rgb(255, 215, 0));
        int badgeCount = currentLevel.badges != null ? currentLevel.badges.size() : 0;
        String badgeText = "徽章: " + collectedBadges.size() + "/" + badgeCount;
        canvas.drawText(badgeText, screenWidth / 2f, 110, textPaint);
        textPaint.setColor(Color.WHITE);
    }
    
    private float clamp(float value, float min, float max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
    
    public void setCallback(GameCallback callback) {
        this.callback = callback;
    }
    
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public LevelManager.Level getCurrentLevel() {
        return currentLevel;
    }
    
    public BallPhysics getBall() {
        return ball;
    }
}
