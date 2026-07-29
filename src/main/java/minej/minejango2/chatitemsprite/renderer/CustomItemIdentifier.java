package minej.minejango2.chatitemsprite.renderer;

import minej.minejango2.chatitemsprite.ChatItemSpritePlugin;
import minej.minejango2.chatitemsprite.renderer.custom.CraftEngineRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.ItemsAdderRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.NexoRenderer;
import minej.minejango2.chatitemsprite.renderer.custom.OraxenRenderer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class CustomItemIdentifier {

    public record Identification(ChatItemSpritePlugin.CustomItemPlugin plugin, String rawId) {}

    private static final String VANILLA_SENTINEL = "chatitemsprite-this-item-is-vanilla";

    private final ItemsAdderRenderer itemsAdderRenderer;
    private final ChatItemSpritePlugin plugin;
    private final Map<ChatItemSpritePlugin.CustomItemPlugin, Function<ItemStack, String>> providers;

    public CustomItemIdentifier(ChatItemSpritePlugin plugin) {
        this.plugin = plugin;
        this.itemsAdderRenderer = new ItemsAdderRenderer();
        OraxenRenderer oraxenRenderer = new OraxenRenderer();
        NexoRenderer nexoRenderer = new NexoRenderer();
        CraftEngineRenderer craftEngineRenderer = new CraftEngineRenderer();

        Map<ChatItemSpritePlugin.CustomItemPlugin, Function<ItemStack, String>> map = new LinkedHashMap<>();
        map.put(ChatItemSpritePlugin.CustomItemPlugin.ITEMSADDER, itemsAdderRenderer::getItemsAdderResult);
        map.put(ChatItemSpritePlugin.CustomItemPlugin.ORAXEN, oraxenRenderer::getOraxenResult);
        map.put(ChatItemSpritePlugin.CustomItemPlugin.NEXO, nexoRenderer::getNexoResult);
        map.put(ChatItemSpritePlugin.CustomItemPlugin.CRAFTENGINE, craftEngineRenderer::getCraftEngineResult);
        this.providers = Collections.unmodifiableMap(map);
    }

    // find custom item's main plugin and it's raw ID.
    @Nullable
    public Identification identify(ItemStack item) {
        for (Map.Entry<ChatItemSpritePlugin.CustomItemPlugin, Function<ItemStack, String>> entry : providers.entrySet()) {
            if (!plugin.isPluginEnabledCustom(entry.getKey())) {
                continue;
            }
            String result = entry.getValue().apply(item);
            if (result != null) {
                return new Identification(entry.getKey(), result);
            }
        }
        return null;
    }

    // ItemsAdder texture fallback
    @Nullable
    public String resolveItemsAdderRawTexture(ItemStack item) {
        String fallbackKey = itemsAdderRenderer.getFallbackKey(item);
        if (fallbackKey == null || fallbackKey.equals(VANILLA_SENTINEL)) {
            return null;
        }
        return fallbackKey;
    }

    @Nullable
    public String getSuggestedSpriteKey(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        Identification id = identify(item);
        if (id != null) {
            return id.plugin().name().toLowerCase() + ":" + id.rawId();
        }

        return item.getType().name().toLowerCase();
    }
}
