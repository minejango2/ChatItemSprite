package minej.minejango2.chatitemsprite.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public final class ChatItemSpriteCommand {

    private final ChatItemSpritePlugin plugin;
    private final MiniMessage miniMessage;

    public ChatItemSpriteCommand(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("chatitemsprite")
                            .executes(ctx -> {
                                sendHelp(ctx.getSource().getSender());
                                return 1;
                            })
                            .then(
                                    Commands.literal("reload")
                                            .requires(src -> src.getSender().hasPermission("chatitemsprite.reload"))
                                            .executes(ctx -> {
                                                plugin.reloadPlugin();
                                                ctx.getSource().getSender().sendMessage(
                                                        miniMessage.deserialize(
                                                                plugin.getConfig().getString("messages.reload-success", "<green>Configuration reloaded!")
                                                        )
                                                );
                                                return 1;
                                            })
                            )
                            .build(),
                    "Manage the ChatItemSprite plugin",
                    java.util.List.of("cis")
            );
        });
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold>ChatItemSprite"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/cis reload <gray>- Reload the configuration"));
    }
}
