#!/bin/bash
# GravityMaze 遠端編譯腳本
# 在本地 Windows 執行 Gradle 編譯

echo "=========================================="
echo "  🎮 GravityMaze v1.0.2 感測器修復版"
echo "=========================================="
echo ""

# 尋找 Java
JAVA_CANDIDATES=(
    "/mnt/c/Program Files/Android/Android Studio/jbr"
    "/mnt/c/Program Files/Android/jbr"
    "/mnt/c/Program Files/Eclipse Adoptium/jdk-17"
    "/mnt/c/Program Files/Microsoft/jdk-17"
)

JAVA_HOME=""
for path in "${JAVA_CANDIDATES[@]}"; do
    if [ -f "$path/bin/java.exe" ]; then
        JAVA_HOME="$path"
        break
    fi
done

if [ -z "$JAVA_HOME" ]; then
    echo "❌ 錯誤：找不到 Java JDK"
    echo "請安裝以下任一項："
    echo "  1. Android Studio (包含 JBR)"
    echo "  2. Eclipse Temurin JDK 17"
    echo "  3. Microsoft OpenJDK 17"
    exit 1
fi

echo "✅ 使用 Java: $JAVA_HOME"

# 設定環境變數並編譯
cd /mnt/d/GravityMaze || exit 1

# 清理舊建置
if [ -d "app/build" ]; then
    rm -rf app/build
    echo "🧹 已清理舊建置"
fi

# 執行 Gradle 編譯
export JAVA_HOME
"$JAVA_HOME/bin/java" -version

echo ""
echo "🔨 開始編譯 APK..."
echo "=========================================="

# 使用 Windows 的 cmd 執行 gradlew
cmd.exe /c "D: && cd D:\\GravityMaze && set JAVA_HOME=$JAVA_HOME && gradlew.bat assembleDebug --no-daemon"

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  ✅ 編譯成功！"
    echo "=========================================="
    
    # 複製 APK
    cp "app/build/outputs/apk/debug/app-debug.apk" "GravityMaze-v1.0.2-sensor-fixed.apk"
    
    SIZE=$(stat -c%s "GravityMaze-v1.0.2-sensor-fixed.apk" 2>/dev/null || stat -f%z "GravityMaze-v1.0.2-sensor-fixed.apk" 2>/dev/null)
    SIZE_MB=$(echo "scale=2; $SIZE / 1024 / 1024" | bc 2>/dev/null || echo "$((SIZE / 1024 / 1024))")
    
    echo ""
    echo "📦 輸出檔案："
    echo "   GravityMaze-v1.0.2-sensor-fixed.apk"
    echo "   大小：${SIZE_MB} MB"
    echo ""
    echo "📝 修復內容："
    echo "   - 左右翻轉修復 (gravityX = -gravityX)"
    echo "   - 上下翻轉修復 (gravityY = -gravityY)"
else
    echo ""
    echo "❌ 編譯失敗，請檢查錯誤訊息"
    exit 1
fi
