package id.kingbrezz.aegisac.player;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {

    private final Map<UUID, PlayerData> players =
            new ConcurrentHashMap<>();

    public PlayerData get(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }

        return get(player.getUniqueId(), player.getName());
    }

    public PlayerData get(UUID uniqueId, String name) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("uniqueId cannot be null");
        }

        PlayerData data = players.computeIfAbsent(
                uniqueId,
                ignored -> new PlayerData(uniqueId, name)
        );

        if (name != null && !name.isBlank()) {
            data.setName(name);
        }

        return data;
    }

    public PlayerData find(UUID uniqueId) {
        if (uniqueId == null) {
            return null;
        }

        return players.get(uniqueId);
    }

    public boolean contains(UUID uniqueId) {
        return uniqueId != null && players.containsKey(uniqueId);
    }

    public void remove(Player player) {
        if (player != null) {
            remove(player.getUniqueId());
        }
    }

    public void remove(UUID uniqueId) {
        if (uniqueId == null) {
            return;
        }

        players.remove(uniqueId);
    }

    public void clear(Player player) {
        if (player == null) {
            return;
        }

        PlayerData data = players.get(player.getUniqueId());

        if (data != null) {
            data.clearRuntimeState();
        }
    }

    public void clearAll() {
        players.values().forEach(PlayerData::clearRuntimeState);
        players.clear();
    }

    public int size() {
        return players.size();
    }

    public Map<UUID, PlayerData> snapshot() {
        return Map.copyOf(players);
    }
                                      }
