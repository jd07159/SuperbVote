package io.minimum.minecraft.superbvote.commands;

import io.minimum.minecraft.superbvote.SuperbVote;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class VotePartyCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("setcounter", "addvotes", "startparty");
    private final SuperbVote plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || !sender.hasPermission("superbvote.voteparty." + args[0].toLowerCase(Locale.ROOT))) {
            showVotesNeeded(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "setcounter": {
                if (args.length < 2) {
                    help(sender);
                    return true;
                }

                plugin.getVoteParty().setCurrentVotes(plugin.getVoteParty().votesNeeded() - Integer.parseInt(args[1]));
                break;
            }
            case "addvotes": {
                if (args.length < 2) {
                    help(sender);
                    return true;
                }

                plugin.getVoteParty().setCurrentVotes(plugin.getVoteParty().getCurrentVotes() + Integer.parseInt(args[1]));
                break;
            }
            case "startparty": {
                plugin.getVoteParty().startVoteParty();
                break;
            }
            default: {
                help(sender);
            }
        }

        return true;
    }

    private void help(CommandSender sender) {
        sender.sendRichMessage("<green>/vp setcounter <votesNeeded> <gray>- <white>Set the vote party counter");
        sender.sendRichMessage("<green>/vp addvotes <votes> <gray>- <white>Add votes to the counter");
        sender.sendRichMessage("<green>/vp startparty <gray>- <white>Start a vote party");
    }

    private void showVotesNeeded(CommandSender sender) {
        sender.sendRichMessage("<#dddddd>[<#ff55a3>RAGESMP</#ff55a3>]</#dddddd> 추천 이벤트까지 남은 추천 수: <#ff55a3>" + (plugin.getVoteParty().votesNeeded() - plugin.getVoteParty().getCurrentVotes()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0, 1 -> {
                return SUBCOMMANDS.stream().filter(subcommand -> sender.hasPermission("superbvote.voteparty." + subcommand)).filter(subcommand -> args.length == 0 || subcommand.regionMatches(true, 0, args[0], 0, args[0].length())).toList();
            }
        }

        return List.of();
    }
}
