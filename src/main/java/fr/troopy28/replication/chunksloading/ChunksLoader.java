package fr.troopy28.replication.chunksloading;

import fr.troopy28.replication.ReplicationMod;
import fr.troopy28.replication.utils.ForgeScheduler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Creation: 08/12/2017.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ChunksLoader implements Runnable {

    private Map<Chunk, IChunkLoadingCallback> chunksAndCallbacks;
    private World world;

    public ChunksLoader(World world) {
        chunksAndCallbacks = new HashMap<>();
        this.world = world;
    }

    public void start() {
        ForgeScheduler.runRepeatingTaskLater(this, 200, 1000);
    }

    @Override
    public void run() {
        chunksAndCallbacks.keySet().parallelStream().forEach(chunk -> {
            if (!chunk.isLoaded()) {
                ReplicationMod.get().getLogger().info("Chunk (" + chunk.x + "; " + chunk.z + ") is not loaded. Loading it...");
                ((ChunkProviderServer) world.getChunkProvider()).loadChunk(chunk.x, chunk.z);
                ReplicationMod.get().getLogger().info("Chunk (" + chunk.x + "; " + chunk.z + ") loaded.");
            }
        });
    }
}