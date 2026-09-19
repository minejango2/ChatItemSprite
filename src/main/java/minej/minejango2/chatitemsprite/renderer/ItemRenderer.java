package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.minimessage.MiniMessageProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ItemRenderer {

    private final ChatItemSpritePlugin plugin;
    private final SpriteLookupResolver spriteLookupResolver;
    private final ItemFormatResolver formatResolver;

    public ItemRenderer(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        CustomItemIdentifier customItemIdentifier = new CustomItemIdentifier(plugin);
        this.spriteLookupResolver = new SpriteLookupResolver(plugin, customItemIdentifier);
        this.formatResolver = new ItemFormatResolver(plugin);
    }

    public Component render(Player player, EquipmentSlot slot) {
        ItemStack item = player.getInventory().getItem(slot);

        if (item.getType() == Material.AIR || item.getAmount() <= 0) {
            return renderEmptySlot(slot);
        }

        return renderItem(item);
    }

    private Component renderItem(ItemStack item) {
        String displayMode = plugin.getConfig().getString("item.display-mode", "both").toLowerCase();

        if (!ItemFormatResolver.VALID_DISPLAY_MODES.contains(displayMode)) {
            plugin.getLogger().warning("Unknown config item.display-mode '" + displayMode + "', falling back to 'both'.");
            displayMode = "both";
        }

        SpriteLookupResolver.Result lookup = spriteLookupResolver.resolve(item);
        Component sprite = lookup.sprite();
        String formatMode = formatResolver.resolveFormatMode(displayMode, sprite == null, lookup.isCustomItem());

        String format = plugin.getConfig().getString("text-format." + formatMode, formatResolver.defaultFormatFor(formatMode));

        Component component = MiniMessageProvider.miniMessage.deserialize(
                format,
                Placeholder.component("cis_sprite", sprite == null ? Component.empty() : sprite),
                Placeholder.component("cis_name", item.effectiveName()),
                Placeholder.component("cis_amount_tag", renderAmountTag(item.getAmount(), format))
        );

        return decorateItem(component, item);
    }

    private Component renderAmountTag(int amount, String format) {
        if (!format.contains("<cis_amount_tag>")) {
            return Component.empty();
        }

        boolean hideWhenOne = plugin.getConfig().getBoolean("item.hide-amount-when-one", false);
        if (hideWhenOne && amount == 1) {
            return Component.empty();
        }

        String template = plugin.getConfig().getString("amount-tag-format", "<cis_amount_number>x ");
        String amountStr = String.valueOf(amount);

        return MiniMessageProvider.miniMessage.deserialize(
                template,
                Placeholder.unparsed("cis_amount_number", amountStr)
        );
    }

    private Component decorateItem(Component component, ItemStack item) {
        if (plugin.getConfig().getBoolean("item.show-hover-info", true)) {
            component = component.hoverEvent(item.asHoverEvent());
        }
        return component;
    }

    private Component renderEmptySlot(EquipmentSlot slot) {
        String slotKey = slotConfigKey(slot);
        String path = "text-format.empty-format." + slotKey;

        String format = plugin.getConfig().getString(path);
        if (format == null) {
            format = plugin.getConfig().getString("text-format.empty-format.hand", "[Empty Hand]");
        }

        return MiniMessageProvider.miniMessage.deserialize(format);
    }

    private String slotConfigKey(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> "hand";
            case OFF_HAND -> "offhand";
            case HEAD -> "head";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "feet";
            default -> "hand";
        };
    }
}
