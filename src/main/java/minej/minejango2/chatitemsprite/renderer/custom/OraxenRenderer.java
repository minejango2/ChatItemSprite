package minej.minejango2.chatitemsprite.renderer.custom;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;

public final class OraxenRenderer {

    public String getOraxenResult(ItemStack item) {
        String id = OraxenItems.getIdByItem(item);
        if (id == null) {
            return null;
        }
        return id;
    }
}
