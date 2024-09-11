package io.minimum.minecraft.superbvote.votes.rewards;

import io.minimum.minecraft.superbvote.configuration.SuperbVoteConfiguration;
import io.minimum.minecraft.superbvote.configuration.message.MessageContext;
import io.minimum.minecraft.superbvote.configuration.message.VoteMessage;
import io.minimum.minecraft.superbvote.votes.Vote;
import io.minimum.minecraft.superbvote.votes.rewards.matchers.RewardMatcher;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@Data
public class VoteReward {
    private final String serviceName;
    private final List<RewardMatcher> rewardMatchers;
    private final List<String> commands;
    private final VoteMessage playerMessage;
    private final VoteMessage broadcastMessage;
    private final boolean cascade;

    public void broadcastVote(MessageContext context, boolean playerAnnounce, boolean broadcast) {
        if (playerMessage != null && context.getPlayer().isPresent() && playerAnnounce) {
            Player votingPlayer = (Player) context.getPlayer().get();
            playerMessage.sendAsBroadcast(votingPlayer, context);
        }

        if (broadcastMessage != null && broadcast) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("superbvote.notify"))
                    .forEach(player -> broadcastMessage.sendAsBroadcast(player, context));
        }
    }

    public void runCommands(Vote vote) {
        for (String command : commands) {
            String fixed = SuperbVoteConfiguration.replaceCommandPlaceholders(command, vote);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fixed);
        }
    }
}
