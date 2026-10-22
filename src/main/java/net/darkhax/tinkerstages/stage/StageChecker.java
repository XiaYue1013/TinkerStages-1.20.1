/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.stage;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.tinkerstages.api.RestrictionType;
import net.minecraft.world.entity.player.Player;

/**
 * Bridges the rule engine to GameStages' live, per-player data. GameStages handles persistence,
 * login restoration, multiplayer isolation, and client synchronization.
 */
public final class StageChecker {

    private StageChecker() {
    }

    public static boolean isAllowed(Player player, RestrictionType type, String target) {
        if (player == null) {
            return false;
        }
        return StageRuleStore.INSTANCE.isAllowed(type, target,
            stage -> GameStageHelper.hasStage(player, stage));
    }

    public static boolean isBlocked(Player player, RestrictionType type, String target) {
        return !isAllowed(player, type, target);
    }
}
