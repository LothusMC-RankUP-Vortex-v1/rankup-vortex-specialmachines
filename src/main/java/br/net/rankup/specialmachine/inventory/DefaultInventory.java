package br.net.rankup.specialmachine.inventory;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.misc.InventoryUtils;
import br.net.rankup.specialmachine.misc.ItemBuilder;
import br.net.rankup.specialmachine.misc.Toolchain;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import br.net.rankup.specialmachine.model.machines.UpgradeModel;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DefaultInventory implements InventoryProvider {

    private MachineModel machineModel;

    public DefaultInventory(MachineModel machineModel) {
        this.machineModel = machineModel;
    }

    public RyseInventory build() {
        return RyseInventory.builder()
                .title("Máquina Especial".replace("&", "§"))
                .rows(3)
                .provider(this)
                .disableUpdateTask()
                .build(SpecialMachinePlugin.getInstance());
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        String friends = (machineModel.getFriends().size() == 0 ? "§eNenhum" : ""+ Toolchain.format(machineModel.getFriends().size()));


        UpgradeModel upgradeModel = machineModel.getUpgradeModel();

        ItemBuilder itemBuilderInfo = new ItemBuilder(Material.SIGN)
                .setName("§aInformações")
                .addLoreLine("§7Veja algumas inforamações")
                .addLoreLine("§7dessa máquina abaixo:")
                .addLoreLine("§7")
                .addLoreLine("§8• §fDono: §e"+machineModel.getPlayerOwner())
                .addLoreLine("§8• §fTipo: §e"+machineModel.getType())
                .addLoreLine("§8• §fAmigos adicionados: §e"+friends)
                .addLoreLine("§8• §fQuantidade de máquinas: §e"+Toolchain.format(machineModel.getStackAmount()))
                .addLoreLine("")
                .addLoreLine("§eUpgrades:")
                .addLoreLine(" §fVelocidade: §7"+upgradeModel.getTimeUpgrade().getValue()+"s")
                .addLoreLine(" §fDrop: §7"+upgradeModel.getDropUpgrade().getValue()+"x");
            contents.set(10, itemBuilderInfo.build());


        ItemBuilder itemBuilderStorable = new ItemBuilder(Material.STORAGE_MINECART)
                .setName("§aArmazém")
                .addLoreLine("§7Venda seus drops dessa máquina")
                .addLoreLine("§7máquina agora mesmo.")
                .addLoreLine("§7")
                .addLoreLine(" §fValor: §e"+ Toolchain.format(machineModel.getAmountDrop()))
                .addLoreLine("")
                .addLoreLine("§aClique vender tudo.");

        if(machineModel.getAmountDrop() <= 0) {
            itemBuilderStorable = new ItemBuilder(Material.MINECART)
                    .setName("§cSem nada ):")
                    .addLoreLine("§cNão tem nada para vender");
        }

        IntelligentItem intelligentStorable = IntelligentItem.of(itemBuilderStorable.build(), event -> {

            if(machineModel.getAmountDrop() <= 0) {
                BukkitUtils.sendMessage(player, "&cEssa máquina não tem nenhum drop.");
                player.closeInventory();
                return;
            }

            player.closeInventory();
            String command = SpecialMachinePlugin.getConfiguration().getString("machines."+machineModel.getType()+".command");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{amount}", ""+machineModel.getAmountDrop()).replace("{player}", player.getName()));
            BukkitUtils.sendMessage(player, "&eVocê vendeu seu armazém com sucesso.");
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 1.0f);
            machineModel.setAmountDrop(0);
            event.setCancelled(true);
        });
        contents.set(12, intelligentStorable);

        ItemBuilder itemBuilderFriends = new ItemBuilder(Material.NETHER_STAR)
                .setName("§aAmigos")
                .addLoreLine("§7Gerencie seus amigos adicionados")
                .addLoreLine("§7a esta máquina e suas permissões.")
                .addLoreLine("§7")
                .addLoreLine("§aClique para abrir.");

        IntelligentItem intelligentFriends = IntelligentItem.of(itemBuilderFriends.build(), event -> {
            if(!machineModel.getPlayerOwner().equalsIgnoreCase(player.getName())) {
                BukkitUtils.sendMessage(player, "&cVocê não pode gerenciar os amigos dessa máquina.");
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 5.0F, 1.0f);
                player.closeInventory();
                return;
            }

            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new FriendsInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 5.0f);
            }
            event.setCancelled(true);
        });
        contents.set(14, intelligentFriends);

        ItemBuilder itemBuilderUpgrade = new ItemBuilder(Material.HOPPER)
                .setName("§aUpgrades")
                .addLoreLine("§7Evolua sua máquina e")
                .addLoreLine("§7multiplique seus ganhos.")
                .addLoreLine("§7")
                .addLoreLine("§aClique para abrir.");

        IntelligentItem intelligentItemUpgrade = IntelligentItem.of(itemBuilderUpgrade.build(), event -> {
            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new UpgradeInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 5.0f);
            }
            event.setCancelled(true);
        });
        contents.set(15, intelligentItemUpgrade);

        ItemBuilder itemBuilderSettings = new ItemBuilder(Material.REDSTONE)
                .setName("§aConfigurações")
                .addLoreLine("§7Gerencie sua máquina da forma")
                .addLoreLine("§7que lhe agradar")
                .addLoreLine("§7")
                .addLoreLine("§aClique para abrir.");

        IntelligentItem intelligentItemSettings = IntelligentItem.of(itemBuilderSettings.build(), event -> {
            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new SettingsInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 5.0f, 5.0f);
            }
            event.setCancelled(true);
        });
        contents.set(16, intelligentItemSettings);
    }

}
