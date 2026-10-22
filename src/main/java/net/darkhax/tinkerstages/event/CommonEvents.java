/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.event;

import net.darkhax.tinkerstages.TinkerStages;
import net.darkhax.tinkerstages.TinkerStagesConfig;
import net.darkhax.tinkerstages.TinkerStagesMessages;
import net.darkhax.tinkerstages.integration.tconstruct.HeldToolChecks;
import net.darkhax.tinkerstages.integration.tconstruct.RuleDiagnostics;
import net.darkhax.tinkerstages.stage.StageRuleStore;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TinkerStages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        RuleDiagnostics.validateAll();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        StageRuleStore.INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        enforceHand(player, InteractionHand.MAIN_HAND);
        enforceHand(player, InteractionHand.OFF_HAND);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        denyInteraction(event.getEntity(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        denyInteraction(event.getEntity(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        denyInteraction(event.getEntity(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        denyInteraction(event.getEntity(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        denyInteraction(event.getEntity(), () -> event.setCanceled(true));
    }

    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Component restriction = HeldToolChecks.findRestriction(player, event.getItem());
            if (restriction != null) {
                event.setCanceled(true);
            }
        }
    }

    private static void denyInteraction(Player player, Runnable cancel) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Component restriction = HeldToolChecks.findRestriction(serverPlayer, serverPlayer.getMainHandItem());
        if (restriction != null) {
            cancel.run();
            TinkerStagesMessages.notifyPlayer(serverPlayer, restriction);
        }
    }

    private static void enforceHand(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Component restriction = HeldToolChecks.findRestriction(player, stack);
        if (restriction == null) {
            return;
        }
        if (TinkerStagesConfig.dropRestrictedHeldTools()) {
            ItemStack dropped = stack.copy();
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.drop(dropped, false);
            TinkerStagesMessages.notifyPlayer(player, Component.translatable(
                "tinkerstages.message.dropped", dropped.getDisplayName()));
        } else {
            TinkerStagesMessages.notifyPlayer(player, restriction);
        }
    }
}
