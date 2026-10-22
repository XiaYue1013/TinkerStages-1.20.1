/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.integration.tconstruct;

public record IdentifierResult(String id, boolean syntaxValid, boolean registryKnown, String diagnostic) {

    public boolean acceptable(boolean strict) {
        return syntaxValid && (!strict || registryKnown);
    }
}
