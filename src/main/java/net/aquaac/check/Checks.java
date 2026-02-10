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

        boolean alerts = plugin.getConfig().getBoolean("alerts.enabled", true);
        String perm = plugin.getConfig().getString("alerts.permission", "aquaac.alerts");

        if (alerts) {
            String msg = "§b[AquaAC] §f" + p.getName()
                    + " §7flagged §e" + checkName
                    + " §7(+" + amount + ", VL=" + String.format("%.2f", d.vl) + ") §8" + detail;

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission(perm)) online.sendMessage(msg);
            }
            plugin.getLogger().info(msg.replace('§','&'));
        }

        if (plugin.getConfig().getBoolean("punishments.enabled", true)) {
            int kickVl = plugin.getConfig().getInt("punishments.kick_vl", 20);
            boolean kickOn = plugin.getConfig().getBoolean("punishments.kick_on", true);

            if (kickOn && d.vl >= kickVl) {
                p.kick(org.bukkit.ChatColor.RED + "Kicked: Suspicious activity detected (" + checkName + ")");
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
}
