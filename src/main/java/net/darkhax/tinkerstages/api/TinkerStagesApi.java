/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.api;

import net.darkhax.tinkerstages.stage.StageRuleStore;

import java.util.List;

/**
 * Public Java API for other mods. ZenScript scripts should use {@code mods.TinkerStages} so
 * CraftTweaker can undo the exact rules created by a previous script run.
 */
public final class TinkerStagesApi {

    private TinkerStagesApi() {
    }

    public static StageRuleHandle addGeneralCraftingStage(String stage) {
        return add(RestrictionType.GENERAL_CRAFTING, "", stage);
    }

    public static StageRuleHandle addGeneralPartReplacingStage(String stage) {
        return add(RestrictionType.GENERAL_PART_REPLACING, "", stage);
    }

    public static StageRuleHandle addGeneralPartBuildingStage(String stage) {
        return add(RestrictionType.GENERAL_PART_BUILDING, "", stage);
    }

    public static StageRuleHandle addGeneralModifierStage(String stage) {
        return add(RestrictionType.GENERAL_MODIFIER_APPLY, "", stage);
    }

    public static StageRuleHandle addToolTypeStage(String stage, String toolId) {
        return add(RestrictionType.TOOL_TYPE, StageIdentifiers.canonicalResource(toolId), stage);
    }

    public static StageRuleHandle addMaterialStage(String stage, String material) {
        return add(RestrictionType.MATERIAL, StageIdentifiers.canonicalResource(material), stage);
    }

    /** Preserves the original behavior: restrict both applying and using the modifier. */
    public static List<StageRuleHandle> addModifierStage(String stage, String modifier) {
        String id = StageIdentifiers.canonicalResource(modifier);
        return List.of(
            add(RestrictionType.MODIFIER_APPLY, id, stage),
            add(RestrictionType.MODIFIER_USE, id, stage)
        );
    }

    public static StageRuleHandle addModifierApplyStage(String stage, String modifier) {
        return add(RestrictionType.MODIFIER_APPLY, StageIdentifiers.canonicalResource(modifier), stage);
    }

    public static StageRuleHandle addModifierUseStage(String stage, String modifier) {
        return add(RestrictionType.MODIFIER_USE, StageIdentifiers.canonicalResource(modifier), stage);
    }

    public static StageRuleHandle add(RestrictionType type, String target, String stage) {
        return StageRuleStore.INSTANCE.add(type, target, stage);
    }

    public static boolean remove(StageRuleHandle handle) {
        return StageRuleStore.INSTANCE.remove(handle);
    }
}
