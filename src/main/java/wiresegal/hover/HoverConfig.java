package wiresegal.hover;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static wiresegal.hover.Hoverboard.MOD_ID;

/**
 * @author WireSegal
 * Created at 9:50 AM on 5/5/18.
 */
@Config(modid = MOD_ID, category = "")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class HoverConfig {

    @Config.Name("uglies")
    public static final UgliesFlight UGLIES = new UgliesFlight();
    @Config.Name("general")
    public static final General GENERAL = new General();

    public static class UgliesFlight {
        @Config.Name("Enable 'Uglies' Flight")
        @Config.Comment({ "Should the hoverboard by able to fly when over blocks like water and iron?",
                "This feature is a reference to the Uglies series by Scott Westerfeld.",
                "Default: false" })
        public boolean ugliesFlight = false;

        @Config.Name("Flight Range")
        @Config.Comment({
                "How far below the hoverboard can the magnetic block be before it stops working?",
                "Default: 8" })
        public int flightRange = 8;
    }

    public static class General {
        @Config.Name("Fuel Cost")
        @Config.Comment({
                "How much RF/FE does the Hoverboard take per tick to run?",
                "It won't consume power while moving down at the speed of gravity or more.",
                "If Fuel Cost or Fuel Storage is 0, the hoverboard will be free to use."
        })
        public int fuelCost = 5;

        @Config.Name("Fuel Storage")
        @Config.Comment({
                "How much RF/FE does the Hoverboard store?",
                "If Fuel Cost or Fuel Storage is 0, the hoverboard won't store power at all."
        })
        public int fuelStorage = 30000;
    }

    public static boolean isBoardFree() {
        return GENERAL.fuelCost == 0 && GENERAL.fuelStorage == 0;
    }

    @SubscribeEvent
    public static void configReload(ConfigChangedEvent event) {
        if (event.getModID().equals(MOD_ID))
            ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
    }

}
