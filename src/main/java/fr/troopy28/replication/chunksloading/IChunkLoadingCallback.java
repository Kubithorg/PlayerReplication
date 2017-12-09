package fr.troopy28.replication.chunksloading;

import net.minecraft.world.chunk.Chunk;

/**
 * Creation: 08/12/2017.
 *
 * @author troopy28
 * @since 1.0.0
 */
public interface IChunkLoadingCallback {
    void loaded(Chunk chunk);
}
