/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.api;

import java.util.Objects;

/**
 * Identity handle for one stage entry. Handles make script reloads deterministic: CraftTweaker
 * can undo only the exact entries created by a previous script run.
 */
public record StageRuleHandle(long id, RestrictionType type, String target, String stage) {

    public StageRuleHandle {
        Objects.requireNonNull(type, "type");
        target = target == null ? "" : target;
        Objects.requireNonNull(stage, "stage");
        if (type.isTargeted() && target.isBlank()) {
            throw new IllegalArgumentException("Targeted restriction " + type + " requires a target");
        }
    }
}
