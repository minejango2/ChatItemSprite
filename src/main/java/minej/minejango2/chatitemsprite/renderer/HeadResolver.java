package minej.minejango2.chatitemsprite.renderer;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.CompoundElement;

public final class HeadResolver {

    private HeadResolver() {}

    @Nullable
    public static Component resolveHeadComponent(ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta skullMeta)) {
            return null;
        }

        PlayerProfile profile = skullMeta.getPlayerProfile();
        if (profile == null) {
            return null;
        }

        // Prefer the embedded texture if present.
        ProfileProperty texture = profile.getProperties().stream()
                .filter(p -> "textures".equals(p.getName()))
                .findFirst()
                .orElse(null);

        if (texture != null && texture.getValue() != null) {
            PlayerHeadObjectContents contents = ObjectContents.playerHead()
                    .profileProperties(
                            profile.getProperties().stream()
                                    .map(p -> PlayerHeadObjectContents.property(
                                            p.getName(),
                                            p.getValue(),
                                            p.getSignature()
                                    ))
                                    .toList()
                    )
                    .build();

            return Component.object(contents);
        }

        // Fallback to the player's name if no texture is stored.
        String name = profile.getName();
        if (name != null && !name.isBlank()) {
            return MiniMessage.miniMessage().deserialize("<head:" + name + ">");
        }

        return null;
    }

    @Nullable
    public static Component resolveMobHeadComponent(ItemStack item) {
        return switch (item.getType()) {
            case ZOMBIE_HEAD -> MiniMessage.miniMessage().deserialize("<head:entity/zombie/zombie>");
            /* fix mojang paper
            case CREEPER_HEAD -> MiniMessage.miniMessage().deserialize("<head:creeper>");
            case DRAGON_HEAD -> MiniMessage.miniMessage().deserialize("<head:ender_dragon>");
            case PIGLIN_HEAD -> MiniMessage.miniMessage().deserialize("<head:piglin>");
            case SKELETON_SKULL -> MiniMessage.miniMessage().deserialize("<head:skeleton>");
            case WITHER_SKELETON_SKULL -> MiniMessage.miniMessage().deserialize("<head:wither_skeleton>");*/
            default -> null;
        };
    }
}