package studio.dreamys.lilac.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.module.Module;

/**
 * Remembers which modules are on and what each setting is set to.
 *
 * This is the user's state, kept separate from modules.json, which is the
 * definition of what exists. Editing the definition never wipes the state.
 *
 * Fields are tab-separated; module and setting names come from a user-edited
 * config and may contain colons, which the old ":" format could not survive.
 * Old ":" files are still read so existing setups are not lost.
 */
public class SaveLoad {
   private static final Charset UTF8 = Charset.forName("UTF-8");
   private static final String SEP = "\t";

   private final File file;
   /** Suppresses saving while loading, so a partial load cannot overwrite the file. */
   private boolean loading;

   public SaveLoad(String modid) {
      File dir = new File(Minecraft.getMinecraft().mcDataDir, modid);
      this.file = new File(dir, "state.txt");

      try {
         if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[lilac] could not create " + dir);
         }

         File legacy = new File(dir, "config.txt");
         if (!this.file.exists() && legacy.exists()) {
            // keep setups made by the previous version
            System.out.println("[lilac] migrating config.txt -> state.txt");
            copy(legacy, this.file);
         }

         if (!this.file.exists() && !this.file.createNewFile()) {
            System.err.println("[lilac] could not create " + this.file);
         }

         this.load();
      } catch (IOException e) {
         System.err.println("[lilac] state init failed: " + e.getMessage());
      }
   }

   public void save() {
      if (this.loading) {
         return;
      }

      PrintWriter pw = null;
      try {
         ArrayList<String> toSave = new ArrayList<String>();

         for (Module mod : lilac.getInstance().getModuleManager().getModules()) {
            toSave.add("MOD" + SEP + mod.getName() + SEP + mod.isToggled() + SEP + mod.getKey());
         }

         for (studio.dreamys.lilac.setting.Setting set : lilac.getInstance().getSettingsManager().getSettings()) {
            String value;
            if (set.isSlider()) {
               value = String.valueOf(set.getValDouble());
            } else if (set.isCheck()) {
               value = String.valueOf(set.getValBoolean());
            } else if (set.isCombo()) {
               value = set.getValString();
            } else {
               continue;
            }
            toSave.add("SET" + SEP + set.getName() + SEP + set.getParentMod().getName() + SEP + value);
         }

         pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.file), UTF8));
         for (String line : toSave) {
            pw.println(line);
         }
      } catch (IOException e) {
         System.err.println("[lilac] could not save state: " + e.getMessage());
      } finally {
         if (pw != null) {
            pw.close();
         }
      }
   }

   public void load() {
      BufferedReader reader = null;
      this.loading = true;
      try {
         reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), UTF8));

         for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.trim().isEmpty()) {
               continue;
            }

            String[] args = line.indexOf('\t') >= 0 ? line.split("\t", 4) : line.split(":", 4);
            if (args.length < 4) {
               continue;
            }

            String kind = args[0].toLowerCase(Locale.ROOT);

            if ("mod".equals(kind)) {
               Module m = lilac.getInstance().getModuleManager().getModule(args[1]);
               // a module removed from the config just has no state to restore
               if (m == null) {
                  continue;
               }
               m.setToggled(Boolean.parseBoolean(args[2]));
               try {
                  m.key(Integer.parseInt(args[3].trim()));
               } catch (NumberFormatException ignored) {
               }
            } else if ("set".equals(kind)) {
               Module m = lilac.getInstance().getModuleManager().getModule(args[2]);
               if (m == null) {
                  continue;
               }
               studio.dreamys.lilac.setting.Setting set =
                  lilac.getInstance().getSettingsManager().findSetting(m, args[1]);
               if (set == null) {
                  continue;
               }
               if (set.isSlider()) {
                  try {
                     set.setValDouble(Double.parseDouble(args[3].trim()));
                  } catch (NumberFormatException ignored) {
                  }
               } else if (set.isCheck()) {
                  set.setValBoolean(Boolean.parseBoolean(args[3]));
               } else if (set.isCombo()) {
                  // the config may have dropped this option since it was saved
                  if (set.getOptions() != null && set.getOptions().contains(args[3])) {
                     set.setValString(args[3]);
                  }
               }
            }
         }
      } catch (IOException e) {
         System.err.println("[lilac] could not load state: " + e.getMessage());
      } finally {
         this.loading = false;
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException ignored) {
            }
         }
      }
   }

   private static void copy(File from, File to) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(from), UTF8));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(to), UTF8));
      try {
         for (String line = in.readLine(); line != null; line = in.readLine()) {
            out.println(line);
         }
      } finally {
         in.close();
         out.close();
      }
   }
}
