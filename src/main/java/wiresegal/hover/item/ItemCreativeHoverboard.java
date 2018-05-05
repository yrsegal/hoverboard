package wiresegal.hover.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import wiresegal.hover.entity.EntityHoverboard;

import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class ItemCreativeHoverboard extends ItemHoverboard {

    public ItemCreativeHoverboard(String name) {
        super(name);
    }

    @Override
    public EntityHoverboard createBoard(World worldIn, ItemStack stack, Vec3d position, float yaw) {
        EntityHoverboard board = super.createBoard(worldIn, stack, position, yaw);
        board.creative = true;
        return board;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return null;
    }
}
