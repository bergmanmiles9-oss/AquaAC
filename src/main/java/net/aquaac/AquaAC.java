package net.aquaac;

import net.aquaac.check.Checks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class AquaAC extends JavaPlugin {
    private Checks checks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.checks = new Checks(this);

        Bukkit.getPluginManager().registerEvents(checks, this);
        getLogger().info("AquaAC enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AquaAC disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("aquaac")) return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("§aAquaAC config reloaded.");
            return true;
        }
        sender.sendMessage("§eUsage: /aquaac reload");
        return true;
    }
}
