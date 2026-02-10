package net.aquaac.gui;

import net.aquaac.check.Checks;
import net.aquaac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SusGUI {
    public static final String TITLE = ChatColor.DARK_AQUA + "Suspicious Players";

    public static Inventory build(Checks core, double minVl) {
        // 6 rows to fit a lot
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData d = core.get(p);
            if (d.vl < minVl) continue;

            if (slot >= inv.getSize()) break;

            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + p.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "VL: " + ChatColor.YELLOW + String.format("%.2f", d.vl));
                lore.add(ChatColor.DARK_GRAY + "Click to spectate + teleport");
                meta.setLore(lore);

                paper.setItemMeta(meta);
            }

            inv.setItem(slot++, paper);
        }

        return inv;
    }
}
