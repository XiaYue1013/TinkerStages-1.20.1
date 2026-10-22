/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 *
 * This is the only invasive integration point. Tinkers' Construct 3 exposes no cancellable
 * output-take event for Tinker Station, Part Builder, or Modifier Worktable. Injecting at the
 * start of AbstractContainerMenu#clicked covers normal clicks, shift-clicks, swaps, quick-craft
 * packets, and every Tinkers' Construct menu before any input is consumed.
 */
package net.darkhax.tinkerstages.mixin;

import net.darkhax.tinkerstages.integration.tconstruct.GateDecision;
import net.darkhax.tinkerstages.integration.tconstruct.TinkerStagesGate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void tinkerstages$gateTinkersOutput(
        int slotId,
        int button,
        ClickType clickType,
        Player player,
        CallbackInfo callback
    ) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        GateDecision decision = TinkerStagesGate.evaluate(menu, slotId, player);
        if (!decision.allowed()) {
            if (player instanceof ServerPlayer serverPlayer) {
                decision.notify(serverPlayer);
            }
            callback.cancel();
        }
    }
}
