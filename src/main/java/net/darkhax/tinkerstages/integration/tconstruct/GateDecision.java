/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public record GateDecision(boolean allowed, @Nullable Component message) {

    public static final GateDecision ALLOW = new GateDecision(true, null);

    public static GateDecision deny(Component message) {
        return new GateDecision(false, message);
    }

    public void notify(ServerPlayer player) {
        if (!allowed && message != null) {
            net.darkhax.tinkerstages.TinkerStagesMessages.notifyPlayer(player, message);
        }
    }
}
