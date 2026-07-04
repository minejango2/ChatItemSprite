package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ItemRenderer {

    private static final Set<Material> UNSUPPORTED_SPRITES = EnumSet.of(
            Material.PLAYER_HEAD,
            Material.SHIELD,
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.RED_BANNER,
            Material.ORANGE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.GREEN_BANNER,
            Material.CYAN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.BLUE_BANNER,
            Material.PURPLE_BANNER,
            Material.MAGENTA_BANNER,
            Material.PINK_BANNER,
            Material.BROWN_BANNER,
            Material.WHITE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.GRAY_BANNER,
            Material.BLACK_BANNER
    );

    private static final List<String> UNSUPPORTED_CATEGORIES = List.of(
            "STAIRS",
            "SLAB",
            "WALL",
            "BUTTON",
            "FENCE",
            "PRESSURE_PLATE",
            "COPPER_CHEST"
    );

    private static final Set<Material> NON_UNSUPPORTED = EnumSet.of(
            Material.COPPER_CHESTPLATE
    );

    private final ChatItemSpritePlugin plugin;
    private final MiniMessage miniMessage;
    private final ItemsAdderRenderer itemsAdderRenderer;

    public ItemRenderer(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.itemsAdderRenderer = new ItemsAdderRenderer();
    }

    public Component render(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR || item.getAmount() <= 0) {
            return renderEmptyHand();
        }

        return renderItem(item);
    }

    private Component renderItem(ItemStack item) {
        Component itemComponent;

        boolean unsupportedCategories = UNSUPPORTED_CATEGORIES.stream().anyMatch(item.getType().name()::contains);
        if ((UNSUPPORTED_SPRITES.contains(item.getType()) || unsupportedCategories) && !NON_UNSUPPORTED.contains(item.getType())) {
            itemComponent = renderUnsupportedItem(item);
        } else {
            String displayMode = plugin.getConfig()
                    .getString("item.display-mode", "both")
                    .toLowerCase();

            switch (displayMode) {
                case "sprite-only":
                    itemComponent = renderSpriteOnly(item);
                    break;

                case "text-only":
                    itemComponent = renderTextOnly(item);
                    break;

                case "both":
                default:
                    itemComponent = renderBoth(item);
                    break;
            }
        }

        return decorateItem(itemComponent, item);
    }

    private Component renderUnsupportedItem(ItemStack item) {
        String fallback = plugin.getConfig()
                .getString("item.unsupported-item-fallback", "text")
                .toLowerCase();

        switch (fallback) {
            case "none":
                return Component.empty();

            case "text":
            default:
                return renderTextOnly(item);
        }
    }

    private Component decorateItem(Component component, ItemStack item) {
        // Add hover info if enabled
        if (plugin.getConfig().getBoolean("item.show-hover-info", true)) {
            component = component.hoverEvent(item.asHoverEvent());
        }

        // Wrap in brackets if enabled
        if (plugin.getConfig().getBoolean("item.wrap-in-brackets", true)) {
            component = Component.text("[")
                    .append(component)
                    .append(Component.text("]"));
        }

        return component;
    }

    private Component renderSpriteOnly(ItemStack item) {
        String spriteKey = getSpriteKey(item);
        String minimessageString = "<sprite:" + spriteKey + ">";
        return miniMessage.deserialize(minimessageString);
    }

    private Component renderTextOnly(ItemStack item) {
        String textFormat = plugin.getConfig().getString("item.text-format", "{name} x{amount}");
        Component nameComponent = getTranslatableItemName(item);
        String amount = String.valueOf(item.getAmount());

        // Replace placeholders
        String formatted = textFormat
                .replace("{amount}", amount);

        // Build component with translatable name in the middle
        Component result = Component.empty();
        String[] nameParts = formatted.split("\\{name\\}", -1);

        for (int i = 0; i < nameParts.length; i++) {
            if (!nameParts[i].isEmpty()) {
                result = result.append(Component.text(nameParts[i]));
            }
            if (i < nameParts.length - 1) {
                result = result.append(nameComponent);
            }
        }

        return result;
    }

    private Component renderBoth(ItemStack item) {
        String spriteKey = getSpriteKey(item);
        String minimessageString = "<sprite:" + spriteKey + ">";
        Component sprite = miniMessage.deserialize(minimessageString);

        Component text = renderTextOnly(item);

        return sprite.append(Component.text(" ")).append(text);
    }

    private Material normalize(Material material) {
        String name = material.name();

        // Remove 'WAXED_', so waxed blocks made of COPPER could get a sprite from their unwaxed form
        if (name.startsWith("WAXED_")) {
            name = name.substring(6);
        }

        if (name.startsWith("INFESTED_")) {
            name = name.substring(9);
        }

        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return material;
        }
    }

    private String getSpriteKey(ItemStack item) {
        Material material = item.getType();
        String materialName = material.toString().toLowerCase();

        String iaKey = itemsAdderRenderer.getSpriteKey(item);
        boolean itemsAdderEnabled = plugin.itemsAdderEnabled;
        if (iaKey != null && itemsAdderEnabled) {
            return iaKey;
        }

        // Vanilla fallback
        material = normalize(material);

        if (material.isBlock()) {
            return BlockResolver.resolveBlockSprite(material);
        }

        return "items:item/" + materialName;
    }

    private Component getTranslatableItemName(ItemStack item) {
        Material material = item.getType();

        if (item.hasItemMeta()) {
            // Check item_name component
            if (item.getItemMeta().hasItemName()) {
                return item.getItemMeta().itemName();
            }

            // Check item's custom display name
            if (item.getItemMeta().hasDisplayName()) {
                return item.getItemMeta().displayName();
            }
        }

        // localization support
        if (material.isBlock()) {
            String translationKey = "block.minecraft." + item.getType().toString().toLowerCase();
            return Component.translatable(translationKey);
        } else {
            String translationKey = "item.minecraft." + item.getType().toString().toLowerCase();
            return Component.translatable(translationKey);
        }
    }

    private Component renderEmptyHand() {
        String format = plugin.getConfig().getString("item.empty-hand-format", "[Empty Hand]");
        return Component.text(format);
    }
}
