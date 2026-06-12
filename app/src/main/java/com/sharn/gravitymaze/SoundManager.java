package com.sharn.gravitymaze;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

/**
 * 音效管理器
 */
public class SoundManager {
    private static final String TAG = "SoundManager";
    
    private SoundPool soundPool;
    private MediaPlayer bgmPlayer;
    
    private int collisionSound;
    private int portalSound;
    private int badgeSound;
    private int winSound;
    private int gameoverSound;
    private volatile boolean soundsLoaded = false;
    
    public SoundManager(Context context) {
        if (context == null) {
            Log.w(TAG, "Context unavailable; audio disabled");
            return;
        }

        // 初始化 SoundPool
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build();
        
        // 設置載入完成監聽
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                soundsLoaded = true;
            }
        });
        
        try {
            // 載入音效
            collisionSound = soundPool.load(context, R.raw.collision, 1);
            portalSound = soundPool.load(context, R.raw.portal, 1);
            badgeSound = soundPool.load(context, R.raw.badge, 1);
            winSound = soundPool.load(context, R.raw.win, 1);
            gameoverSound = soundPool.load(context, R.raw.gameover, 1);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to load sound effects", e);
        }
    }
    
    public void playBGM(Context context) {
        if (context == null) {
            return;
        }
        if (bgmPlayer == null) {
            try {
                bgmPlayer = MediaPlayer.create(context, R.raw.ambient_bg);
                if (bgmPlayer == null) {
                    Log.w(TAG, "BGM resource could not be loaded");
                    return;
                }
                bgmPlayer.setLooping(true);
                bgmPlayer.setVolume(0.5f, 0.5f);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to create BGM player", e);
                bgmPlayer = null;
                return;
            }
        }
        try {
            if (!bgmPlayer.isPlaying()) {
                bgmPlayer.start();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to start BGM", e);
            releaseBgmPlayer();
        }
    }
    
    public void pauseBGM() {
        try {
            if (bgmPlayer != null && bgmPlayer.isPlaying()) {
                bgmPlayer.pause();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to pause BGM", e);
            releaseBgmPlayer();
        }
    }
    
    public void stopBGM() {
        releaseBgmPlayer();
    }
    
    public void playCollision() {
        playSound(collisionSound, 0.7f);
    }
    
    public void playPortal() {
        playSound(portalSound, 0.8f);
    }
    
    public void playBadge() {
        playSound(badgeSound, 0.9f);
    }
    
    public void playWin() {
        playSound(winSound, 1.0f);
    }
    
    public void playGameOver() {
        playSound(gameoverSound, 0.8f);
    }
    
    private void playSound(int soundId, float volume) {
        if (soundPool != null && soundId != 0 && soundsLoaded) {
            try {
                soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to play sound effect", e);
            }
        }
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        stopBGM();
    }

    private void releaseBgmPlayer() {
        if (bgmPlayer != null) {
            try {
                bgmPlayer.release();
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to release BGM player", e);
            } finally {
                bgmPlayer = null;
            }
        }
    }
}
