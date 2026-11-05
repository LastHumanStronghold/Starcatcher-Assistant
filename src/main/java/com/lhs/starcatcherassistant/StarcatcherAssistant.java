package com.lhs.starcatcherassistant;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

@Mod(StarcatcherAssistant.MOD_ID)
public class StarcatcherAssistant {
    public static final String MOD_ID = "starcatcherassistant";
    public StarcatcherAssistant() {
        MinecraftForge.EVENT_BUS.register(new ClientAutomation());
        ControlServer.start();
    }
}
