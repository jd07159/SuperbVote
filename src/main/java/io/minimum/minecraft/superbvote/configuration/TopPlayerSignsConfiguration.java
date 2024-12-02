package io.minimum.minecraft.superbvote.configuration;

import io.minimum.minecraft.superbvote.configuration.message.PlainStringMessage;
import lombok.Value;

import java.util.List;

@Value
public class TopPlayerSignsConfiguration {
    boolean enabled;
    private final List<PlainStringMessage> signText;
}
