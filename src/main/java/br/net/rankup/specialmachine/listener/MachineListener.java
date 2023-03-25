package br.net.rankup.specialmachine.listener;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.inventory.DefaultInventory;
import br.net.rankup.specialmachine.misc.*;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import br.net.rankup.specialmachine.model.machines.SettingsModel;
import br.net.rankup.specialmachine.model.machines.UpgradeModel;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class MachineListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(!event.getAction().toString().contains("RIGHT")) return;
           Block block = event.getClickedBlock();
           if(block != null && block.getType() != Material.AIR) {
               MachineModel machineModel = SpecialMachinePlugin.getMachineManager().getSpawners().get(block.getLocation());
               if(machineModel != null) {
                   if(!machineModel.getPlayerOwner().contains(player.getName()) && !machineModel.getFriends().contains(player.getName())) {
                       BukkitUtils.sendMessage(player, "&cVocê não tem permissão para interagir com esse gerador.");
                       player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0f, 1.0f);
                       return;
                   }

                       if(!InventoryUtils.getList().contains(player.getName())) {
                           RyseInventory inventory = new DefaultInventory(machineModel).build();
                           inventory.open(player);
                           InventoryUtils.addDelay(player);
                           player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 5.0f);
                       }
               }
       }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        MachineModel machineModel = SpecialMachinePlugin.getMachineManager().getSpawners().get(block.getLocation());
        if (machineModel != null) {
            event.setCancelled(true);
            if(machineModel == null) {
                BukkitUtils.sendMessage(player, "&cEssa máquina não está funcionando.");
                return;
            }
            if(!machineModel.getPlayerOwner().equalsIgnoreCase(player.getName()) && !machineModel.getFriends().contains(player.getName())
                    && !player.hasPermission("machines.admin")) {
                BukkitUtils.sendMessage(player, "&cVocê não tem permissão para gerenciar esse gerador.");
                event.setCancelled(true);
                return;
            }


            if(machineModel.getAmountDrop() > 0) {
                BukkitUtils.sendMessage(player, "&cVenda os drops de sua máquina para depois remove-la.");
                return;
            }

            ItemStack itemStack = SpecialMachinePlugin.getMachineManager().getItemStack(machineModel.getType(), machineModel.getUpgradeModel(), machineModel.getStackAmount());
            machineModel.remove();

            machineModel.getLocation().getWorld().getNearbyEntities(machineModel.getLocation(), 2, 2, 2)
                    .stream()
                    .filter(entity -> entity.hasMetadata("smachine_amount") && entity.hasMetadata("smachine_location"))
                    .forEach(entity -> { entity.remove(); });

            block.setType(Material.AIR);
            player.getInventory().addItem(itemStack);
            BukkitUtils.sendMessage(player, "&aYAY! Você removeu a máquina com êxito.");
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
        Block block = event.getBlock();
        if(event.isCancelled()) return;
        if(itemInHand != null && itemInHand.getType() != Material.AIR) {
            if(BukkitUtils.hasNBT(itemInHand, "smachine_type")) {

                if(!player.getWorld().getName().equalsIgnoreCase("plotworld")) {
                    BukkitUtils.sendMessage(player, "&cVocê só pode colocar um gerador no mundo dos tereenos.");
                    event.setCancelled(true);
                    return;
                }

                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemInHand);
                NBTTagCompound itemCompound = nmsItem.getTag();

                String Machinetype = itemCompound.getString("smachine_type");
                Double amount = itemCompound.getDouble("smachine_amount");
                int timeIndentifier = itemCompound.getInt("upgrade_time");
                int dropIntentifier = itemCompound.getInt("upgrade_drop");
                double amountInHand = itemInHand.getAmount()*amount;

                TimeUpgrade timeUpgrade = SpecialMachinePlugin.getUpgradeManager().getTime(timeIndentifier);
                DropUpgrade dropUpgrade = SpecialMachinePlugin.getUpgradeManager().getDrop(dropIntentifier);


                if(getNearSpawners(block.getLocation(), 5, Machinetype).size() >= 1) {
                    for(Block block1 : getNearSpawners(block.getLocation(), 5, Machinetype)) {
                        MachineModel machineModel = SpecialMachinePlugin.getMachineManager().getSpawners().get(block1.getLocation());
                        if(!machineModel.getPlayerOwner().equalsIgnoreCase(player.getName()) && !machineModel.getFriends().contains(player.getName())
                        && !player.hasPermission("machines.admin")) {
                            BukkitUtils.sendMessage(player, "&cVocê não tem permissão para gerenciar esse gerador.");
                            return;
                        }

                        machineModel.addAmount(amountInHand);
                        player.setItemInHand(null);
                        BukkitUtils.sendMessage(player, "&eVocê adicionou &fx{amount} &emáquinas especiais&e."
                                .replace("{amount}", Toolchain.format(amountInHand)));
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 1.0f);
                        //SpecialMachinePlugin.getInstance().getSpawnerRepository().update(machineModel);
                        event.setCancelled(true);
                    }
                } else {

                    net.minecraft.server.v1_8_R3.ItemStack nmsItem2 = CraftItemStack.asNMSCopy(itemInHand);
                    NBTTagCompound itemCompound2 = nmsItem2.getTag();
                    String type = itemCompound2.getString("smachine_type");

                    List<String> hologramLines = SpecialMachinePlugin.getConfiguration().getStringList("machines."+type+".hologram-lines");
                    double hologramHeight = SpecialMachinePlugin.getConfiguration().getDouble("machines."+type+".hologram-height");
                    SettingsModel settingsModel = new SettingsModel(true, true);
                    Hologram hologram = HologramsAPI.createHologram(SpecialMachinePlugin.getInstance(),
                            block.getLocation().clone().add(0.5, hologramHeight, 0.5));
                    for (final String line : hologramLines) {
                        hologram.appendTextLine(line
                                .replace("{amount}", Toolchain.format(amount))
                                .replace("{bar}", ProgressBar.progressBar(0, timeUpgrade.getValue(), "▎"))
                                .replace("{tempo}", TimeFormat.formatTime((int) (timeUpgrade.getValue() - 0)))
                                .replace('&', '§'));
                    }
                    hologram.setAllowPlaceholders(true);

                    UpgradeModel upgradeModel = new UpgradeModel(timeUpgrade, dropUpgrade);


                    MachineModel machineModel =
                            new MachineModel(block.getLocation(), hologram, player.getName(), type, 0, amountInHand, 0, new ArrayList<>(), upgradeModel, settingsModel);
                    SpecialMachinePlugin.getMachineManager().add(machineModel);
                    BukkitUtils.sendMessage(player, "&aYAY! Você colocou a sua máquina especial!");
                    //SpawnerPlugin.getInstance().getSpawnerRepository().update(spawnerModel);


                    player.setItemInHand(null);
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        CreatureSpawner creatureSpawner = event.getSpawner();
        if(creatureSpawner.getBlock().hasMetadata("smachine_type")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) return;
        Entity entity = event.getEntity();
        Player player = (Player) event.getDamager();

        if(entity.hasMetadata("smachine_location")) {
            double stackMobs = entity.getMetadata("smachine_amount").get(0).asDouble();
            double leftHeath = entity.getMetadata("smachine_heath").get(0).asDouble();
            Location location = BukkitUtils.deserializeLocation(entity.getMetadata("smachine_location").get(0).asString());

            if(location == null) return;

            MachineModel machineModel = SpecialMachinePlugin.getMachineManager().getSpawners().get(location);
            if(machineModel == null) {
                BukkitUtils.sendMessage(player, "&cNão foi possivel encontrar essa máquina.");
                return;
            }

            if(!machineModel.getPlayerOwner().equalsIgnoreCase(player.getName()) && !machineModel.getFriends().contains(player.getName())
                    && !player.hasPermission("machines.admin")) {
                BukkitUtils.sendMessage(player, "&cVocê não tem permissão para matar esse mob.");
                event.setCancelled(true);
                return;
            }
            leftHeath--;
            if(leftHeath <= 0 && !entity.isDead()) {
                entity.remove();
                machineModel.setAmountDrop(machineModel.getAmountDrop()+stackMobs*machineModel.getUpgradeModel().getDropUpgrade().getValue());
                String actionBar = "§aVocê recebeu §e{drops} drops §aem seu armazém.";
                BukkitUtils.sendMessage(player,
                        actionBar.replace("{drops}",
                                Toolchain.format(stackMobs*machineModel.getUpgradeModel().getDropUpgrade().getValue())));
            }
            else {
                double maxHeath = SpecialMachinePlugin.getConfiguration().getDouble("machines."+machineModel.getType()+".heath-initial");
                BukkitUtils.sendActionBar("§eMáquina de " + machineModel.getType() + " §7| §fVida: §4❤§c"+ Toolchain.format(leftHeath) + " §8(" +
                        ProgressBar.progressBar(leftHeath, maxHeath, "|") + "§8)", player);
                entity.removeMetadata("smachine_heath", SpecialMachinePlugin.getInstance());
                entity.setMetadata("smachine_heath", new FixedMetadataValue(SpecialMachinePlugin.getInstance(), leftHeath));
            }
        }
    }


    public static List<Block> getNearSpawners(Location location, int radius, String type) {
        ArrayList<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if(SpecialMachinePlugin.getMachineManager().getSpawners().containsKey(block.getLocation())) {
                            String typeBlock = SpecialMachinePlugin.getMachineManager().getSpawners().get(block.getLocation()).getType();
                            if (typeBlock != null) {
                                if (type.equals(typeBlock)) {
                                    blocks.add(block);
                                }
                            }
                    }
                }
            }
        }
        return blocks;
    }
}
