package minej.minejango2.chatitemsprite.config;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SlotKeywordsManager {

    private static final List<String> STANDARD_SLOTS =
            List.of("hand", "offhand", "head", "chest", "legs", "feet");

    private static final Map<String, EquipmentSlot> SLOT_TYPES = Map.of(
            "hand", EquipmentSlot.HAND,
            "offhand", EquipmentSlot.OFF_HAND,
            "head", EquipmentSlot.HEAD,
            "chest", EquipmentSlot.CHEST,
            "legs", EquipmentSlot.LEGS,
            "feet", EquipmentSlot.FEET
    );

    private static final Map<String, List<String>> DEFAULT_ALIASES = Map.of(
            "hand", List.of("hand", "mainhand", "main"),
            "offhand", List.of("offhand"),
            "head", List.of("head", "helmet"),
            "chest", List.of("chest", "chestplate"),
            "legs", List.of("legs", "leggings"),
            "feet", List.of("feet", "boots")
    );

    private final ChatItemSpritePlugin plugin;

    private Map<String, EquipmentSlot> aliasToSlot = Map.of();

    public SlotKeywordsManager(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Map<String, EquipmentSlot> result = new LinkedHashMap<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("item.slot-keywords");

        for (String slotKey : STANDARD_SLOTS) {
            EquipmentSlot equipmentSlot = SLOT_TYPES.get(slotKey);

            List<String> aliases = (section != null) ? section.getStringList(slotKey) : List.of();
            if (aliases.isEmpty()) {
                aliases = DEFAULT_ALIASES.get(slotKey);
                if (section != null && section.isSet(slotKey)) {
                    plugin.getLogger().warning("item.slot-keywords." + slotKey + " is empty or invalid; falling back to built-in aliases " + aliases);
                }
            }

            for (String alias : aliases) {
                String normalized = alias.toLowerCase(Locale.ROOT);
                EquipmentSlot existing = result.get(normalized);
                if (existing != null && existing != equipmentSlot) {
                    plugin.getLogger().warning("Slot alias '" + alias + "' is defined for both " + existing + " and " + equipmentSlot + "; keeping the first (" + existing + ").");
                    continue;
                }
                result.put(normalized, equipmentSlot);
            }
        }

        this.aliasToSlot = Map.copyOf(result);
    }

    public EquipmentSlot resolve(String alias) {
        return aliasToSlot.get(alias.toLowerCase(Locale.ROOT));
    }
}