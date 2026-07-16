package minej.minejango2.chatitemsprite;

import minej.minejango2.chatitemsprite.config.MessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import minej.minejango2.chatitemsprite.command.ChatItemSpriteCommand;
import minej.minejango2.chatitemsprite.listener.ChatListener;

import java.util.EnumSet;

public final class ChatItemSpritePlugin extends JavaPlugin {

    private static ChatItemSpritePlugin instance;
    private MessagesManager messagesManager;
    private final EnumSet<CustomItemPlugin> enabledPlugins = EnumSet.noneOf(CustomItemPlugin.class);
    public boolean isPluginEnabled(CustomItemPlugin thePlugin) {
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
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerListeners();
        registerCommands();

        messagesManager = new MessagesManager(this);
        messagesManager.load();

        for (CustomItemPlugin plugin : CustomItemPlugin.values()) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin.getPluginName())) {
                enabledPlugins.add(plugin);
                if (plugin == CustomItemPlugin.ITEMSADDER) {
                    getLogger().info("(SUPPORTED) Detected " + plugin.getPluginName() + ".");
                } else {
                    getLogger().info("(UNSUPPORTED) Detected " + plugin.getPluginName() + ".");
                }
            }
        }

        getLogger().info("ChatItemSprite enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatItemSprite disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    private void registerCommands() {
        new ChatItemSpriteCommand(this).register();
    }

    public void reloadPlugin() {
        reloadConfig();
        messagesManager.load();
    }

    public static ChatItemSpritePlugin getInstance() {return instance;}
    public MessagesManager getMessagesManager() {return messagesManager;}
}
