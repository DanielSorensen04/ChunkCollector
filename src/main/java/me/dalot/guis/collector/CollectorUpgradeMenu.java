package me.dalot.guis.collector;

import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.CollectionType;
import me.dalot.enums.MenuData;
import me.dalot.hooks.VaultHook;
import me.dalot.model.Collector;
import me.dalot.managers.CollectorManager;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.menu.PlayerMenuUtility;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.util.ArrayList;

public class CollectorUpgradeMenu extends Menu {

    private final Collector collector;

    public CollectorUpgradeMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        collector = Database.getCollectorDataAccess().findById(playerMenuUtility.getData(MenuData.COLLECTOR_ID, Integer.class));
    }

    @Override
    public String getMenuName() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Collector-Upgrade Menu"));
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public boolean cancelAllClicks() {
        return true;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        switch (e.getCurrentItem().getType()) {
            case OAK_FENCE_GATE:

                if (CollectorManager.getNextCapacity(collector.getStorageCapacity()).equalsIgnoreCase("AT MAX")) {
                    p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.max-storage")));
                } else {
                    if (VaultHook.getEconomy().getBalance(p) >= CollectorManager.getCapacityUpgradePrice(collector.getStorageCapacity())) {

                        EconomyResponse response = VaultHook.getEconomy().withdrawPlayer(p, CollectorManager.getCapacityUpgradePrice(collector.getStorageCapacity()));

                        if (response.type == EconomyResponse.ResponseType.FAILURE) {
                            System.out.println("TRANSACTION ERROR");
                            System.out.println(response.errorMessage);
                        } else {

                            p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.upgrade-complete.storage")));
                            p.sendMessage(ChatColor.GREEN + "$" + CollectorManager.getCapacityUpgradePrice(collector.getStorageCapacity()) + ChatColor.YELLOW + " er blevet fjernet fra din konto.");

                            collector.setStorageCapacity(collector.getStorageCapacity() + 1);
                            Database.getCollectorDataAccess().update(collector);

                            new CollectorUpgradeMenu(playerMenuUtility).open();
                        }

                    } else {
                        p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.cant-afford-upgrade")));
                    }
                }

                break;
            case EXPERIENCE_BOTTLE:

                if (collector.getFortuneLevel() == 3) {
                    p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.max-fortune")));
                } else {
                    if (VaultHook.getEconomy().getBalance(p) >= CollectorManager.getFortuneUpgradePrice(collector.getFortuneLevel())) {

                        EconomyResponse response = VaultHook.getEconomy().withdrawPlayer(p, CollectorManager.getFortuneUpgradePrice(collector.getFortuneLevel()));
                        if (response.type == EconomyResponse.ResponseType.FAILURE) {
                            System.out.println("TRANSACTION ERROR");
                            System.out.println(response.errorMessage);
                        } else {

                            p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.upgrade-complete.fortune")));
                            p.sendMessage(ChatColor.GREEN + "$" + CollectorManager.getFortuneUpgradePrice(collector.getFortuneLevel()) + ChatColor.YELLOW + " er blevet fjernet fra din konto.");

                            collector.setFortuneLevel(collector.getFortuneLevel() + 1);
                            Database.getCollectorDataAccess().update(collector);

                            new CollectorUpgradeMenu(playerMenuUtility).open();
                        }

                    } else {
                        p.sendMessage(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Messages.cant-afford-upgrade")));
                    }
                }

                break;
            case BARRIER:
                new CollectorMenu(playerMenuUtility).open();
                break;
        }

    }

    @Override
    public void setMenuItems() {

        if (collector.getType() == CollectionType.CROP) {

            ItemStack fortune = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
            ItemMeta fortuneUpgrade = fortune.getItemMeta();
            fortuneUpgrade.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Fortune");
            ArrayList<String> fortuneLore = new ArrayList<>();
            fortuneLore.add(ChatColor.LIGHT_PURPLE + "Fortune øger mængden af ");
            fortuneLore.add(ChatColor.LIGHT_PURPLE + "produktion ved afgrøder.");
            fortuneLore.add(ChatColor.WHITE + "------------------------");
            fortuneLore.add(ChatColor.GREEN + "Nuværende Fortune: " + ChatColor.AQUA + collector.getFortuneLevel() + "/3");
            fortuneLore.add(ChatColor.WHITE + "------------------------");
            if (collector.getFortuneLevel() == 3) {
                fortuneLore.add(ChatColor.GOLD + "MAX LEVEL");
            } else {
                fortuneLore.add("(Tryk for at opgrader) $" + CollectorManager.getFortuneUpgradePrice(collector.getFortuneLevel()));
            }
            fortuneUpgrade.setLore(fortuneLore);
            fortune.setItemMeta(fortuneUpgrade);

            inventory.setItem(20, fortune);
        }

        ItemStack capacity = new ItemStack(Material.OAK_FENCE_GATE);
        ItemMeta capacityMeta = capacity.getItemMeta();
        capacityMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Lager Kapacitet");
        ArrayList<String> capacityLore = new ArrayList<>();
        capacityLore.add(ChatColor.GOLD + "Lager kapacitet er hvor ");
        capacityLore.add(ChatColor.GOLD + "mange items der maximum ");
        capacityLore.add(ChatColor.GOLD + "kan blive gemt i en collector ");
        capacityLore.add(ChatColor.GOLD + "før de bliver solgt.");
        capacityLore.add(ChatColor.WHITE + "------------------------");
        capacityLore.add(ChatColor.RED + "Nuværende kapacitet: " + ChatColor.GREEN + CollectorManager.getCapacityAmount(collector.getStorageCapacity()));
        capacityLore.add(ChatColor.WHITE + "------------------------");

        if (CollectorManager.getNextCapacity(collector.getStorageCapacity()).equalsIgnoreCase("AT MAX")) {
            capacityLore.add(ChatColor.GOLD + "MAX LEVEL");
        } else {
            capacityLore.add(ChatColor.YELLOW + "Næste Tier: " + ChatColor.GREEN + CollectorManager.getNextCapacity(collector.getStorageCapacity()));
            capacityLore.add(ChatColor.BLUE + "(Tryk for at opgrader) $" + CollectorManager.getCapacityUpgradePrice(collector.getStorageCapacity()));
        }

        capacityMeta.setLore(capacityLore);
        capacity.setItemMeta(capacityMeta);

        inventory.setItem(22, capacity);

        inventory.setItem(40, makeItem(Material.BARRIER, ChatColor.DARK_RED + "Luk"));

        setFillerGlass();
    }
}
