/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.darkhax.tinkerstages.TinkerStages;
import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.api.StageRuleHandle;
import net.darkhax.tinkerstages.stage.StageRuleStore;

/** Re-checks rules after registries and datapacks are fully loaded. */
public final class RuleDiagnostics {

    private RuleDiagnostics() {
    }

    public static void validateAll() {
        int unresolved = 0;
        for (StageRuleHandle rule : StageRuleStore.INSTANCE.snapshot()) {
            IdentifierResult result = switch (rule.type()) {
                case TOOL_TYPE -> TConstructIdentifiers.tool(rule.target());
                case MATERIAL -> TConstructIdentifiers.material(rule.target());
                case MODIFIER_APPLY, MODIFIER_USE -> TConstructIdentifiers.modifier(rule.target());
                default -> new IdentifierResult(rule.target(), true, true, "");
            };
            if (!result.syntaxValid() || !result.registryKnown()) {
                unresolved++;
            }
        }
        TinkerStages.LOGGER.info("Validated {} TinkerStages rules; {} identifier(s) currently unresolved.",
            StageRuleStore.INSTANCE.snapshot().size(), unresolved);
    }

    public static boolean isTargeted(RestrictionType type) {
        return type.isTargeted();
    }
}
