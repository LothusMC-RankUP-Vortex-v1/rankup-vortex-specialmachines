package br.net.rankup.specialmachine.inventory;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.InventoryUtils;
import br.net.rankup.specialmachine.misc.ItemBuilder;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import br.net.rankup.specialmachine.model.machines.SettingsModel;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.crypto.Mac;

public class SettingsInventory implements InventoryProvider {

    private MachineModel machineModel;

    public SettingsInventory(MachineModel machineModel) {
        this.machineModel = machineModel;
    }

    public RyseInventory build() {
        return RyseInventory.builder()
                .title("Máquina - Configurações".replace("&", "§"))
                .rows(4)
                .provider(this)
                .disableUpdateTask()
                .build(SpecialMachinePlugin.getInstance());
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        ItemBuilder itemBuilderHologramInfo = new ItemBuilder(Material.ARMOR_STAND)
                .setName("§eHolograma")
                .addLoreLine("§7Holograma acima da máquina ")
                .addLoreLine("§7com suas inforamações.");
            contents.set(12, itemBuilderHologramInfo.build());


        String hologramColor = machineModel.getSettingsModel().isHologramActived() ? "§a" : "§c";
        String hologramStatus = machineModel.getSettingsModel().isHologramActived() ? "§aHabilitado" : "§cDesabilitado";
        int hologramDate = (machineModel.getSettingsModel().isHologramActived() ? 10 : 8);

        ItemBuilder itemBuilderHologram = new ItemBuilder(Material.getMaterial(351), 1, hologramDate)
                .setName(hologramColor+"Holograma")
                .addLoreLine("§fEstado: "+hologramStatus)
                .addLoreLine("")
                .addLoreLine("§aClique parar mudar o estado.");

        IntelligentItem intelligentToggle = IntelligentItem.of(itemBuilderHologram.build(), event -> {
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

            SettingsModel settingsModel = machineModel.getSettingsModel();

            if(settingsModel.isHologramActived()) {
                settingsModel.setHologramActived(false);
                machineModel.getHologram().delete();
                machineModel.setHologram(null);
            } else {
                machineModel.hologramCreate();
            }
            //SpawnerPlugin.getInstance().getSpawnerRepository().update(spawnerModel);
            reOpenInventory(player, machineModel);

            event.setCancelled(true);
        });
        contents.set(21, intelligentToggle);



        ItemBuilder itemBuilderEnable = new ItemBuilder(Material.EYE_OF_ENDER)
                .setName("§eGerar drops")
                .addLoreLine("§7Gerencie se sua máquina")
                .addLoreLine("§7irá funcionar ou não.");
        contents.set(14, itemBuilderEnable.build());


        String activedColor = machineModel.getSettingsModel().isActived() ? "§a" : "§c";
        String activedStatus = machineModel.getSettingsModel().isActived() ? "§aHabilitado" : "§cDesabilitado";
        int activedDate = (machineModel.getSettingsModel().isActived() ? 10 : 8);

        ItemBuilder itemBuilderActived = new ItemBuilder(Material.getMaterial(351), 1, activedDate)
                .setName(activedColor+"Gerar drops")
                .addLoreLine("§fEstado: "+activedStatus)
                .addLoreLine("")
                .addLoreLine("§aClique parar mudar o estado.");

        IntelligentItem intelligentActived = IntelligentItem.of(itemBuilderActived.build(), event -> {
            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

            machineModel.getSettingsModel().setActived(!machineModel.getSettingsModel().isActived());
            //SpawnerPlugin.getInstance().getSpawnerRepository().update(spawnerModel);
            reOpenInventory(player, machineModel);
        });
        contents.set(23, intelligentActived);
    }

    public void reOpenInventory(Player player, MachineModel machineModel) {
            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new SettingsInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
            }
    }

}
