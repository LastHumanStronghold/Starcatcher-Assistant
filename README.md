# Starcatcher Assistant

Client-side automation companion for **Starcatcher / OperZone on Minecraft 1.20.1 Forge**.

## Current implementation
- Forge client mod listens on localhost TCP `127.0.0.1:28741` for commands from the desktop UI.
- Fishing mode reads Starcatcher `FishingMinigameScreen` state (moving handle angle and active sweet spots) through reflection and invokes the minigame hit action when the handle overlaps a hittable spot.
- Bait mode holds the Minecraft use key for a configurable number of client ticks, then pauses and repeats.
- COMMON catch auto-selling is integrated with OperZone's actual `ru.operzone.client.collect.CollectibleCardScreen` in `DROP` mode. The assistant reads the catch JSON rarity field `r`: value `0` is `COMMON`. It presses the screen's real Sell button and then its confirmation button.
- Every other rarity is left in the collection by pressing the real Keep button. The assistant does not use Selling Bin or move fish through inventory slots.
- The desktop UI has separate controls for fishing, bait, and COMMON catch auto-selling.

## Build and downloads
GitHub Actions builds both outputs:
- `StarcatcherAssistant-0.1.0.jar` — install in the Minecraft 1.20.1 Forge **client** mods folder.
- `StarcatcherAssistant.exe` — Windows desktop control panel.

Download them from the latest successful workflow run's Artifacts section. The mod is built with Java 17 / ForgeGradle.

## Run desktop UI
Use the packaged `StarcatcherAssistant.exe`, or run `python desktop/assistant.py`.

## Important limitations
- The OperZone catch-card integration has been matched against the published `operzone-0.1.0-fix17.jar` and the Forge mod compiles, but a live in-game test on the user's server has not yet been performed.
- The assistant only handles the catch card while it is open. It does not navigate the player to fishing locations or open the catch screen itself.
- Minimized-window behavior depends on whether the Minecraft client continues processing client ticks. It is not guaranteed yet.
- The mod is designed for the Minecraft client; do not install it on a dedicated server.

Commands: `FISH_START`, `BAIT_START`, `SELL_COMMON_START`, `SELL_COMMON_STOP`, `PAUSE`, `RESUME`, `STOP`, `SET_HOLD <ticks>`, `SET_PAUSE <ticks>`.
