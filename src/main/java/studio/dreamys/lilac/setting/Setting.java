package studio.dreamys.lilac.setting;

import java.util.ArrayList;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.module.Module;

public class Setting {
   private final String name;
   private final Module parent;
   private final String mode;
   private String sval;
   private ArrayList<String> options;
   private boolean bval;
   private double dval;
   private double min;
   private double max;
   private boolean onlyint;

   public Setting(String name, Module parent, String sval, ArrayList<String> options) {
      this.name = name;
      this.parent = parent;
      this.sval = sval;
      this.options = options;
      this.mode = "Combo";
   }

   public Setting(String name, Module parent, boolean bval) {
      this.name = name;
      this.parent = parent;
      this.bval = bval;
      this.mode = "Check";
   }

   public Setting(String name, Module parent, double dval, double min, double max, boolean onlyint) {
      this.name = name;
      this.parent = parent;
      this.dval = dval;
      this.min = min;
      this.max = max;
      this.onlyint = onlyint;
      this.mode = "Slider";
   }

   public boolean isOnlyint() {
      return this.onlyint;
   }

   public String getName() {
      return this.name;
   }

   public Module getParentMod() {
      return this.parent;
   }

   public String getValString() {
      return this.sval;
   }

   public void setValString(String in) {
      this.sval = in;
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public ArrayList<String> getOptions() {
      return this.options;
   }

   public boolean getValBoolean() {
      return this.bval;
   }

   public void setValBoolean(boolean in) {
      this.bval = in;
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public double getValDouble() {
      if (this.onlyint) {
         this.dval = (double)((int)this.dval);
      }

      return this.dval;
   }

   public void setValDouble(double in) {
      this.dval = in;
      if (lilac.getInstance().getSaveLoad() != null) {
         lilac.getInstance().getSaveLoad().save();
      }
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public boolean isCombo() {
      return this.mode.equalsIgnoreCase("Combo");
   }

   public boolean isCheck() {
      return this.mode.equalsIgnoreCase("Check");
   }

   public boolean isSlider() {
      return this.mode.equalsIgnoreCase("Slider");
   }
}
