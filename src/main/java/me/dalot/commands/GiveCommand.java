package me.dalot.commands;

import me.dalot.ChunkCollector;
import me.dalot.enums.CollectionType;
import me.dalot.managers.CollectorManager;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveCommand extends SubCommand {

    @Override
    public String getName() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Give.name"));
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getDescription() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Give.description"));
    }

    @Override
    public String getSyntax() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Commands.Give.syntax"));
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (sender instanceof Player p) {

            if (p.hasPermission("chunkcollector.admin") || p.hasPermission("chunkcollector.give")) {
                if (args.length == 2) {

                    if (args[1].equalsIgnoreCase("drop")) {

                        ItemStack collector = CollectorManager.makeCollector(p, CollectionType.DROP);
                        if (collector != null) {
                            p.getInventory().addItem(collector);
                            p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.got-collector.drop")));
                        }

                    } else if (args[1].equalsIgnoreCase("crop")) {

                        ItemStack collector = CollectorManager.makeCollector(p, CollectionType.CROP);
                        if (collector != null) {
                            p.getInventory().addItem(collector);
                            p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.got-collector.crop")));
                        }

                    } else if (args[1].equalsIgnoreCase("ore")) {

                        ItemStack collector = CollectorManager.makeCollector(p, CollectionType.ORE);
                        if (collector != null) {
                            p.getInventory().addItem(collector);
                            p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.got-collector.ore")));
                        }

                    } else {

                        p.sendMessage(ChatColor.RED + "Skriv en gyldig collector type. Prøv igen.");
                        p.sendMessage(ChatColor.GRAY + "Eksempel: /collector give drop [username] | /collector give crop [username]");

                    }

                } else if (args.length == 3) {

                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        p.sendMessage(ChatColor.RED + "Skriv en gyldig online spiller. Prøv igen.");
                        p.sendMessage(ChatColor.GRAY + "Eksempel: /collector give crop Dalot421");
                    } else {

                        if (args[1].equalsIgnoreCase("drop")) {

                            ItemStack collector = CollectorManager.makeCollector(target, CollectionType.DROP);
                            if (collector != null) {
                                target.getInventory().addItem(collector);
                                target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.drop")));

                                p.sendMessage(ChatColor.GREEN + "Collector er blevet givet til " + target.getDisplayName());
                            }

                        } else if (args[1].equalsIgnoreCase("crop")) {

                            ItemStack collector = CollectorManager.makeCollector(target, CollectionType.CROP);
                            if (collector != null) {
                                target.getInventory().addItem(collector);
                                target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.crop")));

                                p.sendMessage(ChatColor.GREEN + "Collector er blevet givet til " + target.getDisplayName());
                            }

                        } else if (args[1].equalsIgnoreCase("ore")) {

                            ItemStack collector = CollectorManager.makeCollector(target, CollectionType.ORE);
                            if (collector != null) {
                                target.getInventory().addItem(collector);
                                target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.ore")));

                                p.sendMessage(ChatColor.GREEN + "Collector er blevet givet til " + target.getDisplayName());
                            }

                        } else {

                            p.sendMessage(ChatColor.RED + "Skriv en guldig collector type. Prøv igen.");
                            p.sendMessage(ChatColor.GRAY + "Eksempel: /collector give drop [username] | /collector give crop [username]");

                        }

                    }

                } else if (args.length == 1) {
                    p.sendMessage(ChatColor.GRAY + "Skriv en collector type. Collector Types: " + ChatColor.GREEN + "drop" + ChatColor.GRAY + "| " + ChatColor.GREEN + "crop" + ChatColor.GRAY + "| " + ChatColor.GREEN + "crop");
                    p.sendMessage(ChatColor.GRAY + "Eksempel: /collector give drop");
                }
            } else {
                p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.no-permission")));
            }
        } else {
            if (args.length == 2) {

                System.out.println("Du skal være en spiller for at give dig selv en collector.");

            } else if (args.length == 3) {

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    System.out.println("Skriv en gyldig online spiller. Prøv igen.");
                    System.out.println("ex: /collector give crop Dalot421");
                } else {

                    if (args[1].equalsIgnoreCase("drop")) {

                        ItemStack collector = CollectorManager.makeCollector(target, CollectionType.DROP);
                        if (collector != null) {
                            target.getInventory().addItem(collector);
                            target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.drop")));

                            System.out.println("Collector er blevet givet til " + target.getDisplayName());
                        }

                    } else if (args[1].equalsIgnoreCase("crop")) {

                        ItemStack collector = CollectorManager.makeCollector(target, CollectionType.CROP);
                        if (collector != null) {
                            target.getInventory().addItem(collector);
                            target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.crop")));

                            System.out.println("Collector er blevet givet til " + target.getDisplayName());
                        }

                    } else if (args[1].equalsIgnoreCase("ore")) {

                        ItemStack collector = CollectorManager.makeCollector(target, CollectionType.ORE);
                        if (collector != null) {
                            target.getInventory().addItem(collector);
                            target.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.given-collector.ore")));

                            System.out.println("Collector er blevet givet til " + target.getDisplayName());
                        }
                    } else {
                        System.out.println("Skriv en gyldig colletor type. Prøv igen.");
                        System.out.println("Eksempel: /collector give drop [username] | /collector give crop [username]");
                    }
                }
            } else if (args.length == 1) {
                System.out.println("Skriv en collector type. Collector Types: drop | crop | ore");
                System.out.println("Eksempel: /collector give drop");
            }
        }


    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return List.of("drop", "crop", "ore");
        } else if (args.length == 3) {

            List<String> playerNames = new ArrayList<>();
            Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
            Bukkit.getServer().getOnlinePlayers().toArray(players);
            for (Player value : players) {
                playerNames.add(value.getName());
            }

            return playerNames;
        }
        return null;
    }
}