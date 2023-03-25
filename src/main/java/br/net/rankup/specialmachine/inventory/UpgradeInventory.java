package br.net.rankup.specialmachine.inventory;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.misc.InventoryUtils;
import br.net.rankup.specialmachine.misc.ItemBuilder;
import br.net.rankup.specialmachine.misc.Toolchain;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.crypto.Mac;
import java.sql.Time;

public class UpgradeInventory implements InventoryProvider {

    private MachineModel machineModel;

    public UpgradeInventory(MachineModel machineModel) {
        this.machineModel = machineModel;
    }

    public RyseInventory build() {
        return RyseInventory.builder()
                .title("Máquina - Upgrades".replace("&", "§"))
                .rows(3)
                .provider(this)
                .disableUpdateTask()
                .build(SpecialMachinePlugin.getInstance());
    }

    @Override
    public void init(Player player, InventoryContents contents) {


        //time upgrade
        TimeUpgrade timeUpgrade = machineModel.getUpgradeModel().getTimeUpgrade();
        if (timeUpgrade != null) {

            ItemBuilder itemBuilderTimeNoHas = new ItemBuilder(Material.getMaterial(381))
                    .setName("§eEvoluir Velocidade")
                    .addLoreLine("§7Nível máximo atingido.");

            ItemStack itemStack = itemBuilderTimeNoHas.build();

            if (SpecialMachinePlugin.getUpgradeManager().hasNextTime(timeUpgrade.getIndentifier())) {

                TimeUpgrade timeUpgradeNext = SpecialMachinePlugin.getUpgradeManager().getNext(timeUpgrade.getIndentifier());

                String hasMoney = SpecialMachinePlugin.getEconomy().has(player.getName(), timeUpgradeNext.getPrice()) ? "§eClique para evoluir" : "§cSem dinheiro para evoluir.";

                ItemBuilder itemBuilderTimeHas = new ItemBuilder(Material.getMaterial(381))
                        .setName("§eEvoluir Velocidade")
                        .addLoreLine("§7Esta evolução faz com que")
                        .addLoreLine("§7os drops gerados sejam")
                        .addLoreLine("§7gerados mais rapidamente.")
                        .addLoreLine("")
                        .addLoreLine(" §fNível: §7{indentifier} §8({time}s) §f➟ §7{next-indentifier} §8({next-time}s)"
                                .replace("{next-indentifier}", "" + timeUpgradeNext.getIndentifier())
                                .replace("{next-time}", "" + timeUpgradeNext.getValue())
                                .replace("{indentifier}", "" + timeUpgrade.getIndentifier())
                                .replace("{time}", "" + timeUpgrade.getValue()))
                        .addLoreLine(" §fCusto: §2$§a{coins}".replace("{coins}", Toolchain.format(timeUpgradeNext.getPrice())))
                        .addLoreLine("")
                        .addLoreLine(hasMoney);
                itemStack = itemBuilderTimeHas.build();
            }

            IntelligentItem intelligentActived = IntelligentItem.of(itemStack, event -> {
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

                if (!SpecialMachinePlugin.getUpgradeManager().hasNextTime(timeUpgrade.getIndentifier())) {
                    BukkitUtils.sendMessage(player, "&cVocê já está no ultima evolução.");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                TimeUpgrade timeUpgradeNext = SpecialMachinePlugin.getUpgradeManager().getNext(timeUpgrade.getIndentifier());

                if (!SpecialMachinePlugin.getEconomy().has(player.getName(), timeUpgradeNext.getPrice())) {
                    BukkitUtils.sendMessage(player, "&cVocê não tem dinheiro suficiente para evoluir.");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                machineModel.getUpgradeModel().setTimeUpgrade(timeUpgradeNext);
                machineModel.setTimeForGenerate(0);
                SpecialMachinePlugin.getEconomy().withdrawPlayer(player.getName(), timeUpgradeNext.getPrice());
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 1.0f);
                BukkitUtils.sendMessage(player, "&aYAY! Evolução adquirida com sucesso.");
                //LogPlugin.getInstance().getLogManager().registerSpawner(player, "SPAWNER_UPGRADE", "TIME", timeUpgradeNext.getPrice());
                //SpecialMachinePlugin.getInstance().getSpawnerRepository().update(spawnerModel);
                reOpenInventory(player, machineModel);
            });
            contents.set(12, intelligentActived);
        }

        //drop upgrade
        DropUpgrade dropUpgrade = machineModel.getUpgradeModel().getDropUpgrade();
        if(dropUpgrade != null) {

            ItemBuilder itemBuilderXpNoHas = new ItemBuilder(Material.GOLD_INGOT)
                    .setName("§eEvoluir Drop")
                    .addLoreLine("§7Nível máximo atingido.");

            ItemStack itemStackXp = itemBuilderXpNoHas.build();

            if (SpecialMachinePlugin.getUpgradeManager().hasNextXP(dropUpgrade.getIndentifier())) {

                DropUpgrade xpUpgradeNext = SpecialMachinePlugin.getUpgradeManager().getNextDrop(dropUpgrade.getIndentifier());

                String hasMoneyXp = SpecialMachinePlugin.getEconomy().has(player.getName(), xpUpgradeNext.getPrice()) ? "§eClique para evoluir" : "§cSem dinheiro para evoluir.";

                ItemBuilder itemBuilderXPHas = new ItemBuilder(Material.GOLD_INGOT)
                        .setName("§eEvoluir Drop")
                        .addLoreLine("§7Esta evolução faz com que")
                        .addLoreLine("§7os drops ao matar um mob seja")
                        .addLoreLine("§7multiplicado ganhando mais.")
                        .addLoreLine("")
                        .addLoreLine(" §fNível: §7{indentifier} §8({multiplier}x) §f➟ §7{next-indentifier} §8({next-multiplier}x)"
                                .replace("{next-indentifier}", "" + xpUpgradeNext.getIndentifier())
                                .replace("{next-multiplier}", "" + xpUpgradeNext.getValue())
                                .replace("{indentifier}", "" + dropUpgrade.getIndentifier())
                                .replace("{multiplier}", "" + dropUpgrade.getValue()))
                        .addLoreLine(" §fCusto: §2$§a{coins}".replace("{coins}", Toolchain.format(xpUpgradeNext.getPrice())))
                        .addLoreLine("")
                        .addLoreLine(hasMoneyXp);
                itemStackXp = itemBuilderXPHas.build();
            }

            IntelligentItem intelligent = IntelligentItem.of(itemStackXp, event -> {
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

                if (!SpecialMachinePlugin.getUpgradeManager().hasNextXP(dropUpgrade.getIndentifier())) {
                    BukkitUtils.sendMessage(player, "&cVocê já está no ultima evolução.");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                DropUpgrade xpUpgradeNext = SpecialMachinePlugin.getUpgradeManager().getNextDrop(dropUpgrade.getIndentifier());

                if (!SpecialMachinePlugin.getEconomy().has(player.getName(), xpUpgradeNext.getPrice())) {
                    BukkitUtils.sendMessage(player, "&cVocê não tem dinheiro suficiente para evoluir.");
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                machineModel.getUpgradeModel().setDropUpgrade(xpUpgradeNext);
                //SpawnerPlugin.getEconomy().withdrawPlayer(player.getName(), xpUpgradeNext.getPrice());
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 1.0f);
                //LogPlugin.getInstance().getLogManager().registerSpawner(player, "SPAWNER_UPGRADE", "XP", xpUpgrade.getPrice());
                BukkitUtils.sendMessage(player, "&aYAY! Evolução adquirida com sucesso.");
               // SpawnerPlugin.getInstance().getSpawnerRepository().update(spawnerModel);

                reOpenInventory(player, machineModel);
            });
            contents.set(14, intelligent);
        }

    }

    public void reOpenInventory(Player player, MachineModel machineModel) {
            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new UpgradeInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
            }
    }

}
