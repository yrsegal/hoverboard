package wiresegal.hover.entity.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.entity.EntityHoverboard;

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

    private final ModelHoverboard MODEL = new ModelHoverboard();

    public void setupRotation(EntityHoverboard hoverboard, float yaw, float partial)
    {
        GlStateManager.rotate(180 - yaw, 0, 1, 0);
        float f = (float)hoverboard.getTimeSinceHit() - partial;
        float f1 = hoverboard.getDamageTaken() - partial;

        if (f1 < 0)
            f1 = 0;

        if (f > 0)
            GlStateManager.rotate(MathHelper.sin(f) * f * f1 / 10, 1, 0, 0);

        GlStateManager.scale(-1, -1, 1);
    }

    public void setupTranslation(double x, double y, double z)
    {
        GlStateManager.translate(x, y + 0.375, z);
    }

    @Override
    public void doRender(@Nonnull EntityHoverboard entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(entity, entityYaw, partialTicks);
        this.bindEntityTexture(entity);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        MODEL.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityHoverboard entity) {
        return TEXTURE;
    }
}
