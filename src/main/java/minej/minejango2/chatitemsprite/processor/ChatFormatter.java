package minej.minejango2.chatitemsprite.processor;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

public final class ChatFormatter {

    private final ChatItemSpritePlugin plugin;

    private final MiniMessage mm = MiniMessage.miniMessage();

    private final LegacyComponentSerializer legacy =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .build();

    public ChatFormatter(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
    }

    public Component format(Player player, Component message) {
        String format = plugin.getConfig().getString(
                "chat-format",
                "{prefix} {player}: {message}"
        );

        format = format
                .replace("{prefix}", "<prefix>")
                .replace("{player}", "<player>")
                .replace("{message}", "<message>");

        return mm.deserialize(
                format, TagResolver.resolver(
                        Placeholder.component("prefix", parse(getPrefix(player))),
                        Placeholder.component("player", player.displayName()),
                        Placeholder.component("message", message)
                )
        );
    }

    private String getPrefix(Player player) {
        try {
            CachedMetaData meta = LuckPermsProvider.get()
                    .getPlayerAdapter(Player.class)
                    .getMetaData(player);
            return meta.getPrefix();
        } catch (Exception e) {
            return "";
        }
    }

    private Component parse(String value) {
        if (value == null || value.isEmpty()) {
            return Component.empty();
        }

        boolean legacyMode = value.indexOf('&') != -1
                || value.indexOf('§') != -1;

        if (legacyMode) {
            return legacy.deserialize(value);
        }

        return mm.deserialize(value);
    }
}
