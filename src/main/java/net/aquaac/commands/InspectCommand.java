package net.aquaac.commands;

import net.aquaac.AquaAC;
import net.aquaac.inspect.InspectManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class InspectCommand implements CommandExecutor {
    private final AquaAC plugin;
    private final InspectManager inspect;

    public InspectCommand(AquaAC plugin, InspectManager inspect) {
        this.plugin = plugin;
        this.inspect = inspect;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        if (args.length < 1) {
            admin.sendMessage("§eUsage: /inspect <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            admin.sendMessage("§cThat player is not online.");
            return true;
        }

        // Save current state if not already inspecting
        if (!inspect.isInspecting(admin.getUniqueId())) {
            inspect.save(admin.getUniqueId(), admin.getGameMode(), admin.getLocation());
        }

        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(target.getLocation());
        admin.sendMessage("§bInspecting §f" + target.getName() + "§b (spectator). Use §f/inspectoff§b to return.");
        return true;
    }
}
