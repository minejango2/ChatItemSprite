package minej.minejango2.chatitemsprite.compat;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.InteractiveChatConfigReloadEvent;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class InteractiveChatCompat implements Listener {

    private final ChatItemSpritePlugin plugin;

    private InteractiveChatCompat(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    public static void registerIfApplicable(ChatItemSpritePlugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("InteractiveChat")) {
            return;
        }

        if (!plugin.getConfig().getBoolean("compatibility.interactivechat.disable-native-item", true)) {
            plugin.getLogger().info("InteractiveChat detected, but compatibility.interactivechat.disable-native-item is false");
            plugin.getLogger().info("Both plugins may race for [item] depending on event priority.");
            return;
        }

        InteractiveChatCompat compat = new InteractiveChatCompat(plugin);
        compat.disableNativeItemDisplay();
        Bukkit.getPluginManager().registerEvents(compat, plugin);
    }

    private void disableNativeItemDisplay() {
        InteractiveChat.useItem = false;
        if (InteractiveChat.itemPlaceholder != null) {
            InteractiveChat.placeholderList.remove(InteractiveChat.itemPlaceholder.getInternalId());
        }
        plugin.getLogger().info("Detected InteractiveChat: always try disable its native [item] display (runtime only).");
    }

    @EventHandler
    public void onInteractiveChatReload(InteractiveChatConfigReloadEvent event) {
        disableNativeItemDisplay();
    }
}
