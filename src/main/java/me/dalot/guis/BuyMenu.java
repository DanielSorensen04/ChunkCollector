package me.dalot.guis;

import me.dalot.ChunkCollector;
import me.dalot.enums.CollectionType;
import me.dalot.enums.MenuData;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.exceptions.MenuManagerException;
import me.kodysimpson.simpapi.exceptions.MenuManagerNotSetupException;
import me.kodysimpson.simpapi.menu.MenuManager;
import me.kodysimpson.simpapi.menu.PlayerMenuUtility;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.awt.*;

public class BuyMenu extends Menu {


    private final PlayerMenuUtility playerMenuUtility;

    public BuyMenu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Buy Menu"));
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
            case DIAMOND_SWORD -> {
                playerMenuUtility.setData(MenuData.BUY_TYPE, CollectionType.DROP);
                MenuManager.openMenu(ConfirmBuyMenu.class, p);
            }
            case BREAD -> {
                playerMenuUtility.setData(MenuData.BUY_TYPE, CollectionType.CROP);
                MenuManager.openMenu(ConfirmBuyMenu.class, p);
            }
            case GOLD_ORE -> {
                playerMenuUtility.setData(MenuData.BUY_TYPE, CollectionType.ORE);
                MenuManager.openMenu(ConfirmBuyMenu.class, p);
            }
            case BARRIER -> playerMenuUtility.getOwner().closeInventory();
        }

    }

    @Override
    public void setMenuItems() {

        inventory.setItem(11, makeItem(Material.DIAMOND_SWORD, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Mob Drop Collector",
                ChatColor.YELLOW + "Automatisk samler mob drops."));

        inventory.setItem(13, makeItem(Material.GOLD_ORE, ChatColor.GOLD + "" + ChatColor.BOLD + "Ore Collector",
                ChatColor.WHITE + "Automatisk miner blocks."));

        inventory.setItem(15, makeItem(Material.BREAD, ChatColor.YELLOW + "" + ChatColor.BOLD + "Crop Collector",
                ChatColor.GREEN + "Automatisk samler afgrøder",

        inventory.setItem(31, makeItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Annuler"));

        setFillerGlass();
    }
}
