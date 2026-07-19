package minej.minejango2.chatitemsprite.config;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpriteManager {

    private final ChatItemSpritePlugin plugin;
    private final Map<String, String> customSpriteMappings = new HashMap<>();
    private final Map<String, String> vanillaSpriteMappings = new HashMap<>();

    public SpriteManager(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        customSpriteMappings.clear();
        vanillaSpriteMappings.clear();

        ConfigurationSection vanillaSection = plugin.getConfig().getConfigurationSection("define-vanilla-sprites");
        ConfigurationSection customSection = plugin.getConfig().getConfigurationSection("define-custom-item-sprites");

        if (vanillaSection != null) {
            for (String key : vanillaSection.getKeys(false)) {
                String sprite = vanillaSection.getString(key);

                if (sprite != null && !sprite.isBlank()) {
                    vanillaSpriteMappings.put(key.toLowerCase(), sprite);
                }
            }
        }

        if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
                String sprite = customSection.getString(key);

                if (sprite != null && !sprite.isBlank()) {
                    customSpriteMappings.put(key.toLowerCase(), sprite);
                }
            }
        }
    }

    @Nullable
    public String getVanillaPath(String itemId) {
        // plugin.getLogger().info("[SpriteManager] " + vanillaSpriteMappings.get(itemId.toLowerCase()));
        return vanillaSpriteMappings.get(itemId.toLowerCase());
    }

    @Nullable
    public String getCustomPath(ChatItemSpritePlugin.CustomItemPlugin ciplugin, String itemId) {
        // plugin.getLogger().info("[SpriteManager] " + customSpriteMappings.get((ciplugin.name().toLowerCase() + ":" + itemId).toLowerCase()));
        return customSpriteMappings.get((ciplugin.name().toLowerCase() + ":" + itemId).toLowerCase());
    }
}