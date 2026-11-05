package com.lhs.starcatcherassistant;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.MinecraftForge;

final class ClientAutomationBridge {
    private static ClientAutomation instance;
    static void register(ClientAutomation a){ instance=a; }
    static void dispatch(String command){ if(instance!=null) instance.command(command.trim()); }
}
