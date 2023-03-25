package br.net.rankup.specialmachine.inventory;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.listener.ChatListener;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.misc.InventoryUtils;
import br.net.rankup.specialmachine.misc.ItemBuilder;
import br.net.rankup.specialmachine.misc.SkullCreatorUtils;
import br.net.rankup.specialmachine.model.machines.MachineModel;
import com.google.common.collect.ImmutableList;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.Pagination;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator;
import io.github.rysefoxx.inventory.plugin.pattern.SlotIteratorPattern;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.crypto.Mac;

public class FriendsInventory implements InventoryProvider {

    private MachineModel machineModel;

    public FriendsInventory(MachineModel machineModel) {
        this.machineModel = machineModel;
    }

    public RyseInventory build() {
        return RyseInventory.builder()
                .title("Máquina - Amigos".replace("&", "§"))
                .rows(5)
                .provider(this)
                .disableUpdateTask()
                .build(SpecialMachinePlugin.getInstance());
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemBuilder itemBuilderFriend = new ItemBuilder(SkullCreatorUtils.itemFromUrl("b985a29957d40fa564d5e31cbd905e3694a616393ce13710bfc31b1b8b0a522d"))
                .setName("§eAdicione um amigo")
                .addLoreLine("§7Adicione um amigo que terá a ")
                .addLoreLine("§7permissão de usar sua máquina.")
                .addLoreLine("")
                .addLoreLine("§aClique para adicionar.");

        IntelligentItem intelligentAdd = IntelligentItem.of(itemBuilderFriend.build(), event -> {
            BukkitUtils.sendMessage(player, "");
            BukkitUtils.sendMessage(player, "&aDigite o nome do jogador que deseja adicionar a máquina");
            BukkitUtils.sendMessage(player, "&7Caso queira cancelar, digite 'cancelar'.");
            BukkitUtils.sendMessage(player, "");
            player.closeInventory();
            ChatListener.getPlayers().put(player.getName(), machineModel);
            event.setCancelled(true);
        });
        contents.set(40, intelligentAdd);
        if (machineModel.getFriends().size() == 0) {
            ItemStack empty = new ItemBuilder(Material.WEB, 1, 0)
                    .owner(player.getName()).setName("§cVazio").setLore(ImmutableList.of(
                            "§7Esse gerador não tem nenhum amigo."
                    )).build();
            contents.set(22, empty);


        } else {

            Pagination pagination = contents.pagination();
            pagination.iterator(SlotIterator.builder().withPattern(SlotIteratorPattern.builder().define(
                                    "XXXXXXXXX",
                                    "XOOOOOOOX",
                                    "XOOOOOOOX",
                                    "XOOOOOOOX",
                                    "XXXXXXXXX",
                                    "XXXXXXXXX")
                            .attach('O')
                            .buildPattern())
                    .build());
            pagination.setItemsPerPage(28);

            machineModel.getFriends().forEach(friendName -> {
                ItemBuilder itemBuilder = new ItemBuilder(SkullCreatorUtils.itemFromName(friendName))
                        .setName("§7"+friendName)
                        .addLoreLine("§7Clique aqui para §fremover")
                        .addLoreLine("§7esse jogador do seu gerador.");
                IntelligentItem intelligentItem = IntelligentItem.of(itemBuilder.build(), event -> {
                    machineModel.getFriends().remove(friendName);
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    BukkitUtils.sendMessage(player, "&eO jogador foi removido da lista de amigos de seu gerador.");
                    //SpecialMachinePlugin.getInstance().getSpawnerRepository().update(spawnerModel);
                    reOpenInventory(player, machineModel);
                    event.setCancelled(true);
                });
                pagination.addItem(intelligentItem);
            });


            if (!pagination.isFirst()) {
                ItemStack itemStack = new ItemBuilder(Material.ARROW).setName("§aPágina anterior").toItemStack();
                IntelligentItem intelligentItem = IntelligentItem.of(itemStack, event -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                        pagination.inventory().open(player, pagination.previous().page());
                    }
                });
                contents.set(36, intelligentItem);
            }

            if (!pagination.isLast()) {
                ItemStack itemStack = new ItemBuilder(Material.ARROW).setName("§aPróxima página").toItemStack();
                IntelligentItem intelligentItem = IntelligentItem.of(itemStack, event -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                        pagination.inventory().open(player, pagination.next().page());
                    }
                });
                contents.set(44, intelligentItem);
            }
        }
    }

    public void reOpenInventory(Player player, MachineModel machineModel) {
            if(!InventoryUtils.getList().contains(player.getName())) {
                RyseInventory inventory = new FriendsInventory(machineModel).build();
                inventory.open(player);
                InventoryUtils.addDelay(player);
            }
    }

}
