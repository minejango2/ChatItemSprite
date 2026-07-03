package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class ItemRenderer {

    private static final Set<Material> UNSUPPORTED_SPRITES = EnumSet.of(
            Material.PLAYER_HEAD,
            Material.SHIELD,
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW
    );

    private final ChatItemSpritePlugin plugin;
    private final MiniMessage miniMessage;
    private final ItemsAdderRenderer itemsAdderRenderer;

    public ItemRenderer(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.itemsAdderRenderer = new ItemsAdderRenderer();
    }

    /**
     * Creates the chat component for the player's held item.
     *
     * @param player The player.
     * @return Item component.
     */
    public Component render(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR || item.getAmount() <= 0) {
            return renderEmptyHand();
        }

        return renderItem(item);
    }

    @SuppressWarnings("deprecation")
    private Component renderItem(ItemStack item) {
        Component itemComponent;

        if (UNSUPPORTED_SPRITES.contains(item.getType())) {
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

    private String getSpriteKey(ItemStack item) {
        Material material = item.getType();
        String materialName = material.toString().toLowerCase();

        String iaKey = itemsAdderRenderer.getSpriteKey(item);
        boolean itemsAdderEnabled = plugin.itemsAdderEnabled;
        if (iaKey != null && itemsAdderEnabled) {
            return iaKey;
        }

        // Vanilla fallback
        if (material.isBlock()) {
            return "blocks:block/" + materialName;
        }

        return "items:item/" + materialName;
    }

    private Component getTranslatableItemName(ItemStack item) {
        Material material = item.getType();
        // Check if item has custom display name
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().displayName();
        }

        // Use Minecraft's translation key for the item
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
