package wiresegal.hover.compat;

import com.google.common.base.Function;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import wiresegal.hover.Hoverboard;
import wiresegal.hover.entity.EntityHoverboard;
import wiresegal.hover.item.ItemHoverboard;

import javax.annotation.Nullable;


public class TOPCompatibility implements Function<ITheOneProbe, Void> {

		@Nullable
		@Override
		public Void apply(ITheOneProbe theOneProbe) {
			theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
				@Override
				public String getID() {
					return Hoverboard.MOD_ID+":default";
				}
				
				@Override
				public void addProbeEntityInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, Entity entity, IProbeHitEntityData iProbeHitEntityData) {
					if(entity instanceof EntityHoverboard) {
						int currentPower = ((EntityHoverboard)entity).getPower();
						int maxPower = ((EntityHoverboard)entity).getMaxFuel();
						
						iProbeInfo.progress(currentPower,maxPower,iProbeInfo.defaultProgressStyle()
								.suffix("FE")
								.filledColor(Config.rfbarFilledColor)
								.alternateFilledColor(Config.rfbarAlternateFilledColor)
								.borderColor(Config.rfbarBorderColor)
								.numberFormat(Config.rfFormat));
					}
				}
				
				
			});
			
			return null;
	}
}