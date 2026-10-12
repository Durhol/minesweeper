# 💣 Retro Minesweeper

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://www.minecraft.net/)
[![Loader NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange.svg)](https://neoforged.net/)
[![Environment Both](https://img.shields.io/badge/Environment-Client%20%26%20Server-blue.svg)](https://neoforged.net/)
[![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![GitHub Releases](https://img.shields.io/badge/Release-v1.0.0-informational.svg)](https://github.com/Durhol/minesweeper/releases)

![Minesweeper Icon](icon.png)

> **Author:** Durhol  
> **Platform:** Minecraft 1.21.1 (NeoForge 21.1.259+)  
> **License:** MIT  
> **Repository:** https://github.com/Durhol/minesweeper  

---

## 🕹️ About the Mod

**Retro Minesweeper** brings the legendary classic Windows Minesweeper experience (Windows 95 / XP style) directly into Minecraft inside a dedicated pocket dimension (`minesweeper:game_world`).

Craft a portable Minesweeper Device, right-click to enter, and test your wits on an isolated floating void arena. Your personal inventory is safely stashed away during the match, and you are equipped with authentic temporary tools: the Trowel, the Flag, and the Forfeit/Exit tool!

---

## ✨ Key Features

- 🎮 **3 Difficulty Tiers:**
  - **Tier 1 (Beginner):** $9 \times 9$ grid, 10 mines. Victory Reward: **1x Diamond** 💎.
  - **Tier 2 (Intermediate):** $16 \times 16$ grid, 40 mines. Victory Reward: **5x Diamonds** 💎💎💎💎💎.
  - **Tier 3 (Expert):** $30 \times 16$ grid, 99 mines. Victory Reward: **2x Netherite Ingots** 🪙🪙.
- 🛡️ **Guaranteed Safe First Click:**
  - Mine distribution is generated strictly *after* your first click — the initial clicked tile and all 8 surrounding neighbors are 100% mine-free!
- 🌊 **Recursive Flood Fill:**
  - Digging a zero-adjacent cell automatically reveals adjacent clear regions recursively.
- 🚩 **Strict Flag Cap:**
  - Flag count cannot exceed total mine count — your mine counter never drops into negative numbers. Flagged tiles are protected from accidental clicks.
- 🎨 **Authentic Windows 95/XP Visuals:**
  - 3D beveled unpressed buttons, classic colored digits 1–8, spiked retro mines, and exploded mine red highlight.
- 🎵 **Custom Sound Design:**
  - Unique sound effects for game start, cell digging/clicking, flag placement, victory fanfare, and defeat explosion!
- 💥 **Explosion Animation & Post-Game Field Inspection:**
  - Triggering a mine produces explosion particles and smoke over all hidden mines without instantly ejecting you. You can walk around, analyze the revealed board, and leave whenever ready via the *Forfeit & Exit* tool.
- 🔒 **Arena & Inventory Protection:**
  - Arena tiles have bedrock-tier blast resistance, tool dropping is prohibited, and your original inventory is automatically restored upon return.

---

## 🔨 Crafting Recipes

### Tier 1 Device (Beginner):
- 1 TNT
- 1 Diamond
- 2 Iron Ingots
- 1 Gold Ingot
- 4 Redstone Dust

---

## 🚀 Installation

1. Install [Minecraft 1.21.1](https://www.minecraft.net/) and [NeoForge](https://neoforged.net/) `21.1.259+`.
2. Download `minesweeper-1.0.0.jar` from the [Releases](https://github.com/Durhol/minesweeper/releases) page or [Modrinth](https://modrinth.com/).
3. Place the `.jar` into your `.minecraft/mods/` directory.
4. Launch Minecraft and enjoy!
