package minej.minejango2.chatitemsprite.renderer.custom;

import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;

public final class CraftEngineRenderer {

    public String getCraftEngineResult(ItemStack item) {

        boolean isCustomItem = CraftEngineItems.isCustomItem(item);
        if (!isCustomItem)
            return null;

        return CraftEngineItems.getCustomItemId(item).toString();
    }
}
