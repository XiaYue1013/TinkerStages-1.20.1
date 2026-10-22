/*
 * TinkerStages - Forge 1.20.1 port
 * Based on TinkerStages by Darkhax, licensed under LGPL-2.1.
 */
package net.darkhax.tinkerstages.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.darkhax.tinkerstages.TinkerStages;
import net.darkhax.tinkerstages.api.StageRuleHandle;
import net.darkhax.tinkerstages.stage.StageRuleStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = TinkerStages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TinkerStagesCommands {

    private TinkerStagesCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(build("tinkerstages", 2));
        event.getDispatcher().register(build("tcondump", 0));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(String name, int permission) {
        return Commands.literal(name)
            .requires(source -> source.hasPermission(permission))
            .executes(context -> {
                context.getSource().sendSuccess(
                    () -> Component.translatable("tinkerstages.command.usage"), false);
                return 1;
            })
            .then(Commands.literal("dump")
                .then(Commands.literal("tools").executes(context -> dumpTools(context.getSource())))
                .then(Commands.literal("materials").executes(context -> dumpMaterials(context.getSource())))
                .then(Commands.literal("modifiers").executes(context -> dumpModifiers(context.getSource()))))
            .then(Commands.literal("rules").executes(context -> dumpRules(context.getSource())));
    }

    private static int dumpTools(CommandSourceStack source) {
        List<String> ids = ToolDefinitionLoader.getInstance().getRegisteredToolDefinitions().stream()
            .map(ToolDefinition::getId)
            .map(Object::toString)
            .sorted()
            .toList();
        sendList(source, "Tinkers' Construct tool definitions", ids);
        return ids.size();
    }

    private static int dumpMaterials(CommandSourceStack source) {
        List<String> ids = MaterialRegistry.getMaterials().stream()
            .map(IMaterial::getIdentifier)
            .map(Object::toString)
            .sorted()
            .toList();
        sendList(source, "Tinkers' Construct materials", ids);
        return ids.size();
    }

    private static int dumpModifiers(CommandSourceStack source) {
        List<String> ids = ModifierManager.INSTANCE.getAllLocations()
            .map(Object::toString)
            .sorted()
            .toList();
        sendList(source, "Tinkers' Construct modifiers", ids);
        return ids.size();
    }

    private static int dumpRules(CommandSourceStack source) {
        List<StageRuleHandle> rules = StageRuleStore.INSTANCE.snapshot().stream()
            .sorted(Comparator.comparing(StageRuleHandle::type)
                .thenComparing(StageRuleHandle::target)
                .thenComparing(StageRuleHandle::stage))
            .toList();
        if (rules.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("tinkerstages.command.rules.empty"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(
            "tinkerstages.command.rules.header", rules.size()), false);
        for (StageRuleHandle rule : rules) {
            String line = rule.type() + " | target=" + (rule.target().isBlank() ? "*" : rule.target())
                + " | stage=" + rule.stage();
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return rules.size();
    }

    private static void sendList(CommandSourceStack source, String title, List<String> ids) {
        source.sendSuccess(() -> Component.literal(title + " (" + ids.size() + "):"), false);
        for (String id : ids) {
            source.sendSuccess(() -> Component.literal(id), false);
        }
    }
}
