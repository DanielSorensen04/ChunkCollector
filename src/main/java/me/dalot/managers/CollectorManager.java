package me.dalot.managers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.CollectionType;
import me.dalot.hooks.VaultHook;
import me.dalot.model.Collector;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CollectorManager {

    public static ArrayList<ItemStack> combine(ArrayList<ItemStack> itemStacks) {
        Multimap<Material, Integer> map = ArrayListMultimap.create();
        for (ItemStack item : itemStacks) {
            if (!map.containsKey(item.getType())) {
                map.put(item.getType(), item.getAmount());
            } else {
                ArrayList<Integer> value = new ArrayList<>(map.get(item.getType()));
                if (value.get(value.size() - 1) + item.getAmount() <= item.getType().getMaxStackSize()) {
                    value.set(value.size() - 1, value.get(value.size() - 1) + item.getAmount());
                } else {
                    int k = value.get(value.size() - 1);
                    value.set(value.size() - 1, item.getType().getMaxStackSize());
                    value.add(item.getAmount() - (item.getType().getMaxStackSize() - k));
                }
                map.replaceValues(item.getType(), value);
            }
        }
        ArrayList<ItemStack> sortedList = new ArrayList<>();

        for (Material key : map.keySet()) {
            Collection<Integer> values = map.get(key);
            for (Integer value : values) {
                ItemStack item = new ItemStack(key, value);
                sortedList.add(item);
            }
        }
        return sortedList;
    }


    public static ItemStack makeCollector(Player p, CollectionType type) {
        int id = 0;

        ItemStack collectorItem = null;
        ItemMeta collectorMeta = null;

        if (type == CollectionType.DROP) {

            collectorItem = new ItemStack(Material.valueOf(ChunkCollector.getPlugin().getConfig().getString("Materials.drop")), 1);
            collectorMeta = collectorItem.getItemMeta();

            collectorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChunkCollector.getPlugin().getConfig().getString("Names.drop")));

            Collector collector = new Collector(p.getUniqueId(), CollectionType.DROP);
            id = Database.getCollectorDataAccess().insert(collector).getId();
        } else if (type == CollectionType.CROP) {

            collectorItem = new ItemStack(Material.valueOf(ChunkCollector.getPlugin().getConfig().getString("Materials.crop")), 1);
            collectorMeta = collectorItem.getItemMeta();

            collectorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChunkCollector.getPlugin().getConfig().getString("Names.crop")));

            Collector collector = new Collector(p.getUniqueId(), CollectionType.CROP);
            id = Database.getCollectorDataAccess().insert(collector).getId();
        } else if (type == CollectionType.ORE) {

            collectorItem = new ItemStack(Material.valueOf(ChunkCollector.getPlugin().getConfig().getString("Materials.ore")), 1);
            collectorMeta = collectorItem.getItemMeta();

            collectorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChunkCollector.getPlugin().getConfig().getString("Names.ore")));

            Collector collector = new Collector(p.getUniqueId(), CollectionType.ORE);
            id = Database.getCollectorDataAccess().insert(collector).getId();
        }
        if (id != 0) {
            collectorMeta.getPersistentDataContainer().set(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER, id);

            collectorItem.setItemMeta(collectorMeta);

            return collectorItem;
        } else {
            p.sendMessage("Fejl ved oprettelse af collector.");
            return null;
        }

    }

    public static boolean isChunkTaken(Chunk chunk) {
        for (BlockState blockState : chunk.getTileEntities()) {
            TileState tileState = (TileState) blockState;

            if (tileState.getPersistentDataContainer().has(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobDrop(Item item) {
        ArrayList<Material> mob_drops = (ArrayList<Material>) ChunkCollector.getPlugin().getConfig().getConfigurationSection("mob-drops").getKeys(false)
                .stream()
                .map(Material::valueOf)
                .collect(Collectors.toList());

        return mob_drops.contains(item.getItemStack().getType());
    }

    public static boolean isMineableBlock(Material type) {
        List<String> blocks = ChunkCollector.getPlugin().getConfig().getStringList("mineable-blocks");
        return blocks.contains(type.toString());
    }

    public static double getItemPrice(Material item, CollectionType type) {

        switch (type) {
            case DROP:
                return ChunkCollector.getPlugin().getConfig().getDouble("mob-drops." + item.toString());
            case CROP:
                return ChunkCollector.getPlugin().getConfig().getDouble("crop-pricing." + item.toString());
            case ORE:
                return ChunkCollector.getPlugin().getConfig().getDouble("ore-pricing." + item.toString());
        }
        return 0.0;
    }

    public static BaseComponent[] constructReceipt(ArrayList<String> lines) {
        TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));

        ComponentBuilder receipt = new ComponentBuilder(net.md_5.bungee.api.ChatColor.GREEN + "" + net.md_5.bungee.api.ChatColor.BOLD + "Kupon af solgte items");
        receipt.append(newLine);

        receipt.append(net.md_5.bungee.api.ChatColor.GREEN + "---------------------------");
        receipt.append(newLine);

        for (String line : lines) {
            receipt.append(new TextComponent(line)).reset();
            receipt.append(newLine);
        }

        receipt.append(net.md_5.bungee.api.ChatColor.GREEN + "---------------------------");

        return receipt.create();
    }

    public static void sellAllItems(Collector collector) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(collector.getOwnerUUID());

        ArrayList<ItemStack> storage = collector.getItems();

        if (collector.getItems().isEmpty()) {
            if (player.isOnline()) {
                player.getPlayer().sendMessage(ChatColor.GRAY + "Din collector er tom.");
            }
        } else {

            long itemsSold = 0;
            double earned = 0.0;

            for (ItemStack itemStack : storage) {
                itemsSold = itemsSold + itemStack.getAmount();
                earned = earned + (CollectorManager.getItemPrice(itemStack.getType(), collector.getType()) * itemStack.getAmount());
            }

            EconomyResponse transaction = VaultHook.getEconomy().depositPlayer(player, earned);

            if (transaction.transactionSuccess()) {
                if (player.isOnline()) {

                    Player p = player.getPlayer();
                    ArrayList<String> receiptItems = new ArrayList<>();

                    HashMap<Material, Long> countedItems = new HashMap<>();
                    storage.stream()
                            .forEach(item -> {
                                if (!countedItems.containsKey(item.getType())) {
                                    countedItems.put(item.getType(), (long) item.getAmount());
                                } else {
                                    countedItems.replace(item.getType(), countedItems.get(item.getType()) + item.getAmount());
                                }
                            });
                    countedItems.forEach(((material, aLong) -> {
                        receiptItems.add(net.md_5.bungee.api.ChatColor.GRAY + "Solgt " + aLong.toString() + " " + material.toString().toLowerCase().replace("_", " ") + " for $" + net.md_5.bungee.api.ChatColor.YELLOW + String.format("%.2f", (CollectorManager.getItemPrice(material, collector.getType()) * aLong)));
                    }));

                    TextComponent text = new TextComponent(net.md_5.bungee.api.ChatColor.BLUE + "" + net.md_5.bungee.api.ChatColor.BOLD + "Hold over for kupon");
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, constructReceipt(receiptItems)));

                    p.sendMessage(" ");
                    if (collector.getType() == CollectionType.DROP) {
                        p.sendMessage(ChatColor.GREEN + "Alle items i din " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Drop Collector" + ChatColor.GREEN + " er blevet solgt.");
                    } else {
                        p.sendMessage(ChatColor.GREEN + "Alle items i din " + ChatColor.YELLOW + "" + ChatColor.BOLD + "Crop Collector" + ChatColor.GREEN + " er blevet solgt.");
                    }
                    p.sendMessage(ChatColor.GRAY + "Total Tjent: " + ChatColor.GREEN + "$" + String.format("%.2f", earned));
                    p.sendMessage(ChatColor.GRAY + "Total Solgt: " + ChatColor.GREEN + itemsSold);
                    p.sendMessage(" ");

                    p.spigot().sendMessage(text);
                } else {
                    Database.getCollectorDataAccess().insertOfflineProfit(player, earned, itemsSold);
                }

                collector.setSold(collector.getSold() + itemsSold);
                collector.setEarned(collector.getEarned() + earned);

                collector.getItems().clear();
                Database.getCollectorDataAccess().update(collector);
            }
        }
    }

    public static void processItems(Collector collector, ArrayList<ItemStack> items) {
        items.stream()
                .forEach(itemStack -> {
                    if ((collector.getItems().stream().mapToInt(ItemStack::getAmount).sum() + itemStack.getAmount()) > CollectorManager.getCapacityAmount(collector.getStorageCapacity())) {
                        collector.getItems().add(itemStack);

                        sellAllItems(collector);

                        collector.getItems().clear();
                    } else {
                        collector.getItems().add(itemStack);
                    }
                });
        Database.getCollectorDataAccess().update(collector);
    }

    public static int getCapacityAmount(int currentLevel) {
        return ChunkCollector.getPlugin().getConfig().getInt("storage-upgrades." + currentLevel + ".items");
    }

    public static String getNextCapacity(int currentLevel) {
        if (ChunkCollector.getPlugin().getConfig().contains("storage-upgrades." + (currentLevel + 1) + ".items")) {
            return String.valueOf(ChunkCollector.getPlugin().getConfig().getInt("storage-upgrades." + (currentLevel + 1) + ".items"));
        } else {
            return "AT MAX";
        }
    }

    public static double getCapacityUpgradePrice(int currentLevel) {
        if (ChunkCollector.getPlugin().getConfig().contains("storage-upgrades." + (currentLevel + 1) + ".price")) {
            return ChunkCollector.getPlugin().getConfig().getDouble("storage-upgrades." + (currentLevel + 1) + ".price");
        } else {
            return 0.0;
        }
    }

    public static int isCollectorInChunk(Chunk chunk) {
        for (BlockState blockState : chunk.getTileEntities()) {
            TileState tileState = (TileState) blockState;
            if (tileState.getPersistentDataContainer().has(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER)) {
                return tileState.getPersistentDataContainer().get(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER);
            }
        }
        return 0;
    }

    public static double getFortuneUpgradePrice(int currentLevel) {
        if (ChunkCollector.getPlugin().getConfig().contains("fortune-prices." + (currentLevel + 1))) {
            return ChunkCollector.getPlugin().getConfig().getDouble("fortune-prices." + (currentLevel + 1));
        }
        return 0.0;
    }

    public static void enableDisableCollector(int id) {
        Collector collector = Database.getCollectorDataAccess().findById(id);

        Player owner = Bukkit.getPlayer(collector.getOwnerUUID());

        if (collector.isEnabled()) {
            collector.setEnabled(false);
            owner.sendMessage(ChatColor.RED + "Collector slået fra.");
        } else {
            collector.setEnabled(true);
            owner.sendMessage(ChatColor.GREEN + "Collector slået til.");
        }

        Database.getCollectorDataAccess().update(collector);
    }

    public static void updateCollectorHolograms(Collector collector) {

    }
}
