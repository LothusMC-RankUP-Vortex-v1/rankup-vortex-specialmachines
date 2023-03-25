package br.net.rankup.specialmachine.adpter;

import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import org.bukkit.configuration.ConfigurationSection;

public class UpgradeAdpter {

    public static TimeUpgrade writeTime(ConfigurationSection section) {
        int indentifier = section.getInt("indentifier");
        double price = section.getDouble("price");
        int value = section.getInt("value");
        return  new TimeUpgrade(indentifier, price, value);
    }

    public static DropUpgrade writeDrop(ConfigurationSection section) {
        int indentifier = section.getInt("indentifier");
        double price = section.getDouble("price");
        int value = section.getInt("value");
        return new DropUpgrade(indentifier, price, value);
    }

}
