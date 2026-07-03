package minej.minejango2.chatitemsprite.processor;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.ItemRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public final class MessageProcessor {

    private final ChatItemSpritePlugin plugin;
    private final ItemRenderer itemRenderer;

    public MessageProcessor(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.itemRenderer = new ItemRenderer(plugin);
    }

    /**
     * Process the player's chat message, replacing [item] tags.
     *
     * @param player The player who sent the message.
     * @param message The original message.
     * @return The processed message component.
     */
    public Component process(Player player, Component message) {
        if (!plugin.getConfig().getBoolean("enabled", true)) {
            return message;
        }

        String plainText = PlainTextComponentSerializer.plainText().serialize(message);

        if (!plainText.contains("[item]")) {
            return message;
        }

        Component result = Component.empty();
        boolean replaceAll = plugin.getConfig().getBoolean("replace-all", true);
        String[] parts = plainText.split("\\[item\\]", replaceAll ? -1 : 2);
        
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result = result.append(Component.text(parts[i]));
            }
            
            if (i < parts.length - 1) {
                result = result.append(itemRenderer.render(player));
            }
        }

        return result;
    }
}

