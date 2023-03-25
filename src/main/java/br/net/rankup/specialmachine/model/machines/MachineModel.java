package br.net.rankup.specialmachine.model.machines;

import br.net.rankup.specialmachine.SpecialMachinePlugin;
import br.net.rankup.specialmachine.misc.ProgressBar;
import br.net.rankup.specialmachine.misc.TimeFormat;
import br.net.rankup.specialmachine.misc.Toolchain;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MachineModel {

    private Location location;
    private Hologram hologram;
    private String playerOwner;
    private String type;
    private double amountDrop;
    private double stackAmount;
    private double timeForGenerate;
    private List<String> friends;
    private UpgradeModel upgradeModel;
    private SettingsModel settingsModel;


    public boolean hologramApply() {

        if(!settingsModel.isHologramActived()) {
            if(!settingsModel.isActived()) return false;
        }

        if(hologram != null) {
            List<String> hologramLines = SpecialMachinePlugin.getConfiguration()
                    .getStringList("machines."+type+".hologram-lines");

            for (int i = 0; i < hologramLines.size(); i++) {
                TextLine line = (TextLine) hologram.getLine(i);
                line.setText(hologramLines.get(i)
                        .replace("{amount}", Toolchain.format(this.stackAmount))
                        .replace("{bar}", ProgressBar.progressBar(this.timeForGenerate, upgradeModel.getTimeUpgrade().getValue(), "▎"))
                        .replace("{tempo}", TimeFormat.formatTime((int) (upgradeModel.getTimeUpgrade().getValue() - this.timeForGenerate)))
                        .replace('&', '§'));
            }
        }
        return true;
    }

    public void hologramCreate() {
        List<String> hologramLines = SpecialMachinePlugin.getConfiguration().getStringList("machines." + type + ".hologram-lines");
        double hologramHeight = SpecialMachinePlugin.getConfiguration().getDouble("machines." + type + ".hologram-height");
        SettingsModel settingsModel = new SettingsModel(true, true);
        Hologram hologram = HologramsAPI.createHologram(SpecialMachinePlugin.getInstance(),
                location.clone().add(0.5, hologramHeight, 0.5));
        for (final String line : hologramLines) {
            hologram.appendTextLine(line
                    .replace("{amount}", Toolchain.format(stackAmount))
                    .replace("{bar}", ProgressBar.progressBar(0, upgradeModel.getTimeUpgrade().getValue(), "▎"))
                    .replace("{tempo}", TimeFormat.formatTime((int) (upgradeModel.getDropUpgrade().getValue() - 0)))
                    .replace('&', '§'));
        }
        this.hologram = hologram;
    }

    public void remove() {
        if(hologram != null) this.hologram.delete();
        SpecialMachinePlugin.getMachineManager().remove(this);
        //SpecialMachinePlugin.getInstance().getSpawnerRepository().deleteTable(location);
    }

    public void addAmount(double amountInHand) {
        stackAmount += amountInHand;
    }
}
