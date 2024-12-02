package io.minimum.minecraft.superbvote.votes.rewards;

import io.minimum.minecraft.superbvote.configuration.SuperbVoteConfiguration;
import io.minimum.minecraft.superbvote.configuration.message.MessageContext;
import io.minimum.minecraft.superbvote.configuration.message.VoteMessage;
import io.minimum.minecraft.superbvote.votes.Vote;
import io.minimum.minecraft.superbvote.votes.rewards.matchers.RewardMatcher;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public class VoteReward {
    private final String serviceName;
    private final List<RewardMatcher> rewardMatchers;
    private final List<String> commands;
    private final VoteMessage playerMessage;
    private final VoteMessage broadcastMessage;
    private final boolean cascade;

    public void broadcastVote(MessageContext context, boolean playerAnnounce, boolean broadcast) {
        if (playerMessage != null && playerAnnounce) {
            context.getPlayer().map(OfflinePlayer::getPlayer).ifPresent(votingPlayer -> playerMessage.sendAsBroadcast(votingPlayer, context));
        }

        if (broadcastMessage != null && broadcast) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("superbvote.notify")) {
                    broadcastMessage.sendAsBroadcast(player, context);
                }
            }
        }
    }

    public void runCommands(Vote vote) {
        for (String command : commands) {
            String fixed = SuperbVoteConfiguration.replaceCommandPlaceholders(command, vote);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fixed);
        }
    }
}
