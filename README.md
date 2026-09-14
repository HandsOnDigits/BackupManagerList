# Backup Manager List

A client-side Minecraft utility mod that integrates a clean backup management screen directly into the singleplayer world selection menu. Easily browse, inspect, and restore `.zip` world backups without leaving the game.

---

## NOTE

At the moment I wirte this, there is no build in backup *wirter* only a *lister and reader*.

---

## Features

* **In-Menu Access:** Adds a dedicated **Backups** button next to every world entry in the Singleplayer world list.
* **One-Click Restores:** View available `.zip` backup archives with clear date formatting and file sizes.
* **Safe Extraction:** Clears old save files before unzipping to prevent safe-state corruption or orphan region files.
* **Accidental Overwrite Protection:** Native Minecraft confirmation prompts guard against accidental restores.
* **Toast Notifications:** Uses Minecraft's built-in system toasts to notify you when a restore succeeds or fails.

---

## Installation

1. Download the latest release `.jar` file for your Minecraft version.
2. Place the file into your `.minecraft/mods` directory.
3. Launch Minecraft using your mod loader (NeoForge).

---

## Usage

1. Open the **Singleplayer** menu.
2. Click the **Backups** button next to the world you want to manage.
3. Select a `.zip` file from the list.
4. Click **Restore**, confirm the prompt, and let the mod handle the unzipping process!

---

## Compatibility

* **Mod Type:** Client-side only. Does not need to be installed on servers.
* **Target Environment:** Designed for modern NeoForge/Minecraft versions. (More coming if needed)
