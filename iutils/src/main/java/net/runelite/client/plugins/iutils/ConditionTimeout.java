package net.runelite.client.plugins.iutils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * ConditionTimeout
 *
 * @author Mikester
 * Holds information about a conditional timeout.
 * Can be used to run a tick timer that waits for a condition to be met before ending.
 * Useful for conditional delays such as waiting for the bank interface to open after clicking the bank object.
 * See HandleTimeout class for timeout implementation and TimeoutUntil/TimeoutWhile classes for example usage.
 */
@Slf4j
public abstract class ConditionTimeout {
    /**
     * condition
     * The condition that needs to be met for the timeout to end.
     */
    private Callable<Boolean> condition;

    /**
     * resetCondition
     * A condition that when met resets the {@link #ticksElapsed}.
     */
    private Callable<Boolean> resetCondition;

    /**
     * exception
     * A condition to determine if the timeout can be skipped for that tick even if it hasn't finished yet.
     * Useful for letting some functionality to happen within timeouts.
     */
    private Callable<Boolean> exception;

    /**
     * expirationTicks
     * An amount of ticks that determine when the timeout expires without the condition being met.
     */
    private int expirationTicks;

    /**
     * ticksElapsed
     * An amount of ticks that determine how long the timeout has been running. Can be reset by {@link #resetCondition}.
     */
    private int ticksElapsed = 0;

    /**
     * frequency
     * How often to poll the condition, defaults to every tick.
     */
    private int frequency;

    public void setCondition(Callable<Boolean> condition) {
        this.condition = condition;
    }

    public void setResetCondition(Callable<Boolean> resetCondition) {
        this.resetCondition = resetCondition;
    }

    public void setException(Callable<Boolean> exception) {
        this.exception = exception;
    }

    public void setExpirationTicks(int ticks) {
        if (ticks < 0) {
            ticks = 0;
        }

        expirationTicks = ticks;
    }


    public void setExpirationTicks(int minTicks, int maxTicks, int targetTicks, int deviationTicks) {
        if (maxTicks < minTicks) {
            maxTicks = minTicks;
        }

        if (minTicks < 0) {
            minTicks = 0;
        }

        if (maxTicks < 0) {
            maxTicks = 0;
        }

        expirationTicks = (int) randomDelay(false, minTicks, maxTicks, deviationTicks, targetTicks);
    }

    public void setTicksElapsed(int ticksElapsed) {
        this.ticksElapsed = ticksElapsed;
    }

    public void incrementTicksElapsed() {
        if (this.ticksElapsed < Integer.MAX_VALUE) {
            ticksElapsed++;
        }
    }

    public boolean isExpired() {
        return ticksElapsed >= expirationTicks;
    }

    public void setFrequency(int frequency) {
        if (frequency < 1) {
            frequency = 1;
        }

        this.frequency = frequency;
    }

    public Callable<Boolean> getCondition() {
        return condition;
    }

    public Callable<Boolean> getResetCondition() {
        return resetCondition;
    }

    public Callable<Boolean> getException() {
        return exception;
    }

    public int getExpirationTicks() {
        return expirationTicks;
    }

    public int getFrequency() {
        return frequency;
    }


    // Ganom's function(s), generates a random number allowing for curve and weight
    public long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target) {
        if (weightedDistribution) {
            /* generate a gaussian random (average at 0.0, std dev of 1.0)
             * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
             * get the log base e of it to make it shifted towards the right side
             * invert it to shift the distribution to the other end
             * clamp it to min max, any values outside of range are set to min or max */
            return (long) clamp((-Math.log(Math.abs(new Random().nextGaussian()))) * deviation + target, min, max);
        } else {
            /* generate a normal even distribution random */
            return (long) clamp(Math.round(new Random().nextGaussian() * deviation + target), min, max);
        }
    }

    private double clamp(double val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

}
