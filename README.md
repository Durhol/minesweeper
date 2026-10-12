# 💣 Retro Minesweeper

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.minecraft.net/)
[![Loader NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange.svg)](https://neoforged.net/)
[![Environment Both](https://img.shields.io/badge/Environment-Client%20%26%20Server-blue.svg)](https://neoforged.net/)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![GitHub Releases](https://img.shields.io/badge/Release-v1.0.0-informational.svg)](https://github.com/Durhol/minesweeper/releases)

![Minesweeper Icon](icon.png)

**Retro Minesweeper** brings the legendary classic Windows Minesweeper experience (Windows 95 / XP style) directly into Minecraft inside a dedicated pocket dimension (`minesweeper:game_world`). Craft a handheld device, right-click to enter, and test your logic on a floating void grid with authentic retro textures, custom sounds, and valuable rewards!

---

## 📸 Features

- 🎮 **3 Difficulty Tiers:**
  - **Tier 1 (Beginner):** $9 \times 9$ grid, 10 mines. Victory Reward: **1x Diamond** 💎.
  - **Tier 2 (Intermediate):** $16 \times 16$ grid, 40 mines. Victory Reward: **5x Diamonds** 💎💎💎💎💎.
  - **Tier 3 (Expert):** $30 \times 16$ grid, 99 mines. Victory Reward: **2x Netherite Ingots** 🪙🪙.
- 🛡️ **Guaranteed Safe First Click:** Mine distribution is generated strictly *after* your first click — the initial clicked tile and all 8 surrounding neighbors are guaranteed to never contain mines.
- 🌊 **Recursive Flood Fill:** Digging an empty cell (0 adjacent mines) automatically triggers a recursive cascade that clears surrounding safe areas.
- 🚩 **Strict Flag Cap:** You cannot place more flags than the total number of mines in the arena. Your mine counter will never drop into negative numbers, and flagged cells are protected from accidental digging.
- 🎨 **Authentic Windows 95/XP Visuals:** 3D beveled buttons, classic colored digits (1–8), spiked retro mines, and exploded mine red highlight.
- 🎵 **Custom Sound Design:** Dedicated retro sound effects for game start, cell digging/clicking, flag toggling, victory fanfare, and defeat explosion.
- 💥 **Explosion Animation & Board Inspection:** Tripping a mine triggers explosion particles and smoke over all hidden mines without immediately kicking you out. You can walk around, inspect the entire revealed minefield, and return home whenever you want via the *Forfeit & Exit* tool.
- 🔒 **Zero Loss & Dimension Isolation:** Player inventories are safely stashed before entering the pocket dimension and fully restored upon return. Arena blocks are indestructible (bedrock-tier), and game tools cannot be dropped or lost.

---

## 🛠️ Installation & Requirements

- **Minecraft:** `1.21.1`
- **Mod Loader:** `NeoForge >= 21.1.259`
- Place the downloaded `minesweeper-1.0.0.jar` into your Minecraft profile's `mods` folder.

---

## 🔨 Crafting Recipes

### Tier 1 Device (Beginner):
- 1 TNT
- 1 Diamond
- 2 Iron Ingots
- 1 Gold Ingot
- 4 Redstone Dust

---

## 🔨 Building from Source

```bash
git clone https://github.com/Durhol/minesweeper.git
cd minesweeper
./gradlew build # or gradlew.bat build on Windows
```

The compiled mod JAR will be located at `build/libs/minesweeper-1.0.0.jar`.

---

## 👤 Author & Contacts

- **Author:** Durhol
- **Telegram:** [@tyxap](https://t.me/tyxap) or [@Durhol](https://t.me/Durhol)

---

## 📄 License

This project is licensed under the [MIT License](https://github.com/Durhol/minesweeper/blob/main/LICENSE).
