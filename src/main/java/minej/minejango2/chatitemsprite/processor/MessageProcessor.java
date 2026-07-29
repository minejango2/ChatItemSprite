package minej.minejango2.chatitemsprite.processor;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.ItemRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MessageProcessor {

    private final ChatItemSpritePlugin plugin;
    private final ItemRenderer itemRenderer;

    public MessageProcessor(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.itemRenderer = new ItemRenderer(plugin);
    }

    public Component process(Player player, Component message) {
        if (!plugin.getConfig().getBoolean("enabled", true) || !player.hasPermission("chatitemsprite.use")) {
            return message;
        }

        List<String> keywords = plugin.getConfig().getStringList("item.keywords");
        if (keywords.isEmpty()) {
            keywords = List.of(
                    "[item]",
                    "[i]"
            );
        }

        List<String> prefixes = new java.util.ArrayList<>();
        for (String keyword : keywords) {
            if (!keyword.endsWith("]")) {
                plugin.getLogger().warning("Ignoring invalid item.keywords entry '" + keyword
                        + "': must end with ']'.");
                continue;
            }
            prefixes.add(keyword.substring(0, keyword.length() - 1));
        }

        if (prefixes.isEmpty()) {
            return message;
        }

        boolean replaceAll = plugin.getConfig().getBoolean("replace-all", true);
        boolean[] alreadyReplaced = {false};

        return replace(player, message, prefixes, replaceAll, alreadyReplaced);
    }

    private Component replace(Player player, Component component, List<String> prefixes, boolean replaceAll, boolean[] alreadyReplaced) {
        Component rebuilt;

        if (component instanceof TextComponent text) {
            rebuilt = text.content("").children(List.of());
            rebuilt = rebuilt.append(replaceText(player, text.content(), prefixes, replaceAll, alreadyReplaced));
        } else {
            rebuilt = component.children(List.of());
        }

        for (Component child : component.children()) {
            rebuilt = rebuilt.append(replace(player, child, prefixes, replaceAll, alreadyReplaced));
        }

        return rebuilt;
    }

    private record Match(int start, int end, EquipmentSlot slot) {}

    @Nullable
    private Match findNextMatch(String text, List<String> prefixes, int fromIndex) {
        Match best = null;

        for (String prefix : prefixes) {
            int idx = text.indexOf(prefix, fromIndex);
            while (idx != -1) {
                Match m = tryParseAt(text, prefix, idx);
                if (m != null && (best == null || m.start() < best.start())) {
                    best = m;
                    break;
                }
                if (m != null) break;
                idx = text.indexOf(prefix, idx + 1);
            }
        }
        return best;
    }

    @Nullable
    private Match tryParseAt(String text, String prefix, int start) {
        int afterPrefix = start + prefix.length();
        if (afterPrefix >= text.length()) return null;

        char next = text.charAt(afterPrefix);
        if (next == ']') {
            return new Match(start, afterPrefix + 1, EquipmentSlot.HAND);
        }

        if (next == ':') {
            int close = text.indexOf(']', afterPrefix + 1);
            if (close == -1) return null;

            String slotName = text.substring(afterPrefix + 1, close);
            EquipmentSlot slot = plugin.getSlotKeywordsManager().resolve(slotName);
            return new Match(start, close + 1, slot != null ? slot : EquipmentSlot.HAND);
        }

        return null;
    }

    private Component replaceText(Player player, String text, List<String> prefixes, boolean replaceAll, boolean[] alreadyReplaced) {
        if (!replaceAll && alreadyReplaced[0]) {
            return Component.text(text);
        }

        Component result = Component.empty();
        int index = 0;

        while (true) {
            Match match = findNextMatch(text, prefixes, index);

            if (match == null) {
                result = result.append(Component.text(text.substring(index)));
                break;
            }

            if (match.start() > index) {
                result = result.append(Component.text(text.substring(index, match.start())));
            }

            result = result.append(itemRenderer.render(player, match.slot()));
            alreadyReplaced[0] = true;
            index = match.end();

            if (!replaceAll) {
                result = result.append(Component.text(text.substring(index)));
                break;
            }
        }

        return result;
    }
}
