package net.runelite.client.plugins.iutils;

import java.util.concurrent.Callable;

public class TimeoutWhile extends ConditionTimeout {

    public TimeoutWhile(Callable<Boolean> condition, int expirationTicks) {
        this(condition, null, null, expirationTicks, expirationTicks, expirationTicks, 0, 1);
    }

    public TimeoutWhile(Callable<Boolean> condition, int minExpiration, int maxExpiration, int targetExpiration, int expirationDeviation) {
        this(condition, null, null, minExpiration, maxExpiration, targetExpiration, expirationDeviation, 1);
    }

    public TimeoutWhile(Callable<Boolean> condition, Callable<Boolean> resetCondition, int expirationTicks) {
        this(condition, resetCondition, null, expirationTicks, expirationTicks, expirationTicks, 0, 1);
    }

    public TimeoutWhile(Callable<Boolean> condition, Callable<Boolean> resetCondition, int minExpiration, int maxExpiration, int targetExpiration, int expirationDeviation) {
        this(condition, resetCondition, null, minExpiration, maxExpiration, targetExpiration, expirationDeviation, 1);
    }

    public TimeoutWhile(Callable<Boolean> condition, Callable<Boolean> resetCondition, int minExpiration, int maxExpiration, int targetExpiration, int expirationDeviation, int frequency) {
        this(condition, resetCondition, null, minExpiration, maxExpiration, targetExpiration, expirationDeviation, frequency);
    }

    public TimeoutWhile(Callable<Boolean> condition, Callable<Boolean> resetCondition, Callable<Boolean> exception, int minExpiration, int maxExpiration, int targetExpiration, int expirationDeviation, int frequency) {
        setCondition(condition);
        setResetCondition(resetCondition);
        setException(exception);
        setExpirationTicks(minExpiration, maxExpiration, targetExpiration, expirationDeviation);
        setFrequency(frequency);
    }


}
