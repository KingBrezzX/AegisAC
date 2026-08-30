package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CheckManager {

    private final AegisAC plugin;
    private final List<Check> checks = new ArrayList<>();

    public CheckManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );
    }

    /**
     * Registers a check.
     */
    public synchronized void register(Check check) {
        Objects.requireNonNull(
                check,
                "check"
        );

        if (!contains(check.getName())) {
            checks.add(check);
        }
    }

    /**
     * Registers multiple checks.
     */
    public synchronized void registerAll(
            Collection<? extends Check> collection
    ) {
        if (collection == null || collection.isEmpty()) {
            return;
        }

        for (Check check : collection) {
            if (check != null) {
                register(check);
            }
        }
    }

    /**
     * Removes a check instance.
     */
    public synchronized void unregister(Check check) {
        if (check == null) {
            return;
        }

        checks.remove(check);
    }

    /**
     * Removes a check by technical name.
     */
    public synchronized void unregister(String name) {
        if (name == null) {
            return;
        }

        checks.removeIf(check ->
                check != null
                        && check.getName().equalsIgnoreCase(name)
        );
    }

    /**
     * Removes all registered checks.
     */
    public synchronized void clear() {
        checks.clear();
    }

    /**
     * Returns an immutable snapshot of registered checks.
     */
    public synchronized List<Check> getChecks() {
        return Collections.unmodifiableList(
                new ArrayList<>(checks)
        );
    }

    /**
     * Finds a check by name.
     */
    public synchronized Check getCheck(String name) {
        if (name == null) {
            return null;
        }

        for (Check check : checks) {
            if (check != null
                    && check.getName().equalsIgnoreCase(name)) {
                return check;
            }
        }

        return null;
    }

    public synchronized boolean contains(String name) {
        return getCheck(name) != null;
    }

    public synchronized int size() {
        return checks.size();
    }

    /*
     * ------------------------------------------------------------
     * PLAYER LIFECYCLE
     * ------------------------------------------------------------
     */

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        forEach(check -> check.onJoin(player));
    }

    public void handleDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            check.onDeath(player, data);
        });
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        /*
         * Quit is deliberately dispatched even when a check has been
         * disabled, allowing it to clean up temporary state.
         */
        forEach(check -> check.onQuit(player));
    }

    /*
     * ------------------------------------------------------------
     * MOVEMENT
     * ------------------------------------------------------------
     */

    public boolean handleMove(
            DetectionEngine.MovementContext context
    ) {
        if (context == null
                || context.player() == null
                || context.from() == null
                || context.to() == null) {
            return false;
        }

        boolean violation = false;

        for (Check check : snapshot()) {
            if (!isEnabled(check)
                    || !check.isMovementCheck()) {
                continue;
            }

            try {
                if (check.onMove(context)) {
                    violation = true;
                }
            } catch (Throwable throwable) {
                handleCheckError(
                        check,
                        throwable
                );
            }
        }

        return violation;
    }

    public void handleRotation(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null
                || from == null
                || to == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)
                    || !check.isRotationCheck()) {
                return;
            }

            check.onRotation(
                    player,
                    from,
                    to,
                    data
            );
        });
    }

    /*
     * ------------------------------------------------------------
     * TELEPORT / WORLD
     * ------------------------------------------------------------
     */

    public void handleTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null || to == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            check.onTeleport(
                    player,
                    from,
                    to,
                    cause,
                    data
            );
        });
    }

    public void handleWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            check.onWorldChange(
                    player,
                    data
            );
        });
    }

    /*
     * ------------------------------------------------------------
     * COMBAT
     * ------------------------------------------------------------
     */

    public void handleAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null
                || target == null
                || event == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)
                    || !check.isCombatCheck()) {
                return;
            }

            check.onAttack(
                    player,
                    target,
                    event,
                    data
            );
        });
    }

    /*
     * ------------------------------------------------------------
     * BLOCK
     * ------------------------------------------------------------
     */

    public void handleBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null || event == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)
                    || !check.isBlockCheck()) {
                return;
            }

            check.onBlockPlace(
                    player,
                    event,
                    data
            );
        });
    }

    public void handleBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (player == null || event == null) {
            return;
        }

        forEach(check -> {
            if (!isEnabled(check)
                    || !check.isBlockCheck()) {
                return;
            }

            check.onBlockBreak(
                    player,
                    event,
                    data
            );
        });
    }

    /*
     * ------------------------------------------------------------
     * INTERNAL
     * ------------------------------------------------------------
     */

    private void forEach(CheckConsumer consumer) {
        if (consumer == null) {
            return;
        }

        for (Check check : snapshot()) {
            try {
                consumer.accept(check);
            } catch (Throwable throwable) {
                handleCheckError(
                        check,
                        throwable
                );
            }
        }
    }

    private synchronized List<Check> snapshot() {
        return new ArrayList<>(checks);
    }

    private boolean isEnabled(Check check) {
        return check != null
                && check.isEnabled()
                && plugin.isEnabled();
    }

    private void handleCheckError(
            Check check,
            Throwable throwable
    ) {
        String name = check == null
                ? "unknown"
                : check.getName();

        plugin.getLogger().warning(
                "Check '" + name + "' failed: "
                        + throwable.getClass().getSimpleName()
                        + ": "
                        + String.valueOf(
                        throwable.getMessage()
                )
        );
    }

    @FunctionalInterface
    private interface CheckConsumer {

        void accept(Check check);
    }
                }
