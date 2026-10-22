/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.api;

/**
 * The independently enforced restriction categories. A single ZenScript method may create
 * more than one category, for example {@code addModifierStage} creates both apply and use rules.
 */
public enum RestrictionType {
    GENERAL_CRAFTING(false),
    GENERAL_PART_REPLACING(false),
    GENERAL_PART_BUILDING(false),
    GENERAL_MODIFIER_APPLY(false),
    TOOL_TYPE(true),
    MATERIAL(true),
    MODIFIER_APPLY(true),
    MODIFIER_USE(true);

    private final boolean targeted;

    RestrictionType(boolean targeted) {
        this.targeted = targeted;
    }

    public boolean isTargeted() {
        return targeted;
    }
}
