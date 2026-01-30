# SmartPOS - Extreme UI Edition v1.1 🚀

![JavaFX](https://img.shields.io/badge/JavaFX-100%25-orange) ![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey) ![License](https://img.shields.io/badge/License-Proprietary-red)

**SmartPOS** is a modern, enterprise-grade Point of Sale (POS) system built with **JavaFX** and **Spring Boot**. The **Extreme UI Edition (v1.1)** features a completely redesigned "Midnight Neon" interface with glassmorphism, advanced animations, and premium aesthetics.

> [!IMPORTANT]
> **WINDOWS COMPATIBILITY:** ✅ verified.
> This application is 100% compatible with Windows (10/11) and provides a native `.exe` installer.

---

## 🪟 Windows Installation (Guide)

To run SmartPOS on Windows, you will build a native `.exe` file. This allows you to install it just like any other program, without needing `java -jar` commands.

### Prerequisites for Windows Build
1.  **JDK 17+** installed.
2.  **Maven** installed (added to PATH).
3.  **WiX Toolset** (Required for creating .exe files). [Download WiX v3](https://wixtoolset.org/releases/)

### How to Build .exe
1.  Navigate to the project folder in Command Prompt or PowerShell.
2.  Run the automated script:
    ```cmd
    package_windows.bat
    ```
3.  **That's it!** The script will:
    *   Clean and rebuild the project (v1.1.0).
    *   Gather all dependencies.
    *   Generate a `SmartPOS-1.1.0.exe` installer in `target/`.

Double-click the generated `.exe` to install SmartPOS.

---

## 🐧 Linux Installation

For Debian/Ubuntu based systems:

1.  Open terminal in project directory.
2.  Run:
    ```bash
    ./package_linux.sh
    ```
3.  Install the generated `.deb` package:
    ```bash
    sudo dpkg -i smartpos_1.1.0-1_amd64.deb
    ```

---

## ✨ Key Features (v1.1)

*   **Extreme UI Overhaul**: Glassmorphism, neon accents, and smooth transitions.
*   **Role-Based Access**: Secure Admin and Cashier modes with PIN authentication.
*   **Real-time Analytics**: Dashboard with live revenue, sales, and stock metrics.
*   **Inventory Management**: Barcode support, batch tracking, and low stock alerts.
*   **Multi-Store Ready**: Built-in architecture for scaling to multiple tenants.

## 🛠 Developer Setup

1.  Clone the repository.
2.  Open in IntelliJ IDEA or Eclipse.
3.  Run `com.smartpos.SmartPosLauncher` to start.
