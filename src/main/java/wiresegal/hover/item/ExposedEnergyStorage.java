package wiresegal.hover.item;

import net.minecraftforge.energy.EnergyStorage;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class ExposedEnergyStorage extends EnergyStorage {

    public ExposedEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public void setPower(int capacity, int rate, float power) {
        this.capacity = capacity;
        this.maxExtract = rate;
        this.maxReceive = rate;
        this.energy = (int) (power * this.capacity);
    }
}
