package io.minimum.minecraft.superbvote.configuration.message;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

public class JsonTextMessage extends MessageBase implements VoteMessage {
    private final String message;

    public JsonTextMessage(String message) {
        this.message = message;
    }

    @Override
    public void sendAsBroadcast(Player player, MessageContext context) {
        String jsonString = replace(message, context);

        // TODO: really json?
        player.sendMessage(GsonComponentSerializer.gson().deserialize(jsonString));
    }

    @Override
    public void sendAsReminder(Player player, MessageContext context) {
        String jsonString = replace(message, context);

        player.sendMessage(GsonComponentSerializer.gson().deserialize(jsonString));

    }
}
