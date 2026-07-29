package minej.minejango2.chatitemsprite.minimessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import minej.minejango2.chatitemsprite.renderer.HeadResolver;

public final class CustomMiniMessageTags {

    private CustomMiniMessageTags() {}

    public static final TagResolver head64 = TagResolver.resolver("head64", (args, ctx) -> {
        String base64 = args.popOr("Missing base64 value").value();

        Component component = HeadResolver.resolveHead64(base64);

        return Tag.selfClosingInserting(component);
    });
}
