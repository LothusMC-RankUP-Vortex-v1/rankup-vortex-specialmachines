package br.net.rankup.specialmachine.model.machines;

import br.net.rankup.specialmachine.model.upgrade.TimeUpgrade;
import br.net.rankup.specialmachine.model.upgrade.DropUpgrade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpgradeModel {

    private TimeUpgrade timeUpgrade;
    private DropUpgrade dropUpgrade;

}
