package wiresegal.hover.item;

import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import wiresegal.hover.HoverConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class ForgeEnergyContainer implements ICapabilitySerializable<NBTTagFloat> {

    public static ForgeEnergyContainer provide() {
        if (HoverConfig.isBoardFree())
            return null;
        return new ForgeEnergyContainer(HoverConfig.GENERAL.fuelCost, HoverConfig.GENERAL.fuelStorage);
    }

    private final ExposedEnergyStorage storage;

    public ForgeEnergyContainer(int fuelCost, int fuelStorage) {
        storage = new ExposedEnergyStorage(fuelStorage, fuelCost * 20);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(storage) : null;
    }

    @Override
    public NBTTagFloat serializeNBT() {
        return new NBTTagFloat((float) storage.getEnergyStored() / storage.getMaxEnergyStored());
    }

    @Override
    public void deserializeNBT(NBTTagFloat nbt) {
        storage.setPower(HoverConfig.GENERAL.fuelStorage, HoverConfig.GENERAL.fuelCost * 20, nbt.getFloat());
    }
}
