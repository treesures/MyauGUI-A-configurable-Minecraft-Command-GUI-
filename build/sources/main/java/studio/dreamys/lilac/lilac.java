package studio.dreamys.lilac;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;
import studio.dreamys.lilac.clickgui.ClickGUI;
import studio.dreamys.lilac.module.Module;
import studio.dreamys.lilac.module.ModuleManager;
import studio.dreamys.lilac.util.SaveLoad;

public class lilac {
   private final String modid;
   private final ModuleProvider provider;
   private ModuleManager moduleManager;
   private studio.dreamys.lilac.setting.SettingsManager settingsManager;
   private ClickGUI clickGUI;
   private SaveLoad saveLoad;
   private ArrayList<String> categories;
   private static lilac instance;

   /**
    * Builds the modules once the framework is ready. Modules register their
    * settings during construction, which needs the SettingsManager and the
    * singleton to already exist, so they cannot be passed in ready-made.
    */
   public interface ModuleProvider {
      List<? extends Module> build();

      /** Frame order for the ClickGUI, re-read on reload. */
      ArrayList<String> categories();
   }

   public lilac(String modid, ModuleProvider provider) {
      instance = this;
      this.modid = modid;
      this.provider = provider;
      MinecraftForge.EVENT_BUS.register(this);
      this.build();
      this.saveLoad = new SaveLoad(modid);
   }

   private void build() {
      this.categories = provider.categories();
      this.settingsManager = new studio.dreamys.lilac.setting.SettingsManager();
      // instance and settingsManager are live from here on, so modules may register settings
      this.moduleManager = new ModuleManager(provider.build());
      this.clickGUI = new ClickGUI();
   }

   /**
    * Rebuilds every module, setting and frame from the current config.
    * Lets a config edit take effect without restarting the game.
    */
   public void reload() {
      // old modules are still on the event bus and would keep reacting to ticks
      if (this.moduleManager != null) {
         for (Module m : this.moduleManager.getModules()) {
            MinecraftForge.EVENT_BUS.unregister(m);
         }
      }

      // rebuilding replays saved toggles, which would fire a burst of commands
      Module.ignoreChatCommand();
      this.build();

      if (this.saveLoad != null) {
         this.saveLoad.load();
      }

      // the player may be standing in the old screen, whose frames are now stale
      if (Minecraft.getMinecraft().currentScreen instanceof ClickGUI) {
         Minecraft.getMinecraft().displayGuiScreen(this.clickGUI);
      }
   }

   @SubscribeEvent
   public void key(KeyInputEvent e) {
      if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
         if (Keyboard.getEventKeyState()) {
            int keyCode = Keyboard.getEventKey();
            if (keyCode <= 0) {
               return;
            }

            for (Module m : this.moduleManager.getModules()) {
               if (m.getKey() == keyCode) {
                  // the client reacts to the bind itself, so only mirror the state here
                  m.setToggledNoEvent(!m.isToggled());
               }
            }
         }
      }
   }

   public String getModid() {
      return this.modid;
   }

   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   public studio.dreamys.lilac.setting.SettingsManager getSettingsManager() {
      return this.settingsManager;
   }

   public ClickGUI getClickGUI() {
      return this.clickGUI;
   }

   public SaveLoad getSaveLoad() {
      return this.saveLoad;
   }

   public ArrayList<String> getCategories() {
      return this.categories;
   }

   public static lilac getInstance() {
      return instance;
   }
}
