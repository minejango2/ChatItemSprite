package minej.minejango2.chatitemsprite.processor;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern TOKEN = Pattern.compile("\\{(prefix|player|message)\\}");

    public Component format(Player player, Component message) {
        String format = plugin.getConfig().getString(
                "chat-format",
                "{prefix} {player}: {message}"
        );

        Component prefix = parse(getPrefix(player));
        Component name = player.displayName();

        Component result = Component.empty();
        Matcher matcher = TOKEN.matcher(format);
        int last = 0;

        while (matcher.find()) {
            if (matcher.start() > last) {
                result = result.append(Component.text(format.substring(last, matcher.start())));
            }

            result = switch (matcher.group(1)) {
                case "prefix"  -> result.append(prefix);
                case "player"  -> result.append(name);
                case "message" -> result.append(message);
                default        -> result;
            };

            last = matcher.end();
        }

        if (last < format.length()) {
            result = result.append(Component.text(format.substring(last)));
        }

        return result;
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
