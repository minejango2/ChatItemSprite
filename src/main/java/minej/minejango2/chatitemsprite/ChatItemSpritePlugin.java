package minej.minejango2.chatitemsprite;

import minej.minejango2.chatitemsprite.config.MessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import minej.minejango2.chatitemsprite.command.ChatItemSpriteCommand;
import minej.minejango2.chatitemsprite.listener.ChatListener;

public final class ChatItemSpritePlugin extends JavaPlugin {

    private static ChatItemSpritePlugin instance;
    private MessagesManager messagesManager;
    public boolean itemsAdderEnabled;

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

        itemsAdderEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");

        if (itemsAdderEnabled) {
            getLogger().info("Detected ItemsAdder.");
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

    public static ChatItemSpritePlugin getInstance() {
        return instance;
    }

    public MessagesManager getMessagesManager() {return messagesManager;}
}
