package net.aquaac.data;

import org.bukkit.Location;

import java.util.ArrayDeque;
import java.util.Deque;

public class PlayerData {
    // movement
    public Location lastLoc;
    public long lastMoveMs = 0;
    public int airTicks = 0;

    // VL
    public double vl = 0.0;
    public long lastVlDecayMs = System.currentTimeMillis();

    // autoclicker
    public final Deque<Long> clickTimesMs = new ArrayDeque<>();

    // fast totem
    public final Deque<Long> totemPopTimesMs = new ArrayDeque<>();
    public final Deque<Long> offhandSwapTimesMs = new ArrayDeque<>();

    // crystal / anchor
    public final Deque<Long> crystalPlaceTimesMs = new ArrayDeque<>();
    public final Deque<Long> anchorInteractTimesMs = new ArrayDeque<>();
}
