/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

import net.darkhax.tinkerstages.TinkerStages;
import net.darkhax.tinkerstages.TinkerStagesConfig;
import net.darkhax.tinkerstages.api.StageIdentifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.Map;

/**
 * Resolves the string IDs used by 1.12.2 scripts against Tinkers' Construct 3 registries.
 * No registry or recipe is modified here.
 */
public final class TConstructIdentifiers {

    private static final Map<String, String> TOOL_ALIASES = Map.of(
        "tconstruct:hammer", "tconstruct:sledge_hammer",
        "tconstruct:hatchet", "tconstruct:hand_axe",
        "tconstruct:lumberaxe", "tconstruct:broad_axe",
        "tconstruct:shovel", "tconstruct:mattock",
        "tconstruct:broadsword", "tconstruct:sword",
        "tconstruct:longsword", "tconstruct:sword",
        "tconstruct:battleaxe", "tconstruct:broad_axe",
        "tconstruct:shortbow", "tconstruct:longbow"
    );

    private static final Map<String, String> MATERIAL_ALIASES = Map.of(
        "tconstruct:stone", "tconstruct:rock",
        "tconstruct:oak", "tconstruct:wood",
        "tconstruct:spruce", "tconstruct:wood",
        "tconstruct:birch", "tconstruct:wood",
        "tconstruct:jungle", "tconstruct:wood",
        "tconstruct:acacia", "tconstruct:wood",
        "tconstruct:dark_oak", "tconstruct:wood"
    );

    private TConstructIdentifiers() {
    }

    public static IdentifierResult tool(String raw) {
        ResourceLocation parsed = StageIdentifiers.resourceLocation(raw);
        if (parsed == null) {
            return new IdentifierResult("", false, false, "Invalid ResourceLocation syntax: " + raw);
        }
        String id = parsed.toString();
        if (TinkerStagesConfig.migrateLegacyIds()) {
            String migrated = TOOL_ALIASES.get(id);
            if (migrated != null) {
                logMigration("tool", id, migrated);
                id = migrated;
            }
        }
        ResourceLocation location = ResourceLocation.tryParse(id);
        boolean known = location != null && isToolDefinitionRegistered(location);
        String diagnostic = known ? "" : "No Tinkers' Construct tool definition is registered as " + id;
        if (!known) {
            logUnknown("tool", id, diagnostic);
        }
        return new IdentifierResult(id, location != null, known, diagnostic);
    }

    public static IdentifierResult material(String raw) {
        String qualified = StageIdentifiers.qualifyTinkersId(raw);
        String[] parts = qualified.split("#", 2);
        ResourceLocation parsed = ResourceLocation.tryParse(parts[0]);
        if (parsed == null) {
            return new IdentifierResult("", false, false, "Invalid material ID syntax: " + raw);
        }
        String id = parsed.toString();
        if (TinkerStagesConfig.migrateLegacyIds()) {
            String migrated = MATERIAL_ALIASES.get(id);
            if (migrated != null) {
                logMigration("material", id, migrated);
                id = migrated;
            }
        }
        MaterialId materialId = MaterialId.tryParse(id);
        boolean available = MaterialRegistry.isFullyLoaded();
        boolean known = false;
        if (available) {
            IMaterial material = MaterialRegistry.getMaterial(materialId);
            known = material != IMaterial.UNKNOWN;
        }
        String diagnostic = parts.length > 1
            ? "Material variants are matched by base material; configured " + qualified
            : "";
        if (available && !known) {
            diagnostic = "No Tinkers' Construct material is registered as " + id;
            logUnknown("material", id, diagnostic);
        } else if (!available) {
            diagnostic = "Material registry is not loaded yet; rule retained for deferred validation";
        }
        return new IdentifierResult(id, materialId != null, known, diagnostic);
    }

    public static IdentifierResult modifier(String raw) {
        ResourceLocation parsed = StageIdentifiers.resourceLocation(raw);
        if (parsed == null) {
            return new IdentifierResult("", false, false, "Invalid modifier ID syntax: " + raw);
        }
        String id = parsed.toString();
        ModifierId modifierId = ModifierId.tryParse(id);
        boolean loaded = ModifierManager.INSTANCE.isDynamicModifiersLoaded();
        boolean known = loaded && ModifierManager.INSTANCE.contains(modifierId);
        String diagnostic = "";
        if (loaded && !known) {
            diagnostic = "No Tinkers' Construct modifier is registered as " + id;
            logUnknown("modifier", id, diagnostic);
        } else if (!loaded) {
            diagnostic = "Modifier registry is not loaded yet; rule retained for deferred validation";
        }
        return new IdentifierResult(id, modifierId != null, known, diagnostic);
    }

    /** Used by diagnostics and tests to expose the documented migration table. */
    public static Map<String, String> toolAliases() {
        return TOOL_ALIASES;
    }

    public static Map<String, String> materialAliases() {
        return MATERIAL_ALIASES;
    }

    private static boolean isToolDefinitionRegistered(ResourceLocation id) {
        for (ToolDefinition definition : ToolDefinitionLoader.getInstance().getRegisteredToolDefinitions()) {
            if (definition.getId().equals(id)) {
                return true;
            }
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item instanceof IModifiable modifiable && modifiable.getToolDefinition().getId().equals(id);
    }

    private static void logMigration(String kind, String from, String to) {
        if (TinkerStagesConfig.dumpMigrationWarnings()) {
            TinkerStages.LOGGER.warn("Migrating legacy {} ID {} to {}; update scripts to the new ID.", kind, from, to);
        }
    }

    private static void logUnknown(String kind, String id, String message) {
        if (TinkerStagesConfig.dumpMigrationWarnings()) {
            TinkerStages.LOGGER.error("Unresolved TinkerStages {} ID '{}': {}", kind, id, message);
        }
    }
}
