
package br.net.rankup.specialmachine;

import br.net.rankup.specialmachine.commands.SpecialMachineCommand;
import br.net.rankup.specialmachine.listener.ChatListener;
import br.net.rankup.specialmachine.listener.MachineListener;
import br.net.rankup.specialmachine.manager.MachineManager;
import br.net.rankup.specialmachine.manager.UpgradeManager;
import br.net.rankup.specialmachine.misc.BukkitUtils;
import br.net.rankup.specialmachine.misc.ConfigUtils;
import br.net.rankup.specialmachine.tasks.SpecialMachineRunnable;
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import me.saiintbrisson.bukkit.command.BukkitFrame;
import me.saiintbrisson.minecraft.command.message.MessageHolder;
import me.saiintbrisson.minecraft.command.message.MessageType;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public final class SpecialMachinePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        instance = this;

        start = System.currentTimeMillis();
        configuration = new ConfigUtils(this,"config.yml");
        configuration.saveDefaultConfig();

            for(Entity entity : Bukkit.getWorld("plotworld").getEntities()) {
                if(entity instanceof ArmorStand)
                    entity.remove();
            }

        bukkitFrame = new BukkitFrame(SpecialMachinePlugin.getInstance());
        loadCommands();
        inventoryManager = new InventoryManager(this);
        inventoryManager.invoke();

        final RegisteredServiceProvider<Economy> registration = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (registration != null) {
            economy = (Economy)registration.getProvider();
        }

        bukkitFrame.registerCommands(new SpecialMachineCommand());
        (this.machineManager = new MachineManager()).load();
        (this.upgradeManager = new UpgradeManager()).load();
        this.getServer().getPluginManager().registerEvents(new MachineListener(), this);
        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        new SpecialMachineRunnable().runTaskTimer(this, 20L, 20L);

        BukkitUtils.sendMessage(Bukkit.getConsoleSender(), "&aplugin started successfully ({time} ms)"
                .replace("{time}",""+(System.currentTimeMillis() - start)));
        enable.set(true);
    }

    @Override
    public void onDisable() {
        for (World world : getServer().getWorlds()) {
            for(Entity entity : world.getEntities()) {
                if(entity.hasMetadata("smachine_location")){
                    entity.remove();
                }
            }
         }
        if(enable.get()) {
            BukkitUtils.sendMessage(Bukkit.getConsoleSender(), "&cplugin successfully turned off!");
        } else {
            BukkitUtils.sendMessage(Bukkit.getConsoleSender(), "&cplugin suffered a some problem");
        }
    }



    @Getter
    private static Economy economy;
    @Getter
    private static MachineManager machineManager;
    public static MachineManager getMachineManager() {
        return machineManager;
    }
    @Getter
    private static UpgradeManager upgradeManager;
    public static UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
    static long start = 0;
    private AtomicBoolean enable = new AtomicBoolean(false);
    private static ConfigUtils configuration;
    private static SpecialMachinePlugin instance;
    @Getter
    private InventoryManager inventoryManager;
    @Getter
    private  BukkitFrame bukkitFrame;
    public static Plugin getInstance() { return instance; }
    public static FileConfiguration getConfiguration() {
        return configuration.getConfig();
    }
    public static ConfigUtils getConfigUtils() {
        return configuration;
    }
    public static long getStart() {
        return start;
    }


    private void loadCommands() {
        MessageHolder messageHolder = getBukkitFrame().getMessageHolder();
        messageHolder.setMessage(MessageType.ERROR, "§cOcorreu um erro durante a execução deste comando, erro: §7{error}§c.");
        messageHolder.setMessage(MessageType.INCORRECT_USAGE, "§cUtilize: /{usage}");
        messageHolder.setMessage(MessageType.NO_PERMISSION, "§cVocê não tem permissão para executar esse comando.");
        messageHolder.setMessage(MessageType.INCORRECT_TARGET, "§cVocê não pode utilizar este comando pois ele é direcionado apenas para {target}.");
    }
        }
