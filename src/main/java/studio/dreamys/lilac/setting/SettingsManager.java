package studio.dreamys.lilac.setting;

import java.util.ArrayList;
import studio.dreamys.lilac.module.Module;

public class SettingsManager {
   private final ArrayList<studio.dreamys.lilac.setting.Setting> settings;

   public SettingsManager() {
      this.settings = new ArrayList<studio.dreamys.lilac.setting.Setting>();
   }

   public void rSetting(studio.dreamys.lilac.setting.Setting in) {
      this.settings.add(in);
   }

   public ArrayList<studio.dreamys.lilac.setting.Setting> getSettings() {
      return this.settings;
   }

   public ArrayList<studio.dreamys.lilac.setting.Setting> getSettingsByMod(Module mod) {
      ArrayList<studio.dreamys.lilac.setting.Setting> out =
         new ArrayList<studio.dreamys.lilac.setting.Setting>();

      for (studio.dreamys.lilac.setting.Setting s : this.getSettings()) {
         if (s.getParentMod().equals(mod)) {
            out.add(s);
         }
      }

      return out.isEmpty() ? null : out;
   }

   /** Quiet lookup. Returns null when absent, for callers where that is expected. */
   public studio.dreamys.lilac.setting.Setting findSetting(Module mod, String name) {
      for (studio.dreamys.lilac.setting.Setting set : this.getSettings()) {
         if (set.getName().equalsIgnoreCase(name) && set.getParentMod() == mod) {
            return set;
         }
      }

      return null;
   }

   /** Lookup that reports a miss, for names that are expected to exist. */
   public studio.dreamys.lilac.setting.Setting getSettingByName(Module mod, String name) {
      studio.dreamys.lilac.setting.Setting set = this.findSetting(mod, name);
      if (set == null) {
         System.err.println("[lilac] Setting not found: '" + mod.getName() + "' -> '" + name + "'");
      }
      return set;
   }
}
