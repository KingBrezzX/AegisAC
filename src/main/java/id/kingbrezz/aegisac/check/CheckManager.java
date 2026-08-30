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
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void register(Check check) {
        Objects.requireNonNull(check, "check");

        if (!checks.contains(check)) {
            checks.add(check);
        }
    }

    public void registerAll(Collection<? extends Check> checks) {
        if (checks == null) {
            return;
        }

        for (Check check : checks) {
            register(check);
        }
    }

    public void unregister(Check check) {
        if (check != null) {
            checks.remove(check);
        }
    }

    public void clear() {
        checks.clear();
    }

    public List<Check> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    public int size() {
        return checks.size();
    }

    public void handleJoin(Player player) {
        forEach(check -> check.onJoin(player));
    }

    public boolean handleMove(
            DetectionEngine.MovementContext context
    ) {
        boolean violation = false;

        for (Check check : snapshot()) {
            if (!isEnabled(check)) {
                continue;
            }

            if (!check.isMovementCheck()) {
                continue;
            }

            try {
                if (check.onMove(context)) {
                    violation = true;
                }
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
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
        forEach(check -> {
            if (!isEnabled(check) || !check.isRotationCheck()) {
                return;
            }

            try {
                check.onRotation(player, from, to, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            try {
                check.onTeleport(player, from, to, cause, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            try {
                check.onWorldChange(player, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check) || !check.isCombatCheck()) {
                return;
            }

            try {
                check.onAttack(player, target, event, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check) || !check.isBlockCheck()) {
                return;
            }

            try {
                check.onBlockPlace(player, event, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check) || !check.isBlockCheck()) {
                return;
            }

            try {
                check.onBlockBreak(player, event, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        forEach(check -> {
            if (!isEnabled(check)) {
                return;
            }

            try {
                check.onDeath(player, data);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    public void handleQuit(Player player) {
        forEach(check -> {
            try {
                check.onQuit(player);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        });
    }

    private void forEach(CheckConsumer consumer) {
        for (Check check : snapshot()) {
            try {
                consumer.accept(check);
            } catch (Throwable throwable) {
                handleCheckError(check, throwable);
            }
        }
    }

    private List<Check> snapshot() {
        return List.copyOf(checks);
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
                        + throwable.getMessage()
        );
    }

    @FunctionalInterface
    private interface CheckConsumer {
        void accept(Check check);
    }
                                 }
