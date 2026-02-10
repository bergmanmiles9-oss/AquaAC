package net.aquaac.check;

import net.aquaac.AquaAC;
import net.aquaac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class MoveChecks implements Listener {
    private final AquaAC plugin;
    private final Checks core;

    public MoveChecks(AquaAC plugin, Checks core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        core.remove(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) return;
        if (p.isFlying() || p.isInsideVehicle() || p.isGliding() || p.isRiptiding()) return;

        PlayerData d = core.get(p);
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        long now = System.currentTimeMillis();
        d.lastMoveMs = now;

        // ===== Fly (basic airtime) =====
        if (plugin.getConfig().getBoolean("checks.fly.enabled", true)) {
            boolean onGround = p.isOnGround() || to.clone().subtract(0, 0.01, 0).getBlock().getType().isSolid();
            if (!onGround) d.airTicks++;
            else d.airTicks = 0;

            int maxAir = plugin.getConfig().getInt("checks.fly.max_air_ticks", 18);
            if (d.airTicks > maxAir && !nearClimbOrLiquid(to)) {
                core.addVL(p, "Fly", plugin.getConfig().getDouble("checks.fly.vl_add", 3), "airTicks=" + d.airTicks);
            }
        }

        // ===== Speed (basic horizontal) =====
        if (plugin.getConfig().getBoolean("checks.speed.enabled", true)) {
            double maxH = plugin.getConfig().getDouble("checks.speed.max_horizontal", 0.42);

            Vector delta = to.toVector().subtract(from.toVector());
            double horizontal = Math.hypot(delta.getX(), delta.getZ());

            // allow extra on ice, slime, speed potions etc. (very rough)
            double allowance = 0.0;
            if (isIceBelow(to)) allowance += 0.15;
            if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) allowance += 0.10;

            if (horizontal > (maxH + allowance) && !nearClimbOrLiquid(to)) {
                core.addVL(p, "Speed", plugin.getConfig().getDouble("checks.speed.vl_add", 2),
                        "h=" + String.format("%.3f", horizontal));
            }
        }
    }

    private boolean nearClimbOrLiquid(Location loc) {
        Material feet = loc.getBlock().getType();
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        return feet == Material.WATER || feet == Material.LAVA
                || below == Material.WATER || below == Material.LAVA
                || feet == Material.LADDER || feet == Material.VINE;
    }

    private boolean isIceBelow(Location loc) {
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        return below == Material.ICE || below == Material.PACKED_ICE || below == Material.BLUE_ICE || below == Material.FROSTED_ICE;
    }
}
