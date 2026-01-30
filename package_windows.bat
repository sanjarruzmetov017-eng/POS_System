@echo off
REM SmartPOS Windows Packaging Script

echo 🚀 Windows uchun SmartPOS installer yaratish boshlandi...

REM 1. Tozalash va JAR yaratish
echo 📦 Maven yordamida loyihani yig'ish...
call mvn clean package -DskipTests

REM 2. Tarqatish papkasini tayyorlash (Flat JAR structure)
echo 📁 Kutubxonalarni tayyorlash...
if exist target\dist rmdir /s /q target\dist
mkdir target\dist
copy target\smartpos-0.0.1-SNAPSHOT.jar.original target\dist\smartpos.jar
call mvn dependency:copy-dependencies -DoutputDirectory=target\dist

REM 3. jpackage orqali installer yaratish
echo 🛠️ Native installer (.exe) yaratish...
REM Diqqat: WiX Toolset o'rnatilgan bo'lishi shart!
jpackage --type exe ^
  --input target\dist\ ^
  --main-jar smartpos.jar ^
  --main-class com.smartpos.SmartPosLauncher ^
  --name "SmartPOS" ^
  --app-version "1.0.0" ^
  --vendor "Antigravity" ^
  --description "Smart Point of Sale System" ^
  --icon src\main\resources\icons\app_icon.png ^
  --win-shortcut --win-menu

echo ✅ Windows uchun installer tayyor: SmartPOS-1.0.0.exe
pause
