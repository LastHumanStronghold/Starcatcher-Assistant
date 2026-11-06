package com.lhs.starcatcherassistant;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClientAutomation {
    private static final String OPERZONE_CARD = "ru.operzone.client.collect.CollectibleCardScreen";
    private static final String OPERZONE_BUTTON = "ru.operzone.client.gui.widget.OzButton";

    private volatile boolean fishing, bait, paused, autoSellCommon;
    private int cooldown, baitTicks, baitPause;
    private int holdTicks = 35, pauseTicks = 10;
    private boolean useDown;

    // 0 = not processed, 1 = waiting for the sell confirmation, 2 = action sent.
    private Object activeDropScreen;
    private int dropActionStage;
    private int dropActionDelay;

    void command(String cmd) {
        switch (cmd) {
            case "FISH_START" -> { fishing = true; bait = false; paused = false; }
            case "BAIT_START" -> { bait = true; fishing = false; paused = false; }
            case "SELL_COMMON_START" -> { autoSellCommon = true; paused = false; resetDropAction(); }
            case "SELL_COMMON_STOP" -> { autoSellCommon = false; resetDropAction(); }
            case "PAUSE" -> paused = true;
            case "RESUME" -> paused = false;
            case "STOP" -> {
                fishing = false;
                bait = false;
                autoSellCommon = false;
                paused = false;
                resetDropAction();
                releaseUse();
            }
            default -> {
                if (cmd.startsWith("SET_HOLD ")) holdTicks = clamp(parse(cmd, 9), 1, 1200);
                if (cmd.startsWith("SET_PAUSE ")) pauseTicks = clamp(parse(cmd, 10), 0, 1200);
            }
        }
    }

    private static int parse(String s, int n) {
        try { return Integer.parseInt(s.substring(n).trim()); }
        catch (Exception e) { return 35; }
    }

    private static int clamp(int v, int a, int b) {
        return Math.max(a, Math.min(b, v));
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            releaseUse();
            resetDropAction();
            return;
        }
        if (paused) {
            releaseUse();
            return;
        }
        if (autoSellCommon) runCommonSelling(mc);
        else resetDropAction();
        if (bait) {
            runBait(mc);
            return;
        }
        if (fishing) runFishing(mc);
    }

    /**
     * OperZone's actual catch dialog is CollectibleCardScreen in DROP mode.
     * The collectible JSON uses r=0 for COMMON. The left OzButton keeps the
     * catch; the right OzButton sells it and requires a second confirmation.
     */
    private void runCommonSelling(Minecraft mc) {
        Object screen = mc.screen;
        if (screen == null || !OPERZONE_CARD.equals(screen.getClass().getName())) {
            resetDropAction();
            return;
        }

        if (screen != activeDropScreen) {
            activeDropScreen = screen;
            dropActionStage = 0;
            dropActionDelay = 1;
        }
        if (dropActionStage >= 2) return;
        if (dropActionDelay > 0) {
            dropActionDelay--;
            return;
        }

        try {
            Class<?> screenClass = screen.getClass();

            Field modeField = screenClass.getDeclaredField("mode");
            modeField.setAccessible(true);
            Object mode = modeField.get(screen);
            if (mode == null || !"DROP".equals(mode.toString())) {
                dropActionStage = 2;
                return;
            }

            Field instField = screenClass.getDeclaredField("inst");
            instField.setAccessible(true);
            Object collectible = instField.get(screen);
            int rarity = jsonInt(collectible, "r", -1);

            List<Object> buttons = new ArrayList<>();
            for (Object child : mc.screen.children()) {
                if (child != null && OPERZONE_BUTTON.equals(child.getClass().getName())) {
                    buttons.add(child);
                }
            }
            if (buttons.size() < 2) {
                dropActionDelay = 1;
                return;
            }

            buttons.sort(Comparator.comparingInt(ClientAutomation::widgetX));
            Object keepButton = buttons.get(0);
            Object sellButton = buttons.get(buttons.size() - 1);

            if (rarity == 0) {
                if (dropActionStage == 0) {
                    pressButton(sellButton);
                    dropActionStage = 1;
                    dropActionDelay = 2;
                } else if (dropActionStage == 1) {
                    Field confirmField = screenClass.getDeclaredField("confirmSell");
                    confirmField.setAccessible(true);
                    if (confirmField.getBoolean(screen)) {
                        // Second click confirms OperZone's own col.sell action.
                        pressButton(sellButton);
                        dropActionStage = 2;
                    } else {
                        dropActionDelay = 1;
                    }
                }
            } else {
                // All non-COMMON rarities are kept; no inventory or sale request is made.
                pressButton(keepButton);
                dropActionStage = 2;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Unknown OperZone build or UI layout: fail closed, never guess a button.
            dropActionStage = 2;
        }
    }

    private static int jsonInt(Object jsonObject, String key, int fallback)
            throws ReflectiveOperationException {
        if (jsonObject == null) return fallback;
        Object primitive = jsonObject.getClass().getMethod("get", String.class)
                .invoke(jsonObject, key);
        if (primitive == null) return fallback;
        return ((Number) primitive.getClass().getMethod("getAsInt").invoke(primitive)).intValue();
    }

    private static int widgetX(Object widget) {
        try {
            return ((Number) widget.getClass().getMethod("getX").invoke(widget)).intValue();
        } catch (ReflectiveOperationException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    private static void pressButton(Object button) throws ReflectiveOperationException {
        // This is OperZone OzButton's own callback dispatcher (not a guessed screen coordinate).
        Method press = button.getClass().getDeclaredMethod("m_5691_");
        press.setAccessible(true);
        press.invoke(button);
    }

    private void resetDropAction() {
        activeDropScreen = null;
        dropActionStage = 0;
        dropActionDelay = 0;
    }

    private void runBait(Minecraft mc) {
        if (mc.screen != null) {
            releaseUse();
            return;
        }
        if (useDown) {
            if (++baitTicks >= holdTicks) {
                releaseUse();
                baitTicks = 0;
                baitPause = 0;
            }
        } else if (++baitPause >= pauseTicks) {
            mc.options.keyUse.setDown(true);
            useDown = true;
            baitTicks = 0;
        }
    }

    private void releaseUse() {
        if (useDown) Minecraft.getInstance().options.keyUse.setDown(false);
        useDown = false;
    }

    private void runFishing(Minecraft mc) {
        if (cooldown > 0) cooldown--;
        Object screen = mc.screen;
        if (screen == null || !screen.getClass().getName()
                .equals("com.wdiscute.starcatcher.minigame.FishingMinigameScreen")) return;
        if (cooldown > 0) return;
        try {
            Method angleMethod = screen.getClass().getMethod("getHandlePosPrecise");
            Method spotsMethod = screen.getClass().getMethod("getActiveSweetSpots");
            float angle = ((Number) angleMethod.invoke(screen)).floatValue();
            List<?> spots = (List<?>) spotsMethod.invoke(screen);
            for (Object spot : spots) {
                Field canHit = spot.getClass().getField("canHit");
                Field pos = spot.getClass().getField("pos");
                Field thickness = spot.getClass().getField("thickness");
                if (!canHit.getBoolean(spot)) continue;
                float target = ((Number) pos.get(spot)).floatValue();
                int width = ((Number) thickness.get(spot)).intValue() / 2;
                float delta = Math.abs(target - angle);
                if (delta < width || delta > 360 - width) {
                    Method hit = screen.getClass().getMethod("inputPressed");
                    hit.invoke(screen);
                    cooldown = 8;
                    break;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Starcatcher API mismatch: fail safely.
        }
    }
}
