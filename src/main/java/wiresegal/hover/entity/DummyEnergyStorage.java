package wiresegal.hover.entity;

import net.minecraftforge.energy.EnergyStorage;

/**
 * @author WireSegal
 * Created at 12:16 PM on 5/5/18.
 */
public class DummyEnergyStorage extends EnergyStorage {

    public DummyEnergyStorage() {
        super(0);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
