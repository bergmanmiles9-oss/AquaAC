package net.aquaac.check;

import net.aquaac.AquaAC;
import net.aquaac.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Deque;

public class CombatChecks implements Listener {
    private final AquaAC plugin;
    private final Checks core;

    public CombatChecks(AquaAC plugin, Checks core) {
        this.plugin = plugin;
        this.core = core;
    }

    // ===== AutoClicker (based on attack events) =====
    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;

        if (!plugin.getConfig().getBoolean("checks.autoclicker.enabled", true)) return;

        PlayerData d = core.get(p);
        long now = System.currentTimeMillis();

        d.clickTimesMs.addLast(now);

        long window = plugin.getConfig().getLong("checks.autoclicker.sample_ms", 1200);
        trimOlder(d.clickTimesMs, now - window);

        double cps = d.clickTimesMs.size() / (window / 1000.0);

        // compute variance of intervals (auto-clickers often have very “perfect” timing)
        double variance = intervalVarianceMs(d.clickTimesMs);

        double maxCps = plugin.getConfig().getDouble("checks.autoclicker.max_cps", 16.5);
        double minVar = plugin.getConfig().getDouble("checks.autoclicker.min_variance_ms", 3.0);

        if (cps > maxCps && variance < minVar) {
            core.addVL(p, "AutoClicker", plugin.getConfig().getDouble("checks.autoclicker.vl_add", 3),
                    "cps=" + String.format("%.1f", cps) + " var=" + String.format("%.2f", variance));
        }
    }

    // ===== Fast Totem (totem pops + swap spam) =====
    @EventHandler(ignoreCancelled = true)
    public void onTotemPop(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!plugin.getConfig().getBoolean("checks.fast_totem.enabled", true)) return;

        PlayerData d = core.get(p);
        long now = System.currentTimeMillis();
        d.totemPopTimesMs.addLast(now);

        long popWindow = plugin.getConfig().getLong("checks.fast_totem.pop_window_ms", 2500);
        trimOlder(d.totemPopTimesMs, now - popWindow);

        int maxPops = plugin.getConfig().getInt("checks.fast_totem.max_pops_in_window", 2);
        if (d.totemPopTimesMs.size() > maxPops) {
            core.addVL(p, "FastTotem(Pops)", plugin.getConfig().getDouble("checks.fast_totem.vl_add", 4),
                    "pops=" + d.totemPopTimesMs.size() + "/" + popWindow + "ms");
        }
    }

    // offhand swap speed (shift-clicking or moving to offhand)
    @EventHandler(ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!plugin.getConfig().getBoolean("checks.fast_totem.enabled", true)) return;

        // If they interact with a totem in inventory, we track swap spam.
        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();
        boolean touchingTotem = (current != null && current.getType() == Material.TOTEM_OF_UNDYING)
                || (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING);

        if (!touchingTotem) return;

        PlayerData d = core.get(p);
        long now = System.currentTimeMillis();
        d.offhandSwapTimesMs.addLast(now);

        long swapWindow = plugin.getConfig().getLong("checks.fast_totem.swap_window_ms", 400);
        trimOlder(d.offhandSwapTimesMs, now - swapWindow);

        int maxSwaps = plugin.getConfig().getInt("checks.fast_totem.max_swaps_in_window", 3);
        if (d.offhandSwapTimesMs.size() > maxSwaps) {
            core.addVL(p, "FastTotem(SwapSpam)", plugin.getConfig().getDouble("checks.fast_totem.vl_add", 4),
                    "swaps=" + d.offhandSwapTimesMs.size() + "/" + swapWindow + "ms");
        }
    }

    // ===== Fast Crystal place =====
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) item = p.getInventory().getItemInMainHand();

        long now = System.currentTimeMillis();

        // End Crystal place rate
        if (plugin.getConfig().getBoolean("checks.fast_crystal.enabled", true)
                && item != null && item.getType() == Material.END_CRYSTAL) {

            PlayerData d = core.get(p);
            d.crystalPlaceTimesMs.addLast(now);

            long win = plugin.getConfig().getLong("checks.fast_crystal.window_ms", 1000);
            trimOlder(d.crystalPlaceTimesMs, now - win);

            int max = plugin.getConfig().getInt("checks.fast_crystal.max_place_per_window", 8);
            if (d.crystalPlaceTimesMs.size() > max) {
                core.addVL(p, "FastCrystal", plugin.getConfig().getDouble("checks.fast_crystal.vl_add", 3),
                        "places=" + d.crystalPlaceTimesMs.size() + "/" + win + "ms");
            }
        }

        // Respawn Anchor spam (charge/use)
        if (plugin.getConfig().getBoolean("checks.fast_anchor.enabled", true)) {
            Material hand = (item != null ? item.getType() : Material.AIR);

            boolean isAnchorRelated =
                    hand == Material.RESPAWN_ANCHOR || hand == Material.GLOWSTONE
                    || (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.RESPAWN_ANCHOR);

            if (isAnchorRelated) {
                PlayerData d = core.get(p);
                d.anchorInteractTimesMs.addLast(now);

                long win = plugin.getConfig().getLong("checks.fast_anchor.window_ms", 1000);
                trimOlder(d.anchorInteractTimesMs, now - win);

                int max = plugin.getConfig().getInt("checks.fast_anchor.max_interacts_per_window", 10);
                if (d.anchorInteractTimesMs.size() > max) {
                    core.addVL(p, "FastAnchor", plugin.getConfig().getDouble("checks.fast_anchor.vl_add", 3),
                            "actions=" + d.anchorInteractTimesMs.size() + "/" + win + "ms");
                }
            }
        }
    }

    // ===== helpers =====
    private void trimOlder(Deque<Long> q, long minTime) {
        while (!q.isEmpty() && q.peekFirst() < minTime) q.pollFirst();
    }

    private double intervalVarianceMs(Deque<Long> times) {
        if (times.size() < 6) return 9999.0;

        Long[] arr = times.toArray(new Long[0]);
        double[] intervals = new double[arr.length - 1];
        for (int i = 1; i < arr.length; i++) intervals[i - 1] = arr[i] - arr[i - 1];

        double mean = 0;
        for (double v : intervals) mean += v;
        mean /= intervals.length;

        double var = 0;
        for (double v : intervals) {
            double d = v - mean;
            var += d * d;
        }
        var /= intervals.length;
        return Math.sqrt(var); // std dev
    }
}
