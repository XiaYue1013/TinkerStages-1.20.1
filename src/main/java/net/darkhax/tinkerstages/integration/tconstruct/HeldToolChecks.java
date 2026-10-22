/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.darkhax.tinkerstages.TinkerStagesMessages;
import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.stage.StageChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Checks specific restrictions that continue to apply after a tool has already been crafted. */
public final class HeldToolChecks {

    private HeldToolChecks() {
    }

    /**
     * Reproduces the original mod's held-tool rules: tool type, every material in the tool, and
     * every effective modifier (including material traits).
     */
    @Nullable
    public static Component findRestriction(ServerPlayer player, ItemStack stack) {
        ToolInspection tool = ToolInspection.from(stack);
        if (tool == null) {
            return null;
        }

        String toolId = tool.definitionId().toString();
        if (StageChecker.isBlocked(player, RestrictionType.TOOL_TYPE, toolId)) {
            return TinkerStagesMessages.toolType(toolId);
        }
        for (String material : tool.materialIds()) {
            if (StageChecker.isBlocked(player, RestrictionType.MATERIAL, material)) {
                return TinkerStagesMessages.material(material);
            }
        }
        for (String modifier : tool.effectiveModifierIds()) {
            if (StageChecker.isBlocked(player, RestrictionType.MODIFIER_USE, modifier)) {
                return TinkerStagesMessages.modifier(modifier, false);
            }
        }
        return null;
    }
}
