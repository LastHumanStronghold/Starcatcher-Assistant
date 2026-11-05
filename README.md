# Starcatcher Assistant

Client-side automation companion for **Starcatcher 1.20.1 Forge**.

## Current implementation
- Forge client mod listens on localhost TCP `127.0.0.1:28741` for commands from the desktop UI.
- Fishing mode reads Starcatcher `FishingMinigameScreen` state (moving handle angle and active sweet spots) through the 1.20.1 API and invokes the minigame hit action when the handle overlaps a hittable spot.
- Bait mode holds the Minecraft use key for a configurable number of client ticks, then pauses and repeats.
- Desktop UI source is in `desktop/`.

## Build
Requires Java 17. Run `gradle build` in the repository root (Gradle 8.8 is used by CI). The mod jar is created in `build/libs/`.

## Run desktop UI
`python desktop/assistant.py`

## Important limitations
- This is an early integration build. It has not yet been validated in a live Minecraft client or against a server-side Starcatcher installation.
- Automatic selling of COMMON fish is **not implemented yet**. It must be integrated with the server's actual Selling Bin interaction; the assistant will not delete fish from inventory.
- Minimized-window behavior depends on whether the Minecraft client continues processing client ticks. It is not guaranteed yet.
- The mod is designed for the Minecraft client; do not install it on a dedicated server.

Commands: `FISH_START`, `BAIT_START`, `PAUSE`, `RESUME`, `STOP`, `SET_HOLD <ticks>`, `SET_PAUSE <ticks>`.
