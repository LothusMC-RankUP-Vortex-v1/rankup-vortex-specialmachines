package br.net.rankup.specialmachine.commands;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.model.machines.UpgradeModel;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SpecialMachineCommand {


    @Command(name = "specialmachine", aliases = {"machines", "maquina", "maquinas"})
    public void handlerMachineCommand(Context<CommandSender> context) {
        CommandSender sender = context.getSender();
        if(sender instanceof Player) {
            Player player = (Player) sender;
            //RyseInventory ryseInventory = new SpawnerShopInventory().build();
            //ryseInventory.open(player);
        }
    }

    @Command(name = "specialmachine.help", aliases = {"ajuda"})
    public void handlerHelpCommand(Context<CommandSender> context) {
        CommandSender sender = context.getSender();
        if(sender instanceof Player) {
            BukkitUtils.sendMessage(sender, "");
            BukkitUtils.sendMessage(sender, " &a/machines &f- &7Para abrir a loja de máquinas.");
            if(sender.hasPermission("commands.machines")) {
                BukkitUtils.sendMessage(sender, "");
                BukkitUtils.sendMessage(sender, " &a/machines give <player> <type> <amount> &f- &7Enviar uma máquina a um jogador.");
                BukkitUtils.sendMessage(sender, " &a/machines help &f- &7Ver mensagem de ajuda.");
            }
            BukkitUtils.sendMessage(sender, "");
        }
    }

    @Command(name = "specialmachine.give", aliases = {"enviar"}, permission = "commands.machines", usage = "specialmachine give <player> <type> <amount>")
    public void handlerGiveCommand(Context<CommandSender> context, Player target, String type, double amount) {
        CommandSender sender = context.getSender();

        if(amount < 0) {
            BukkitUtils.sendMessage(sender, "&cO valor não pode ser abaixo de 0.");
            return;
        }

        if(!SpecialMachinePlugin.getInstance().getConfig().contains("machines."+type)) {
            BukkitUtils.sendMessage(sender, "&cEssa máquina não existe.");
            return;
        }

        TimeUpgrade timeUpgrade = SpecialMachinePlugin.getUpgradeManager().getTime(0);
        DropUpgrade dropUpgrade = SpecialMachinePlugin.getUpgradeManager().getDrop(0);

        UpgradeModel upgradeModel = new UpgradeModel(timeUpgrade, dropUpgrade);


        ItemStack itemStack = SpecialMachinePlugin.getMachineManager().getItemStack(type, upgradeModel, amount);
        target.getInventory().addItem(itemStack);
        BukkitUtils.sendMessage(sender, "&aYay! Você enviou as máquinas ao jogador.");

    }

}
