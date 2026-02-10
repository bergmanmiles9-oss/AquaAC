package net.aquaac.commands;

import net.aquaac.AquaAC;
import net.aquaac.check.Checks;
import net.aquaac.gui.SusGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SusCommand implements CommandExecutor {
    private final AquaAC plugin;
    private final Checks core;

    public SusCommand(AquaAC plugin, Checks core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        double minVl = plugin.getConfig().getDouble("sus.min_vl", 8.0);
        admin.openInventory(SusGUI.build(core, minVl));
        return true;
    }
}
