package me.dalot.tasks;

import me.dalot.ChunkCollector;
import me.dalot.database.Database;
import me.dalot.enums.CollectionType;
import me.dalot.model.Collector;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class AutoKill extends BukkitRunnable {

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
                                if (collector.getType() == CollectionType.DROP) {

                                    Arrays.stream(chunk.getEntities())
                                            .filter((Entity entity) -> entity.isOnGround())
                                            .filter((Entity entity) -> entity instanceof Mob)
                                            .map((Entity entity) -> (Mob) entity)
                                            .forEach(mob -> mob.damage(1000));
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
        ChunkCollector.getPlugin().getConfig().getStringList("worlds").stream()
                .forEach(AutoKill::processChunks);

    }

}
