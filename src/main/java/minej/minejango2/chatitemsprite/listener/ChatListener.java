package minej.minejango2.chatitemsprite.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.processor.MessageProcessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {

    private final MessageProcessor messageProcessor;

    public ChatListener(ChatItemSpritePlugin plugin) {
        this.messageProcessor = new MessageProcessor(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        event.message(
                messageProcessor.process(
                        event.getPlayer(),
                        event.message()
                )
        );
    }
}
