package net.aquaac.commands;

import net.aquaac.inspect.InspectManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class InspectOffCommand implements CommandExecutor {
    private final InspectManager inspect;

    public InspectOffCommand(InspectManager inspect) {
        this.inspect = inspect;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        InspectManager.InspectState st = inspect.get(admin.getUniqueId());
        if (st == null) {
            admin.sendMessage("§eYou are not inspecting anyone.");
            return true;
        }

        admin.teleport(st.oldLoc);
        admin.setGameMode(st.oldMode);
        inspect.clear(admin.getUniqueId());
        admin.sendMessage("§aReturned from inspect mode.");
        return true;
    }
}
