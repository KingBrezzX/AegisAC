package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import id.kingbrezz.aegisac.violation.ViolationEvent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base implementation for AegisAC checks.
 *
 * Provides lightweight per-player violation tracking,
 * decay and violation event creation.
 */
public abstract class AbstractCheck implements Check {

    protected final AegisAC plugin;

    private final String name;

    private final Map<UUID, ViolationState> violations =
            new ConcurrentHashMap<>();

    private volatile boolean enabled = true;

    protected AbstractCheck(
            AegisAC plugin,
            String name
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be null or blank"
            );
        }

        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }

    /*
     * ------------------------------------------------------------
     * VIOLATION SYSTEM
     * ------------------------------------------------------------
     */

    /**
     * Adds violation level to a player.
     *
     * @return resulting violation level
     */
    protected final double fail(
            Player player,
            double amount
    ) {
        if (player == null
                || !player.isOnline()
                || !Double.isFinite(amount)
                || amount <= 0.0D) {
            return 0.0D;
        }

        ViolationState state =
                violations.computeIfAbsent(
                        player.getUniqueId(),
                        ignored -> new ViolationState()
                );

        long now =
                System.currentTimeMillis();

        state.decay(now);

        state.violationLevel =
                Math.min(
                        100.0D,
                        state.violationLevel
                                + amount
                );

        state.lastViolation =
                now;

        return state.violationLevel;
    }

    /**
     * Reduces violation level for a player.
     */
    protected final void reward(
            Player player,
            double amount
    ) {
        if (player == null
                || !Double.isFinite(amount)
                || amount <= 0.0D) {
            return;
        }

        ViolationState state =
                violations.get(
                        player.getUniqueId()
                );

        if (state == null) {
            return;
        }

        long now =
                System.currentTimeMillis();

        state.decay(now);

        state.violationLevel =
                Math.max(
                        0.0D,
                        state.violationLevel
                                - amount
                );
    }

    /**
     * Gets current violation level.
     */
    protected final double getViolationLevel(
            Player player
    ) {
        if (player == null) {
            return 0.0D;
        }

        return getViolationLevel(
                player.getUniqueId()
        );
    }

    protected final double getViolationLevel(
            UUID uuid
    ) {
        if (uuid == null) {
            return 0.0D;
        }

        ViolationState state =
                violations.get(uuid);

        if (state == null) {
            return 0.0D;
        }

        state.decay(
                System.currentTimeMillis()
        );

        return state.violationLevel;
    }

    /**
     * Resets a player's violation level.
     */
    protected final void resetViolations(
            Player player
    ) {
        if (player != null) {
            resetViolations(
                    player.getUniqueId()
            );
        }
    }

    protected final void resetViolations(
            UUID uuid
    ) {
        if (uuid != null) {
            violations.remove(uuid);
        }
    }

    /**
     * Clears all runtime state.
     */
    protected final void clearViolations() {
        violations.clear();
    }

    /*
     * ------------------------------------------------------------
     * ALERT / EVENT
     * ------------------------------------------------------------
     */

    /**
     * Creates and dispatches a violation event.
     *
     * @return true when the event was dispatched
     */
    protected final boolean flag(
            Player player,
            double amount,
            double confidence,
            String action,
            String detail
    ) {
        if (player == null
                || !player.isOnline()
                || player.isDead()) {
            return false;
        }

        if (isExempt(player)) {
            return false;
        }

        if (!Double.isFinite(confidence)) {
            confidence = 0.0D;
        }

        confidence =
                Math.max(
                        0.0D,
                        Math.min(
                                1.0D,
                                confidence
                        )
                );

        double vl =
                fail(
                        player,
                        amount
                );

        ViolationEvent event =
                new ViolationEvent(
                        player,
                        name,
                        vl,
                        confidence,
                        getPing(player),
                        action,
                        detail
                );

        if (plugin.getAlertManager() != null) {
            plugin.getAlertManager()
                    .handle(event);
        }

        return true;
    }

    /**
     * Shortcut for a normal detection.
     */
    protected final boolean flag(
            Player player,
            double amount,
            double confidence,
            String detail
    ) {
        return flag(
                player,
                amount,
                confidence,
                "none",
                detail
        );
    }

    /*
     * ------------------------------------------------------------
     * COOLDOWN
     * ------------------------------------------------------------
     */

    /**
     * Returns true if the check should currently wait
     * before producing another alert.
     */
    protected final boolean isOnCooldown(
            Player player,
            long cooldownMillis
    ) {
        if (player == null
                || cooldownMillis <= 0L) {
            return false;
        }

        ViolationState state =
                violations.get(
                        player.getUniqueId()
                );

        if (state == null
                || state.lastViolation <= 0L) {
            return false;
        }

        long elapsed =
                System.currentTimeMillis()
                        - state.lastViolation;

        return elapsed >= 0L
                && elapsed < cooldownMillis;
    }

    /**
     * Returns milliseconds since the last violation.
     */
    protected final long millisSinceViolation(
            Player player
    ) {
        if (player == null) {
            return Long.MAX_VALUE;
        }

        ViolationState state =
                violations.get(
                        player.getUniqueId()
                );

        if (state == null
                || state.lastViolation <= 0L) {
            return Long.MAX_VALUE;
        }

        return Math.max(
                0L,
                System.currentTimeMillis()
                        - state.lastViolation
        );
    }

    /*
     * ------------------------------------------------------------
     * EXEMPTION
     * ------------------------------------------------------------
     */

    protected boolean isExempt(
            Player player
    ) {
        if (player == null) {
            return true;
        }

        if (player.hasPermission(
                "aegisac.bypass"
        )) {
            return true;
        }

        PlayerDataManager.PlayerData data =
                plugin.getPlayerDataManager()
                        .get(player);

        return data.isExempt();
    }

    /*
     * ------------------------------------------------------------
     * PLAYER LIFECYCLE
     * ------------------------------------------------------------
     */

    @Override
    public void onJoin(Player player) {
        if (player != null) {
            resetViolations(player);
        }
    }

    @Override
    public void onTeleport(
            Player player,
            org.bukkit.Location from,
            org.bukkit.Location to,
            org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
        resetViolations(player);
    }

    @Override
    public void onWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        resetViolations(player);
    }

    @Override
    public void onDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        resetViolations(player);
    }

    @Override
    public void onQuit(Player player) {
        resetViolations(player);
    }

    /*
     * ------------------------------------------------------------
     * UTILITY
     * ------------------------------------------------------------
     */

    protected final int getPing(
            Player player
    ) {
        if (player == null) {
            return 0;
        }

        try {
            return Math.max(
                    0,
                    player.getPing()
            );
        } catch (Throwable ignored) {
            return 0;
        }
    }

    protected final AegisAC getPlugin() {
        return plugin;
    }

    /*
     * ------------------------------------------------------------
     * INTERNAL STATE
     * ------------------------------------------------------------
     */

    private static final class ViolationState {

        private double violationLevel;
        private long lastViolation;

        private long lastDecay;

        private void decay(long now) {
            if (lastDecay == 0L) {
                lastDecay = now;
                return;
            }

            long elapsed =
                    now - lastDecay;

            if (elapsed < 1000L) {
                return;
            }

            /*
             * Decay approximately 1 VL per second.
             *
             * The state is intentionally bounded so a player
             * cannot accumulate unbounded VL.
             */
            double seconds =
                    elapsed / 1000.0D;

            violationLevel =
                    Math.max(
                            0.0D,
                            violationLevel
                                    - seconds
                    );

            lastDecay = now;
        }
    }
    }
