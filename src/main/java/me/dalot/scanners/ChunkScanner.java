package me.dalot.scanners;

import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.CollectionType;
import me.dalot.model.Collector;
import me.dalot.managers.CollectorManager;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ChunkScanner extends BukkitRunnable {

    private static void processChunks(String worldName) {
        if (Bukkit.getServer().getWorld(worldName) != null) {

            final Chunk[] loadedChunks = Bukkit.getServer().getWorld(worldName).getLoadedChunks();

            for (Chunk chunk : loadedChunks) {

                Collector collector = null;
                boolean foundCollector = false;

                for (BlockState blockState : chunk.getTileEntities()) {
                    TileState tileState = (TileState) blockState;

                    if (tileState.getPersistentDataContainer().has(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER)) {

                        collector = Database.getCollectorDataAccess().findById(tileState.getPersistentDataContainer().get(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER));

                        if (collector != null) {
                            foundCollector = true;
                            if (collector.isEnabled()) {
                                if (collector.getType() == CollectionType.DROP) {

                                    ArrayList<ItemStack> groundItems = (ArrayList<ItemStack>) Arrays.stream(chunk.getEntities())
                                            .filter((Entity entity) -> entity.isOnGround())
                                            .filter((Entity entity) -> entity instanceof Item)
                                            .map((Entity entity) -> (Item) entity)
                                            .filter(item -> CollectorManager.isMobDrop(item))
                                            .map(Item::getItemStack)
                                            .collect(Collectors.toList());

                                    CollectorManager.processItems(collector, groundItems);

                                    Arrays.stream(chunk.getEntities())
                                            .filter((Entity entity) -> entity.isOnGround())
                                            .filter((Entity entity) -> entity instanceof Item)
                                            .map((Entity entity) -> (Item) entity)
                                            .filter(item -> CollectorManager.isMobDrop(item))
                                            .forEach(item -> item.remove());

                                    break;
                                } else if (collector.getType() == CollectionType.CROP) {

                                    ArrayList<ItemStack> cropGroundItems = (ArrayList<ItemStack>) Arrays.stream(chunk.getEntities())
                                            .filter((Entity entity) -> entity.isOnGround())
                                            .filter((Entity entity) -> entity instanceof Item)
                                            .map((Entity entity) -> (Item) entity)
                                            .map(Item::getItemStack)
                                            .filter(item -> item.getType() == Material.CACTUS || item.getType() == Material.SUGAR_CANE)
                                            .collect(Collectors.toList());

                                    CollectorManager.processItems(collector, cropGroundItems);

                                    Arrays.stream(chunk.getEntities())
                                            .filter((Entity entity) -> entity.isOnGround())
                                            .filter((Entity entity) -> entity instanceof Item)
                                            .map((Entity entity) -> (Item) entity)
                                            .filter(item -> item.getItemStack().getType() == Material.CACTUS || item.getItemStack().getType() == Material.SUGAR_CANE)
                                            .forEach(item -> item.remove());

                                    break;

                                }
                            }
                        }
                    }
                }
                if (foundCollector) {

                    Collector finalCollector = collector;
                    Arrays.stream(chunk.getEntities())
                            .filter(entity -> entity.getPersistentDataContainer().has(new NamespacedKey(ChunkCollector.getPlugin(), "holo-line"), PersistentDataType.INTEGER)).map(entity -> (ArmorStand) entity)
                            .forEach(armorStand -> {

                                if (ChunkCollector.getPlugin().getConfig().getBoolean("hologram.enabled")) {
                                    switch (armorStand.getPersistentDataContainer().get(new NamespacedKey(ChunkCollector.getPlugin(), "holo-line"), PersistentDataType.INTEGER)) {
                                        case 1:
                                            if (finalCollector.getType() == CollectionType.DROP) {
                                                armorStand.setCustomName(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("hologram.drop")));
                                            } else if (finalCollector.getType() == CollectionType.CROP) {
                                                armorStand.setCustomName(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("hologram.crop")));
                                            } else {
                                                armorStand.setCustomName(ColorTranslator.translateColorCodes(ChunkCollector.getPlugin().getConfig().getString("hologram.ore")));
                                            }
                                            break;
                                        case 2:
                                            armorStand.setCustomName(ChatColor.GRAY + "Items Solgt: " + ChatColor.GREEN + finalCollector.getSold());
                                            break;
                                        case 3:
                                            armorStand.setCustomName(ChatColor.GRAY + "Penge Tjent: " + ChatColor.GREEN + "$" + String.format("%.2f", finalCollector.getEarned()));
                                            break;
                                    }
                                } else {
                                    armorStand.remove();
                                }

                            });
                }
            }
        }
    }

    @Override
    public void run() {

        final HashMap<Integer, ArrayList<Block>> items = ChunkCollector.getCrops();

        items.forEach((integer, blocks) -> blocks.forEach(block -> {

            if (block.getWorld().getBlockAt(block.getLocation()).getState().getBlockData() instanceof Ageable) {
                Ageable ageableBlock = (Ageable) block.getWorld().getBlockAt(block.getLocation()).getState().getBlockData();

                if (ageableBlock.getAge() == ageableBlock.getMaximumAge()) {
                    BlockState blockState = block.getState();
                    Ageable ageable = (Ageable) blockState.getBlockData();

                    ItemStack fortuneItem = new ItemStack(Material.WOODEN_SHOVEL, 1);

                    int fortuneLevel = Database.getCollectorDataAccess().findById(integer.intValue()).getFortuneLevel();
                    if (fortuneLevel != 0) {
                        fortuneItem.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, fortuneLevel);
                    }

                    CollectorManager.processItems(Database.getCollectorDataAccess().findById(integer.intValue()), (ArrayList<ItemStack>) block.getDrops(fortuneItem));

                    ageable.setAge(0);
                    blockState.setBlockData((BlockData) ageable);
                    blockState.update();
                }
            }
        }));
        ChunkCollector.getCrops().clear();

        ChunkCollector.getPlugin().getConfig().getStringList("worlds").stream()
                .forEach(ChunkScanner::processChunks);

    }
}
