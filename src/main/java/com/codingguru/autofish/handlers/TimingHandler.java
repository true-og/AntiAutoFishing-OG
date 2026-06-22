package com.codingguru.autofish.handlers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.codingguru.autofish.AntiAutoFish;

/**
 * Detects auto-fishing from the timing of a player's catches, independent of
 * their position.
 *
 * <p>
 * Two complementary signals are combined; either one tripping flags the player:
 *
 * <ul>
 * <li><b>Catch interval</b> &mdash; the coefficient of variation (standard
 * deviation / mean) of the gaps between consecutive catches. A metronomic
 * recast loop collapses this toward zero. Note that vanilla Minecraft
 * randomizes the bite wait (~5-30s), so this signal mostly catches bots that
 * force catches on a fixed timer.</li>
 * <li><b>Reaction time</b> &mdash; the delay between a fish biting
 * ({@code BITE}) and the player reeling it in ({@code CAUGHT_FISH}). Macros
 * reel almost instantly and with near-zero variance; humans react in a few
 * hundred milliseconds and jitter. This is the stronger signal because the
 * vanilla bite timer does not pollute it.</li>
 * </ul>
 *
 * <p>
 * All access happens from Bukkit fishing events on the main server thread, so
 * plain maps are sufficient.
 */
public class TimingHandler {

    private final static TimingHandler INSTANCE = new TimingHandler();

    /**
     * A reaction longer than this is treated as not a real bite-then-reel pair and
     * discarded.
     */
    private static final long MAX_REACTION_NANOS = 10_000L * 1_000_000L;

    private final Map<UUID, Deque<Long>> catchTimes = new HashMap<>();
    private final Map<UUID, Deque<Long>> reactionTimes = new HashMap<>();
    private final Map<UUID, Long> biteTimes = new HashMap<>();

    /**
     * Records the timestamp (nanoseconds) of a catch for interval analysis.
     */
    public void recordCatch(UUID uuid, long timestamp) {

        push(catchTimes.computeIfAbsent(uuid, k -> new ArrayDeque<>()), timestamp);

    }

    /**
     * Records the moment (nanoseconds) a fish bit the player's hook.
     */
    public void recordBite(UUID uuid, long timestamp) {

        biteTimes.put(uuid, timestamp);

    }

    /**
     * Records the reaction time for a catch, derived from the most recent bite for
     * this player. Does nothing if no bite was seen, or if the gap is non-positive
     * or implausibly large.
     */
    public void recordReaction(UUID uuid, long catchTimestamp) {

        final Long bite = biteTimes.remove(uuid);

        if (bite == null)
            return;

        final long reaction = catchTimestamp - bite;

        if (reaction <= 0 || reaction > MAX_REACTION_NANOS)
            return;

        push(reactionTimes.computeIfAbsent(uuid, k -> new ArrayDeque<>()), reaction);

    }

    /**
     * Returns true when either the catch-interval or reaction-time signal looks
     * inhuman.
     */
    public boolean isSuspicious(UUID uuid) {

        if (!isEnabled())
            return false;

        return intervalSuspicious(uuid) || reactionSuspicious(uuid);

    }

    public void clear(UUID uuid) {

        catchTimes.remove(uuid);
        reactionTimes.remove(uuid);
        biteTimes.remove(uuid);

    }

    /* ------------------------------ Signals ------------------------------ */

    private boolean intervalSuspicious(UUID uuid) {

        final long[] stamps = snapshot(catchTimes.get(uuid));

        if (stamps == null || stamps.length < 2)
            return false;

        final long[] gaps = new long[stamps.length - 1];

        for (int i = 1; i < stamps.length; i++) {

            gaps[i - 1] = stamps[i] - stamps[i - 1];

        }

        final double[] stats = meanAndStdDev(gaps);

        if (stats[0] <= 0)
            return false;

        return (stats[1] / stats[0]) < getCvThreshold();

    }

    private boolean reactionSuspicious(UUID uuid) {

        if (!isReactionEnabled())
            return false;

        final long[] reactions = snapshot(reactionTimes.get(uuid));

        if (reactions == null)
            return false;

        final double[] stats = meanAndStdDev(reactions);
        final double meanMs = stats[0] / 1_000_000.0;

        if (meanMs <= 0)
            return false;

        // Inhumanly fast on average.
        if (meanMs < getReactionFloorMs())
            return true;

        // Robotically consistent while still fast.
        final double cv = stats[1] / stats[0];
        return cv < getReactionCvThreshold() && meanMs < getReactionMaxMeanMs();

    }

    /* ------------------------------ Helpers ------------------------------ */

    private void push(Deque<Long> deque, long value) {

        final int sampleSize = getSampleSize();
        deque.addLast(value);

        while (deque.size() > sampleSize) {

            deque.removeFirst();

        }

    }

    /**
     * Returns the deque's contents as an array, or null if it is missing or has
     * fewer than min-catches entries.
     */
    private long[] snapshot(Deque<Long> deque) {

        if (deque == null || deque.size() < getMinCatches())
            return null;

        final long[] values = new long[deque.size()];
        int i = 0;

        for (final long value : deque) {

            values[i++] = value;

        }

        return values;

    }

    /** Returns {mean, populationStandardDeviation} of the given values. */
    private static double[] meanAndStdDev(long[] values) {

        double sum = 0;

        for (final long value : values) {

            sum += value;

        }

        final double mean = sum / values.length;
        double squaredDiff = 0;

        for (final long value : values) {

            final double diff = value - mean;
            squaredDiff += diff * diff;

        }

        return new double[] { mean, Math.sqrt(squaredDiff / values.length) };

    }

    /* ------------------------------ Config ------------------------------- */

    private boolean isEnabled() {

        return AntiAutoFish.getInstance().getConfig().getBoolean("fishing-timing.enabled", true);

    }

    private boolean isReactionEnabled() {

        return AntiAutoFish.getInstance().getConfig().getBoolean("fishing-timing.reaction-enabled", true);

    }

    private int getSampleSize() {

        return Math.max(2, AntiAutoFish.getInstance().getConfig().getInt("fishing-timing.sample-size", 15));

    }

    private int getMinCatches() {

        final int min = AntiAutoFish.getInstance().getConfig().getInt("fishing-timing.min-catches", 10);
        return Math.max(2, Math.min(min, getSampleSize()));

    }

    private double getCvThreshold() {

        return clampCv(AntiAutoFish.getInstance().getConfig().getDouble("fishing-timing.cv-threshold", 0.10));

    }

    private double getReactionCvThreshold() {

        return clampCv(AntiAutoFish.getInstance().getConfig().getDouble("fishing-timing.reaction-cv-threshold", 0.15));

    }

    private double getReactionFloorMs() {

        return Math.max(0, AntiAutoFish.getInstance().getConfig().getDouble("fishing-timing.reaction-floor-ms", 120));

    }

    private double getReactionMaxMeanMs() {

        return Math.max(0,
                AntiAutoFish.getInstance().getConfig().getDouble("fishing-timing.reaction-max-mean-ms", 400));

    }

    /**
     * Keeps a coefficient-of-variation threshold within (0, 1]; a misconfigured
     * value falls back to the default.
     */
    private static double clampCv(double raw) {

        if (raw <= 0)
            return 0.10;

        return Math.min(raw, 1.0);

    }

    public static TimingHandler getInstance() {

        return INSTANCE;

    }

}
