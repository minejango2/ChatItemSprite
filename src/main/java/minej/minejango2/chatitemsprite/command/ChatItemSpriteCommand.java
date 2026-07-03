package minej.minejango2.chatitemsprite.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class ChatItemSpriteCommand implements BasicCommand {

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
                    "chatitemsprite",
                    "Manage the ChatItemSprite plugin",
                    java.util.List.of("cis"),
                    this
            );
        });
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        var sender = stack.getSender();
        if (!sender.hasPermission("chatitemsprite.reload")) {
            String noPermMsg = plugin.getConfig().getString("messages.no-permission", "<red>You don't have permission to reload this plugin.");
            sender.sendMessage(miniMessage.deserialize(noPermMsg));
            return;
        }
        
        plugin.reloadPlugin();
        String reloadMsg = plugin.getConfig().getString("messages.reload-success", "<green>Configuration reloaded!");
        sender.sendMessage(miniMessage.deserialize(reloadMsg));
    }
}
