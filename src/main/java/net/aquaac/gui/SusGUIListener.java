package net.aquaac.gui;

import net.aquaac.inspect.InspectManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SusGUIListener implements Listener {
    private final InspectManager inspect;

    public SusGUIListener(InspectManager inspect) {
        this.inspect = inspect;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player admin)) return;
        if (e.getView().getTitle() == null) return;

        if (!e.getView().getTitle().equals(SusGUI.TITLE)) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() != Material.PAPER) return;
        if (!item.hasItemMeta() || item.getItemMeta() == null) return;
        if (!item.getItemMeta().hasDisplayName()) return;

        String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            admin.sendMessage("§cPlayer went offline.");
            admin.closeInventory();
            return;
        }

        // Save state if not already inspecting
        if (!inspect.isInspecting(admin.getUniqueId())) {
            inspect.save(admin.getUniqueId(), admin.getGameMode(), admin.getLocation());
        }

        admin.closeInventory();
        admin.setGameMode(GameMode.SPECTATOR);
        admin.teleport(target.getLocation());
        admin.sendMessage("§bInspecting §f" + target.getName() + "§b (spectator). Use §f/inspectoff§b to return.");
    }
}
