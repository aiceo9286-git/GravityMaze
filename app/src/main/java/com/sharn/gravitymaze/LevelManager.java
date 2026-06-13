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
     * 初始化10關迷宮資料 - 每關都是真正的迷宮設計
     */
    private void initLevels() {
        // ========== 關卡 1：新手迷宮（簡單入門）==========
        Level level1 = new Level(1, "時空啟程");
        level1.mazeWidth = 1080;
        level1.mazeHeight = 1920;
        level1.timeLimit = 45;
        level1.startX = 150; level1.startY = 1700;
        level1.goalX = 930; level1.goalY = 200;
        // 外牆邊界
        level1.walls.add(new RectF(0, 0, 80, 1920));      // 左牆
        level1.walls.add(new RectF(1000, 0, 1080, 1920)); // 右牆
        level1.walls.add(new RectF(0, 0, 1080, 80));      // 上牆
        level1.walls.add(new RectF(0, 1840, 1080, 1920)); // 下牆
        // 迷宮內牆 - 簡單彎道
        level1.walls.add(new RectF(80, 400, 300, 480));   // 左上橫牆
        level1.walls.add(new RectF(700, 600, 1000, 680)); // 中右橫牆
        level1.walls.add(new RectF(300, 1000, 380, 1400));// 左中直牆
        level1.walls.add(new RectF(600, 1200, 1000, 1280));// 右下橫牆
        level1.walls.add(new RectF(700, 1400, 780, 1840)); // 右下直牆
        level1.badges.add(new Badge(540, 960, 25, "新手駕駛"));
        level1.cheatCode = "GODMODE";
        levels.add(level1);
        
        // ========== 關卡 2：S型迷宮 ==========
        Level level2 = new Level(2, "曲速彎道");
        level2.mazeWidth = 1080;
        level2.mazeHeight = 1920;
        level2.timeLimit = 60;
        level2.startX = 150; level2.startY = 1750;
        level2.goalX = 930; level2.goalY = 150;
        // 外牆
        level2.walls.add(new RectF(0, 0, 80, 1920));
        level2.walls.add(new RectF(1000, 0, 1080, 1920));
        level2.walls.add(new RectF(0, 0, 1080, 80));
        level2.walls.add(new RectF(0, 1840, 1080, 1920));
        // S形迷宮結構
        level2.walls.add(new RectF(80, 300, 700, 380));    // 上橫牆
        level2.walls.add(new RectF(380, 600, 1000, 680)); // 中橫牆
        level2.walls.add(new RectF(80, 900, 700, 980));    // 中下橫牆
        level2.walls.add(new RectF(380, 1300, 1000, 1380));// 下橫牆
        level2.walls.add(new RectF(300, 1500, 380, 1840)); // 左下直牆
        level2.badges.add(new Badge(540, 1100, 25, "迴旋大師"));
        level2.cheatCode = "NITRO";
        levels.add(level2);
        
        // ========== 關卡 3：中央黑洞迷宮 ==========
        Level level3 = new Level(3, "黑洞邊緣");
        level3.mazeWidth = 1080;
        level3.mazeHeight = 1920;
        level3.timeLimit = 75;
        level3.startX = 150; level3.startY = 1750;
        level3.goalX = 930; level3.goalY = 150;
        // 外牆
        level3.walls.add(new RectF(0, 0, 80, 1920));
        level3.walls.add(new RectF(1000, 0, 1080, 1920));
        level3.walls.add(new RectF(0, 0, 1080, 80));
        level3.walls.add(new RectF(0, 1840, 1080, 1920));
        // 中央黑洞區域（四周圍牆）
        level3.blackHoles.add(new BlackHole(540, 960, 70));
        level3.walls.add(new RectF(400, 820, 680, 840));   // 黑洞上
        level3.walls.add(new RectF(400, 1080, 680, 1100)); // 黑洞下
        level3.walls.add(new RectF(400, 820, 420, 1100));  // 黑洞左
        level3.walls.add(new RectF(660, 820, 680, 1100));  // 黑洞右
        // 左側通道障礙
        level3.walls.add(new RectF(200, 500, 350, 1500));
        // 右側通道障礙
        level3.walls.add(new RectF(730, 400, 880, 1400));
        level3.badges.add(new Badge(200, 300, 25, "黑洞倖存者"));
        level3.cheatCode = "INVINCIBLE";
        levels.add(level3);
        
        // ========== 關卡 4：傳送門迷宮 ==========
        Level level4 = new Level(4, "次元跳躍");
        level4.mazeWidth = 1080;
        level4.mazeHeight = 1920;
        level4.timeLimit = 80;
        level4.startX = 150; level4.startY = 1750;
        level4.goalX = 930; level4.goalY = 150;
        // 外牆
        level4.walls.add(new RectF(0, 0, 80, 1920));
        level4.walls.add(new RectF(1000, 0, 1080, 1920));
        level4.walls.add(new RectF(0, 0, 1080, 80));
        level4.walls.add(new RectF(0, 1840, 1080, 1920));
        // 左側垂直迷宮牆
        level4.walls.add(new RectF(300, 200, 380, 1400));
        // 右側垂直迷宮牆
        level4.walls.add(new RectF(700, 500, 780, 1840));
        // 中間分隔牆（必須用傳送門）
        level4.walls.add(new RectF(380, 900, 700, 980));
        // 傳送門：左下入口 -> 右上出口
        level4.portals.add(new Portal(200, 1100, 880, 400, 50));
        level4.badges.add(new Badge(540, 500, 25, "次元旅人"));
        level4.cheatCode = "TELEPORT";
        levels.add(level4);
        
        // ========== 關卡 5：大型迷宮（2x2螢幕）==========
        Level level5 = new Level(5, "時空裂隙");
        level5.mazeWidth = 2160;
        level5.mazeHeight = 3840;
        level5.timeLimit = 150;
        level5.startX = 150; level5.startY = 3600;
        level5.goalX = 2010; level5.goalY = 200;
        // 外牆
        level5.walls.add(new RectF(0, 0, 100, 3840));
        level5.walls.add(new RectF(2060, 0, 2160, 3840));
        level5.walls.add(new RectF(0, 0, 2160, 100));
        level5.walls.add(new RectF(0, 3740, 2160, 3840));
        // 第一區域（左下）迷宮
        level5.walls.add(new RectF(400, 2800, 1000, 2900));
        level5.walls.add(new RectF(100, 2200, 600, 2300));
        level5.walls.add(new RectF(800, 2400, 1080, 3200));
        // 第二區域（右下）迷宮
        level5.walls.add(new RectF(1200, 3000, 1800, 3100));
        level5.walls.add(new RectF(1400, 2400, 1500, 3500));
        level5.walls.add(new RectF(1600, 2600, 2000, 2700));
        // 第三區域（左上）迷宮
        level5.walls.add(new RectF(200, 1000, 1000, 1100));
        level5.walls.add(new RectF(400, 400, 500, 1200));
        level5.walls.add(new RectF(800, 600, 1080, 700));
        // 第四區域（右上）迷宮
        level5.walls.add(new RectF(1200, 800, 2000, 900));
        level5.walls.add(new RectF(1400, 400, 1500, 1400));
        level5.walls.add(new RectF(1700, 600, 1800, 1200));
        // 中央連接區域
        level5.walls.add(new RectF(1000, 1700, 1100, 2100)); // 中央分隔
        level5.walls.add(new RectF(1000, 1500, 1400, 1600));
        level5.walls.add(new RectF(720, 1900, 1000, 2000));
        // 黑洞
        level5.blackHoles.add(new BlackHole(1080, 1920, 80));
        level5.blackHoles.add(new BlackHole(540, 960, 60));
        level5.blackHoles.add(new BlackHole(1620, 2880, 70));
        // 傳送門
        level5.portals.add(new Portal(300, 3300, 1800, 600, 50));
        level5.portals.add(new Portal(1900, 3500, 200, 400, 50));
        level5.badges.add(new Badge(1080, 1000, 25, "裂隙穿越者"));
        level5.cheatCode = "WARP";
        levels.add(level5);
        
        // ========== 關卡 6：螺旋迷宮 ==========
        Level level6 = new Level(6, "迴旋走廊");
        level6.mazeWidth = 1620;  // 1.5x寬度
        level6.mazeHeight = 2880; // 1.5x高度
        level6.timeLimit = 120;
        level6.startX = 150; level6.startY = 2700;
        level6.goalX = 1470; level6.goalY = 150;
        // 外牆
        level6.walls.add(new RectF(0, 0, 100, 2880));
        level6.walls.add(new RectF(1520, 0, 1620, 2880));
        level6.walls.add(new RectF(0, 0, 1620, 100));
        level6.walls.add(new RectF(0, 2780, 1620, 2880));
        // 螺旋迷宮牆（由外到內）
        level6.walls.add(new RectF(300, 300, 1320, 380));
        level6.walls.add(new RectF(300, 300, 380, 2400));
        level6.walls.add(new RectF(300, 2320, 1100, 2400));
        level6.walls.add(new RectF(1020, 600, 1100, 2400));
        level6.walls.add(new RectF(600, 600, 1100, 680));
        level6.walls.add(new RectF(600, 600, 680, 1800));
        level6.walls.add(new RectF(600, 1720, 900, 1800));
        // 黑洞（螺旋中心附近）
        level6.blackHoles.add(new BlackHole(810, 1200, 60));
        level6.blackHoles.add(new BlackHole(1200, 2200, 50));
        level6.portals.add(new Portal(200, 200, 1400, 2600, 45));
        level6.badges.add(new Badge(810, 1440, 25, "螺旋行者"));
        level6.cheatCode = "SPIRAL";
        levels.add(level6);
        
        // ========== 關卡 7：多重黑洞迷宮 ==========
        Level level7 = new Level(7, "重力陷阱");
        level7.mazeWidth = 1620;
        level7.mazeHeight = 2880;
        level7.timeLimit = 140;
        level7.startX = 150; level7.startY = 2700;
        level7.goalX = 1470; level7.goalY = 150;
        // 外牆
        level7.walls.add(new RectF(0, 0, 100, 2880));
        level7.walls.add(new RectF(1520, 0, 1620, 2880));
        level7.walls.add(new RectF(0, 0, 1620, 100));
        level7.walls.add(new RectF(0, 2780, 1620, 2880));
        // 複雜迷宮牆 - 網格狀
        for (int i = 0; i < 4; i++) {
            level7.walls.add(new RectF(400 + i * 300, 400, 450 + i * 300, 2400));
        }
        for (int i = 0; i < 3; i++) {
            level7.walls.add(new RectF(100, 700 + i * 700, 1520, 750 + i * 700));
        }
        // 多個黑洞（通道中）
        level7.blackHoles.add(new BlackHole(300, 1000, 55));
        level7.blackHoles.add(new BlackHole(1300, 1000, 55));
        level7.blackHoles.add(new BlackHole(300, 1700, 60));
        level7.blackHoles.add(new BlackHole(1300, 1700, 60));
        level7.blackHoles.add(new BlackHole(810, 1350, 70));
        // 傳送門（繞過危險區域）
        level7.portals.add(new Portal(200, 500, 1400, 2400, 45));
        level7.badges.add(new Badge(810, 2800, 25, "陷阱大師"));
        level7.cheatCode = "GRAVITY";
        levels.add(level7);
        
        // ========== 關卡 8：雙層迷宮 ==========
        Level level8 = new Level(8, "鏡像迴廊");
        level8.mazeWidth = 1620;
        level8.mazeHeight = 2880;
        level8.timeLimit = 160;
        level8.startX = 150; level8.startY = 2700;
        level8.goalX = 1470; level8.goalY = 150;
        // 外牆
        level8.walls.add(new RectF(0, 0, 100, 2880));
        level8.walls.add(new RectF(1520, 0, 1620, 2880));
        level8.walls.add(new RectF(0, 0, 1620, 100));
        level8.walls.add(new RectF(0, 2780, 1620, 2880));
        // 中央對稱結構
        level8.walls.add(new RectF(790, 100, 830, 2780)); // 中央分隔線
        // 左側迷宮
        level8.walls.add(new RectF(300, 400, 700, 450));
        level8.walls.add(new RectF(300, 400, 350, 1400));
        level8.walls.add(new RectF(300, 1350, 600, 1400));
        level8.walls.add(new RectF(550, 1350, 600, 2200));
        level8.walls.add(new RectF(300, 2150, 600, 2200));
        // 右側迷宮（鏡像）
        level8.walls.add(new RectF(920, 400, 1320, 450));
        level8.walls.add(new RectF(1270, 400, 1320, 1400));
        level8.walls.add(new RectF(1020, 1350, 1320, 1400));
        level8.walls.add(new RectF(1020, 1350, 1070, 2200));
        level8.walls.add(new RectF(1020, 2150, 1320, 2200));
        // 連接左右兩側的傳送門
        level8.portals.add(new Portal(400, 2600, 1220, 2600, 45));
        level8.portals.add(new Portal(400, 300, 1220, 300, 45));
        // 黑洞
        level8.blackHoles.add(new BlackHole(650, 1700, 50));
        level8.blackHoles.add(new BlackHole(970, 1700, 50));
        level8.badges.add(new Badge(810, 1440, 25, "鏡像大師"));
        level8.cheatCode = "MIRROR";
        levels.add(level8);
        
        // ========== 關卡 9：終極迷宮（3x2螢幕）==========
        Level level9 = new Level(9, "多重宇宙");
        level9.mazeWidth = 2700;
        level9.mazeHeight = 3840;
        level9.timeLimit = 200;
        level9.startX = 150; level9.startY = 3600;
        level9.goalX = 2550; level9.goalY = 200;
        // 外牆
        level9.walls.add(new RectF(0, 0, 100, 3840));
        level9.walls.add(new RectF(2600, 0, 2700, 3840));
        level9.walls.add(new RectF(0, 0, 2700, 100));
        level9.walls.add(new RectF(0, 3740, 2700, 3840));
        // 三個區域的分隔
        level9.walls.add(new RectF(900, 0, 1000, 3840));  // 第一分隔
        level9.walls.add(new RectF(1800, 0, 1900, 3840)); // 第二分隔
        // 第一區域迷宮（左）
        level9.walls.add(new RectF(200, 800, 800, 900));
        level9.walls.add(new RectF(200, 1600, 700, 1700));
        level9.walls.add(new RectF(400, 2400, 800, 2500));
        level9.walls.add(new RectF(200, 3200, 600, 3300));
        // 第二區域迷宮（中）
        level9.walls.add(new RectF(1100, 500, 1700, 600));
        level9.walls.add(new RectF(1200, 1300, 1700, 1400));
        level9.walls.add(new RectF(1100, 2100, 1600, 2200));
        level9.walls.add(new RectF(1300, 2900, 1700, 3000));
        // 第三區域迷宮（右）
        level9.walls.add(new RectF(2000, 700, 2600, 800));
        level9.walls.add(new RectF(2100, 1500, 2600, 1600));
        level9.walls.add(new RectF(2000, 2300, 2500, 2400));
        level9.walls.add(new RectF(2200, 3100, 2600, 3200));
        // 多個傳送門連接三個區域
        level9.portals.add(new Portal(500, 3600, 1400, 400, 50));  // 左下->中上
        level9.portals.add(new Portal(1400, 3600, 2300, 400, 50)); // 中下->右上
        level9.portals.add(new Portal(2300, 3600, 500, 2000, 50)); // 右下->左中
        // 黑洞
        level9.blackHoles.add(new BlackHole(500, 1200, 65));
        level9.blackHoles.add(new BlackHole(1400, 1920, 70));
        level9.blackHoles.add(new BlackHole(2300, 2700, 65));
        level9.badges.add(new Badge(1350, 1920, 25, "宇宙旅者"));
        level9.cheatCode = "COSMOS";
        levels.add(level9);
        
        // ========== 關卡 10：終極挑戰（3x3螢幕）==========
        Level level10 = new Level(10, "終極時空");
        level10.mazeWidth = 3240;
        level10.mazeHeight = 5760;
        level10.timeLimit = 300;
        level10.startX = 150; level10.startY = 5500;
        level10.goalX = 3090; level10.goalY = 150;
        // 外牆
        level10.walls.add(new RectF(0, 0, 120, 5760));
        level10.walls.add(new RectF(3120, 0, 3240, 5760));
        level10.walls.add(new RectF(0, 0, 3240, 120));
        level10.walls.add(new RectF(0, 5640, 3240, 5760));
        // 九宮格結構 - 垂直分隔
        level10.walls.add(new RectF(1080, 120, 1160, 5640));
        level10.walls.add(new RectF(2200, 120, 2280, 5640));
        // 九宮格結構 - 水平分隔
        level10.walls.add(new RectF(120, 1920, 3120, 2000));
        level10.walls.add(new RectF(120, 3840, 3120, 3920));
        // 各區域內部迷宮（每個3x3區域都有獨特設計）
        // 左下區域
        level10.walls.add(new RectF(300, 4800, 900, 4900));
        level10.walls.add(new RectF(600, 4200, 700, 4900));
        // 中下區域
        level10.walls.add(new RectF(1300, 4500, 1900, 4600));
        level10.walls.add(new RectF(1500, 4600, 1600, 5400));
        // 右下區域
        level10.walls.add(new RectF(2400, 4800, 3000, 4900));
        level10.walls.add(new RectF(2600, 4200, 2700, 4900));
        // 左中區域
        level10.walls.add(new RectF(300, 2880, 900, 2960));
        level10.walls.add(new RectF(600, 2200, 700, 2880));
        // 中央區域（核心，最複雜）
        level10.walls.add(new RectF(1300, 2500, 1400, 3300));
        level10.walls.add(new RectF(1900, 2500, 2000, 3300));
        level10.walls.add(new RectF(1400, 2880, 1900, 2960));
        // 右中區域
        level10.walls.add(new RectF(2400, 2880, 3000, 2960));
        level10.walls.add(new RectF(2600, 2200, 2700, 2880));
        // 左上區域
        level10.walls.add(new RectF(300, 960, 900, 1040));
        level10.walls.add(new RectF(600, 200, 700, 960));
        // 中上區域
        level10.walls.add(new RectF(1300, 700, 1900, 780));
        level10.walls.add(new RectF(1500, 200, 1600, 700));
        // 右上區域
        level10.walls.add(new RectF(2400, 960, 3000, 1040));
        level10.walls.add(new RectF(2600, 200, 2700, 960));
        // 傳送門系統（連接各區域）
        level10.portals.add(new Portal(500, 5400, 1620, 1000, 55));  // 左下->中央上
        level10.portals.add(new Portal(1620, 5400, 2800, 3000, 55)); // 中下->右中
        level10.portals.add(new Portal(2800, 5400, 500, 3000, 55));  // 右下->左中
        level10.portals.add(new Portal(500, 3600, 2800, 1000, 55)); // 左中->右上
        level10.portals.add(new Portal(1620, 3600, 2800, 5000, 55)); // 中央->右下
        level10.portals.add(new Portal(2800, 3600, 500, 1000, 55)); // 右中->左上
        // 黑洞大軍
        level10.blackHoles.add(new BlackHole(1620, 2880, 80));  // 中央大黑洞
        level10.blackHoles.add(new BlackHole(500, 1500, 60));
        level10.blackHoles.add(new BlackHole(2740, 1500, 60));
        level10.blackHoles.add(new BlackHole(500, 4500, 60));
        level10.blackHoles.add(new BlackHole(2740, 4500, 60));
        level10.blackHoles.add(new BlackHole(1100, 2880, 50));
        level10.blackHoles.add(new BlackHole(2140, 2880, 50));
        level10.badges.add(new Badge(1620, 2880, 30, "時空主宰"));
        level10.cheatCode = "MASTER";
        levels.add(level10);
        
        Log.d(TAG, "已初始化 " + levels.size() + " 關迷宮地圖");
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
