package com.sharn.gravitymaze;

import android.graphics.RectF;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 關卡管理器
 * 管理10關迷宮的載入、切換與狀態
 */
public class LevelManager {
    
    private static final String TAG = "LevelManager";
    public static final int TOTAL_LEVELS = 10;
    
    private int currentLevel = 1;
    private List<Level> levels = new ArrayList<>();
    
    // 關卡資料類別
    public static class Level {
        public int id;
        public String name;
        public int mazeWidth;    // 迷宮寬度（像素）
        public int mazeHeight;   // 迷宮高度（像素）
        public float timeLimit;  // 時間限制（秒），複雜關卡時間加多
        public List<RectF> walls = new ArrayList<>();
        public List<BlackHole> blackHoles = new ArrayList<>();
        public List<Portal> portals = new ArrayList<>();
        public float startX, startY;  // 起始點
        public float goalX, goalY;    // 終點
        public List<Badge> badges = new ArrayList<>();  // 徽章收集點
        public String cheatCode;       // 本關密技
        public boolean cheatUnlocked = false;  // 密技是否解鎖
        
        public Level(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    // 黑洞類別
    public static class BlackHole {
        public float x, y, radius;
        public BlackHole(float x, float y, float radius) {
            this.x = x; this.y = y; this.radius = radius;
        }
    }
    
    // 傳送門類別
    public static class Portal {
        public float x1, y1;  // 入口
        public float x2, y2;  // 出口
        public float radius;
        public Portal(float x1, float y1, float x2, float y2, float radius) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.radius = radius;
        }
    }
    
    // 徽章類別
    public static class Badge {
        public float x, y, radius;
        public boolean collected = false;
        public String name;  // 徽章名稱
        public Badge(float x, float y, float radius, String name) {
            this.x = x; this.y = y; this.radius = radius;
            this.name = name;
        }
    }
    
    public LevelManager() {
        // 初始化10關
        initLevels();
    }
    
    /**
     * 初始化10關迷宮資料
     */
    private void initLevels() {
        // 關卡 1：新手教學（簡單直線）
        Level level1 = new Level(1, "時空啟程");
        level1.mazeWidth = 1080;
        level1.mazeHeight = 1920;
        level1.timeLimit = 30;
        level1.startX = 540; level1.startY = 1700;
        level1.goalX = 540; level1.goalY = 200;
        // 左牆
        level1.walls.add(new RectF(0, 0, 100, 1920));
        // 右牆
        level1.walls.add(new RectF(980, 0, 1080, 1920));
        // 上牆
        level1.walls.add(new RectF(0, 0, 1080, 100));
        // 下牆
        level1.walls.add(new RectF(0, 1820, 1080, 1920));
        // 徽章：無碰撞通關
        level1.badges.add(new Badge(540, 960, 30, "新手駕駛"));
        level1.cheatCode = "GODMODE";
        levels.add(level1);
        
        // 關卡 2：彎道練習
        Level level2 = new Level(2, "曲速彎道");
        level2.mazeWidth = 1080;
        level2.mazeHeight = 1920;
        level2.timeLimit = 45;
        level2.startX = 150; level2.startY = 1700;
        level2.goalX = 930; level2.goalY = 200;
        // 邊界牆
        level2.walls.add(new RectF(0, 0, 100, 1920));
        level2.walls.add(new RectF(980, 0, 1080, 1920));
        level2.walls.add(new RectF(0, 0, 1080, 100));
        level2.walls.add(new RectF(0, 1820, 1080, 1920));
        // S形彎道
        level2.walls.add(new RectF(100, 500, 700, 600));
        level2.walls.add(new RectF(380, 900, 980, 1000));
        level2.walls.add(new RectF(100, 1300, 700, 1400));
        level2.badges.add(new Badge(540, 300, 30, "迴旋大師"));
        level2.cheatCode = "NITRO";
        levels.add(level2);
        
        // 關卡 3：首個黑洞
        Level level3 = new Level(3, "黑洞邊緣");
        level3.mazeWidth = 1080;
        level3.mazeHeight = 1920;
        level3.timeLimit = 60;
        level3.startX = 150; level3.startY = 1700;
        level3.goalX = 930; level3.goalY = 200;
        // 邊界
        level3.walls.add(new RectF(0, 0, 100, 1920));
        level3.walls.add(new RectF(980, 0, 1080, 1920));
        level3.walls.add(new RectF(0, 0, 1080, 100));
        level3.walls.add(new RectF(0, 1820, 1080, 1920));
        // 中央黑洞
        level3.blackHoles.add(new BlackHole(540, 960, 80));
        // 狹窄通道
        level3.walls.add(new RectF(200, 800, 400, 1920));
        level3.walls.add(new RectF(680, 0, 880, 1120));
        level3.badges.add(new Badge(540, 150, 30, "黑洞倖存者"));
        level3.cheatCode = "INVINCIBLE";
        levels.add(level3);
        
        // 關卡 4：傳送門初體驗
        Level level4 = new Level(4, "次元跳躍");
        level4.mazeWidth = 1080;
        level4.mazeHeight = 1920;
        level4.timeLimit = 60;
        level4.startX = 150; level4.startY = 1700;
        level4.goalX = 930; level4.goalY = 200;
        // 邊界
        level4.walls.add(new RectF(0, 0, 100, 1920));
        level4.walls.add(new RectF(980, 0, 1080, 1920));
        level4.walls.add(new RectF(0, 0, 1080, 100));
        level4.walls.add(new RectF(0, 1820, 1080, 1920));
        // 障礙牆（阻擋直線路徑）
        level4.walls.add(new RectF(100, 900, 600, 1920));
        // 傳送門：左下 -> 右上
        level4.portals.add(new Portal(200, 600, 880, 400, 60));
        level4.badges.add(new Badge(400, 150, 30, "次元旅人"));
        level4.cheatCode = "TELEPORT";
        levels.add(level4);
        
        // 關卡 5：迷宮擴大 2x2（首次大迷宮）
        Level level5 = new Level(5, "時空裂隙");
        level5.mazeWidth = 2160;  // 2x 螢幕寬度
        level5.mazeHeight = 3840; // 2x 螢幕高度
        level5.timeLimit = 120;   // 時間加多
        level5.startX = 200; level5.startY = 3500;
        level5.goalX = 1960; level5.goalY = 400;
        // 複雜迷宮牆面（簡化版）
        level5.walls.add(new RectF(0, 0, 150, 3840));
        level5.walls.add(new RectF(2010, 0, 2160, 3840));
        level5.walls.add(new RectF(0, 0, 2160, 150));
        level5.walls.add(new RectF(0, 3690, 2160, 3840));
        // 多個隔間
        level5.walls.add(new RectF(500, 500, 700, 1500));
        level5.walls.add(new RectF(1200, 800, 1400, 2800));
        level5.walls.add(new RectF(600, 2200, 1600, 2400));
        // 多個黑洞
        level5.blackHoles.add(new BlackHole(900, 600, 60));
        level5.blackHoles.add(new BlackHole(1700, 2000, 80));
        // 傳送門
        level5.portals.add(new Portal(300, 1800, 1800, 3200, 50));
        level5.badges.add(new Badge(1080, 1920, 30, "裂隙穿越者"));
        level5.cheatCode = "WARP";
        levels.add(level5);
        
        // 關卡 6-10：更複雜的組合...
        // 為節省篇幅，這裡先建立基本框架
        for (int i = 6; i <= 10; i++) {
            Level level = new Level(i, "時空迷宮 Lv." + i);
            level.mazeWidth = 1080 + (i - 5) * 540;  // 遞增
            level.mazeHeight = 1920 + (i - 5) * 960;
            level.timeLimit = 60 + (i - 5) * 30;  // 時間遞增
            level.startX = 200;
            level.startY = level.mazeHeight - 200;
            level.goalX = level.mazeWidth - 200;
            level.goalY = 200;
            
            // 邊界
            level.walls.add(new RectF(0, 0, 100, level.mazeHeight));
            level.walls.add(new RectF(level.mazeWidth - 100, 0, level.mazeWidth, level.mazeHeight));
            level.walls.add(new RectF(0, 0, level.mazeWidth, 100));
            level.walls.add(new RectF(0, level.mazeHeight - 100, level.mazeWidth, level.mazeHeight));
            
            // 多個機關
            for (int j = 0; j < i; j++) {
                level.blackHoles.add(new BlackHole(
                    (float) (Math.random() * (level.mazeWidth - 400) + 200),
                    (float) (Math.random() * (level.mazeHeight - 600) + 300),
                    50 + j * 10
                ));
            }
            
            // 徽章與密技
            level.badges.add(new Badge(level.mazeWidth / 2f, level.mazeHeight / 2f, 30, "時空行者 Lv" + i));
            level.cheatCode = "CHEAT" + i;
            levels.add(level);
        }
        
        Log.d(TAG, "已初始化 " + levels.size() + " 關");
    }
    
    public Level getCurrentLevel() {
        return levels.get(currentLevel - 1);
    }
    
    public Level getLevel(int levelId) {
        if (levelId >= 1 && levelId <= TOTAL_LEVELS) {
            return levels.get(levelId - 1);
        }
        return null;
    }
    
    public boolean setCurrentLevel(int levelId) {
        if (levelId >= 1 && levelId <= TOTAL_LEVELS) {
            currentLevel = levelId;
            return true;
        }
        return false;
    }
    
    public boolean nextLevel() {
        if (currentLevel < TOTAL_LEVELS) {
            currentLevel++;
            return true;
        }
        return false;
    }
    
    public int getCurrentLevelId() {
        return currentLevel;
    }
    
    /**
     * 檢查是否完成全部10關
     */
    public boolean isGameComplete() {
        return currentLevel > TOTAL_LEVELS;
    }
}
