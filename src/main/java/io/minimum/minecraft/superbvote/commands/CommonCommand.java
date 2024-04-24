package io.minimum.minecraft.superbvote.commands;

import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.configuration.message.MessageContext;
import io.minimum.minecraft.superbvote.configuration.message.VoteMessage;
import io.minimum.minecraft.superbvote.storage.VoteStorage;
import io.minimum.minecraft.superbvote.util.BrokenNag;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CommonCommand implements CommandExecutor {
    private final SuperbVote plugin;
    private final VoteMessage message;
    private final boolean streakRelated;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players."));
            return true;
        }

        if (plugin.getConfiguration().isConfigurationError()) {
            BrokenNag.nag(player);
            return true;
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            VoteStorage voteStorage = plugin.getVoteStorage();
            MessageContext ctx = new MessageContext(null, voteStorage.getVotes(player.getUniqueId()), voteStorage.getVoteStreakIfSupported(player.getUniqueId(), streakRelated), player);
            message.sendAsReminder(player, ctx);
        });
        return true;
    }
}
