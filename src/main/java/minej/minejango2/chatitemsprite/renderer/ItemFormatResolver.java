package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;

import java.util.Set;

public final class ItemFormatResolver {

    public static final Set<String> VALID_DISPLAY_MODES = Set.of("both", "sprite-only", "text-only");
    public static final Set<String> VALID_VANILLA_FALLBACKS = Set.of("text", "none");
    public static final Set<String> VALID_CUSTOM_FALLBACKS = Set.of("text", "raw", "none");

    private final ChatItemSpritePlugin plugin;

    public ItemFormatResolver(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    public String resolveFormatMode(String displayMode, boolean spriteMissing, boolean isCustomItem) {
        if (!spriteMissing || displayMode.equals("text-only")) {
            return displayMode;
        }

        if (isCustomItem) {
            String customFallback = plugin.getConfig().getString("item.unsupported-custom-item-fallback", "text").toLowerCase();

            if (!VALID_CUSTOM_FALLBACKS.contains(customFallback)) {
                plugin.getLogger().warning("Unknown item.unsupported-custom-item-fallback '" + customFallback + "', defaulting to 'text'. Valid values: " + VALID_CUSTOM_FALLBACKS);
                customFallback = "text";
            }

            if (customFallback.equals("text")) {
                return "text-only";
            }
            if (customFallback.equals("none")) {
                return "fallback-none";
            }

            return resolveVanillaFallbackMode(displayMode);
        }

        return resolveVanillaFallbackMode(displayMode);
    }

    private String resolveVanillaFallbackMode(String displayMode) {
        String fallback = plugin.getConfig().getString("item.unsupported-vanilla-item-fallback", "text").toLowerCase();

        if (!VALID_VANILLA_FALLBACKS.contains(fallback)) {
            plugin.getLogger().warning("Unknown item.unsupported-vanilla-item-fallback '" + fallback + "', defaulting to 'text'. Valid values: " + VALID_VANILLA_FALLBACKS);
            fallback = "text";
        }

        return switch (fallback) {
            case "text" -> "text-only";
            case "none" -> "fallback-none";
            default -> displayMode;
        };
    }

    public String defaultFormatFor(String formatMode) {
        return switch (formatMode) {
            case "sprite-only" -> "<gray>[<reset><cis_sprite><gray>]";
            case "text-only" -> "<gray>[<reset><cis_name><cis_amount_tag><gray>]";
            case "fallback-none" -> "<gray>[<reset>?<gray>]";
            default -> "<gray>[<reset><cis_sprite> <cis_name><cis_amount_tag><gray>]";
        };
    }
}
