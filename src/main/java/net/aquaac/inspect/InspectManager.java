package net.aquaac.inspect;

import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InspectManager {
    public static class InspectState {
        public final GameMode oldMode;
        public final Location oldLoc;

        public InspectState(GameMode oldMode, Location oldLoc) {
            this.oldMode = oldMode;
            this.oldLoc = oldLoc;
        }
    }

    private final Map<UUID, InspectState> state = new ConcurrentHashMap<>();

    public void save(UUID adminId, GameMode mode, Location loc) {
        state.put(adminId, new InspectState(mode, loc));
    }

    public InspectState get(UUID adminId) {
        return state.get(adminId);
    }

    public void clear(UUID adminId) {
        state.remove(adminId);
    }

    public boolean isInspecting(UUID adminId) {
        return state.containsKey(adminId);
    }
}
