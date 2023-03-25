package br.net.rankup.specialmachine.manager;

import br.net.rankup.specialmachine.adpter.UpgradeAdpter;
import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;

public class UpgradeManager {

    //time
    private HashMap<Integer, TimeUpgrade> upgradeTime;

    public HashMap<Integer, TimeUpgrade> getTime() {
        return upgradeTime;
    }
    //xp
    private HashMap<Integer, DropUpgrade> dropUpgradeHashMap;

    public HashMap<Integer, DropUpgrade> getXP() {
        return dropUpgradeHashMap;
    }

    public void load() {
        upgradeTime = new HashMap<>();
        dropUpgradeHashMap = new HashMap<>();
        for (final String name : SpecialMachinePlugin.getConfiguration().getConfigurationSection("upgrades.times").getKeys(false)) {
            final ConfigurationSection section = SpecialMachinePlugin.getConfiguration().getConfigurationSection("upgrades.times." + name);
            TimeUpgrade timeUpgrade = UpgradeAdpter.writeTime(section);
            this.addTime(timeUpgrade.getIndentifier(), timeUpgrade);
        }

        for (final String name : SpecialMachinePlugin.getConfiguration().getConfigurationSection("upgrades.drop").getKeys(false)) {
            final ConfigurationSection section = SpecialMachinePlugin.getConfiguration().getConfigurationSection("upgrades.drop." + name);
            DropUpgrade dropUpgrade = UpgradeAdpter.writeDrop(section);
            this.addDrop(dropUpgrade.getIndentifier(), dropUpgrade);
        }

    }


    //xp
    public void addDrop(int indentifer, DropUpgrade dropUpgrade) {
        if(!dropUpgradeHashMap.containsKey(indentifer)) {
            this.dropUpgradeHashMap.put(indentifer, dropUpgrade);
        }
    }

    public DropUpgrade getDrop(int indentifier) {
        if(dropUpgradeHashMap.containsKey(indentifier)) {
            return dropUpgradeHashMap.get(indentifier);
        }
        return null;
    }

    public DropUpgrade getNextDrop(int indentifier) {
        if(dropUpgradeHashMap.containsKey(indentifier+1)) {
            return dropUpgradeHashMap.get(indentifier+1);
        }
        return null;
    }

    public boolean hasNextXP(int indentifier) {
        return this.dropUpgradeHashMap.containsKey(indentifier+1);
    }

    //time
    public void addTime(int indentifer, TimeUpgrade time) {
        if(!upgradeTime.containsKey(indentifer)) {
            this.upgradeTime.put(indentifer, time);
        }
    }

    public TimeUpgrade getTime(int indentifier) {
        if(upgradeTime.containsKey(indentifier)) {
            return upgradeTime.get(indentifier);
        }
        return null;
    }

    public TimeUpgrade getNext(int indentifier) {
        if(upgradeTime.containsKey(indentifier+1)) {
            return upgradeTime.get(indentifier+1);
        }
        return null;
    }

    public boolean hasNextTime(int indentifier) {
        return this.upgradeTime.containsKey(indentifier+1);
    }

}
