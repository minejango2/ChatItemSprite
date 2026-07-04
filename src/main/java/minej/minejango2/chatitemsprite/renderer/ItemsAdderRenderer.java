package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ItemsAdderRenderer {

    public String getSpriteKey(ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        ChatItemSpritePlugin plugin = ChatItemSpritePlugin.getInstance();

        if (stack == null) {
            return null;
        }

        plugin.getLogger().info("[ItemsAdderRenderer] stack found:");
        plugin.getLogger().info(" - id: " + stack.getId());

        List<String> textures = stack.getTextures();

        if (textures == null) {
            plugin.getLogger().info("[ItemsAdderRenderer] textures == null");
            return null;
        }

        if (textures.isEmpty()) {
            plugin.getLogger().info("[ItemsAdderRenderer] textures is EMPTY");
            return null;
        }

        plugin.getLogger().info("[ItemsAdderRenderer] textures = " + textures);

        String texture = textures.getFirst().replaceFirst("(?i)\\.(png|gif)$", "");

        String result = null;

        if (stack.isBlock()) {
            result = "blocks:\"" + texture + "\"";
        } else {
            result = "items:\"" + texture + "\"";
        }

        return result;
    }
}