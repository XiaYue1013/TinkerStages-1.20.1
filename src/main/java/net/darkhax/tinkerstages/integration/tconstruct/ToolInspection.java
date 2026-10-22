/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only view of the tool data that TinkerStages needs for enforcement. */
public record ToolInspection(
    ResourceLocation definitionId,
    List<String> materialIds,
    List<String> effectiveModifierIds,
    Map<String, Integer> upgradeLevels
) {

    @Nullable
    public static ToolInspection from(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IModifiable modifiable)) {
            return null;
        }
        ToolStack tool = ToolStack.from(stack);
        List<String> materials = new ArrayList<>();
        for (MaterialVariant material : tool.getMaterials()) {
            materials.add(material.getId().toString());
        }
        List<String> modifiers = new ArrayList<>();
        for (ModifierEntry entry : tool.getModifierList()) {
            modifiers.add(entry.getId().toString());
        }
        Map<String, Integer> upgrades = new LinkedHashMap<>();
        for (ModifierEntry entry : tool.getUpgrades()) {
            upgrades.put(entry.getId().toString(), entry.getLevel());
        }
        return new ToolInspection(
            modifiable.getToolDefinition().getId(),
            List.copyOf(materials),
            List.copyOf(modifiers),
            Map.copyOf(upgrades)
        );
    }

    /** Modifiers whose level increased from input to output. */
    public static Set<String> increasedUpgrades(@Nullable ToolInspection input, ToolInspection output) {
        Set<String> changed = new LinkedHashSet<>();
        for (Map.Entry<String, Integer> entry : output.upgradeLevels().entrySet()) {
            int oldLevel = input == null ? 0 : input.upgradeLevels().getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > oldLevel) {
                changed.add(entry.getKey());
            }
        }
        return changed;
    }
}
