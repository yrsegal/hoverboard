package wiresegal.hover.core;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.entity.EntityHoverboard;

/**
 * @author WireSegal
 * Created at 1:16 PM on 5/5/18.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void updateInputs(EntityHoverboard hoverboard, EntityPlayer player) {
        if (player instanceof EntityPlayerSP) {
            EntityPlayerSP sp = (EntityPlayerSP) player;
            hoverboard.updateInputs(sp.movementInput.leftKeyDown,
                    sp.movementInput.rightKeyDown,
                    sp.movementInput.forwardKeyDown,
                    sp.movementInput.backKeyDown);
        }
    }
}
