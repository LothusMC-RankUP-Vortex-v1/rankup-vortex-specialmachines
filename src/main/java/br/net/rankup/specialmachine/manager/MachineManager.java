package br.net.rankup.specialmachine.manager;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.ItemBuilder;
import br.net.rankup.specialmachine.misc.Toolchain;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import br.net.rankup.specialmachine.model.machines.UpgradeModel;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.stream.Collectors;

public class MachineManager {

    private HashMap<Location, MachineModel> machines;

    public void load() {
        machines = new HashMap<>();
    }

    public HashMap<Location, MachineModel> getSpawners() {
        return machines;
    }

    public ItemStack getItemStack(String machine, UpgradeModel upgradeModel, double amount) {
        ConfigurationSection section = SpecialMachinePlugin.getConfiguration().getConfigurationSection("machines."+machine);

        if(section == null) return null;
        ItemBuilder itemBuilder = new ItemBuilder(Material.getMaterial(section.getInt("icon.material")))
                .setName(this.translate(section.getString("icon.display-name")))
                .addLores(section.getStringList("icon.lore").stream()
                        .map(s -> s.replace("{amount}", Toolchain.format(amount)))
                        .map(s -> s.replace("{drops}", ""+upgradeModel.getDropUpgrade().getValue()))
                        .map(s -> s.replace("{speed}", ""+upgradeModel.getTimeUpgrade().getValue()))
                        .map(this::translate)
                        .collect(Collectors.toList()));

        ItemStack itemStack = itemBuilder.build();
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        itemCompound.set("smachine_type", new NBTTagString(machine));
        itemCompound.set("smachine_amount", new NBTTagDouble(amount));
        itemCompound.set("upgrade_time", new NBTTagInt(upgradeModel.getTimeUpgrade().getIndentifier()));
        itemCompound.set("upgrade_drop", new NBTTagInt(upgradeModel.getDropUpgrade().getIndentifier()));
        nmsItem.setTag(itemCompound);
        CraftItemStack.asBukkitCopy(nmsItem);
        itemStack = (CraftItemStack.asBukkitCopy(nmsItem));

        return itemStack;
    }

    public void add(MachineModel machineModel) {
        if(!machines.containsKey(machineModel.getLocation())) {
            this.machines.put(machineModel.getLocation(), machineModel);
        }
    }

    private String translate(final String data) {
        return ChatColor.translateAlternateColorCodes('&', data);
    }

    public void remove(MachineModel machineModel) {
        if(machines.containsKey(machineModel.getLocation())) {
            this.machines.remove(machineModel.getLocation());
        }
    }
}

