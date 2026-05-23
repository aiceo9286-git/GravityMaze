# 重力滾球：時空迷宮

## 📋 系統需求
- Windows 10/11
- Android Studio 或 Eclipse Temurin JDK 17
- 8GB+ RAM

## 🎮 遊戲特色
- 重力感測：傾斜手機控制鋼珠
- 10 關迷宮：黑洞、傳送門、時間挑戰
- 徽章收集：解鎖 10 個密技
- 迷幻時空：AI 生成視覺風格 + Ambient 音效

## 🚀 安裝方式

### 方法一：使用批次檔編譯
1. 雙擊 `Compile.bat`
2. 等待編譯完成（約 2-3 分鐘）
3. APK 將生成在 `app/build/outputs/apk/debug/app-debug.apk`

### 方法二：Android Studio 開啟
1. 開啟 Android Studio
2. 選擇 "Open an existing project"
3. 選擇此資料夾 (`D:\GravityMaze`)
4. 點緊 ▶️ Run 按鈕

### 方法三：使用 PowerShell
```powershell
# 以系統管理員身份開啟 PowerShell
cd D:\GravityMaze
.\Build-APK.ps1
```

## 📱 開發者安裝已編譯 APK
```cmd
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 🎨 技術架構
| 組件 | 技術 |
|------|------|
| 物理引擎 | Sensor Fusion (加速度計+陀螺儀) |
| 渲染 | Android Canvas 2D |
| 音頻 | SoundPool + MediaPlayer |
| 美術 | PIL 程式生成 |

## 📝 版本記錄
**v1.0.0** - 初始完整版
- 10 關完整迷宮
- 徽章與密技系統
- Ambient 背景音樂

---
Created by Zoe AI | 2026
