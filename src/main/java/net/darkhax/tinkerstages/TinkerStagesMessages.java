/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages;

import net.darkhax.tinkerstages.api.RestrictionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TinkerStagesMessages {

    private TinkerStagesMessages() {
    }

    public static Component general(RestrictionType type) {
        String key = switch (type) {
            case GENERAL_CRAFTING -> "tinkerstages.message.general.crafting";
            case GENERAL_PART_BUILDING -> "tinkerstages.message.general.part_building";
            case GENERAL_PART_REPLACING -> "tinkerstages.message.general.part_replacing";
            case GENERAL_MODIFIER_APPLY -> "tinkerstages.message.general.modifier";
            default -> "tinkerstages.message.general.crafting";
        };
        return Component.translatable(key);
    }

    public static Component toolType(String toolId) {
        return Component.translatable("tinkerstages.message.tool_type", toolId);
    }

    public static Component material(String materialId) {
        return Component.translatable("tinkerstages.message.material", materialId);
    }

    public static Component modifier(String modifierId, boolean applyOnly) {
        String key = applyOnly
            ? "tinkerstages.message.modifier.apply"
            : "tinkerstages.message.modifier";
        return Component.translatable(key, modifierId);
    }

    public static void notifyPlayer(ServerPlayer player, Component message) {
        if (TinkerStagesConfig.notifyPlayers()) {
            player.displayClientMessage(message, false);
        }
    }
}
