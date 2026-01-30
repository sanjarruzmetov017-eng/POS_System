#!/bin/bash
# SmartPOS Linux Packaging Script

echo "🚀 Linux uchun SmartPOS installer yaratish boshlandi..."

# 1. Tozalash va JAR yaratish
echo "📦 Maven yordamida loyihani yig'ish..."
mvn clean package -DskipTests

# 2. Tarqatish papkasini tayyorlash (Flat JAR structure)
echo "📁 Kutubxonalarni tayyorlash..."
rm -rf target/dist
mkdir -p target/dist
cp target/smartpos-1.1.0.jar.original target/dist/smartpos.jar
mvn dependency:copy-dependencies -DoutputDirectory=target/dist

# 3. jpackage orqali installer yaratish
echo "🛠️ Native installer (.deb) yaratish..."
# JDK 17 dan foydalanamiz (barqarorligi uchun)
JAVA_HOME_PATH="/usr/lib/jvm/java-1.17.0-openjdk-amd64"

$JAVA_HOME_PATH/bin/jpackage --type deb \
  --input target/dist/ \
  --main-jar smartpos.jar \
  --main-class com.smartpos.SmartPosLauncher \
  --name "smartpos" \
  --app-version "1.1.0" \
  --vendor "Antigravity" \
  --description "Smart Point of Sale System" \
  --icon src/main/resources/icons/app_icon.png \
  --linux-shortcut --linux-menu-group "Office"

echo "✅ Linux uchun installer tayyor: smartpos_1.0.0-1_amd64.deb"
