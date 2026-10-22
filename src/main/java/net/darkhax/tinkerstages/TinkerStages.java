/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 * Modified and ported in 2026 for Tinkers' Construct 3 / Minecraft 1.20.1.
 */
package net.darkhax.tinkerstages;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TinkerStages.MOD_ID)
public final class TinkerStages {

    public static final String MOD_ID = "tinkerstages";
    public static final String MOD_NAME = "TinkerStages";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public TinkerStages() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TinkerStagesConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().register(TinkerStagesConfig.class);
    }
}
