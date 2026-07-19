package minej.minejango2.chatitemsprite.config;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesManager {
    private final Plugin plugin;
    private final MiniMessage miniMessage;

    private FileConfiguration messages;

    public MessagesManager(Plugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();

        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public void reload() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getString(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }

    public Component getComponent(String path, String defaultValue) {
        return miniMessage.deserialize(getString(path, defaultValue));
    }

    public void sendMessage(CommandSender sender, String path, String defaultMessage) {
        sender.sendMessage(getComponent(path, defaultMessage));
    }

    public void sendMessageList(CommandSender sender, String path) {
        for (String line : messages.getStringList(path)) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
    }

    public void sendActionBar(CommandSender sender, String path, String defaultMessage) {
        if (sender instanceof Audience audience) {
            audience.sendActionBar(getComponent(path, defaultMessage));
        }
    }

    public FileConfiguration getConfig() {
        return messages;
    }
}