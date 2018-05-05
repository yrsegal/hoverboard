package wiresegal.hover.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static wiresegal.hover.Hoverboard.MOD_ID;

/**
 * @author WireSegal
 * Created at 2:16 PM on 5/5/18.
 */
@SideOnly(Side.CLIENT)
public class RenderHoverboard extends Render<EntityHoverboard> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/entity/hoverboard.png");

    public RenderHoverboard(RenderManager renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityHoverboard entity) {
        return TEXTURE;
    }
}
