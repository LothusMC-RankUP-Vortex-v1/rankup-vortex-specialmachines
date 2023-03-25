package br.net.rankup.specialmachine.tasks;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.misc.Toolchain;
import br.net.rankup.specialmachine.model.machines.UpgradeModel;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SpecialMachineRunnable extends BukkitRunnable {
    @Override
    public void run() {
        SpecialMachinePlugin.getMachineManager().getSpawners().values().forEach(machineModel -> {

            if(machineModel.hologramApply()) {
                UpgradeModel upgradeModel = machineModel.getUpgradeModel();
                if(!machineModel.getSettingsModel().isActived()) {
                    return;
                }
                if (machineModel.getTimeForGenerate() >= upgradeModel.getTimeUpgrade().getValue()) {
                    final Entity findedEntity = machineModel.getLocation().getWorld().getNearbyEntities(machineModel.getLocation(), 5D, 5D, 5D)
                            .stream()
                            .filter(entity -> entity.hasMetadata("smachine_amount") && entity.hasMetadata("smachine_location")
                                    && entity.getMetadata("smachine_type").get(0).asString().equals(machineModel.getType()))
                            .findFirst()
                            .orElse(null);

                        if(findedEntity != null) {
                            final double mobAmount = findedEntity.getMetadata("smachine_amount").get(0).asDouble();
                            double amountPerGenerate = SpecialMachinePlugin.getConfiguration().getDouble("machines."+machineModel.getType()+".cash-per-generate");
                            double newAmount = mobAmount + (machineModel.getStackAmount()*amountPerGenerate);

                            findedEntity.removeMetadata("smachine_location", SpecialMachinePlugin.getInstance());
                            findedEntity.removeMetadata("smachine_amount", SpecialMachinePlugin.getInstance());
                            findedEntity.setMetadata("smachine_location", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), BukkitUtils.serializeLocation(machineModel.getLocation())));
                            findedEntity.setMetadata("smachine_amount", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), newAmount));
                            findedEntity.setMetadata("smachine_type", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), machineModel.getType()));

                            String entityName = SpecialMachinePlugin.getConfiguration().getString("machines."+machineModel.getType()+".entity-name");
                            findedEntity.setCustomName(entityName.replace("{amount}", Toolchain.format(newAmount)).replace("&", "§"));
                        } else {
                            double amountPerGenerate = SpecialMachinePlugin.getConfiguration().getDouble("machines."+machineModel.getType()+".cash-per-generate");
                            double amount = machineModel.getStackAmount()*amountPerGenerate;
                            String entityName = SpecialMachinePlugin.getConfiguration().getString("machines."+machineModel.getType()+".entity-name");
                            Random random = new Random();
                            int x = random.nextInt(2);
                            int z = random.nextInt(2);
                            int i = random.nextInt(2);

                            if(x < 1) x = 2;
                            if(z < 1) x = 2;

                            Location location;
                            if(i == 1) {
                                location = machineModel.getLocation().clone().add(-x, 0, -z);
                            } else {
                                location = machineModel.getLocation().clone().add(x, 0, z);
                            }
                            final ArmorStand armorStand = (ArmorStand) machineModel.getLocation().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                            armorStand.setGravity(false);
                            armorStand.setVisible(false);

                            double heath = SpecialMachinePlugin.getConfiguration().getDouble("machines."+machineModel.getType()+".heath-initial");
                            armorStand.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99 * 99 * 99, 127));
                            armorStand.setMetadata("smachine_location", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), BukkitUtils.serializeLocation(machineModel.getLocation())));
                            armorStand.setMetadata("smachine_amount", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), amount));
                            armorStand.setMetadata("smachine_heath", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), heath));
                            armorStand.setMetadata("smachine_type", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), machineModel.getType()));
                            armorStand.setCustomNameVisible(true);
                            armorStand.setCustomName(entityName.replace("{amount}", Toolchain.format(amount)).replace("&", "§"));
                            this.renameEntity(armorStand);
                        }
                    machineModel.setTimeForGenerate(0);
                     return;
                }
                machineModel.setTimeForGenerate(machineModel.getTimeForGenerate() + 1);
            }

        });
    }

    private void renameEntity(Entity entity) {
        final net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        final NBTTagCompound nbtTag = nmsEntity.getNBTTag() == null ? new NBTTagCompound() : nmsEntity.getNBTTag();
        nmsEntity.c(nbtTag);
        nbtTag.setInt("NoAI", 1);
        nbtTag.setInt("NoGravity", 0);
        nmsEntity.f(nbtTag);
    }

}
