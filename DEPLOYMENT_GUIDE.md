## 🚀 Avtomatik Packaging (Tavsiya etiladi)

Men siz uchun jarayonni to'liq avtomatlashtirib berdim. Kompyuteringiz turiga qarab quyidagi fayllarni ishlating:

### 🐧 Linux uchun:
Terminalda loyiha papkasiga kiring va quyidagilarni bajaring:
```bash
chmod +x package_linux.sh
./package_linux.sh
```
Natijada `smartpos_1.0.0-1_amd64.deb` fayli hosil bo'ladi.

### 🪟 Windows uchun:
Loyiha papkasini Windows-ga ko'chiring va **`package_windows.bat`** faylini shunchaki ishlating.
*(Eslatma: Windows-da WiX Toolset o'rnatilgan bo'lishi shart).*

---

## 1. Tayyorgarlik (Build)
Dastlab, barcha kodlarni bitta JAR faylga yig'ishimiz kerak:
```bash
mvn clean package
```
Bu buyruq `target/smartpos-0.0.1-SNAPSHOT.jar` faylini yaratadi.

## 2. Native Installer yaratish (jpackage)

### 🐧 Linux uchun (.deb paket)
Ubuntu/Debian-da o'rnatiladigan fayl yaratish uchun:
```bash
jpackage --type deb \
  --input target/ \
  --main-jar smartpos-0.0.1-SNAPSHOT.jar \
  --main-class com.smartpos.SmartPosLauncher \
  --name "SmartPOS" \
  --app-version "1.0.0" \
  --vendor "Antigravity" \
  --icon src/main/resources/icons/app_icon.png \
  --linux-shortcut --linux-menu-group "Office"
```

### 🪟 Windows uchun (.exe)
Windows kompyuterda terminalni ochib quyidagini ishlating (WiX Toolset o'rnatilgan bo'lishi shart):
```cmd
jpackage --type exe ^
  --input target/ ^
  --main-jar smartpos-0.0.1-SNAPSHOT.jar ^
  --main-class com.smartpos.SmartPosLauncher ^
  --name "SmartPOS" ^
  --win-shortcut --win-menu
```

## 3. Nima uchun jpackage?
- **JRE kerak emas:** Foydalanuvchi kompyuterida Java o'rnatilgan bo'lishi shart emas (dastur o'zi bilan minimal Java muhitini olib yuradi).
- **Ikona va Menu:** Dastur menyuda va ish stolida o'z ikonasi bilan paydo bo'ladi.
- **O'rnatish oson:** Oddiy "Next-Next" orqali o'rnatiladi.

> [!TIP]
> Agar ikonangiz tayyor bo'lsa, `src/main/resources/icons/` papkasiga joylang. Hozircha biz terminalda `mvn package` qilib JAR faylni ishlatib tura olamiz.
