package minej.minejango2.chatitemsprite.minimessage;

import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MiniMessageProvider {

    public static final MiniMessage miniMessage = MiniMessage.builder()
            .editTags(tags -> tags.resolver(CustomMiniMessageTags.head64))
            .build();

    private MiniMessageProvider() {}
}
