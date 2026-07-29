package minej.minejango2.chatitemsprite.config;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpriteManager {

    private final ChatItemSpritePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> spriteMappings = new HashMap<>();
    private int lastReloadWarningCount = 0;

    public SpriteManager(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        spriteMappings.clear();
        lastReloadWarningCount = 0;

        warnIfLegacySectionsPresent();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("define-sprites");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String value = section.getString(key);

            if (value == null) {
                plugin.getLogger().warning("Skipping define-sprites entry '" + key + "': expected a plain text value but found something else" + " (check for a stray space after a colon — keys containing ':' should be quoted).");
                lastReloadWarningCount++;
                continue;
            }

            if (value.isBlank()) {
                continue;
            }

            if (!isValidSpriteValue(value)) {
                plugin.getLogger().warning("Skipping invalid define-sprites entry '" + key + "': value could not be parsed as MiniMessage ('" + value + "').");
                lastReloadWarningCount++;
                continue;
            }

            spriteMappings.put(key.toLowerCase(), value);
        }
    }

    public boolean hadWarningsOnLastReload() {
        return lastReloadWarningCount > 0;
    }

    public boolean isValidSpriteValue(String value) {
        try {
            miniMessage.deserialize(value);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void warnIfLegacySectionsPresent() {
        boolean hasLegacy = plugin.getConfig().isConfigurationSection("define-vanilla-sprites") || plugin.getConfig().isConfigurationSection("define-custom-item-sprites");
        if (hasLegacy) {
            plugin.getLogger().warning("define-vanilla-sprites / define-custom-item-sprites are no longer read" + " as of 3.0.0. Please move their entries into the unified 'define-sprites' section.");
        }
    }

    @Nullable
    public String getVanillaPath(String itemId) {
        return spriteMappings.get(itemId.toLowerCase());
    }

    @Nullable
    public String getCustomPath(ChatItemSpritePlugin.CustomItemPlugin customItemPlugin, String itemId) {
        return spriteMappings.get((customItemPlugin.name().toLowerCase() + ":" + itemId).toLowerCase());
    }
}
