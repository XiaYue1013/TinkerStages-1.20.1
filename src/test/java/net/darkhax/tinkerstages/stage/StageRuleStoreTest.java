package net.darkhax.tinkerstages.stage;

import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.api.StageRuleHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageRuleStoreTest {

    private final StageRuleStore store = StageRuleStore.INSTANCE;

    @AfterEach
    void clearRules() {
        store.clear();
    }

    @Test
    void unconfiguredRestrictionAlwaysAllows() {
        assertTrue(store.isAllowed(RestrictionType.TOOL_TYPE, "tconstruct:pickaxe", stage -> false));
    }

    @Test
    void sameRestrictionUsesOrLogic() {
        store.add(RestrictionType.TOOL_TYPE, "tconstruct:pickaxe", "advanced");
        store.add(RestrictionType.TOOL_TYPE, "tconstruct:pickaxe", "master");

        assertTrue(store.isAllowed(RestrictionType.TOOL_TYPE, "tconstruct:pickaxe",
            Set.of("master")::contains));
        assertFalse(store.isAllowed(RestrictionType.TOOL_TYPE, "tconstruct:pickaxe",
            Set.of("unrelated")::contains));
    }

    @Test
    void targetRestrictionsRemainIndependent() {
        store.add(RestrictionType.MATERIAL, "tconstruct:cobalt", "nether");

        assertFalse(store.isAllowed(RestrictionType.MATERIAL, "tconstruct:cobalt", stage -> false));
        assertTrue(store.isAllowed(RestrictionType.MATERIAL, "tconstruct:iron", stage -> false));
    }

    @Test
    void undoRemovesOnlyTheExactRule() {
        StageRuleHandle removed = store.add(RestrictionType.MODIFIER_APPLY, "tconstruct:reinforced", "smith");
        store.add(RestrictionType.MODIFIER_APPLY, "tconstruct:reinforced", "master");

        assertTrue(store.remove(removed));
        assertTrue(store.isAllowed(RestrictionType.MODIFIER_APPLY, "tconstruct:reinforced",
            Set.of("master")::contains));
        assertFalse(store.isAllowed(RestrictionType.MODIFIER_APPLY, "tconstruct:reinforced",
            Set.of("smith")::contains));
    }

    @Test
    void applyAndUseRestrictionsAreSeparate() {
        store.add(RestrictionType.MODIFIER_APPLY, "tconstruct:haste", "smith");

        assertFalse(store.isAllowed(RestrictionType.MODIFIER_APPLY, "tconstruct:haste", stage -> false));
        assertTrue(store.isAllowed(RestrictionType.MODIFIER_USE, "tconstruct:haste", stage -> false));
    }

    @Test
    void stageNamesAreNormalizedForStableReloads() {
        StageRuleHandle handle = store.add(RestrictionType.GENERAL_CRAFTING, "", " Progression_A ");

        assertTrue(store.isAllowed(RestrictionType.GENERAL_CRAFTING, "",
            Set.of("progression_a")::contains));
        assertTrue(store.remove(handle));
    }
}
