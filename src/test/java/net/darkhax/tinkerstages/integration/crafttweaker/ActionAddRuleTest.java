package net.darkhax.tinkerstages.integration.crafttweaker;

import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.stage.StageRuleStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionAddRuleTest {

    @AfterEach
    void clearRules() {
        StageRuleStore.INSTANCE.clear();
    }

    @Test
    void undoRemovesAllRulesCreatedByOneZenCall() {
        ActionAddRule action = new ActionAddRule(
            "modifier_mastery",
            "",
            RestrictionType.GENERAL_CRAFTING,
            RestrictionType.GENERAL_PART_BUILDING
        );

        action.apply();
        assertEquals(2, StageRuleStore.INSTANCE.snapshot().size());

        action.undo();
        assertTrue(StageRuleStore.INSTANCE.isEmpty());
    }
}
