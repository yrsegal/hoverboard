package wiresegal.hover.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import wiresegal.hover.HoverConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class ForgeEnergyContainer implements ICapabilityProvider {

    public static ForgeEnergyContainer provide(ItemStack stack) {
        if (HoverConfig.isBoardFree())
            return null;
        return new ForgeEnergyContainer(stack, HoverConfig.GENERAL.fuelCost, HoverConfig.GENERAL.fuelStorage);
    }

    private final WrappedEnergyStorage storage;

    public ForgeEnergyContainer(ItemStack stack, int fuelCost, int fuelStorage) {
        this.storage = new WrappedEnergyStorage(stack, fuelStorage, fuelCost * 20);
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
}
