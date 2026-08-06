package studio.dreamys.lilac.clickgui.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.clickgui.ClickGUI;
import studio.dreamys.lilac.clickgui.component.components.Profile;
import studio.dreamys.lilac.module.Module;

public class ProfilesFrame implements IFrame {
   private final int width;
   private final int barHeight;
   public final ArrayList<Component> components;
   public final String category;
   public int dragX;
   public int dragY;
   private boolean open;
   private int y;
   private int x;
   private boolean isDragging;
   private boolean needRefresh = false;

   public ProfilesFrame(String cat) {
      this.components = new ArrayList<>();
      this.category = cat;
      this.width = 88;
      this.x = 5;
      this.y = 5;
      this.barHeight = 13;
      this.dragX = 0;
      this.open = false;
      this.isDragging = false;
      this.components.add(new Profile(this, 0, () -> this.refreshProfiles(), () -> {
      }, "Refresh"));
      this.refresh();
      this.refreshProfiles();
   }

   private void refreshProfiles() {
      this.needRefresh = true;
   }

   private void saveProfile(File file) {
      Minecraft.getMinecraft()
         .ingameGUI
         .getChatGUI()
         .printChatMessage(new ChatComponentText("Save profile: " + file.getName().substring(0, file.getName().length() - 5)));

      try {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         LinkedHashMap<String, LinkedHashMap<String, Object>> map = new LinkedHashMap<>();

         for (Module module : lilac.getInstance().getModuleManager().getModules()) {
            LinkedHashMap<String, Object> settingsMap = new LinkedHashMap<>();
            ArrayList<studio.dreamys.lilac.setting.Setting> settingsByMod = lilac.getInstance().getSettingsManager().getSettingsByMod(module);
            if (settingsByMod != null) {
               for (studio.dreamys.lilac.setting.Setting setting : settingsByMod) {
                  if (setting.isCheck()) {
                     settingsMap.put(setting.getName(), Boolean.valueOf(setting.getValBoolean()));
                  } else if (setting.isCombo()) {
                     settingsMap.put(setting.getName(), setting.getValString());
                  } else if (setting.isSlider()) {
                     if (setting.isOnlyint()) {
                        settingsMap.put(setting.getName(), Integer.valueOf((int)setting.getValDouble()));
                     } else {
                        settingsMap.put(setting.getName(), Double.valueOf(setting.getValDouble()));
                     }
                  }
               }
            }

            settingsMap.put("toggled", Boolean.valueOf(module.isToggled()));
            settingsMap.put("key", Integer.valueOf(module.getKey()));
            map.put(module.getName(), settingsMap);
         }

         String json = gson.toJson(map);
         PrintWriter printWriter = new PrintWriter(file);
         printWriter.write(json);
         printWriter.close();
      } catch (IOException var10) {
         var10.printStackTrace();
      }
   }

   private void loadProfile(File file) {
      if (Minecraft.getMinecraft().thePlayer != null) {
         Minecraft.getMinecraft().thePlayer.sendChatMessage(".c load " + file.getName().substring(0, file.getName().length() - 5));
      }

      Minecraft.getMinecraft()
         .ingameGUI
         .getChatGUI()
         .printChatMessage(new ChatComponentText("Load profile: " + file.getName().substring(0, file.getName().length() - 5)));
      Module.ignoreChatCommand();

      try {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         FileReader fileReader = new FileReader(file);
         JsonObject jsonObject = (JsonObject)gson.fromJson(fileReader, JsonObject.class);

         for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String moduleName = entry.getKey();
            JsonObject settings = entry.getValue().getAsJsonObject();
            Module module = lilac.getInstance().getModuleManager().getModule(moduleName);
            if (module != null) {
               for (Entry<String, JsonElement> setting : settings.entrySet()) {
                  String settingName = setting.getKey();
                  if (!settingName.equals("hidden")) {
                     if (settingName.equals("toggled")) {
                        module.setToggledNoEvent(setting.getValue().getAsBoolean());
                     } else if (settingName.equals("key")) {
                        module.key(setting.getValue().getAsInt());
                     } else {
                        if (moduleName.equalsIgnoreCase("BBLR") && settingName.equalsIgnoreCase("hud-position-x")) {
                           settingName = "HUD Pos-X";
                        } else if (moduleName.equalsIgnoreCase("BBLR") && settingName.equalsIgnoreCase("hud-position-y")) {
                           settingName = "HUD Pos-Y";
                        } else if (moduleName.equalsIgnoreCase("BBLR") && settingName.equalsIgnoreCase("hud-offset-x")) {
                           settingName = "HUD Offset-X";
                        } else if (moduleName.equalsIgnoreCase("BBLR") && settingName.equalsIgnoreCase("hud-offset-y")) {
                           settingName = "HUD Offset-Y";
                        }

                        // quiet lookup: trying both spellings is expected here
                        studio.dreamys.lilac.setting.Setting moduleSetting =
                           lilac.getInstance().getSettingsManager().findSetting(module, settingName);
                        if (moduleSetting == null) {
                           moduleSetting = lilac.getInstance().getSettingsManager()
                              .findSetting(module, settingName.replace("-", " "));
                        }

                        if (moduleSetting != null) {
                           if (moduleSetting.isCheck()) {
                              moduleSetting.setValBoolean(setting.getValue().getAsBoolean());
                           } else if (moduleSetting.isCombo()) {
                              moduleSetting.setValString(setting.getValue().getAsString());
                           } else if (moduleSetting.isSlider()) {
                              if (moduleSetting.isOnlyint()) {
                                 moduleSetting.setValDouble((double)setting.getValue().getAsInt());
                              } else {
                                 moduleSetting.setValDouble(setting.getValue().getAsDouble());
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         fileReader.close();
      } catch (Exception var14) {
         var14.printStackTrace();
         Minecraft.getMinecraft().ingameGUI.getChatGUI()
            .printChatMessage(new ChatComponentText("Failed to load profile: " + var14.getMessage()));
      }
   }

   @Override
   public void setDragX(int dragX) {
      this.dragX = dragX;
   }

   @Override
   public void setDragY(int dragY) {
      this.dragY = dragY;
   }

   @Override
   public ArrayList<Component> getComponents() {
      return this.components;
   }

   @Override
   public void setDrag(boolean drag) {
      this.isDragging = drag;
   }

   @Override
   public boolean isOpen() {
      return this.open;
   }

   @Override
   public void setOpen(boolean open) {
      this.open = open;
   }

   @Override
   public void renderFrame(FontRenderer fontRenderer) {
      if (this.needRefresh) {
         this.needRefresh = false;

         while (this.components.size() > 1) {
            this.components.remove(this.components.size() - 1);
         }

         File mcDir = Minecraft.getMinecraft().mcDataDir;
         File profilesDir = new File(mcDir, "config/weirdGUI");
         profilesDir.mkdirs();

         File[] files = profilesDir.listFiles();
         if (files != null) {
            for (File file : files) {
               if (file.isFile() && file.getName().endsWith(".json")) {
                  String profileName = file.getName().substring(0, file.getName().length() - 5);
                  this.components.add(new Profile(this, 0, () -> this.loadProfile(file), () -> {
                  }, profileName));
               }
            }
         }

         this.refresh();
      }

      Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.barHeight, ClickGUI.color);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      fontRenderer.drawStringWithShadow(this.category, (float)((this.x + 2) * 2 + 5), ((float)this.y + 2.5F) * 2.0F + 5.0F, -1);
      fontRenderer.drawStringWithShadow(this.open ? "-" : "+", (float)((this.x + this.width - 10) * 2 + 5), ((float)this.y + 2.5F) * 2.0F + 5.0F, -1);
      GL11.glPopMatrix();
      if (this.open && !this.components.isEmpty()) {
         for (Component component : this.components) {
            component.renderComponent();
         }
      }
   }

   @Override
   public void refresh() {
      int off = this.barHeight;

      for (Component comp : this.components) {
         comp.setOff(off);
         off += comp.getHeight();
      }
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setX(int newX) {
      this.x = newX;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setY(int newY) {
      this.y = newY;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void updatePosition(int mouseX, int mouseY) {
      if (this.isDragging) {
         this.setX(mouseX - this.dragX);
         this.setY(mouseY - this.dragY);
      }
   }

   @Override
   public boolean isWithinHeader(int x, int y) {
      return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
   }
}
