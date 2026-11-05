# Starcatcher Assistant

Client-side automation companion for **Starcatcher 1.20.1 Forge**.

## Current implementation
- Forge client mod listens on localhost TCP `127.0.0.1:28741` for commands from the desktop UI.
- Fishing mode reads Starcatcher `FishingMinigameScreen` state (moving handle angle and active sweet spots) through the 1.20.1 API and invokes the minigame hit action when the handle overlaps a hittable spot.
- Bait mode holds the Minecraft use key for a configurable number of client ticks, then pauses and repeats.
- COMMON fish selling mode works while the player has the **wd's Selling Bin** screen open: it shift-clicks only Starcatcher fish whose caught-fish component says `COMMON`, and triggers Sell All only if a COMMON fish actually reached the bin input slot.
- The desktop UI has separate controls for fishing, bait, and COMMON selling.

## Build and downloads
GitHub Actions builds both outputs:
- `StarcatcherAssistant-0.1.0.jar` — install in the Minecraft 1.20.1 Forge **client** mods folder.
- `StarcatcherAssistant.exe` — Windows desktop control panel.

Download them from the latest successful run's Artifacts section. The mod is built with Java 17 / ForgeGradle.

## Run desktop UI
Use the packaged `StarcatcherAssistant.exe`, or run `python desktop/assistant.py`.

## Important limitations
- This is an integration build and still needs a live in-game test on the user's exact Starcatcher + Selling Bin setup.
- COMMON selling requires the server's Selling Bin datapack/config to assign a sell value to those fish. If the bin rejects an item, the assistant leaves it in the player's inventory.
- COMMON selling runs while the Selling Bin GUI is open; it does not navigate the player to a bin or open one automatically.
- Minimized-window behavior depends on whether the Minecraft client continues processing client ticks. It is not guaranteed yet.
- The mod is designed for the Minecraft client; do not install it on a dedicated server.

Commands: `FISH_START`, `BAIT_START`, `SELL_COMMON_START`, `SELL_COMMON_STOP`, `PAUSE`, `RESUME`, `STOP`, `SET_HOLD <ticks>`, `SET_PAUSE <ticks>`.
