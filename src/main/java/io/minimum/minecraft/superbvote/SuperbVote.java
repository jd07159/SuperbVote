package io.minimum.minecraft.superbvote;

import io.minimum.minecraft.superbvote.commands.SuperbVoteCommand;
import io.minimum.minecraft.superbvote.commands.VotePartyCommand;
import io.minimum.minecraft.superbvote.configuration.SuperbVoteConfiguration;
import io.minimum.minecraft.superbvote.scoreboard.ScoreboardHandler;
import io.minimum.minecraft.superbvote.signboard.TopPlayerSignFetcher;
import io.minimum.minecraft.superbvote.signboard.TopPlayerSignListener;
import io.minimum.minecraft.superbvote.signboard.TopPlayerSignStorage;
import io.minimum.minecraft.superbvote.storage.QueuedVotesStorage;
import io.minimum.minecraft.superbvote.storage.RecentVotesStorage;
import io.minimum.minecraft.superbvote.storage.VoteStorage;
import io.minimum.minecraft.superbvote.util.BrokenNag;
import io.minimum.minecraft.superbvote.util.cooldowns.VoteServiceCooldown;
import io.minimum.minecraft.superbvote.votes.SuperbVoteListener;
import io.minimum.minecraft.superbvote.votes.VoteParty;
import io.minimum.minecraft.superbvote.votes.VoteReminder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SuperbVote extends JavaPlugin {
    @Getter
    private static SuperbVote plugin;
    @Getter
    private SuperbVoteConfiguration configuration;
    @Getter
    private VoteStorage voteStorage;
    @Getter
    private QueuedVotesStorage queuedVotes;
    @Getter
    private RecentVotesStorage recentVotesStorage;
    @Getter
    private ScoreboardHandler scoreboardHandler;
    @Getter
    private VoteServiceCooldown voteServiceCooldown;
    @Getter
    private TopPlayerSignStorage topPlayerSignStorage;
    private ScheduledTask voteReminderTask;
    @Getter
    private final boolean foliaDetected = isFolia();
    @Getter
    private final VoteParty voteParty = new VoteParty(this);

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        configuration = new SuperbVoteConfiguration(getConfig());

        if (configuration.isConfigurationError()) {
            BrokenNag.nag(getServer().getConsoleSender());
        }

        try {
            voteStorage = configuration.initializeVoteStorage();
        } catch (Exception e) {
            throw new RuntimeException("Exception whilst initializing vote storage", e);
        }

        try {
            queuedVotes = new QueuedVotesStorage(new File(getDataFolder(), "queued_votes.json"));
        } catch (IOException e) {
            throw new RuntimeException("Exception whilst initializing queued vote storage", e);
        }

        recentVotesStorage = new RecentVotesStorage();

        scoreboardHandler = new ScoreboardHandler(this);
        voteServiceCooldown = new VoteServiceCooldown(getConfig().getInt("votes.cooldown-per-service", 3600));

        topPlayerSignStorage = new TopPlayerSignStorage();
        try {
            topPlayerSignStorage.load(new File(getDataFolder(), "top_voter_signs.json"));
        } catch (IOException e) {
            throw new RuntimeException("Exception whilst loading top player signs", e);
        }
        voteParty.load();

        Objects.requireNonNull(getCommand("superbvote")).setExecutor(new SuperbVoteCommand(this));
        Objects.requireNonNull(getCommand("vote")).setExecutor(configuration.getVoteCommand());
        Objects.requireNonNull(getCommand("votestreak")).setExecutor(configuration.getVoteStreakCommand());
        Objects.requireNonNull(getCommand("voteparty")).setExecutor(new VotePartyCommand(this));

        getServer().getPluginManager().registerEvents(new SuperbVoteListener(this), this);
        getServer().getPluginManager().registerEvents(new TopPlayerSignListener(this), this);

        getServer().getAsyncScheduler().runAtFixedRate(this, task -> voteStorage.save(), 1, 30, TimeUnit.SECONDS);
        getServer().getAsyncScheduler().runAtFixedRate(this, task -> queuedVotes.save(), 1, 30, TimeUnit.SECONDS);
        getServer().getAsyncScheduler().runNow(this, task -> scoreboardHandler.doPopulate());
        getServer().getAsyncScheduler().runNow(this, task -> new TopPlayerSignFetcher(topPlayerSignStorage.getSignList()).run());

        int r = getConfig().getInt("vote-reminder.repeat");
        String text = getConfig().getString("vote-reminder.message");
        if (text != null && !text.isEmpty()) {
            if (r > 0) {
                final VoteReminder reminder = new VoteReminder();
                voteReminderTask = getServer().getAsyncScheduler().runAtFixedRate(this, task -> reminder.run(), r, r, TimeUnit.SECONDS);
            }
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Using clip's PlaceholderAPI to provide extra placeholders.");
        }

        // Disable update checking
        /*
        SpigotUpdater updater = new SpigotUpdater();
        getServer().getScheduler().runTaskAsynchronously(this, updater);
        getServer().getPluginManager().registerEvents(updater, this);
        */
    }

    @Override
    public void onDisable() {
        if (voteReminderTask != null) {
            voteReminderTask.cancel();
            voteReminderTask = null;
        }
        voteStorage.save();
        queuedVotes.save();
        voteStorage.close();
        try {
            topPlayerSignStorage.save(new File(getDataFolder(), "top_voter_signs.json"));
        } catch (IOException e) {
            throw new RuntimeException("Exception whilst saving top player signs", e);
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        configuration = new SuperbVoteConfiguration(getConfig());
        scoreboardHandler.reload();
        voteServiceCooldown = new VoteServiceCooldown(getConfig().getInt("votes.cooldown-per-service", 3600));
        getServer().getAsyncScheduler().runNow(this, task -> scoreboardHandler.doPopulate());

        Objects.requireNonNull(getCommand("vote")).setExecutor(configuration.getVoteCommand());
        Objects.requireNonNull(getCommand("votestreak")).setExecutor(configuration.getVoteStreakCommand());

        if (voteReminderTask != null) {
            voteReminderTask.cancel();
            voteReminderTask = null;
        }
        int r = getConfig().getInt("vote-reminder.repeat");
        String text = getConfig().getString("vote-reminder.message");
        if (text != null && !text.isEmpty() && r > 0) {
            voteReminderTask = getServer().getAsyncScheduler().runAtFixedRate(this, task -> new VoteReminder().run(), r, r, TimeUnit.SECONDS);
        }
    }

    public ClassLoader _exposeClassLoader() {
        return getClassLoader();
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
