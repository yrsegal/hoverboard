package wiresegal.hover.entity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.HoverConfig;
import wiresegal.hover.Hoverboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author WireSegal
 * Created at 12:16 PM on 5/5/18.
 */
public class EntityHoverboard extends Entity implements IJumpingMount {
    private static final DataParameter<Integer> TIME_SINCE_HIT
            = EntityDataManager.createKey(EntityHoverboard.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE_TAKEN
            = EntityDataManager.createKey(EntityHoverboard.class, DataSerializers.FLOAT);

    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYaw;
    private double lerpPitch;
    private boolean leftInputDown;
    private boolean rightInputDown;
    private boolean forwardInputDown;
    private boolean backInputDown;

    public boolean creative = false;
    private ItemStack containedItem = ItemStack.EMPTY;
    private IEnergyStorage energyStorage = new EnergyStorage(0);

    public EntityHoverboard(World worldIn) {
        super(worldIn);
        preventEntitySpawning = true;
        setSize(1F, 0.25F);
    }

    public EntityHoverboard(World worldIn, ItemStack stack, double x, double y, double z) {
        this(worldIn);
        setPosition(x, y, z);
        setContainedItem(stack);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
        dataManager.register(TIME_SINCE_HIT, 0);
        dataManager.register(DAMAGE_TAKEN, 0F);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() && !entityIn.isRidingOrBeingRiddenBy(this) ? entityIn.getEntityBoundingBox() : null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
        if (isEntityInvulnerable(source))
            return false;
        else if (!world.isRemote && !isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null && isPassenger(source.getTrueSource()))
                return false;
            else {
                setTimeSinceHit(10);
                setDamageTaken(getDamageTaken() + amount * 10.0F);
                markVelocityChanged();
                boolean instantBreak = source.getTrueSource() instanceof EntityPlayer &&
                        ((EntityPlayer) source.getTrueSource()).capabilities.isCreativeMode;

                if (instantBreak || getDamageTaken() > 40.0F) {
                    if (!instantBreak && world.getGameRules().getBoolean("doEntityDrops"))
                        entityDropItem(getContainedItem(), 0.0F);

                    setDead();
                }

                return true;
            }
        } else
            return true;
    }

    @Override
    public void applyEntityCollision(@Nonnull Entity entityIn) {
        if (entityIn instanceof EntityHoverboard) {
            if (entityIn.getEntityBoundingBox().minY < getEntityBoundingBox().maxY)
                super.applyEntityCollision(entityIn);
        } else if (entityIn.getEntityBoundingBox().minY <= getEntityBoundingBox().minY)
            super.applyEntityCollision(entityIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() * 11.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYaw = yaw;
        lerpPitch = pitch;
        lerpSteps = 10;
    }

    @Override
    protected void onInsideBlock(IBlockState state) {
        onGround |= state.getMaterial().blocksMovement();
    }

    private long lastJumped = 0;

    @Override
    public void setJumpPower(int jumpPower) {
        if (world.getTotalWorldTime() - lastJumped >= 20 && canFly()) {
            lastJumped = world.getTotalWorldTime();
            motionY += 0.42F * jumpPower / 75;
        }
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int jumpPower) {
        // NO-OP
    }

    @Override
    public void handleStopJump() {
        // NO-OP
    }

    @Nonnull
    @Override
    public EnumFacing getAdjustedHorizontalFacing() {
        return getHorizontalFacing().rotateY();
    }

    @Override
    public void onUpdate() {
        if (getTimeSinceHit() > 0)
            setTimeSinceHit(getTimeSinceHit() - 1);

        if (getDamageTaken() > 0.0F)
            setDamageTaken(getDamageTaken() - 1.0F);

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        super.onUpdate();

        tickLerp();

        if (canPassengerSteer()) {
            updateMotion();

            if (world.isRemote)
                controlBoard();

            double mX = motionX, mZ = motionZ;
            move(MoverType.SELF, motionX, motionY, motionZ);

            double prevMag = Math.sqrt(mX * mX + mZ * mZ);
            double newMag = Math.sqrt(motionX * motionX + motionZ * motionZ);


            if (prevMag > newMag && !world.isRemote) {
                float impactForce = (float)((prevMag - newMag) * 10.0D - 3.0D);

                if (impactForce > 0.0F) {
                    playSound(impactForce > 4 ? SoundEvents.ENTITY_PLAYER_BIG_FALL : SoundEvents.ENTITY_PLAYER_SMALL_FALL, 1, 1);
                    for (Entity passenger : getRecursivePassengers())
                        passenger.attackEntityFrom(DamageSource.FLY_INTO_WALL, impactForce);
                }
            }
        } else {
            motionX = 0.0D;
            motionY = 0.0D;
            motionZ = 0.0D;
        }

        if (!onGround && motionY > -0.08) // If we're moving in free-fall, no need to take power
            isPowered(true);


        doBlockCollisions();
        List<Entity> list = world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().grow(0.2, -0.01, 0.2), EntitySelectors.getTeamCollisionPredicate(this));

        for (Entity entity : list)
            if (!entity.isPassenger(this))
                applyEntityCollision(entity);
    }

    private void tickLerp() {
        if (lerpSteps > 0 && !canPassengerSteer()) {
            double x = posX + (lerpX - posX) / lerpSteps;
            double y = posY + (lerpY - posY) / lerpSteps;
            double z = posZ + (lerpZ - posZ) / lerpSteps;
            double yaw = MathHelper.wrapDegrees(lerpYaw - rotationYaw);
            rotationYaw = (float) (rotationYaw + yaw / lerpSteps);
            rotationPitch = (float) (rotationPitch + (lerpPitch - rotationPitch) / lerpSteps);
            --lerpSteps;
            setPosition(x, y, z);
            setRotation(rotationYaw, rotationPitch);
        }
    }

    private boolean overWater() {
        return !inWater && world.isMaterialInBB(getEntityBoundingBox().grow(0, -2, 0).shrink(0.001), Material.WATER);
    }

    private boolean overAny(World world, AxisAlignedBB bb) {
        int mX = MathHelper.floor(bb.minX);
        int mY = MathHelper.ceil(bb.maxX);
        int mZ = MathHelper.floor(bb.minY);
        int mxX = MathHelper.ceil(bb.maxY);
        int mxY = MathHelper.floor(bb.minZ);
        int mxZ = MathHelper.ceil(bb.maxZ);
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();

        for (int x = mX; x < mY; ++x) {
            for (int y = mZ; y < mxX; ++y) {
                for (int z = mxY; z < mxZ; ++z) {
                    IBlockState state = world.getBlockState(pos.setPos(x, y, z));
                    AxisAlignedBB bound = state.getCollisionBoundingBox(world, pos);
                    if (bound == null || !bound.offset(pos).intersects(bb))
                        continue;
                    pos.release();
                    return true;
                }
            }
        }

        pos.release();
        return false;
    }


    private boolean canFly() {
        return inWater || overWater() ||
                overAny(world, getEntityBoundingBox().grow(0, -HoverConfig.GENERAL.flightRange, 0).shrink(0.001)) ||
                (HoverConfig.UGLIES.ugliesFlight &&
                        world.isMaterialInBB(getEntityBoundingBox().grow(0, -HoverConfig.UGLIES.flightRange, 0).shrink(0.001), Material.WATER) ||
                        world.isMaterialInBB(getEntityBoundingBox().grow(0, -HoverConfig.UGLIES.flightRange, 0).shrink(0.001), Material.IRON));
    }

    private void updateMotion() {
        boolean isPowered = isPowered(false);

        boolean movingUp = motionY > 0 && !inWater;

        double dy = hasNoGravity() ? 0.0D : (isPowered && !movingUp ? -0.005 : -0.04);

        if (inWater)
            dy = -Math.min(motionY, 0) + 0.125 + dy;
        else if (canFly())
            dy = -Math.min(motionY, 0);

        float momentum = isPowered ? 0.95F : 0.25F;

        motionX *= momentum;
        motionZ *= momentum;
        deltaRotation *= momentum * 0.9;
        motionY += dy;
    }

    private void controlBoard() {
        Entity rider = getControllingPassenger();

        if (rider != null) {
            float f = 0.0F;

            if (leftInputDown)
                deltaRotation -= 2;

            if (rightInputDown)
                deltaRotation += 2;

            if (rightInputDown != leftInputDown && !forwardInputDown && !backInputDown)
                f += 0.05F;

            double riderDelta = ((((rider.rotationYaw - rotationYaw) % 360) + 540) % 360) - 180;

            rotationYaw += deltaRotation + riderDelta * 0.25;

            if (forwardInputDown)
                f += 0.4F;

            if (backInputDown)
                f -= 0.05F;

            if (f > 0) {
                float xDir = MathHelper.sin(-rotationYaw * (float) Math.PI / 180);
                float yDir = MathHelper.sin(-rider.rotationPitch * (float) Math.PI / 180);
                float zDir = MathHelper.cos(rotationYaw * (float) Math.PI / 180);

                float maxMomentum = 0.8F;
                float momentum = MathHelper.sqrt(motionX * motionX * xDir * xDir + motionZ * motionZ * zDir * zDir);

                float dm = (maxMomentum - momentum) * f / 2;

                motionX += (xDir * dm);
                motionZ += (zDir * dm);


                boolean allowUp = canFly();
                double threshold = 0.25;
                if (overWater()) threshold = 0.1;
                if (yDir < (threshold - 1) || (allowUp && yDir > (1 - threshold))) {
                    float yComponent = MathHelper.sqrt(momentum * momentum + yDir * yDir * motionY * motionY) - momentum;
                    float dym = (maxMomentum - yComponent) * f / 3;
                    if (allowUp)
                        dym *= 1.5;
                    motionY += (yDir * dym);
                }
            }
        }
    }

    @Override
    public void updatePassenger(@Nonnull Entity passenger) {
        if (isPassenger(passenger)) {
            float yaw = (float) (-rotationYaw * Math.PI / 180 - (Math.PI / 2));
            double x = MathHelper.cos(yaw) * 0.2;
            double y = (isDead ? 0.01 : getMountedYOffset()) + passenger.getYOffset();
            double z = MathHelper.sin(yaw) * 0.2;

            passenger.setPosition(posX + x, posY + y, posZ + z);
            passenger.fallDistance = 0.0f;
            passenger.rotationYaw += deltaRotation;
            passenger.setRotationYawHead(passenger.getRotationYawHead() + deltaRotation);
            applyYawToEntity(passenger);

            if (passenger instanceof EntityPlayer)
                Hoverboard.PROXY.updateInputs(this, (EntityPlayer) passenger);
        }
    }

    protected void applyYawToEntity(Entity entityToUpdate) {
        entityToUpdate.setRenderYawOffset(rotationYaw);
        float yaw = MathHelper.wrapDegrees(entityToUpdate.rotationYaw - rotationYaw);
        float maxChange = MathHelper.clamp(yaw, -105.0F, 105.0F);
        entityToUpdate.prevRotationYaw += maxChange - yaw;
        entityToUpdate.rotationYaw += maxChange - yaw;
        entityToUpdate.setRotationYawHead(entityToUpdate.rotationYaw);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void applyOrientationToEntity(Entity entityToUpdate) {
        applyYawToEntity(entityToUpdate);
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
        compound.setTag("Item", getContainedItem().serializeNBT());
        compound.setBoolean("Creative", creative);
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
        if (compound.hasKey("Item", Constants.NBT.TAG_COMPOUND))
            setContainedItem(new ItemStack(compound.getCompoundTag("Item")));
        if (compound.hasKey("Creative", Constants.NBT.TAG_ANY_NUMERIC))
            creative = compound.getBoolean("Creative");
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking())
            return false;

        player.startRiding(this);

        return true;

    }

    public void setDamageTaken(float damageTaken) {
        dataManager.set(DAMAGE_TAKEN, damageTaken);
    }

    public float getDamageTaken() {
        return dataManager.get(DAMAGE_TAKEN);
    }

    public void setTimeSinceHit(int timeSinceHit) {
        dataManager.set(TIME_SINCE_HIT, timeSinceHit);
    }

    public int getTimeSinceHit() {
        return dataManager.get(TIME_SINCE_HIT);
    }
    
    public void setContainedItem(ItemStack stack) {
        containedItem = stack;
        energyStorage = stack.hasCapability(CapabilityEnergy.ENERGY, null) ?
                stack.getCapability(CapabilityEnergy.ENERGY, null) :
                (HoverConfig.isBoardFree() ?
                        new DummyEnergyStorage() :
                        new EnergyStorage(0));
    }

    public boolean isPowered(boolean consumePower) {
        return creative ||
                (energyStorage.extractEnergy(HoverConfig.GENERAL.fuelCost, true) == 0 &&
                (!consumePower || energyStorage.extractEnergy(HoverConfig.GENERAL.fuelCost, false) == 0));
    }

    public ItemStack getContainedItem() {
        return containedItem;
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        List<Entity> list = getPassengers();
        return list.isEmpty() ? null : list.get(0);
    }

    @SideOnly(Side.CLIENT)
    public void updateInputs(boolean left, boolean right, boolean forward, boolean back) {
        leftInputDown = left;
        rightInputDown = right;
        forwardInputDown = forward;
        backInputDown = back;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (canPassengerSteer() && lerpSteps > 0) {
            lerpSteps = 0;
            posX = lerpX;
            posY = lerpY;
            posZ = lerpZ;
            rotationYaw = (float) lerpYaw;
            rotationPitch = (float) lerpPitch;
        }
    }

}
