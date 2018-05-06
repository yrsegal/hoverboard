package wiresegal.hover.entity.render;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.HoverConfig;
import wiresegal.hover.Hoverboard;
import wiresegal.hover.entity.EntityHoverboard;

/**
 * @author WireSegal
 * Created at 11:03 PM on 5/5/18.
 */
@SideOnly(Side.CLIENT)
public class MovingSoundHoverboard extends MovingSound {
    private final EntityHoverboard hoverboard;

    public MovingSoundHoverboard(EntityHoverboard hoverboardIn) {
        super(Hoverboard.WHIRR, SoundCategory.NEUTRAL);
        this.hoverboard = hoverboardIn;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.25f * HoverConfig.GENERAL.hoverboardVolume;
        this.pitch = 0.25f;
    }

    @Override
    public void update() {
        if (this.hoverboard.isDead)
            this.donePlaying = true;
        else {
            this.xPosF = (float) this.hoverboard.posX;
            this.yPosF = (float) this.hoverboard.posY;
            this.zPosF = (float) this.hoverboard.posZ;
        }
    }
}
