package minej.minejango2.chatitemsprite.config;

// import net.kyori.adventure.audience.Audience;
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

    boolean usePluginPrefix;
    private Component prefix;

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

        usePluginPrefix = messages.getBoolean("use-plugin-prefix", true);
        prefix = miniMessage.deserialize(messages.getString("plugin-prefix", ""));
    }

    public String getString(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }

    public Component getComponent(String path, String defaultValue, String... placeholders) {
        if ((placeholders.length & 1) != 0) {
            throw new IllegalArgumentException("Placeholders must be key/value pairs.");
        }

        String message = getString(path, defaultValue);

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }

        return miniMessage.deserialize(message);
    }


    public void sendMessage(CommandSender sender, String path, String defaultMessage, String... placeholders) {
        Component message = getComponent(path, defaultMessage, placeholders);

        if (usePluginPrefix) {
            message = prefix.append(Component.space()).append(message);
        }

        sender.sendMessage(message);
    }

    public void sendMessageList(CommandSender sender, String path) {
        for (String line : messages.getStringList(path)) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
    }
}
