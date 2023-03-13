package me.dalot;

import me.dalot.database.Database;
import me.dalot.hooks.VaultHook;
import me.dalot.listeners.CollectorListener;
import me.dalot.scanners.ChunkScanner;
import me.dalot.tasks.AutoKill;
import me.dalot.tasks.MineTask;
import me.kodysimpson.simpapi.command.CommandList;
import me.kodysimpson.simpapi.command.CommandManager;
import me.kodysimpson.simpapi.command.SubCommand;
import me.kodysimpson.simpapi.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public final class ChunkCollector extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    //collector ID
    public static HashMap<Integer, ArrayList<Block>> crops = new HashMap<>();
    private static ChunkCollector plugin;
    private static String url;

    public static ChunkCollector getPlugin() {
        return plugin;
    }

    public static String getConnectionURL() {
        return url;
    }

    public static HashMap<Integer, ArrayList<Block>> getCrops() {
        return crops;
    }

    @Override
    public void onEnable() {

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();

        plugin = this;

        //Vault setup
        if (!VaultHook.setupEconomy(plugin)) {
            log.severe(String.format("[%s] - Slået fra pga. intet Vault Dependecy plugin fundet!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        url = "jdbc:h2:" + getDataFolder().getAbsolutePath() + "/data/chunkcollector";
        Database.initializeDatabase();

        try {
            CommandManager.createCoreCommand(this, "collector", "Chunk Collector kommando", "/collector", new CommandList() {
                @Override
                public void displayCommandList(CommandSender p, List<SubCommand> subCommandList) {
                    p.sendMessage(" ");
                    p.sendMessage(ChatColor.GREEN + "======= " + ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Chunk" + ChatColor.AQUA + "Collector" + ChatColor.GRAY + "] " + ChatColor.YELLOW + "Kommandoer " + ChatColor.GREEN + "=======");
                    p.sendMessage(" ");
                    for (SubCommand subCommand : subCommandList) {
                        p.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.YELLOW + subCommand.getSyntax() + " - " + ChatColor.GRAY + subCommand.getDescription());
                    }
                    p.sendMessage(" ");
                    p.sendMessage(ChatColor.GREEN + "=====================================");
                    p.sendMessage(" ");
                }
            }, List.of("cc", "chunkcollector"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        MenuManager.setup(getServer(), this);

        Bukkit.getServer().getPluginManager().registerEvents(new CollectorListener(), this);

        new ChunkScanner().runTaskTimer(this, 1200, getConfig().getLong("collection-duration"));

        if (getConfig().getBoolean("auto-kill")) {
            new AutoKill().runTaskTimer(this, 1200, getConfig().getLong("auto-kill-interval"));
        }

        new MineTask().runTaskTimer(this, 600, 1200);

    }

    @Override
    public void onDisable() {

        try {
            System.out.println("LUKKER NED FOR DATABASE CONNECTION");
            Database.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

}
