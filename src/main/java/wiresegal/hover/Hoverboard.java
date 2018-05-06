package wiresegal.hover;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.hover.core.CommonProxy;
import wiresegal.hover.entity.EntityHoverboard;
import wiresegal.hover.entity.render.MovingSoundHoverboard;
import wiresegal.hover.entity.render.RenderHoverboard;
import wiresegal.hover.item.ItemCreativeHoverboard;
import wiresegal.hover.item.ItemHoverboard;

import static wiresegal.hover.Hoverboard.MOD_ID;

/**
 * @author WireSegal
 * Created at 9:47 AM on 5/5/18.
 */
@Mod(modid = MOD_ID, name = "Hoverboard", version = "GRADLE:VERSION")
@Mod.EventBusSubscriber
public class Hoverboard {
    public static final String MOD_ID = "hoverboard";

    @SidedProxy(clientSide = "wiresegal.hover.core.ClientProxy",
                serverSide = "wiresegal.hover.core.CommonProxy")
    public static CommonProxy PROXY;

    public static ItemHoverboard HOVERBOARD;
    public static ItemHoverboard CREATIVE;

    public static SoundEvent WHIRR;

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(HOVERBOARD = new ItemHoverboard("hoverboard"));
        event.getRegistry().register(CREATIVE = new ItemCreativeHoverboard("creative_hoverboard"));
    }

    @SubscribeEvent
    public static void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(WHIRR = new SoundEvent(new ResourceLocation(MOD_ID, "whirr"))
                .setRegistryName(new ResourceLocation(MOD_ID, "whirr")));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void playSound(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityHoverboard && event.getWorld().isRemote)
            Minecraft.getMinecraft().getSoundHandler()
                    .playSound(new MovingSoundHoverboard((EntityHoverboard) event.getEntity()));
    }

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityHoverboard.class)
                .tracker(80, 3, true)
                .name("hoverboard")
                .id("hoverboard", 1)
                .build());
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void onClientInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityHoverboard.class, RenderHoverboard::new);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void modelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(HOVERBOARD, 0, getRL(HOVERBOARD));
        ModelLoader.setCustomModelResourceLocation(CREATIVE, 0, getRL(CREATIVE));
    }

    @SideOnly(Side.CLIENT)
    private static ModelResourceLocation getRL(Item item) {
        ResourceLocation loc = item.getRegistryName();
        if (loc == null)
            return new ModelResourceLocation("missingno");

        return new ModelResourceLocation(loc, "inventory");
    }

}
