/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class TinkerStagesConfig {

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue NOTIFY_PLAYERS;
    private static final ForgeConfigSpec.BooleanValue DROP_RESTRICTED_HELD_TOOLS;
    private static final ForgeConfigSpec.BooleanValue STRICT_IDENTIFIER_VALIDATION;
    private static final ForgeConfigSpec.BooleanValue MIGRATE_LEGACY_IDS;
    private static final ForgeConfigSpec.BooleanValue DUMP_MIGRATION_WARNINGS;

    private static volatile boolean notifyPlayers = true;
    private static volatile boolean dropRestrictedHeldTools = true;
    private static volatile boolean strictIdentifierValidation = false;
    private static volatile boolean migrateLegacyIds = true;
    private static volatile boolean dumpMigrationWarnings = true;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Server-authoritative behavior. Stage checks are never disabled by these options.")
            .push("restrictions");
        NOTIFY_PLAYERS = builder
            .comment("Send a chat message when an operation or held tool is denied.")
            .define("notifyPlayers", true);
        DROP_RESTRICTED_HELD_TOOLS = builder
            .comment("Match the original mod by dropping restricted held tools. If false, interactions are",
                "still denied, but passive modifier effects cannot be fully suppressed by Forge events.")
            .define("dropRestrictedHeldTools", true);
        STRICT_IDENTIFIER_VALIDATION = builder
            .comment("Reject unknown tool/material/modifier IDs instead of keeping a deferred rule.")
            .define("strictIdentifierValidation", false);
        MIGRATE_LEGACY_IDS = builder
            .comment("Apply documented 1.12.2 -> 1.20.1 aliases for known tool/material IDs.")
            .define("migrateLegacyIds", true);
        DUMP_MIGRATION_WARNINGS = builder
            .comment("Log every legacy ID alias and every unresolved identifier.")
            .define("dumpMigrationWarnings", true);
        builder.pop();
        SPEC = builder.build();
    }

    private TinkerStagesConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        refresh();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        refresh();
    }

    private static void refresh() {
        if (SPEC.isLoaded()) {
            notifyPlayers = NOTIFY_PLAYERS.get();
            dropRestrictedHeldTools = DROP_RESTRICTED_HELD_TOOLS.get();
            strictIdentifierValidation = STRICT_IDENTIFIER_VALIDATION.get();
            migrateLegacyIds = MIGRATE_LEGACY_IDS.get();
            dumpMigrationWarnings = DUMP_MIGRATION_WARNINGS.get();
        }
    }

    public static boolean notifyPlayers() {
        return notifyPlayers;
    }

    public static boolean dropRestrictedHeldTools() {
        return dropRestrictedHeldTools;
    }

    public static boolean strictIdentifierValidation() {
        return strictIdentifierValidation;
    }

    public static boolean migrateLegacyIds() {
        return migrateLegacyIds;
    }

    public static boolean dumpMigrationWarnings() {
        return dumpMigrationWarnings;
    }
}
