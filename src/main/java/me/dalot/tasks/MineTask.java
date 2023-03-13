package me.dalot.tasks;

import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.CollectionType;
import me.dalot.model.Collector;
import me.dalot.managers.CollectorManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class MineTask extends BukkitRunnable {

    //
    //JEG VED GODT DET ER GRIMT KODE I DEN HER CLASS, PLS IK HATE
    //
    private static void processChunks(String worldName) {
        if (Bukkit.getServer().getWorld(worldName) != null) {
            final Chunk[] loadedChunks = Bukkit.getServer().getWorld(worldName).getLoadedChunks();

            for (Chunk chunk : loadedChunks) {
                for (BlockState blockState : chunk.getTileEntities()) {

                    TileState tileState = (TileState) blockState;

                    if (tileState.getPersistentDataContainer().has(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER)) {

                        Collector collector = Database.getCollectorDataAccess().findById(tileState.getPersistentDataContainer().get(new NamespacedKey(ChunkCollector.getPlugin(), "collector-id"), PersistentDataType.INTEGER));

                        if (collector != null) {
                            if (collector.isEnabled()) {
                                if (collector.getType() == CollectionType.ORE) {

                                    long delay = 0;

                                    for (int y = 0; y < 128; y++) {
                                        for (int i = 0; i < 16; i++) {
                                            for (int j = 0; j < 16; j++) {

                                                int finalI = i;
                                                int finalY = y;
                                                int finalJ = j;

                                                Block block = chunk.getBlock(finalI, finalY, finalJ);

                                                if (CollectorManager.isMineableBlock(block.getType())) {
                                                    delay = delay + 5;
                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            CollectorManager.processItems(collector, (ArrayList<ItemStack>) block.getDrops());
                                                            block.setType(Material.AIR);
                                                        }
                                                    }.runTaskLater(ChunkCollector.getPlugin(), delay);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        ChunkCollector.getPlugin().getConfig().getStringList("worlds")
                .forEach(MineTask::processChunks);

    }
}
