package net.runelite.client.plugins.iutils.util;

import java.util.concurrent.ThreadLocalRandom;

public class Calculations {

    protected static final java.util.Random random = new java.util.Random();

    public static int getRandomIntBetweenRange(int min, int max) {
        //return (int) ((Math.random() * ((max - min) + 1)) + min); //This does not allow return of negative values
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    //Ganom's function, generates a random number allowing for curve and weight
    public static long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target) {
        if (weightedDistribution) {
            /* generate a gaussian random (average at 0.0, std dev of 1.0)
             * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
             * get the log base e of it to make it shifted towards the right side
             * invert it to shift the distribution to the other end
             * clamp it to min max, any values outside of range are set to min or max */
            return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
        } else {
            /* generate a normal even distribution random */
            return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
        }
    }

    private static double clamp(double val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Returns a random double with min as the inclusive lower bound and max as
     * the exclusive upper bound.
     *
     * @param min The inclusive lower bound.
     * @param max The exclusive upper bound.
     * @return Random double min <= n < max.
     */
    public static double random(double min, double max) {
        return Math.min(min, max) + random.nextDouble() * Math.abs(max - min);
    }

    /**
     * Returns a random integer with min as the inclusive lower bound and max as
     * the exclusive upper bound.
     *
     * @param min The inclusive lower bound.
     * @param max The exclusive upper bound.
     * @return Random integer min <= n < max.
     */
    public static int random(int min, int max) {
        int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
    }
}
