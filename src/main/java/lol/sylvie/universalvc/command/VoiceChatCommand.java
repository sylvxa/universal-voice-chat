package lol.sylvie.universalvc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.sdk.DiscordHandler;
import lol.sylvie.universalvc.util.Result;
import lol.sylvie.universalvc.voice.LobbyHandler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.RandomStringUtils;
import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;

public class VoiceChatCommand {
    private static void sendResult(CommandContext<FabricClientCommandSource> context, String successKey, String errorKey, Result result) {
        Minecraft.getInstance().execute(() -> {
            if (result.success()) {
                context.getSource().sendFeedback(Component.translatable(successKey, result.message()));
            } else {
                context.getSource().sendError(Component.translatable(errorKey, result.message()));
            }
        });
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext ctx) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommands.literal("uvc");
        root.requires(_ -> !UniversalVoiceChat.isUnavailable());

        LiteralArgumentBuilder<FabricClientCommandSource> create = commandCreate();
        root.then(create);

        LiteralArgumentBuilder<FabricClientCommandSource> join = ClientCommands.literal("join");
        join.then(
                ClientCommands.argument("secret", StringArgumentType.word())
                .executes(context -> {
                    LobbyHandler.createOrJoin(StringArgumentType.getString(context, "secret"), result -> {
                        sendResult(context, "commands.uvc.join", "commands.uvc.error.join", result);
                    });

                    return 0;
                }));
        root.then(join);

        LiteralArgumentBuilder<FabricClientCommandSource> leave = ClientCommands.literal("leave");
        leave.executes(context -> {
            LobbyHandler.leave(result -> {
                sendResult(context, "commands.uvc.leave", "commands.uvc.error.leave", result);
            });

            return 0;
        });
        root.then(leave);

        dispatcher.register(root);
    }

    private static @NonNull LiteralArgumentBuilder<FabricClientCommandSource> commandCreate() {
        LiteralArgumentBuilder<FabricClientCommandSource> create = ClientCommands.literal("create");
        create.executes(context -> {
            String secret = RandomStringUtils.insecure().nextAlphanumeric(8);
            LobbyHandler.createOrJoin(secret, result -> {
                Minecraft.getInstance().execute(() -> {
                    if (result.success()) {
                        String command = "/uvc join " + secret;
                        context.getSource().sendFeedback(Component.translatable("commands.uvc.create",
                                Component.literal(command)
                                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard("/uvc join " + secret)).withUnderlined(true))));
                    } else {
                        context.getSource().sendError(Component.translatable("commands.uvc.error.create"));
                    }
                });
            });

            return 0;
        });
        return create;
    }

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(VoiceChatCommand::register);
    }
}
