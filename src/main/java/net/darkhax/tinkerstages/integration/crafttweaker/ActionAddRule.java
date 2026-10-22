/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.crafttweaker;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.tinkerstages.TinkerStagesConfig;
import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.api.StageRuleHandle;
import net.darkhax.tinkerstages.integration.tconstruct.IdentifierResult;
import net.darkhax.tinkerstages.integration.tconstruct.TConstructIdentifiers;
import net.darkhax.tinkerstages.stage.StageRuleStore;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Undoable CraftTweaker action. CraftTweaker calls undo before executing a subsequent
 * {@code /reload}, so removed script lines do not leave stale restrictions behind.
 */
public final class ActionAddRule implements IUndoableAction {

    private final RestrictionType[] types;
    private final String rawTarget;
    private final String rawStage;
    private final List<StageRuleHandle> handles = new ArrayList<>();
    private String resolvedTarget = "";

    public ActionAddRule(String stage, String target, RestrictionType... types) {
        this.rawStage = stage;
        this.rawTarget = target;
        this.types = types.clone();
    }

    @Override
    public void apply() {
        boolean targeted = Arrays.stream(types).anyMatch(RestrictionType::isTargeted);
        if (targeted && resolvedTarget.isBlank()) {
            IdentifierResult resolved = resolveTarget();
            if (!resolved.syntaxValid() || resolved.id().isBlank()) {
                return;
            }
            resolvedTarget = resolved.id();
        }
        for (RestrictionType type : types) {
            handles.add(StageRuleStore.INSTANCE.add(type, resolvedTarget, rawStage));
        }
    }

    @Override
    public boolean validate(Logger logger) {
        String stage = StageRuleStore.normalizeStage(rawStage);
        if (stage.isBlank() || !GameStageHelper.isValidStageName(stage)) {
            logger.error("TinkerStages: invalid stage name '{}'. Stage names may contain a-z, 0-9, _ and :.",
                rawStage);
            return false;
        }

        boolean targeted = Arrays.stream(types).anyMatch(RestrictionType::isTargeted);
        if (!targeted) {
            resolvedTarget = "";
            return true;
        }

        IdentifierResult result = resolveTarget();
        if (!result.syntaxValid()) {
            logger.error("TinkerStages: {}", result.diagnostic());
            return false;
        }
        resolvedTarget = result.id();
        if (!result.diagnostic().isBlank()) {
            if (!result.registryKnown()) {
                logger.warn("TinkerStages: {}", result.diagnostic());
            } else {
                logger.info("TinkerStages: {}", result.diagnostic());
            }
        }
        if (TinkerStagesConfig.strictIdentifierValidation() && !result.registryKnown()) {
            logger.error("TinkerStages: strictIdentifierValidation rejected '{}'.", rawTarget);
            return false;
        }
        return true;
    }

    @Override
    public void undo() {
        for (StageRuleHandle handle : handles) {
            StageRuleStore.INSTANCE.remove(handle);
        }
        handles.clear();
    }

    @Override
    public String describe() {
        return "Add TinkerStages rule " + Arrays.toString(types) + " target='" + rawTarget
            + "' stage='" + rawStage + "'";
    }

    @Override
    public String describeUndo() {
        return "Remove TinkerStages rule " + Arrays.toString(types) + " target='" + rawTarget
            + "' stage='" + rawStage + "'";
    }

    @Override
    public String systemName() {
        return "TinkerStages";
    }

    private IdentifierResult resolveTarget() {
        for (RestrictionType type : types) {
            IdentifierResult result = switch (type) {
                case TOOL_TYPE -> TConstructIdentifiers.tool(rawTarget);
                case MATERIAL -> TConstructIdentifiers.material(rawTarget);
                case MODIFIER_APPLY, MODIFIER_USE -> TConstructIdentifiers.modifier(rawTarget);
                default -> new IdentifierResult("", true, true, "");
            };
            if (type.isTargeted()) {
                return result;
            }
        }
        return new IdentifierResult("", true, true, "");
    }
}
