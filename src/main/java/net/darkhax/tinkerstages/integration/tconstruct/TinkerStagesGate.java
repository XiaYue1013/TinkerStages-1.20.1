/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.darkhax.tinkerstages.TinkerStagesMessages;
import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.stage.StageChecker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.MaterialSwappingRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.table.ModifierWorktableBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.ModifierWorktableContainerMenu;
import slimeknights.tconstruct.tables.menu.PartBuilderContainerMenu;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Central, server-authoritative gate for all Tinkers' Construct output slots.
 *
 * <p>The gate runs before an output slot is removed from its menu. That means denied operations
 * do not consume materials, patterns, modifier inputs, or the tool itself.</p>
 */
public final class TinkerStagesGate {

    private TinkerStagesGate() {
    }

    public static GateDecision evaluate(AbstractContainerMenu menu, int slotId, Player player) {
        if (!(player instanceof ServerPlayer) || slotId < 0 || slotId >= menu.slots.size()) {
            return GateDecision.ALLOW;
        }

        Slot slot = menu.getSlot(slotId);
        if (slot == null || !slot.hasItem()) {
            return GateDecision.ALLOW;
        }

        if (menu instanceof TinkerStationContainerMenu station) {
            return evaluateTinkerStation(station, slot, (ServerPlayer) player);
        }
        if (menu instanceof PartBuilderContainerMenu partBuilder) {
            return evaluatePartBuilder(partBuilder, slot, (ServerPlayer) player);
        }
        if (menu instanceof ModifierWorktableContainerMenu worktable) {
            return evaluateModifierWorktable(worktable, slot, (ServerPlayer) player);
        }
        return GateDecision.ALLOW;
    }

    private static GateDecision evaluateTinkerStation(
        TinkerStationContainerMenu menu,
        Slot clickedSlot,
        ServerPlayer player
    ) {
        if (!(clickedSlot.container instanceof LazyResultContainer)) {
            return GateDecision.ALLOW;
        }
        TinkerStationBlockEntity tile = findStationTile(menu);
        if (tile == null) {
            return GateDecision.ALLOW;
        }

        ItemStack result = clickedSlot.getItem();
        if (result.isEmpty()) {
            return GateDecision.ALLOW;
        }
        ItemStack toolInput = tile.getItem(TinkerStationBlockEntity.TINKER_SLOT);
        ITinkerStationRecipe recipe = tile.getLastRecipe();

        // Building a new tool: Tinker Station slot 0 is empty.
        if (toolInput.isEmpty() && result.getItem() instanceof IModifiable) {
            GateDecision decision = gateGeneral(RestrictionType.GENERAL_CRAFTING, player);
            if (!decision.allowed()) {
                return decision;
            }
            ToolInspection output = ToolInspection.from(result);
            if (output == null) {
                return GateDecision.ALLOW;
            }
            decision = gateToolType(output, player);
            if (!decision.allowed()) {
                return decision;
            }
            return gateMaterials(output, player);
        }

        // Tinkers' Construct 3's closest equivalent to 1.12.2 part replacing.
        if (recipe instanceof MaterialSwappingRecipe) {
            GateDecision decision = gateGeneral(RestrictionType.GENERAL_PART_REPLACING, player);
            if (!decision.allowed()) {
                return decision;
            }
            ToolInspection output = ToolInspection.from(result);
            return output == null ? GateDecision.ALLOW : gateMaterials(output, player);
        }

        ToolInspection input = ToolInspection.from(toolInput);
        ToolInspection output = ToolInspection.from(result);
        if (output == null) {
            return GateDecision.ALLOW;
        }
        Set<String> changedModifiers = new LinkedHashSet<>(ToolInspection.increasedUpgrades(input, output));
        if (recipe instanceof IModifierRecipe modifierRecipe) {
            changedModifiers.add(modifierRecipe.getModifier().getId().toString());
        }
        if (changedModifiers.isEmpty()) {
            return GateDecision.ALLOW;
        }

        GateDecision decision = gateGeneral(RestrictionType.GENERAL_MODIFIER_APPLY, player);
        if (!decision.allowed()) {
            return decision;
        }
        String blockedModifier = firstBlocked(player, RestrictionType.MODIFIER_APPLY, changedModifiers);
        if (blockedModifier != null) {
            return GateDecision.deny(TinkerStagesMessages.modifier(blockedModifier, true));
        }
        return GateDecision.ALLOW;
    }

    private static GateDecision evaluatePartBuilder(
        PartBuilderContainerMenu menu,
        Slot clickedSlot,
        ServerPlayer player
    ) {
        if (menu.getOutputSlot() == null || clickedSlot != menu.getOutputSlot()) {
            return GateDecision.ALLOW;
        }
        ItemStack result = clickedSlot.getItem();
        if (result.isEmpty() || !(result.getItem() instanceof IMaterialItem part)) {
            return GateDecision.ALLOW;
        }

        GateDecision decision = gateGeneral(RestrictionType.GENERAL_PART_BUILDING, player);
        if (!decision.allowed()) {
            return decision;
        }
        String material = part.getMaterial(result).getId().toString();
        if (StageChecker.isBlocked(player, RestrictionType.MATERIAL, material)) {
            return GateDecision.deny(TinkerStagesMessages.material(material));
        }
        return GateDecision.ALLOW;
    }

    private static GateDecision evaluateModifierWorktable(
        ModifierWorktableContainerMenu menu,
        Slot clickedSlot,
        ServerPlayer player
    ) {
        if (menu.getOutputSlot() == null || clickedSlot != menu.getOutputSlot()) {
            return GateDecision.ALLOW;
        }
        ModifierWorktableBlockEntity tile = findWorktableTile(menu);
        if (tile == null) {
            return GateDecision.ALLOW;
        }
        ItemStack result = clickedSlot.getItem();
        if (result.isEmpty()) {
            return GateDecision.ALLOW;
        }

        Set<String> changedModifiers = new LinkedHashSet<>();
        int selected = tile.getSelectedIndex();
        List<slimeknights.tconstruct.library.modifiers.ModifierEntry> buttons = tile.getCurrentButtons();
        if (selected >= 0 && selected < buttons.size()) {
            changedModifiers.add(buttons.get(selected).getId().toString());
        }
        ItemStack inputStack = tile.getItem(ModifierWorktableBlockEntity.TINKER_SLOT);
        ToolInspection input = ToolInspection.from(inputStack);
        ToolInspection output = ToolInspection.from(result);
        if (output != null) {
            changedModifiers.addAll(ToolInspection.increasedUpgrades(input, output));
        }
        if (changedModifiers.isEmpty()) {
            return GateDecision.ALLOW;
        }

        GateDecision decision = gateGeneral(RestrictionType.GENERAL_MODIFIER_APPLY, player);
        if (!decision.allowed()) {
            return decision;
        }
        String blockedModifier = firstBlocked(player, RestrictionType.MODIFIER_APPLY, changedModifiers);
        if (blockedModifier != null) {
            return GateDecision.deny(TinkerStagesMessages.modifier(blockedModifier, true));
        }
        return GateDecision.ALLOW;
    }

    private static GateDecision gateGeneral(RestrictionType type, ServerPlayer player) {
        return StageChecker.isBlocked(player, type, "")
            ? GateDecision.deny(TinkerStagesMessages.general(type))
            : GateDecision.ALLOW;
    }

    private static GateDecision gateToolType(ToolInspection tool, ServerPlayer player) {
        String id = tool.definitionId().toString();
        return StageChecker.isBlocked(player, RestrictionType.TOOL_TYPE, id)
            ? GateDecision.deny(TinkerStagesMessages.toolType(id))
            : GateDecision.ALLOW;
    }

    private static GateDecision gateMaterials(ToolInspection tool, ServerPlayer player) {
        String blocked = firstBlocked(player, RestrictionType.MATERIAL, tool.materialIds());
        return blocked == null
            ? GateDecision.ALLOW
            : GateDecision.deny(TinkerStagesMessages.material(blocked));
    }

    @Nullable
    private static String firstBlocked(ServerPlayer player, RestrictionType type, Iterable<String> targets) {
        for (String target : targets) {
            if (target != null && !target.isBlank() && StageChecker.isBlocked(player, type, target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private static TinkerStationBlockEntity findStationTile(TinkerStationContainerMenu menu) {
        for (Slot slot : menu.getInputSlots()) {
            if (slot.container instanceof TinkerStationBlockEntity tile) {
                return tile;
            }
        }
        return null;
    }

    @Nullable
    private static ModifierWorktableBlockEntity findWorktableTile(ModifierWorktableContainerMenu menu) {
        for (Slot slot : menu.getInputSlots()) {
            if (slot.container instanceof ModifierWorktableBlockEntity tile) {
                return tile;
            }
        }
        return null;
    }
}
