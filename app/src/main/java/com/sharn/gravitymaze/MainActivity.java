package com.sharn.gravitymaze;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * 主活動：遊戲入口與流程控制
 */
public class MainActivity extends AppCompatActivity 
    implements GameView.GameCallback, GravitySensorManager.GravityCallback {
    
    private static final String TAG = "MainActivity";
    private static final float DELTA_TIME = 1f / 60f; // 60 FPS
    
    // 遊戲元件
    private GameView gameView;
    private GravitySensorManager sensorManager;
    private LevelManager levelManager;
    
    // 遊戲循環
    private final Handler gameHandler = new Handler(Looper.getMainLooper());
    private Runnable gameLoop;
    private boolean isPaused = false;
    private SoundManager soundManager;
    
    // UI
    private Button pauseButton;
    private LinearLayout menuLayout;
    private LinearLayout gameOverLayout;
    private LinearLayout levelCompleteLayout;
    private TextView cheatCodeText;
    
    // 當前重力值
    private float currentGravityX = 0;
    private float currentGravityY = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化元件
        initViews();
        initGameComponents();
    }
    
    private void initViews() {
        gameView = findViewById(R.id.gameView);
        if (gameView == null) throw new IllegalStateException("Missing required view: gameView");
        gameView.setCallback(this);
        // ✅ 修復：soundManager 在 initGameComponents() 中初始化，這裡延後設置
        // gameView.setSoundManager(soundManager);  // 移到 initGameComponents 之後
        
        pauseButton = findViewById(R.id.pauseButton);
        if (pauseButton == null) throw new IllegalStateException("Missing required view: pauseButton");
        menuLayout = findViewById(R.id.menuLayout);
        if (menuLayout == null) throw new IllegalStateException("Missing required view: menuLayout");
        gameOverLayout = findViewById(R.id.gameOverLayout);
        if (gameOverLayout == null) throw new IllegalStateException("Missing required view: gameOverLayout");
        levelCompleteLayout = findViewById(R.id.levelCompleteLayout);
        if (levelCompleteLayout == null) throw new IllegalStateException("Missing required view: levelCompleteLayout");
        cheatCodeText = findViewById(R.id.cheatCodeText);
        if (cheatCodeText == null) throw new IllegalStateException("Missing required view: cheatCodeText");
        
        //開始遊戲按鈕
        Button startButton = findViewById(R.id.startButton);
        if (startButton == null) throw new IllegalStateException("Missing required view: startButton");
        startButton.setOnClickListener(v -> startGame());
        
        // 繼續按鈕
        pauseButton.setOnClickListener(v -> togglePause());
        
        // 重新開始
        Button restartButton = findViewById(R.id.restartButton);
        if (restartButton == null) throw new IllegalStateException("Missing required view: restartButton");
        restartButton.setOnClickListener(v -> restartLevel());
        
        // 下一關
        Button nextLevelButton = findViewById(R.id.nextLevelButton);
        if (nextLevelButton == null) throw new IllegalStateException("Missing required view: nextLevelButton");
        nextLevelButton.setOnClickListener(v -> nextLevel());
        
        // 返回主選單
        Button menuButton = findViewById(R.id.menuButton);
        if (menuButton == null) throw new IllegalStateException("Missing required view: menuButton");
        menuButton.setOnClickListener(v -> returnToMenu());
        
        // 主選單
        Button menuRestartButton = findViewById(R.id.menuRestartButton);
        if (menuRestartButton == null) throw new IllegalStateException("Missing required view: menuRestartButton");
        menuRestartButton.setOnClickListener(v -> {
            if (gameView != null && gameView.isGameRunning()) {
                restartLevel();
            } else {
                startGame();
            }
        });
    }
    
    private void initGameComponents() {
        levelManager = new LevelManager();
        sensorManager = new GravitySensorManager(this, this);
        soundManager = new SoundManager(this);
        // ✅ 修復：在 soundManager 初始化後再設置到 gameView
        gameView.setSoundManager(soundManager);
    }
    
    private void startGame() {
        if (gameView == null || levelManager == null) {
            Toast.makeText(this, "遊戲初始化失敗", Toast.LENGTH_SHORT).show();
            return;
        }

        menuLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);
        levelCompleteLayout.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        
        // 播放背景音樂
        if (soundManager != null) {
            soundManager.playBGM(this);
        }
        
        // 載入第一關
        LevelManager.Level level = levelManager.getCurrentLevel();
        if (level == null) {
            Toast.makeText(this, "關卡載入失敗", Toast.LENGTH_SHORT).show();
            return;
        }
        gameView.startLevel(level);
        
        // 啟動感測器
        if (sensorManager != null) {
            sensorManager.start();
        }
        
        // 啟動遊戲循環
        isPaused = false;
        startGameLoop();
        
        Toast.makeText(this, "關卡 " + level.id + ": " + level.name, Toast.LENGTH_SHORT).show();
    }
    
    private void startGameLoop() {
        stopGameLoop();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isPaused && gameView.isGameRunning()) {
                    gameView.update(DELTA_TIME, currentGravityX, currentGravityY);
                    
                    // 檢查碰撞震動
                    if (gameView.getBall() != null && 
                        gameView.getBall().getWallCollisionCount() > 0) {
                        sensorManager.triggerHapticFeedback();
                        gameView.getBall().resetCollisionCount();
                    }
                }
                gameHandler.postDelayed(this, 16); // ~60 FPS
            }
        };
        gameHandler.post(gameLoop);
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "繼續" : "暫停");
    }
    
    private void restartLevel() {
        if (gameView == null || levelManager == null) return;

        stopGameLoop();
        LevelManager.Level level = levelManager.getCurrentLevel();
        if (level == null) {
            Toast.makeText(this, "關卡載入失敗", Toast.LENGTH_SHORT).show();
            return;
        }
        gameView.startLevel(level);
        menuLayout.setVisibility(View.GONE);
        gameOverLayout.setVisibility(View.GONE);
        levelCompleteLayout.setVisibility(View.GONE);
        isPaused = false;
        pauseButton.setText("暫停");
        pauseButton.setVisibility(View.VISIBLE);
        if (sensorManager != null) {
            sensorManager.start();
        }
        startGameLoop();
    }
    
    private void nextLevel() {
        if (gameView == null || levelManager == null) return;

        stopGameLoop();
        
        if (levelManager.nextLevel()) {
            // 有下一關
            LevelManager.Level level = levelManager.getCurrentLevel();
            if (level == null) {
                Toast.makeText(this, "關卡載入失敗", Toast.LENGTH_SHORT).show();
                return;
            }
            gameView.startLevel(level);
            levelCompleteLayout.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
            isPaused = false;
            if (sensorManager != null) {
                sensorManager.start();
            }
            startGameLoop();
            Toast.makeText(this, "關卡 " + level.id + ": " + level.name, Toast.LENGTH_SHORT).show();
        } else {
            // 通關全部10關！
            showGameCompleteDialog();
        }
    }
    
    private void returnToMenu() {
        stopGameLoop();
        menuLayout.setVisibility(View.VISIBLE);
        gameOverLayout.setVisibility(View.GONE);
        levelCompleteLayout.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        if (sensorManager != null) {
            sensorManager.stop();
        }
        if (soundManager != null) {
            soundManager.pauseBGM();
        }
    }
    
    private void showGameCompleteDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🎉 恭喜通關！")
            .setMessage("您已完成全部10關《重力滾球：時空迷宮》！\n\n感謝遊玩！")
            .setPositiveButton("返回主選單", (dialog, which) -> returnToMenu())
            .setCancelable(false)
            .show();
    }
    
    // ================== 回調介面實作 ==================
    
    @Override
    public void onGravityChanged(float x, float y, float tiltAngle) {
        currentGravityX = x;
        currentGravityY = y;
    }
    
    @Override
    public void onLevelComplete(List<LevelManager.Badge> badges) {
        if (sensorManager != null) {
            sensorManager.stop();
        }
        stopGameLoop();
        
        // 顯示通關畫面
        levelCompleteLayout.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        
        // 顯示收集的徽章
        TextView badgesText = findViewById(R.id.badgesCollectedText);
        if (badgesText == null) {
            Log.e(TAG, "badgesCollectedText view not found");
            return;
        }
        if (badges == null || badges.isEmpty()) {
            badgesText.setText("本關無徽章收集");
        } else {
            StringBuilder sb = new StringBuilder("收集徽章:\n");
            for (LevelManager.Badge badge : badges) {
                if (badge != null) {
                    sb.append("🏅 ").append(badge.name).append("\n");
                }
            }
            badgesText.setText(sb.toString());
        }
        
        // 檢查是否解鎖密技
        LevelManager.Level level = levelManager.getCurrentLevel();
        if (level != null && level.cheatUnlocked) {
            cheatCodeText.setVisibility(View.VISIBLE);
            cheatCodeText.setText("🔓 密技解鎖: " + level.cheatCode);
        } else {
            cheatCodeText.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onGameOver(String reason) {
        if (sensorManager != null) {
            sensorManager.stop();
        }
        stopGameLoop();
        
        // 顯示失敗畫面
        gameOverLayout.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        
        TextView reasonText = findViewById(R.id.gameOverReasonText);
        if (reasonText != null) {
            reasonText.setText(reason != null ? reason : "未知原因");
        }
    }
    
    @Override
    public void onBadgeCollected(LevelManager.Badge badge) {
        runOnUiThread(() -> {
            Toast.makeText(this, "🏅 獲得徽章: " + badge.name, Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onCheatUnlocked(String cheatCode) {
        runOnUiThread(() -> {
            Toast.makeText(this, "🔓 密技解鎖: " + cheatCode, Toast.LENGTH_LONG).show();
        });
    }
    
    // ================== 生命周期 ==================
    
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.stop();
        }
        if (soundManager != null) {
            soundManager.pauseBGM();
        }
        isPaused = true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null && gameView.isGameRunning()) {
            if (sensorManager != null) {
                sensorManager.start();
            }
            isPaused = false;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameLoop();
        if (sensorManager != null) {
            sensorManager.stop();
        }
        if (soundManager != null) {
            soundManager.release();
            soundManager = null;
        }
        if (gameView != null) {
            gameView.setSoundManager(null);
            gameView.setCallback(null);
        }
    }

    private void stopGameLoop() {
        if (gameLoop != null) {
            gameHandler.removeCallbacks(gameLoop);
            gameLoop = null;
        }
    }
}
