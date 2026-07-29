package minej.minejango2.chatitemsprite.renderer.custom;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ItemsAdderRenderer {

    public String getItemsAdderResult(ItemStack item) {
        CustomStack customStack = CustomStack.byItemStack(item);

        if (customStack == null) {
            return null;
        }

        return customStack.getNamespacedID();
    }

    public String getFallbackKey(ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);

        if (stack == null) {
            return "chatitemsprite-this-item-is-vanilla";
        }

        List<String> textures = stack.getTextures();

        if (textures == null || textures.isEmpty()) {
            return null;
        }

        String firstTexture = textures.getFirst();

        // ItemsAdder only registers textures inside subfolders to the sprite atlas.
        if (!firstTexture.contains("/")) {
            return null;
        }

        //plugin.getLogger().info("[ItemsAdderRenderer] textures = " + textures);

        String texture = firstTexture.replaceFirst("(?i)\\.(png|gif)$", "");
        String result;

        if (stack.isBlock()) {
            result = "blocks:\"" + texture + "\"";
        } else {
            result = "items:\"" + texture + "\"";
        }

        return result;
    }
}
