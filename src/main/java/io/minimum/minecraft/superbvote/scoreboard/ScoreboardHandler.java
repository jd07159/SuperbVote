package io.minimum.minecraft.superbvote.scoreboard;

import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.util.PlayerVotes;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardHandler {
    private final SuperbVote plugin;
    private final Scoreboard scoreboard;
    private final Objective objective;

    public ScoreboardHandler(SuperbVote plugin) {
        this.plugin = plugin;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("votes", Criteria.DUMMY, Component.text("Votes"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("None found").setScore(1);
        reload();
    }

    public void reload() {
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("leaderboard.scoreboard.title", "Top voters")));
    }

    public void doPopulate() {
        if (plugin.isFoliaDetected() || !plugin.getConfig().getString("leaderboard.display").equals("scoreboard")) {
            return;
        }
        List<PlayerVotes> leaderboardAsUuids = plugin.getVoteStorage().getTopVoters(
                Math.min(16, plugin.getConfig().getInt("leaderboard.scoreboard.max", 10)), 0);
        List<String> leaderboardAsNames = leaderboardAsUuids.stream()
                .map(ue -> Bukkit.getOfflinePlayer(ue.getUuid()).getName())
                .toList();
        if (leaderboardAsNames.isEmpty()) {
            scoreboard.getEntries().stream().filter(s -> !s.equals("None found")).forEach(scoreboard::resetScores);
            objective.getScore("None found").setScore(1);
        } else {
            scoreboard.getEntries().stream().filter(s -> !leaderboardAsNames.contains(s)).forEach(scoreboard::resetScores);
            for (int i = 0; i < leaderboardAsUuids.size(); i++) {
                PlayerVotes e = leaderboardAsUuids.get(i);
                String name = leaderboardAsNames.get(i);
                objective.getScore(name).setScore(e.getVotes());
            }
        }
    }

    public void toggle(Player player) {
        player.setScoreboard(scoreboard);
    }
}
