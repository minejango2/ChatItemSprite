package minej.minejango2.chatitemsprite.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.updater.VersionComparator;
import minej.minejango2.chatitemsprite.updater.VersionInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public final class ChatItemSpriteCommand {

    private final ChatItemSpritePlugin plugin;

    public ChatItemSpriteCommand(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    private int showHelp(CommandSourceStack source) {
        plugin.getMessagesManager().sendMessageList(source.getSender(), "help");
        return 1;
    }

    private int showVersion(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("Checking for updates..."));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                VersionInfo latest = plugin.getUpdateChecker().getLatestVersion();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    String current = plugin.getPluginMeta().getVersion();

                    source.getSender().sendMessage(Component.text("ChatItemSprite " + current));

                    if (latest == null) {
                        source.getSender().sendMessage(Component.text("Could not check for updates."));
                        return;
                    }

                    boolean update = VersionComparator.isNewer(current, latest.version());

                    source.getSender().sendMessage(Component.text("Latest: " + latest.version()));
                    source.getSender().sendMessage(Component.text("Type: " + latest.type()));
                    source.getSender().sendMessage(Component.text(
                            update ? "Update available!" : "You are using the latest version."
                    ));
                });

            } catch (Exception e) {

                Bukkit.getScheduler().runTask(plugin, () ->
                        source.getSender().sendMessage(Component.text("Failed to check for updates."))
                );

            }

        });

        return 1;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("chatitemsprite")
                            .requires(src -> src.getSender().hasPermission("chatitemsprite.command"))
                            .executes(ctx -> showHelp(ctx.getSource()))
                            .then(
                                    Commands.literal("help")
                                            .requires(src -> src.getSender().hasPermission("chatitemsprite.command.help"))
                                            .executes(ctx -> showHelp(ctx.getSource()))
                            )
                            .then(Commands.literal("reload")
                                          .requires(src -> src.getSender().hasPermission("chatitemsprite.command.reload"))
                                          .executes(ctx -> {
                                              plugin.reloadPlugin();
                                              plugin.getMessagesManager().sendMessage(ctx.getSource().getSender(), "messages.reload-success", "<green>Configuration reloaded!");
                                              return 1;
                                          })
                            )
                            .then(
                                    Commands.literal("version")
                                            .requires(src -> src.getSender().hasPermission("chatitemsprite.command.version"))
                                            .executes(ctx -> showVersion(ctx.getSource()))
                            )
                            .build(),
                    "Manage the ChatItemSprite plugin",
                    java.util.List.of("cis")
            );
        });
    }
}
