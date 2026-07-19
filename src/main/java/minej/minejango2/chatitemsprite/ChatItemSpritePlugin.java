package minej.minejango2.chatitemsprite;

import minej.minejango2.chatitemsprite.config.MessagesManager;
import minej.minejango2.chatitemsprite.config.SpriteManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import minej.minejango2.chatitemsprite.command.ChatItemSpriteCommand;
import minej.minejango2.chatitemsprite.listener.ChatListener;

import java.util.EnumSet;

public final class ChatItemSpritePlugin extends JavaPlugin {

    private static ChatItemSpritePlugin instance;
    private MessagesManager messagesManager;
    private SpriteManager spriteManager;
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
    public void onLoad() {}

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        registerListeners();
        registerCommands();

        messagesManager = new MessagesManager(this);
        messagesManager.reload();
        spriteManager = new SpriteManager(this);
        spriteManager.reload();

        for (CustomItemPlugin plugin : CustomItemPlugin.values()) {
            if (Bukkit.getPluginManager().isPluginEnabled(plugin.getPluginName())) {
                enabledPlugins.add(plugin);
                getLogger().info("Detected " + plugin.getPluginName() + ".");
            }
        }

        getLogger().info("ChatItemSprite enabled.");
    }

    @Override
    public void onDisable() {
        instance = null;
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
        messagesManager.reload();
        spriteManager.reload();
    }

    public static ChatItemSpritePlugin getInstance() {return instance;}
    public MessagesManager getMessagesManager() {return messagesManager;}
    public SpriteManager getSpriteManager() {return spriteManager;}
}
