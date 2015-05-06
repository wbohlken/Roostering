package nl.uva.heuristiek.util;

import java.security.SecureRandom;

/**
 * Created by remco on 03/05/15.
 */
public class Random {
    private static final SecureRandom sRandom = new SecureRandom();

    public static int nextInt(int bound) {
        return sRandom.nextInt(bound);
    }
}
