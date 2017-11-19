package fr.troopy28.replication.api;

import java.util.Random;

/**
 * Creation: 19/11/2017.
 * A simple class to manage a deterministic random, hence enabling to have "random" visual effects on each server without
 * any packets, or communication of any form.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ReplicableRandom {

    private static final long SEED = 21521420;
    private static final Random RANDOM = new Random(SEED);

    private ReplicableRandom() {

    }

    /**
     * @return Returns a deterministic {@link Random}, so that the result will be the same on every servers. This is useful to
     * have the same effect on a sponsor on every servers, without having to send any packets to ensure that the results
     * are the same everywhere.
     */
    public static Random get() {
        return RANDOM;
    }

}
