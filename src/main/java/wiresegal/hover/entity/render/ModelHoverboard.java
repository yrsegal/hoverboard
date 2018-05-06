package wiresegal.hover.entity.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author WireSegal
 * Created at 9:18 PM on 5/5/18.
 */
@SideOnly(Side.CLIENT)
public class ModelHoverboard extends ModelBase {
    public final ModelRenderer renderer;

    public ModelHoverboard() {
        this.renderer = new ModelRenderer(this, "box").setTextureSize(32, 32).addBox(-5, 0, -1, 11, 16, 1);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.translate(-0.025, 0.2, -0.5);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        renderer.render(scale);
    }
}
