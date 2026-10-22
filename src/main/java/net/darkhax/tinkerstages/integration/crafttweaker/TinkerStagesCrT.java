/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import net.darkhax.tinkerstages.api.RestrictionType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * CraftTweaker 1.20.1 entry point. The class name intentionally remains
 * {@code mods.TinkerStages} so existing scripts keep their familiar calls.
 */
@ZenRegister
@ZenCodeType.Name("mods.TinkerStages")
public final class TinkerStagesCrT {

    private TinkerStagesCrT() {
    }

    @ZenCodeType.Method
    public static void addGeneralCraftingStage(String stage) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, "", RestrictionType.GENERAL_CRAFTING));
    }

    @ZenCodeType.Method
    public static void addGeneralPartReplacingStage(String stage) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, "", RestrictionType.GENERAL_PART_REPLACING));
    }

    @ZenCodeType.Method
    public static void addGeneralPartBuildingStage(String stage) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, "", RestrictionType.GENERAL_PART_BUILDING));
    }

    @ZenCodeType.Method
    public static void addGeneralModifierStage(String stage) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, "", RestrictionType.GENERAL_MODIFIER_APPLY));
    }

    @ZenCodeType.Method
    public static void addToolTypeStage(String stage, String toolId) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, toolId, RestrictionType.TOOL_TYPE));
    }

    @ZenCodeType.Method
    public static void addMaterialStage(String stage, String material) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, material, RestrictionType.MATERIAL));
    }

    /** Original behavior: one call restricts both applying and using the modifier. */
    @ZenCodeType.Method
    public static void addModifierStage(String stage, String modifier) {
        CraftTweakerAPI.apply(new ActionAddRule(
            stage, modifier, RestrictionType.MODIFIER_APPLY, RestrictionType.MODIFIER_USE));
    }

    /** New granular API for packs that intentionally allow one side only. */
    @ZenCodeType.Method
    public static void addModifierApplyStage(String stage, String modifier) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, modifier, RestrictionType.MODIFIER_APPLY));
    }

    @ZenCodeType.Method
    public static void addModifierUseStage(String stage, String modifier) {
        CraftTweakerAPI.apply(new ActionAddRule(stage, modifier, RestrictionType.MODIFIER_USE));
    }
}
