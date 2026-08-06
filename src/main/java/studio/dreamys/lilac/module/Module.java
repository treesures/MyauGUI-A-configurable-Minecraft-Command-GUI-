package studio.dreamys.lilac.module;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Keyboard;
import studio.dreamys.lilac.lilac;

public class Module {
   private final String name;
   private final String category;
   private int key;
   private boolean toggled;
   private static int ignore = 0;

   public static void startTickListener() {
      MinecraftForge.EVENT_BUS.register(new Module.TickListener());
   }

   public Module(String name, String category) {
      this.name = name;
      this.key = 0;
      this.category = category;
      this.toggled = false;
      MinecraftForge.EVENT_BUS.register(this);
   }

   public String getName() {
      return this.name;
   }

   public String getCategory() {
      return this.category;
   }

   public int getKey() {
      return this.key;
   }

   public void key(int key) {
      this.key = key;
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }

      String command = this.buildKeybindCommand(key);
      if (command != null && !command.trim().isEmpty()) {
         this.sendChatCommand(command);
      }
   }

   /**
    * Command sent when this module's keybind changes. Overridden by config-driven
    * modules so the format can be edited without recompiling.
    */
   protected String buildKeybindCommand(int key) {
      return ".bind " + this.getName().toLowerCase() + " " + Keyboard.getKeyName(key).toLowerCase();
   }

   public studio.dreamys.lilac.setting.Setting getSetting(String name) {
      return lilac.getInstance().getSettingsManager().getSettingByName(this, name);
   }

   public void set(studio.dreamys.lilac.setting.Setting set) {
      lilac.getInstance().getSettingsManager().rSetting(set);
   }

   public void setToggledNoEvent(boolean toggled) {
      this.toggled = toggled;
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public void setToggled(boolean toggled) {
      this.toggled = toggled;
      if (this.toggled) {
         this.onEnable();
      } else {
         this.onDisable();
      }

      this.onToggled();
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void toggle() {
      this.toggled = !this.toggled;
      if (this.toggled) {
         this.onEnable();
      } else {
         this.onDisable();
      }

      this.onToggled();
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public void onToggled() {
   }

   public static void ignoreChatCommand() {
      ignore = 3;
   }

   public void sendChatCommand(String command) {
      if (ignore <= 0) {
         if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
         }
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private static class TickListener {
      TickListener() {
      }

      @SubscribeEvent
      public void onClientTick(ClientTickEvent event) {
         if (Module.ignore > 0) {
            Module.ignore--;
         }
      }
   }
}
