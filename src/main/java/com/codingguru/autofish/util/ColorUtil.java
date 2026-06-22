package com.codingguru.autofish.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class ColorUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ColorUtil() {

    }

    /**
     * Parses a MiniMessage-formatted string into an Adventure {@link Component}. A
     * null input yields an empty component.
     */
    public static Component format(String message) {

        return MINI_MESSAGE.deserialize(message == null ? "" : message);

    }

}
