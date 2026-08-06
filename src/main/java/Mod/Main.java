package Mod;

import Mod.config.ConfigModule;
import Mod.config.GuiOpenModule;
import Mod.config.ModConfig;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.module.Module;

@net.minecraftforge.fml.common.Mod(
   modid = Main.MODID,
   name = Main.NAME,
   version = Main.VERSION,
   acceptedMinecraftVersions = "[1.8.9]"
)
public class Main {
   public static final String MODID = "weirdGUI";
   public static final String NAME = "weirdGUI";
   public static final String VERSION = "2.0";

   public static Minecraft mc;
   public static Field fxLayersField;
   public static Field rightClickDelayTimerField;
   public static Field leftClickCounter;
   public static Method rightClickMouse;
   public static Method mouseClicked;
   public static Random random = new Random();
   public static KeyBinding toggleClickGuiKey;

   /** Everything that used to be hardcoded now lives here. */
   private static ModConfig config;
   private static File configFile;

   public static ModConfig getConfig() {
      return config;
   }

   public static File getConfigFile() {
      return configFile;
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent e) {
      File dir = new File(e.getModConfigurationDirectory(), MODID);
      configFile = new File(dir, "modules.json");
      config = ModConfig.load(configFile);

      toggleClickGuiKey = new KeyBinding("Toggle ClickGUI", config.guiKey, "weirdGUI");
      ClientRegistry.registerKeyBinding(toggleClickGuiKey);

      new lilac(MODID, new lilac.ModuleProvider() {
         @Override
         public List<? extends Module> build() {
            return buildModules(config);
         }

         @Override
         public ArrayList<String> categories() {
            return new ArrayList<String>(config.categories);
         }
      });
   }

   /** Re-reads the config file and rebuilds the GUI in place. */
   public static void reload() {
      config = ModConfig.load(configFile);
      lilac.getInstance().reload();
   }

   /** Turns config entries into live modules. */
   private static List<Module> buildModules(ModConfig cfg) {
      List<Module> built = new ArrayList<Module>();
      for (ModConfig.ModuleEntry entry : cfg.modules) {
         try {
            built.add(entry.isClickGui()
               ? (Module) new GuiOpenModule(entry)
               : (Module) new ConfigModule(cfg, entry));
         } catch (Exception ex) {
            // one bad entry must not take down the whole GUI
            System.err.println("[weirdGUI] could not build module '"
               + entry.name + "': " + ex);
         }
      }
      return built;
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(this);
      mc = Minecraft.getMinecraft();
      ClientCommandHandler.instance.registerCommand(new Command());

      try {
         fxLayersField = ReflectionHelper.findField(EffectRenderer.class, new String[]{"field_78876_b", "fxLayers"});
         this.setFieldAccessible(fxLayersField);
      } catch (Exception ex) {
         System.err.println("Failed to set fxLayersField accessible: " + ex.getMessage());
      }

      rightClickMouse = this.findAndSetMethodAccessible(
         Minecraft.getMinecraft().getClass(), "func_147121_ag", "rightClickMouse");
      rightClickDelayTimerField = ReflectionHelper.findField(
         Minecraft.class, new String[]{"field_71467_ac", "rightClickDelayTimer"});
      this.setFieldAccessible(rightClickDelayTimerField);
      leftClickCounter = ReflectionHelper.findField(
         Minecraft.class, new String[]{"field_71429_W", "leftClickCounter"});
      this.setFieldAccessible(leftClickCounter);
   }

   private void setFieldAccessible(Field field) {
      if (field != null) {
         field.setAccessible(true);
      }
   }

   private Method findAndSetMethodAccessible(Class<?> clazz, String... methodNames) {
      Method method = null;

      for (String methodName : methodNames) {
         try {
            method = clazz.getDeclaredMethod(methodName);
            break;
         } catch (NoSuchMethodException ignored) {
         }
      }

      if (method != null) {
         method.setAccessible(true);
      }

      return method;
   }

   @SubscribeEvent
   public void onKeyInput(KeyInputEvent event) {
      if (toggleClickGuiKey.isPressed()) {
         Minecraft.getMinecraft().displayGuiScreen(lilac.getInstance().getClickGUI());
      }
   }
}
