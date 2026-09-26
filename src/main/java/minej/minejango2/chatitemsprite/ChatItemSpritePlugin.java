package minej.minejango2.chatitemsprite;

import minej.minejango2.chatitemsprite.compat.InteractiveChatCompat;
import minej.minejango2.chatitemsprite.config.MessageManager;
import minej.minejango2.chatitemsprite.config.SlotKeywordsManager;
import minej.minejango2.chatitemsprite.config.SpriteManager;
import minej.minejango2.chatitemsprite.update.UpdateChecker;
import minej.minejango2.chatitemsprite.update.VersionComparator;
import minej.minejango2.chatitemsprite.update.VersionInfo;
import minej.minejango2.chatitemsprite.renderer.CustomItemIdentifier;
import org.bstats.bukkit.Metrics;
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

    private MessageManager messageManager;
    private SpriteManager spriteManager;
    private UpdateChecker updateChecker;
    private SlotKeywordsManager slotKeywordsManager;
    private CustomItemIdentifier customItemIdentifier;
    private final EnumSet<CustomItemPlugin> enabledPlugins = EnumSet.noneOf(CustomItemPlugin.class);
    public boolean isPluginEnabledCustom(CustomItemPlugin thePlugin) {return enabledPlugins.contains(thePlugin);}

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
        int pluginId = 	32813;
        Metrics metrics = new Metrics(this, pluginId);

        saveDefaultConfig();

        messageManager = new MessageManager(this);
        messageManager.reload();
        spriteManager = new SpriteManager(this);
        spriteManager.reload();
        updateChecker = new UpdateChecker(this);
        slotKeywordsManager = new SlotKeywordsManager(this);

        for (CustomItemPlugin plugin : CustomItemPlugin.values()) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin.getPluginName())) {
                enabledPlugins.add(plugin);
                getLogger().info("Detected " + plugin.getPluginName() + ".");
            }
        }

        customItemIdentifier = new CustomItemIdentifier(this);

        registerListeners();
        registerCommands();

        InteractiveChatCompat.registerIfApplicable(this);

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
                    getLogger().warning("Update check: could not compare current version '" + current + "' with latest '" + latest.version() + "'.");
                    return;
                }

                if (isNewer.get()) {
                    getLogger().warning("A new version of ChatItemSprite is available: " + latest.version() + " (current: " + current + "). Download: " + latest.url());
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
        new ChatListener(this).register();
    }

    private void registerCommands() {
        new ChatItemSpriteCommand(this).register();
    }

    public void reloadPlugin() {
        reloadConfig();
        messageManager.reload();
        spriteManager.reload();
        slotKeywordsManager.reload();
    }

    public MessageManager getMessageManager() {return messageManager;}
    public SpriteManager getSpriteManager() {return spriteManager;}
    public UpdateChecker getUpdateChecker() {return updateChecker;}
    public SlotKeywordsManager getSlotKeywordsManager() {return slotKeywordsManager;}
    public CustomItemIdentifier getCustomItemIdentifier() {return customItemIdentifier;}
}
