package me.dalot.guis.collector;

import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.MenuData;
import me.dalot.model.Collector;
import me.dalot.managers.CollectorManager;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.exceptions.MenuManagerException;
import me.kodysimpson.simpapi.exceptions.MenuManagerNotSetupException;
import me.kodysimpson.simpapi.menu.MenuManager;
import me.kodysimpson.simpapi.menu.PlayerMenuUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class CollectorMenu extends Menu {

    private final Collector collector;

    public CollectorMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        collector = Database.getCollectorDataAccess().findById(playerMenuUtility.getData(MenuData.COLLECTOR_ID, Integer.class));
    }

    @Override
    public String getMenuName() {

        String isEnabled;
        if (collector.isEnabled()) {
            isEnabled = ChatColor.GREEN + "[Enabled]";
        } else {
            isEnabled = ChatColor.RED + "[Disabled]";
        }

        return switch (collector.getType()) {
            case DROP -> ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Collector Menu.drop")) + " | " + isEnabled;
            case CROP -> ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Collector Menu.crop")) + " | " + isEnabled;
            case ORE -> ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Collector Menu.ore")) + " | " + isEnabled;
        };

    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public boolean cancelAllClicks() {
        return true;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) throws MenuManagerException, MenuManagerNotSetupException {

        switch (e.getCurrentItem().getType()) {
            case CHEST -> MenuManager.openMenu(CollectorStorageMenu.class, p);
            case EMERALD -> MenuManager.openMenu(CollectorUpgradeMenu.class, p);
            case BARRIER -> e.getWhoClicked().closeInventory();
            case DIAMOND_SWORD, BREAD, GOLD_ORE -> {
                CollectorManager.enableDisableCollector(collector.getId());
                reload();
            }
        }

    }

    @Override
    public void setMenuItems() {

        String isEnabled;
        if (collector.isEnabled()) {
            isEnabled = ChatColor.RED + "[Disable]";
        } else {
            isEnabled = ChatColor.GREEN + "[Enable]";
        }

        if (collector.getType() == DROP) {

            inventory.setItem(11, makeItem(Material.CHEST, ChatColor.RED + "" + ChatColor.BOLD + "Se Drop Lager",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Mængde Gemt: " + ChatColor.GREEN + collector.getItems().stream().mapToInt(ItemStack::getAmount).sum() + "/" + CollectorManager.getCapacityAmount(collector.getStorageCapacity())));

            inventory.setItem(13, makeItem(Material.DIAMOND_SWORD, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Mob Drop Collector", ChatColor.YELLOW + "Automatisk samler mob drops.",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Total Solgt: " + ChatColor.BLUE + collector.getSold(),
                    ChatColor.GRAY + "Total Tjent: $" + ChatColor.GREEN + String.format("%.2f", collector.getEarned()),
                    ChatColor.GRAY + "Tryk for at " + isEnabled));

        } else if (collector.getType() == CROP) {

            inventory.setItem(11, makeItem(Material.CHEST, ChatColor.RED + "" + ChatColor.BOLD + "Se Crops Lager",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Mængde Gemt: " + ChatColor.GREEN + collector.getItems().stream().mapToInt(ItemStack::getAmount).sum() + "/" + CollectorManager.getCapacityAmount(collector.getStorageCapacity())));

            inventory.setItem(13, makeItem(Material.BREAD, ChatColor.YELLOW + "" + ChatColor.BOLD + "Crops Collector",
                    ChatColor.GREEN + "Automatisk samler afgrøder",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Total Solgt: " + ChatColor.BLUE + collector.getSold(),
                    ChatColor.GRAY + "Total Tjent: $" + ChatColor.GREEN + String.format("%.2f", collector.getEarned()),
                    ChatColor.GRAY + "Tryk for at " + isEnabled));
        } else if (collector.getType() == ORE) {

            inventory.setItem(11, makeItem(Material.CHEST, ChatColor.RED + "" + ChatColor.BOLD + "Se Block Lager",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Mængde Gemt: " + ChatColor.GREEN + collector.getItems().stream().mapToInt(ItemStack::getAmount).sum() + "/" + CollectorManager.getCapacityAmount(collector.getStorageCapacity())));

            inventory.setItem(13, makeItem(Material.GOLD_ORE, ChatColor.GOLD + "" + ChatColor.BOLD + "Ore Collector",
                    ChatColor.GREEN + "Automatisk miner blocks",
                    ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "                            ",
                    ChatColor.GRAY + "Total Solgt: " + ChatColor.BLUE + collector.getSold(),
                    ChatColor.GRAY + "Total Tjent: $" + ChatColor.GREEN + String.format("%.2f", collector.getEarned()),
                    ChatColor.GRAY + "Tryk for at " + isEnabled));
        }

        inventory.setItem(15, makeItem(Material.EMERALD, ChatColor.GREEN + "" + ChatColor.BOLD + "Upgrade Options"));

        inventory.setItem(31, makeItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Annuler"));

        setFillerGlass();
    }

}
