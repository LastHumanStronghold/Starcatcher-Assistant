package com.lhs.starcatcherassistant;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class ClientAutomation {
    private volatile boolean fishing, bait, paused;
    private int cooldown, baitTicks, baitPause;
    private int holdTicks = 35, pauseTicks = 10;
    private boolean useDown;

    void command(String cmd) {
        switch (cmd) {
            case "FISH_START" -> { fishing = true; bait = false; paused = false; }
            case "BAIT_START" -> { bait = true; fishing = false; paused = false; }
            case "PAUSE" -> paused = true;
            case "RESUME" -> paused = false;
            case "STOP" -> { fishing = false; bait = false; paused = false; releaseUse(); }
            default -> {
                if (cmd.startsWith("SET_HOLD ")) holdTicks = clamp(parse(cmd, 9), 1, 1200);
                if (cmd.startsWith("SET_PAUSE ")) pauseTicks = clamp(parse(cmd, 10), 0, 1200);
            }
        }
    }
    private static int parse(String s,int n){ try{return Integer.parseInt(s.substring(n).trim());}catch(Exception e){return 35;} }
    private static int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}

    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if(e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null) { releaseUse(); return; }
        if(paused) { releaseUse(); return; }
        if(bait) { runBait(mc); return; }
        if(fishing) runFishing(mc);
    }

    private void runBait(Minecraft mc) {
        if(mc.screen != null) { releaseUse(); return; }
        if(useDown) {
            if(++baitTicks >= holdTicks) { releaseUse(); baitTicks=0; baitPause=0; }
        } else if(++baitPause >= pauseTicks) {
            mc.options.keyUse.setDown(true); useDown=true; baitTicks=0;
        }
    }
    private void releaseUse() {
        if(useDown) Minecraft.getInstance().options.keyUse.setDown(false);
        useDown=false;
    }

    private void runFishing(Minecraft mc) {
        if(cooldown>0) cooldown--;
        Object screen=mc.screen;
        if(screen==null || !screen.getClass().getName().equals("com.wdiscute.starcatcher.minigame.FishingMinigameScreen")) return;
        if(cooldown>0) return;
        try {
            Method angleMethod=screen.getClass().getMethod("getHandlePosPrecise");
            Method spotsMethod=screen.getClass().getMethod("getActiveSweetSpots");
            float angle=((Number)angleMethod.invoke(screen)).floatValue();
            List<?> spots=(List<?>)spotsMethod.invoke(screen);
            for(Object spot:spots) {
                Field canHit=spot.getClass().getField("canHit");
                Field pos=spot.getClass().getField("pos");
                Field thickness=spot.getClass().getField("thickness");
                if(!canHit.getBoolean(spot)) continue;
                float target=((Number)pos.get(spot)).floatValue();
                int width=((Number)thickness.get(spot)).intValue()/2;
                float delta=Math.abs(target-angle);
                if(delta < width || delta > 360-width) {
                    Method hit=screen.getClass().getMethod("inputPressed");
                    hit.invoke(screen);
                    cooldown=8;
                    break;
                }
            }
        } catch (ReflectiveOperationException ignored) { /* Starcatcher API mismatch: fail safely */ }
    }
}
