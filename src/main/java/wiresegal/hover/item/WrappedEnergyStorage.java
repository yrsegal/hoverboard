package wiresegal.hover.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class WrappedEnergyStorage implements IEnergyStorage {

    private final ItemStack stack;
    private final int capacity;
    private final int maxTransfer;

    public WrappedEnergyStorage(ItemStack stack, int capacity, int maxTransfer) {
        this.stack = stack;
        this.capacity = capacity;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energy = getEnergyStored();
        int energyReceived = Math.min(capacity - energy, Math.min(maxTransfer, maxReceive));
        if (!simulate)
            setEnergy(energy + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energy = getEnergyStored();
        int energyExtracted = Math.min(energy, Math.min(maxTransfer, maxExtract));
        if (!simulate)
            setEnergy(energy - energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        float percent = 0f;
        NBTTagCompound comp = stack.getTagCompound();
        if (comp != null && comp.hasKey("energy", Constants.NBT.TAG_ANY_NUMERIC))
            percent = comp.getFloat("energy");

        return (int) (MathHelper.clamp(percent, 0, 1) * capacity);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxTransfer > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxTransfer > 0;
    }

    private void setEnergy(int energy) {
        float percent = energy / (float) capacity;
        stack.setTagInfo("energy", new NBTTagFloat(MathHelper.clamp(percent, 0, 1)));
    }
}
