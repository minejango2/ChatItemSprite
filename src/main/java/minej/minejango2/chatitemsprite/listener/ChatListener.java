package minej.minejango2.chatitemsprite.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.processor.ChatFormatter;
import minej.minejango2.chatitemsprite.processor.MessageProcessor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {
    private final ChatItemSpritePlugin plugin;
    private final MessageProcessor messageProcessor;
    private final ChatFormatter chatFormatter;

    public ChatListener(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.messageProcessor = new MessageProcessor(plugin);
        this.chatFormatter = new ChatFormatter(plugin);
    }

    public void register() {
        EventPriority priority = resolvePriority();

        plugin.getServer().getPluginManager().registerEvent(
                AsyncChatEvent.class,
                this,
                priority,
                (listener, event) -> onAsyncChat((AsyncChatEvent) event),
                plugin,
                true // ignoreCancelled
        );
    }

    private EventPriority resolvePriority() {
        String value = plugin.getConfig().getString("item.chat-event-priority", "HIGHEST").toUpperCase();
        try {
            return EventPriority.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown item.chat-event-priority '" + value + "', falling back to HIGHEST. Valid values: LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR.");
            return EventPriority.HIGHEST;
        }
    }

    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component processed = messageProcessor.process(player, event.message());
        boolean useLuckPermsFormatting = plugin.getConfig().getBoolean("luckperms-formatting", false) && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms");

        if (useLuckPermsFormatting) {
            event.renderer((source, displayName, message, viewer) -> chatFormatter.format(source, processed));
        } else {
            event.message(processed);
        }
    }
}
