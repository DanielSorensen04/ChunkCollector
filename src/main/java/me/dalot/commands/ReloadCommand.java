package me.dalot.commands;

import me.dalot.ChunkCollector;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand extends SubCommand {

    @Override
    public String getName() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Reload.name"));
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getDescription() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Reload.description"));
    }

    @Override
    public String getSyntax() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Reload.syntax"));
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (sender instanceof Player p) {
            if (p.hasPermission("chunkcollector.admin") || p.hasPermission("chunkcollector.reload")) {

                ChunkCollector.getPlugin().reloadConfig();

                p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.reloaded")));

            } else {
                p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.no-permission")));
            }
        } else {
            sender.sendMessage("Du skal være en spiller for at bruge denne komando.");
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
