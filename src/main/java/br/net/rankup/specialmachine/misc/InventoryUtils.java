package br.net.rankup.specialmachine.misc;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    static List<String> list = new ArrayList<>();

    public static List<String> getList() {
        return list;
    }

    public static void addDelay(Player player) {
        if(list.contains(player.getName())) return;
        list.add(player.getName());
        Bukkit.getServer().getScheduler().runTaskLater((Plugin) SpecialMachinePlugin.getInstance(), (Runnable)new Runnable() {
            @Override
            public void run() {
                list.remove(player.getName());
            }
        }, 3L);

     }
}
