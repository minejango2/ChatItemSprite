package minej.minejango2.chatitemsprite.renderer.custom;

import org.bukkit.inventory.ItemStack;
import com.nexomc.nexo.api.NexoItems;

public final class NexoRenderer {

    public String getNexoResult(ItemStack item) {
        if (!NexoItems.exists(item)) {
            return null;
        }

        return NexoItems.idFromItem(item);
    }
}
