package net.aquaac.check;

import net.aquaac.AquaAC;
import net.aquaac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Checks implements Listener {

    private final AquaAC plugin;
    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();

    public final MoveChecks moveChecks;
    public final CombatChecks combatChecks;

    public Checks(AquaAC plugin) {
        this.plugin = plugin;
        this.moveChecks = new MoveChecks(plugin, this);
        this.combatChecks = new CombatChecks(plugin, this);

        Bukkit.getPluginManager().registerEvents(moveChecks, plugin);
        Bukkit.getPluginManager().registerEvents(combatChecks, plugin);
    }

    public PlayerData get(Player p) {
        return data.computeIfAbsent(p.getUniqueId(), k -> new PlayerData());
    }

    public void remove(Player p) {
        data.remove(p.getUniqueId());
    }

    public void addVL(Player p, String checkName, double amount, String detail) {
        if (p.hasPermission("aquaac.bypass")) return;

        PlayerData d = get(p);
        decayVL(d);

        d.vl += amount;

        // ===== Alerts =====
        boolean alertsEnabled = plugin.getConfig().getBoolean("alerts.enabled", true);
        String perm = plugin.getConfig().getString("alerts.permission", "aquaac.alerts");

        String msg = "§b[AquaAC] §f" + p.getName()
                + " §7flagged §e" + checkName
                + " §7(+" + amount + ", VL=" + String.format("%.2f", d.vl) + ") §8" + detail;

        if (alertsEnabled) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission(perm)) {
                    online.sendMessage(msg);
                }
            }
            plugin.getLogger().info(msg.replace('§', '&'));
        }

        // ===== Per-check action =====
        String key = mapCheckKey(checkName);
        PunishAction action = PunishAction.from(
                plugin.getConfig().getString("checks." + key + ".action", "ALERT")
        );

        switch (action) {
            case NONE:
            case ALERT:
                return;

            case SETBACK: {
                double dist = plugin.getConfig().getDouble("punishments.setback_distance", 0.7);

                if (d.lastLoc != null) {
                    var back = d.lastLoc.clone();
                    var now = p.getLocation();
                    var dir = back.toVector().subtract(now.toVector());

                    if (dir.lengthSquared() > 0.001) {
                        dir.normalize().multiply(dist);
                        var tp = now.clone().add(dir);
                        tp.setYaw(now.getYaw());
                        tp.setPitch(now.getPitch());
                        p.teleport(tp);
                    } else {
                        p.teleport(back);
                    }
                }
                return;
            }

            case KICK: {
                String kickMsg = plugin.getConfig().getString(
                        "punishments.kick_message",
                        "&cKicked: Suspicious activity detected (%check%)"
                );

                kickMsg = org.bukkit.ChatColor.translateAlternateColorCodes(
                        '&',
                        kickMsg.replace("%check%", checkName)
                );

                p.kick(org.bukkit.ChatColor.RESET + kickMsg);
                return;
            }

            case BAN: {
                String cmd = plugin.getConfig().getString(
                        "punishments.ban_command",
                        "ban %player% Cheating (%check%)"
                );

                cmd = cmd.replace("%player%", p.getName())
                         .replace("%check%", checkName);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                return;
            }
        }
    }

    private void decayVL(PlayerData d) {
        long now = System.currentTimeMillis();
        double perSecond = plugin.getConfig().getDouble("vl.decay_per_second", 0.25);

        long elapsed = now - d.lastVlDecayMs;
        if (elapsed <= 0) return;

        double dec = (elapsed / 1000.0) * perSecond;
        d.vl = Math.max(0.0, d.vl - dec);
        d.lastVlDecayMs = now;
    }

    // ===== Helper: map display name → config key =====
    private String mapCheckKey(String checkName) {
        String c = checkName.toLowerCase();
        if (c.startsWith("fly")) return "fly";
        if (c.startsWith("speed")) return "speed";
        if (c.startsWith("autoclicker")) return "autoclicker";
        if (c.startsWith("fasttotem")) return "fast_totem";
        if (c.startsWith("fastcrystal")) return "fast_crystal";
        if (c.startsWith("fastanchor")) return "fast_anchor";
        return c.replace(" ", "_");
    }
}
