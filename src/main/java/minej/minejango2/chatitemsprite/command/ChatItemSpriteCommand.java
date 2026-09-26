package minej.minejango2.chatitemsprite.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.CustomItemIdentifier;
import minej.minejango2.chatitemsprite.renderer.ItemFormatResolver;
import minej.minejango2.chatitemsprite.update.VersionComparator;
import minej.minejango2.chatitemsprite.update.VersionInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class ChatItemSpriteCommand {

    private static final String BASE_PERMISSION = "chatitemsprite.command";

    private final ChatItemSpritePlugin plugin;
    private final CustomItemIdentifier customItemIdentifier;

    public ChatItemSpriteCommand(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.customItemIdentifier = plugin.getCustomItemIdentifier();
    }

    // check permission
    private static Predicate<CommandSourceStack> perm(String suffix) {
        String permission = suffix.isEmpty() ? BASE_PERMISSION : BASE_PERMISSION + "." + suffix;
        return src -> src.getSender().hasPermission(permission);
    }

    // print help
    private int showHelp(CommandSourceStack source) {
        plugin.getMessageManager().sendMessageList(source.getSender(), "help");
        return 1;
    }

    // print help
    private int showSettingsHelp(CommandSourceStack source) {
        plugin.getMessageManager().sendMessageList(source.getSender(), "help-settings");
        return 1;
    }

    // print help
    private int showSpriteSettingsHelp(CommandSourceStack source) {
        plugin.getMessageManager().sendMessageList(source.getSender(), "help-settings-sprite");
        return 1;
    }

    // get update checker
    private int showVersion(CommandSourceStack source) {
        var sender = source.getSender();
        plugin.getMessageManager().sendMessage(sender, "messages.version-checking", "<gray>Checking for updates...</gray>");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            VersionInfo latest;
            Exception failure = null;

            try {
                latest = plugin.getUpdateChecker().getLatestVersion();
            } catch (Exception e) {
                latest = null;
                failure = e;
            }

            VersionInfo finalLatest = latest;
            Exception finalFailure = failure;

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (finalFailure != null) {
                    plugin.getLogger().warning("Update check failed: " + finalFailure.getMessage());
                    plugin.getMessageManager().sendMessage(sender, "messages.version-check-failed", "<red>Failed to check for updates.</red>");
                    return;
                }

                String current = plugin.getPluginMeta().getVersion();
                plugin.getMessageManager().sendMessage(sender, "messages.version-current", "<gray>ChatItemSprite <current></gray>", "<current>", current);

                if (finalLatest == null) {
                    plugin.getMessageManager().sendMessage(sender, "messages.version-unknown", "<yellow>Could not check for updates.</yellow>");
                    return;
                }

                var isNewer = VersionComparator.tryIsNewer(current, finalLatest.version());

                plugin.getMessageManager().sendMessage(sender, "messages.version-latest", "<gray>Latest: <latest></gray>", "<latest>", finalLatest.version());
                plugin.getMessageManager().sendMessage(sender, "messages.version-type", "<gray>Type: <type></gray>", "<type>", finalLatest.type());

                if (isNewer.isEmpty()) {
                    plugin.getLogger().warning("Could not compare versions: current='" + current + "', latest='" + finalLatest.version() + "'");
                    plugin.getMessageManager().sendMessage(sender, "messages.version-compare-failed", "<yellow>Could not compare version numbers.</yellow>");
                } else if (isNewer.get()) {
                    plugin.getMessageManager().sendMessage(sender, "messages.version-update-available", "<green>Update available!</green>");
                } else {
                    plugin.getMessageManager().sendMessage(sender, "messages.version-up-to-date", "<green>You are using the latest version.</green>");
                }
            });
        });

        return 1;
    }

    private int updateConfig(CommandSourceStack source, String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();

        plugin.getMessageManager().sendMessage(
                source.getSender(),
                "messages.saved-config",
                "<green>Successfully saved <config_path> to <config_value>!</green>",
                "<config_path>", path, "<config_value>", String.valueOf(value)
        );

        return 1;
    }

    private int addSprite(CommandSourceStack source, String key, String value) {
        var sender = source.getSender();

        if (!plugin.getSpriteManager().isValidSpriteValue(value)) {
            plugin.getMessageManager().sendMessage(sender, "messages.settings-sprite-invalid", "<red>'<value>' is not a valid MiniMessage value.</red>", "<value>", value);
            return 0;
        }

        plugin.getConfig().set("define-sprites." + key, value);
        plugin.saveConfig();
        plugin.getSpriteManager().reload();

        plugin.getMessageManager().sendMessage(sender, "messages.settings-sprite-set", "<green>Added/Set: '<key>' -> <value></green>", "<key>", key, "<value>", value);

        return 1;
    }

    private int removeSprite(CommandSourceStack source, String key) {
        var sender = source.getSender();

        if (!plugin.getConfig().isSet("define-sprites." + key)) {
            plugin.getMessageManager().sendMessage(sender, "messages.settings-sprite-not-found", "<yellow>No sprite defined for '<key>'.</yellow>", "<key>", key);
            return 0;
        }

        plugin.getConfig().set("define-sprites." + key, null);
        plugin.saveConfig();
        plugin.getSpriteManager().reload();

        plugin.getMessageManager().sendMessage(sender, "messages.settings-sprite-removed", "<green>Removed: '<key>'.</green>", "<key>", key);

        return 1;
    }

    private LiteralArgumentBuilder<CommandSourceStack> enumSetting(
            String literalName, String configKey, Set<String> allowedValues) {

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(literalName)
                .requires(perm("settings." + literalName));

        for (String value : allowedValues) {
            node.then(Commands.literal(value)
                    .executes(ctx -> updateConfig(ctx.getSource(), configKey, value)));
        }

        return node;
    }

    private CompletableFuture<Suggestions> suggestHeldItemKey(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        CommandSender sender = ctx.getSource().getSender();

        if (sender instanceof Player player) {
            String key = customItemIdentifier.getSuggestedSpriteKey(player.getInventory().getItemInMainHand());
            if (key != null) {
                builder.suggest(key.contains(":") ? "\"" + key + "\"" : key);
            }
        }

        return builder.buildFuture();
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            commands.register(
                    Commands.literal("chatitemsprite")
                            .requires(perm(""))
                            .executes(ctx -> {
                                var sender = ctx.getSource().getSender();
                                plugin.getMessageManager().sendMessage(sender, "messages.check-help", "<gradient:#89A685:#637758>Welcome! Please use \"/chatitemsprite help\" for helps!</gradient>");
                                return 1;
                            })
                            .then(
                                    Commands.literal("help")
                                            .requires(perm("help"))
                                            .executes(ctx -> showHelp(ctx.getSource()))
                                            .then(
                                                    Commands.literal("settings")
                                                            .requires(perm("help.settings"))
                                                            .executes(ctx -> showSettingsHelp(ctx.getSource()))
                                                            .then(
                                                                    Commands.literal("sprite")
                                                                            .requires(perm("help.settings.sprite"))
                                                                            .executes(ctx -> showSpriteSettingsHelp(ctx.getSource()))
                                                            )
                                            )

                            )
                            .then(
                                    Commands.literal("reload")
                                            .requires(perm("reload"))
                                            .executes(ctx -> {
                                                plugin.reloadPlugin();

                                                var sender = ctx.getSource().getSender();
                                                if (plugin.getSpriteManager().hadWarningsOnLastReload()) {
                                                    plugin.getMessageManager().sendMessage(sender, "messages.reloaded-with-warn", "<yellow>ChatItemSprite got an issue while reloading. Please check console for details.</yellow>");
                                                } else {
                                                    plugin.getMessageManager().sendMessage(sender, "messages.reload-success", "<green>Reloaded ChatItemSprite!</green>");
                                                }
                                                return 1;
                                            })
                            )
                            .then(
                                    Commands.literal("version")
                                            .requires(perm("version"))
                                            .executes(ctx -> showVersion(ctx.getSource()))
                            )
                            .then(
                                    Commands.literal("settings")
                                            .requires(perm("settings"))
                                            .then(enumSetting("display-mode", "item.display-mode", ItemFormatResolver.VALID_DISPLAY_MODES))
                                            .then(enumSetting("vanilla-fallback", "item.unsupported-vanilla-item-fallback", ItemFormatResolver.VALID_VANILLA_FALLBACKS))
                                            .then(enumSetting("custom-fallback", "item.unsupported-custom-item-fallback", ItemFormatResolver.VALID_CUSTOM_FALLBACKS))
                                            .then(
                                                    Commands.literal("sprite")
                                                            .requires(perm("settings.sprite"))
                                                            .then(
                                                                    Commands.literal("set")
                                                                            .requires(perm("settings.sprite.set"))
                                                                            .then(Commands.argument("key", StringArgumentType.string())
                                                                                    .suggests(this::suggestHeldItemKey)
                                                                                    .then(Commands.argument("value", StringArgumentType.greedyString())
                                                                                            .executes(ctx -> addSprite(
                                                                                                    ctx.getSource(),
                                                                                                    StringArgumentType.getString(ctx, "key"),
                                                                                                    StringArgumentType.getString(ctx, "value")
                                                                                            ))
                                                                                    )
                                                                            )
                                                            )
                                                            .then(
                                                                    Commands.literal("remove")
                                                                            .requires(perm("settings.sprite.remove"))
                                                                            .then(Commands.argument("key", StringArgumentType.string())
                                                                                    .executes(ctx -> removeSprite(
                                                                                            ctx.getSource(),
                                                                                            StringArgumentType.getString(ctx, "key")
                                                                                    ))
                                                                            )
                                                            )
                                            )
                            )
                            .build(),

                    "Manage the ChatItemSprite plugin",
                    List.of("cis")
            );
        });
    }
}
