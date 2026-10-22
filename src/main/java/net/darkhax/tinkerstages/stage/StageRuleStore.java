/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.stage;

import net.darkhax.tinkerstages.api.RestrictionType;
import net.darkhax.tinkerstages.api.StageRuleHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Thread-safe, player-independent rule storage. Rules use OR semantics: every stage configured
 * for the same restriction is an alternative. No recipe or registry entry is modified globally.
 */
public final class StageRuleStore {

    public static final StageRuleStore INSTANCE = new StageRuleStore();

    private final AtomicLong nextId = new AtomicLong(1);
    private final CopyOnWriteArrayList<StageRuleHandle> rules = new CopyOnWriteArrayList<>();

    private StageRuleStore() {
    }

    public StageRuleHandle add(RestrictionType type, String target, String stage) {
        Objects.requireNonNull(type, "type");
        String normalizedStage = normalizeStage(stage);
        String normalizedTarget = type.isTargeted() ? Objects.requireNonNull(target, "target") : "";
        StageRuleHandle handle = new StageRuleHandle(nextId.getAndIncrement(), type, normalizedTarget, normalizedStage);
        rules.add(handle);
        return handle;
    }

    public boolean remove(StageRuleHandle handle) {
        return handle != null && rules.remove(handle);
    }

    public void clear() {
        rules.clear();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    public List<StageRuleHandle> snapshot() {
        return List.copyOf(rules);
    }

    /**
     * Returns the distinct allowed stages for a restriction. An empty result means the
     * restriction was never configured and the operation must remain available.
     */
    public Set<String> matchingStages(RestrictionType type, String target) {
        String normalizedTarget = type.isTargeted() ? Objects.requireNonNull(target, "target") : "";
        LinkedHashSet<String> stages = new LinkedHashSet<>();
        for (StageRuleHandle rule : rules) {
            if (rule.type() == type && rule.target().equals(normalizedTarget)) {
                stages.add(rule.stage());
            }
        }
        return stages;
    }

    public boolean isAllowed(RestrictionType type, String target, Predicate<String> hasStage) {
        Collection<String> stages = matchingStages(type, target);
        if (stages.isEmpty()) {
            return true;
        }
        for (String stage : stages) {
            if (hasStage.test(stage)) {
                return true;
            }
        }
        return false;
    }

    public List<String> describeAll() {
        List<String> descriptions = new ArrayList<>();
        for (StageRuleHandle rule : rules) {
            String target = rule.target().isBlank() ? "*" : rule.target();
            descriptions.add(rule.type() + "[" + target + "] <- " + rule.stage());
        }
        descriptions.sort(Comparator.naturalOrder());
        return descriptions;
    }

    public static String normalizeStage(String stage) {
        if (stage == null) {
            return "";
        }
        return stage.trim().toLowerCase(Locale.ROOT);
    }
}
