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

import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

public final class HeadResolver {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private HeadResolver() {}

    public static Component createHeadComponent(Consumer<PlayerHeadObjectContents.Builder> consumer) {
        PlayerHeadObjectContents.Builder builder = ObjectContents.playerHead();
        consumer.accept(builder);
        return Component.object(builder.build());
    }

    public static Component resolveHead64(String base64) {
        // check base64 value
        try {
            Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return miniMessage.deserialize("<head:entity/player/wide/steve>");
        }
        return createHeadComponent(builder ->
                builder.profileProperty(
                        PlayerHeadObjectContents.property(
                                "textures",
                                base64
                        )
                )
        );
    }

    @Nullable
    public static Component resolveHeadComponent(ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta skullMeta)) {
            return null;
        }

        PlayerProfile profile = skullMeta.getPlayerProfile();
        if (profile == null) {
            return null;
        }

        // 1. Base64
        ProfileProperty texture = profile.getProperties().stream()
                .filter(p -> "textures".equals(p.getName()))
                .findFirst()
                .orElse(null);

        if (texture != null) {
            return createHeadComponent(builder ->
                    builder.profileProperties(
                            profile.getProperties().stream()
                                    .map(p -> PlayerHeadObjectContents.property(
                                            p.getName(),
                                            p.getValue(),
                                            p.getSignature()
                                    ))
                                    .toList()
                    )
            );
        }

        UUID uuid = profile.getId();
        String name = profile.getName();

        // 2. UUID + name
        if (uuid != null && name != null && !name.isBlank()) {
            return miniMessage.deserialize("<head:entity/player/wide/ari>");
        }

        // 3. name only
        if (name != null && !name.isBlank()) {
            return miniMessage.deserialize("<head:" + name + ">");
        }

        // 4. UUID only
        if (uuid != null) {
            return miniMessage.deserialize("<head:" + uuid + ">");
        }

        return null;
    }
}
