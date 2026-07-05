package minej.minejango2.chatitemsprite.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;

public final class ChatItemSpriteCommand {

    private final ChatItemSpritePlugin plugin;

    public ChatItemSpriteCommand(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    private int showHelp(CommandSourceStack source) {
        plugin.getMessagesManager().sendMessageList(source.getSender(), "help");
        return 1;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("chatitemsprite")
                            .executes(ctx -> showHelp(ctx.getSource()))
                            .then(
                                    Commands.literal("help")
                                            .executes(ctx -> showHelp(ctx.getSource()))
                            )
                            .then(Commands.literal("reload")
                                          .requires(src -> src.getSender().hasPermission("chatitemsprite.reload"))
                                          .executes(ctx -> {
                                              plugin.reloadPlugin();
                                              plugin.getMessagesManager().sendMessage(ctx.getSource().getSender(), "messages.reload-success", "<green>Configuration reloaded!");
                                              return 1;
                                          })
                            )
                            .build(),
                    "Manage the ChatItemSprite plugin",
                    java.util.List.of("cis")
            );
        });
    }
}
