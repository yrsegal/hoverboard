package wiresegal.hover.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.HoverConfig;
import wiresegal.hover.entity.EntityHoverboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author WireSegal
 * Created at 10:20 AM on 5/5/18.
 */
public class ItemHoverboard extends Item {

    public ItemHoverboard(String name) {
        setUnlocalizedName("hoverboard." + name);
        setRegistryName(name);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TRANSPORTATION);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        float pitchInterp = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch);
        float yawInterp = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw);
        double xInterp = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX);
        double yInterp = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) + playerIn.getEyeHeight();
        double zInterp = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ);
        Vec3d position = new Vec3d(xInterp, yInterp, zInterp);
        float yawZ = MathHelper.cos(-yawInterp * (float) Math.PI / 180 - (float) Math.PI);
        float yawX = MathHelper.sin(-yawInterp * (float) Math.PI / 180 - (float) Math.PI);
        float pitchForward = -MathHelper.cos(-pitchInterp * (float) Math.PI / 180);
        float lookY = MathHelper.sin(-pitchInterp * (float) Math.PI / 180);
        float lookX = yawX * pitchForward;
        float lookZ = yawZ * pitchForward;

        double reach = playerIn.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

        Vec3d target = position.addVector(lookX * reach, lookY * reach, lookZ * reach);
        RayTraceResult trace = worldIn.rayTraceBlocks(position, target, true);

        if (trace == null)
            return new ActionResult<>(EnumActionResult.PASS, stack);
        else {
            if (trace.typeOfHit != RayTraceResult.Type.BLOCK)
                return new ActionResult<>(EnumActionResult.PASS, stack);

            for (Entity entity : worldIn.getEntitiesWithinAABBExcludingEntity(playerIn,
                    playerIn.getEntityBoundingBox()
                            .expand(lookX * reach, lookY * reach, lookZ * reach)
                            .grow(1.0D)))
                if (entity.canBeCollidedWith() && entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize())
                            .contains(position))
                        return new ActionResult<>(EnumActionResult.PASS, stack);

            EntityHoverboard board = createBoard(worldIn, stack, trace.hitVec, playerIn.rotationYaw);

            if (!worldIn.getCollisionBoxes(board, board.getEntityBoundingBox().grow(-0.1D)).isEmpty())
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            else {
                if (!worldIn.isRemote)
                    worldIn.spawnEntity(board);

                if (!playerIn.capabilities.isCreativeMode)
                    stack.shrink(1);

                playerIn.addStat(Objects.requireNonNull(StatList.getObjectUseStats(this)));
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
    }

    public EntityHoverboard createBoard(World worldIn, ItemStack stack, Vec3d position, float yaw) {
        EntityHoverboard board = new EntityHoverboard(worldIn, stack, position.x, position.y, position.z);
        board.rotationYaw = yaw;
        return board;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !HoverConfig.isBoardFree() && stack.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (storage != null)
            return 1f - ((float) storage.getEnergyStored() / storage.getMaxEnergyStored());
        return 0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        float amount = (float) (1.0F - getDurabilityForDisplay(stack) / 2);
        return MathHelper.hsvToRGB(0.0F, amount, amount);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return ForgeEnergyContainer.provide(stack);
    }
}
