package minej.minejango2.chatitemsprite;

import minej.minejango2.chatitemsprite.config.MessagesManager;
import minej.minejango2.chatitemsprite.config.SpriteManager;
import minej.minejango2.chatitemsprite.updater.UpdateChecker;
import minej.minejango2.chatitemsprite.updater.VersionComparator;
import minej.minejango2.chatitemsprite.updater.VersionInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import minej.minejango2.chatitemsprite.command.ChatItemSpriteCommand;
import minej.minejango2.chatitemsprite.listener.ChatListener;

import java.util.EnumSet;
import java.util.Optional;

public final class ChatItemSpritePlugin extends JavaPlugin implements Listener {

    private MessagesManager messagesManager;
    private SpriteManager spriteManager;
    private UpdateChecker updateChecker;
    private final EnumSet<CustomItemPlugin> enabledPlugins = EnumSet.noneOf(CustomItemPlugin.class);
    public boolean isPluginEnabledCustom(CustomItemPlugin thePlugin) {
        return enabledPlugins.contains(thePlugin);
    }

    public enum CustomItemPlugin {
        ITEMSADDER("ItemsAdder"),
        ORAXEN("Oraxen"),
        NEXO("Nexo"),
        CRAFTENGINE("CraftEngine");

        private final String pluginName;

        CustomItemPlugin(String pluginName) {
            this.pluginName = pluginName;
        }

        public String getPluginName() {
            return pluginName;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerListeners();
        registerCommands();

        messagesManager = new MessagesManager(this);
        messagesManager.reload();
        spriteManager = new SpriteManager(this);
        spriteManager.reload();
        updateChecker = new UpdateChecker(this);

        for (CustomItemPlugin plugin : CustomItemPlugin.values()) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin.getPluginName())) {
                enabledPlugins.add(plugin);
                getLogger().info("Detected " + plugin.getPluginName() + ".");
            }
        }

        getLogger().info("ChatItemSprite enabled.");
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }
        if (getConfig().getBoolean("update-check.enabled", true)) {
            checkForUpdatesAsync();
        }
    }

    private void checkForUpdatesAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            getLogger().info("Checking for updates...");

            try {
                VersionInfo latest = updateChecker.getLatestVersion();

                if (latest == null) {
                    getLogger().info("Update check: no eligible version found on Modrinth.");
                    return;
                }

                String current = getPluginMeta().getVersion();
                Optional<Boolean> isNewer = VersionComparator.tryIsNewer(current, latest.version());

                if (isNewer.isEmpty()) {
                    getLogger().warning("Update check: could not compare current version '"
                            + current + "' with latest '" + latest.version() + "'.");
                    return;
                }

                if (isNewer.get()) {
                    getLogger().warning("A new version of ChatItemSprite is available: "
                            + latest.version() + " (current: " + current + "). Download: " + latest.url());
                } else {
                    getLogger().info("ChatItemSprite is up to date (" + current + ").");
                }
            } catch (Exception e) {
                getLogger().warning("Update check failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatItemSprite disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    private void registerCommands() {
        new ChatItemSpriteCommand(this).register();
    }

    public void reloadPlugin() {
        reloadConfig();
        messagesManager.reload();
        spriteManager.reload();
    }

    public MessagesManager getMessagesManager() {return messagesManager;}
    public SpriteManager getSpriteManager() {return spriteManager;}
    public UpdateChecker getUpdateChecker() {return updateChecker;}
}
