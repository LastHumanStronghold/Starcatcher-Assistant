package com.lhs.starcatcherassistant;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

@Mod(StarcatcherAssistant.MOD_ID)
public class StarcatcherAssistant {
    public static final String MOD_ID = "starcatcherassistant";
    public StarcatcherAssistant() {
        ClientAutomation automation = new ClientAutomation();
        ClientAutomationBridge.register(automation);
        MinecraftForge.EVENT_BUS.register(automation);
        ControlServer.start();
    }
}
