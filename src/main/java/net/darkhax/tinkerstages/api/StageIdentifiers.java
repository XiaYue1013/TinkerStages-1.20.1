/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Locale;

/** Identifier helpers shared by the public API and the Tinkers' Construct adapter. */
public final class StageIdentifiers {

    public static final String TCONSTRUCT_NAMESPACE = "tconstruct";

    private StageIdentifiers() {
    }

    /** Adds the Tinkers' Construct namespace to legacy bare IDs. */
    public static String qualifyTinkersId(String value) {
        String trimmed = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return trimmed.indexOf(':') >= 0 ? trimmed : TCONSTRUCT_NAMESPACE + ":" + trimmed;
    }

    @Nullable
    public static ResourceLocation resourceLocation(String value) {
        return ResourceLocation.tryParse(qualifyTinkersId(value));
    }

    public static String canonicalResource(String value) {
        ResourceLocation id = resourceLocation(value);
        return id == null ? "" : id.toString();
    }
}
