package me.dalot.guis;


import me.dalot.ChunkCollector;
import me.dalot.enums.CollectionType;
import me.dalot.enums.MenuData;
import me.dalot.hooks.VaultHook;
import me.dalot.managers.CollectorManager;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.menu.PlayerMenuUtility;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class ConfirmBuyMenu extends Menu {

    private final CollectionType buyType;
    private final PlayerMenuUtility playerMenuUtility;

    public ConfirmBuyMenu(PlayerMenuUtility playerMenuUtility) {
        super(String.valueOf(playerMenuUtility));
        this.playerMenuUtility = playerMenuUtility;
        this.buyType = playerMenuUtility.getData(MenuData.BUY_TYPE, CollectionType.class);
    }

    @Override
    public String getMenuName() {
        if (buyType == CollectionType.DROP) {
            return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Confirm-Buy Menu.drop"));
        } else if (buyType == CollectionType.CROP) {
            return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Confirm-Buy Menu.crop"));
        } else if (buyType == CollectionType.ORE) {
            return ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("Menu Titles.Confirm-Buy Menu.ore"));
        }
        return "pickle";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public boolean cancelAllClicks() {
        return true;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        ItemStack collector = null;
        Player p = playerMenuUtility.getOwner();

        if ((e.getCurrentItem().getType() == Material.DIAMOND_SWORD) || (e.getCurrentItem().getType() == Material.BREAD) || (e.getCurrentItem().getType() == Material.GOLD_ORE)) {

            double cost = 0;

            if (buyType == CollectionType.DROP) {
                cost = ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.drop");
            } else if (buyType == CollectionType.CROP) {
                cost = ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.crop");
            } else if (buyType == CollectionType.ORE) {
                cost = ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.ore");
            }

            if (VaultHook.getEconomy().getBalance(playerMenuUtility.getOwner()) >= cost){

                EconomyResponse response = VaultHook.getEconomy().withdrawPlayer(playerMenuUtility.getOwner(), cost);

                if (response.transactionSuccess()){

                    if (buyType == CollectionType.DROP) {
                        p.sendMessage(ChatColor.GREEN + "Købt Drop Collector for $" + ChatColor.YELLOW + cost);
                        collector = CollectorManager.makeCollector(p, CollectionType.DROP);
                    } else if (buyType == CollectionType.CROP) {
                        p.sendMessage(ChatColor.GREEN + "Købt Crop Collector for $" + ChatColor.YELLOW + cost);
                        collector = CollectorManager.makeCollector(p, CollectionType.CROP);
                    } else if (buyType == CollectionType.ORE) {
                        p.sendMessage(ChatColor.GREEN + "Købt Ore Collector for $" + ChatColor.YELLOW + cost);
                        collector = CollectorManager.makeCollector(p, CollectionType.ORE);
                    }

                    if (p.getInventory().firstEmpty() == -1){
                        p.sendMessage(ChatColor.RED + "Dit inveontory er fuldt op, dropper istedet collector på jorden.");
                        p.getWorld().dropItem(p.getLocation(), collector);
                    }else{
                        p.getInventory().addItem(collector);
                    }

                    p.sendMessage(ChatColor.GRAY + "Placer din collector i en tom chunk for at starte.");
                    p.sendMessage(ChatColor.GRAY + "Når din collector er fuldt op, vil den sælge alle overflødige items.");
                    p.closeInventory();

                }else{
                    playerMenuUtility.getOwner().sendMessage(ChatColor.RED + "Fejl opstået ved køb");
                }

            }else{
                playerMenuUtility.getOwner().sendMessage(ChatColor.RED + "Du har ikke nok penge.");
            }


        }else if(e.getCurrentItem().getType() == Material.BARRIER){
            new BuyMenu((PlayerMenuUtility) playerMenuUtility).open();
        }

    }

    @Override
    public void setMenuItems() {

        if (buyType == CollectionType.DROP) {

            inventory.setItem(3, makeItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "" + ChatColor.BOLD + "Køb",
                    ChatColor.AQUA + "Køb Drop Collector for",
                    ChatColor.GOLD + "$" + ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.drop")));

        } else if (buyType == CollectionType.CROP) {

            inventory.setItem(3, makeItem(Material.BREAD, ChatColor.GREEN + "" + ChatColor.BOLD + "Køb",
                    ChatColor.AQUA + "Køb Crop Collector for",
                    ChatColor.GOLD + "$" + ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.crop")));

        } else if (buyType == CollectionType.ORE) {

            inventory.setItem(3, makeItem(Material.BREAD, ChatColor.GREEN + "" + ChatColor.BOLD + "Køb",
                    ChatColor.AQUA + "Køb Ore Collector for",
                    ChatColor.GOLD + "$" + ChunkCollector.getPlugin().getConfig().getDouble("collector-cost.ore")));

        }

        inventory.setItem(5, makeItem(Material.BARRIER, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Annuler"));

        setFillerGlass();
    }
}
