package com.sharn.gravitymaze;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * 音效管理器
 */
public class SoundManager {
    
    private SoundPool soundPool;
    private MediaPlayer bgmPlayer;
    
    private int collisionSound;
    private int portalSound;
    private int badgeSound;
    private int winSound;
    private int gameoverSound;
    
    public SoundManager(Context context) {
        // 初始化 SoundPool
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        
        soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build();
        
        // 載入音效
        collisionSound = soundPool.load(context, R.raw.collision, 1);
        portalSound = soundPool.load(context, R.raw.portal, 1);
        badgeSound = soundPool.load(context, R.raw.badge, 1);
        winSound = soundPool.load(context, R.raw.win, 1);
        gameoverSound = soundPool.load(context, R.raw.gameover, 1);
    }
    
    public void playBGM(Context context) {
        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(context, R.raw.ambient_bg);
            bgmPlayer.setLooping(true);
            bgmPlayer.setVolume(0.5f, 0.5f);
        }
        if (!bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }
    
    public void pauseBGM() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }
    
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.release();
            bgmPlayer = null;
        }
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
        if (soundPool != null) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        }
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        stopBGM();
    }
}
