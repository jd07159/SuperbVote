package io.minimum.minecraft.superbvote.votes;

import io.minimum.minecraft.superbvote.SuperbVote;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@RequiredArgsConstructor
public class VoteParty {
    private final SuperbVote plugin;
    @Getter
    private int currentVotes = 0;

    public void countVote() {
        currentVotes++;
        save();

        if (currentVotes >= votesNeeded()) {
            startVoteParty();
        }
    }

    public void startVoteParty() {
        currentVotes = 0;

        final Component broadcastMessage = MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("vote-party.broadcast-message", ""),
                Placeholder.unparsed("count", String.valueOf(votesNeeded())),
                Placeholder.unparsed("delay", String.valueOf(delay())));

        if (!broadcastMessage.equals(Component.empty()))
            plugin.getServer().broadcast(broadcastMessage);

        // Go to global thread to run commands
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> {
            for (final String command : plugin.getConfig().getStringList("vote-party.commands")) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            }
        }, Math.max(1, delay() * 20L));
    }

    public int votesNeeded() {
        return plugin.getConfig().getInt("vote-party.votes-needed", 5000);
    }

    private int delay() {
        return plugin.getConfig().getInt("vote-party.delay-seconds", 5);
    }

    public void setCurrentVotes(final int currentVotes) {
        this.currentVotes = currentVotes;

        if (currentVotes >= votesNeeded())
            startVoteParty();

        save();
    }

    public void load() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "votepartycount.yml"));
        currentVotes = config.getInt("count");
    }

    public void save() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            YamlConfiguration config = new YamlConfiguration();
            config.set("count", currentVotes);

            try {
                config.save(new File(plugin.getDataFolder(), "votepartycount.yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save vote party count", e);
            }
        });
    }
}
