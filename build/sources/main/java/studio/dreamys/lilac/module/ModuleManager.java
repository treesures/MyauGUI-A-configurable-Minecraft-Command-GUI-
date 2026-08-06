package studio.dreamys.lilac.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleManager {
   private final ArrayList<Module> modules;

   /**
    * Takes already-built modules. The old constructor took classes and
    * reflected them into existence, which meant every module had to be its own
    * compiled class; config-driven modules are instances of one shared class.
    */
   public ModuleManager(List<? extends Module> modules) {
      long l = System.currentTimeMillis();
      this.modules = new ArrayList<Module>(modules);
      System.out.println("Initialized Module Manager with " + this.modules.size()
         + " modules. (" + (System.currentTimeMillis() - l) + "ms)");
      Module.startTickListener();
   }

   public ArrayList<Module> getModules() {
      return this.modules;
   }

   public Module getModule(String name) {
      for (Module m : this.modules) {
         if (m.getName().equalsIgnoreCase(name)) {
            return m;
         }
      }

      return null;
   }

   public ArrayList<Module> getModulesInCategory(String c) {
      ArrayList<Module> mods = new ArrayList<Module>();

      for (Module m : this.modules) {
         if (Objects.equals(m.getCategory(), c)) {
            mods.add(m);
         }
      }

      return mods;
   }
}
