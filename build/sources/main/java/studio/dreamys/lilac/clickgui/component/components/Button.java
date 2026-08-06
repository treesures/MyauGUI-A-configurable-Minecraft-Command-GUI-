package studio.dreamys.lilac.clickgui.component.components;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.IFrame;
import studio.dreamys.lilac.clickgui.component.components.sub.Checkbox;
import studio.dreamys.lilac.clickgui.component.components.sub.Keybind;
import studio.dreamys.lilac.clickgui.component.components.sub.ModeButton;
import studio.dreamys.lilac.clickgui.component.components.sub.Slider;
import studio.dreamys.lilac.module.Module;

public class Button extends Component {
   private final ArrayList<Component> subcomponents;
   public final Module mod;
   public final IFrame parent;
   public int offset;
   public boolean open;
   private boolean isHovered;

   public Button(Module mod, IFrame parent, int offset) {
      this.mod = mod;
      this.parent = parent;
      this.offset = offset;
      this.subcomponents = new ArrayList<>();
      this.open = false;
      int opY = offset + 12;
      if (lilac.getInstance().getSettingsManager().getSettingsByMod(mod) != null) {
         for (studio.dreamys.lilac.setting.Setting s : lilac.getInstance().getSettingsManager().getSettingsByMod(mod)) {
            if (s.isCombo()) {
               this.subcomponents.add(new ModeButton(s, this, opY));
               opY += 12;
            }

            if (s.isSlider()) {
               this.subcomponents.add(new Slider(s, this, opY));
               opY += 12;
            }

            if (s.isCheck()) {
               this.subcomponents.add(new Checkbox(s, this, opY));
               opY += 12;
            }
         }
      }

      this.subcomponents.add(new Keybind(this, opY));
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
      int opY = this.offset + 12;

      for (Component comp : this.subcomponents) {
         comp.setOff(opY);
         opY += 12;
      }
   }

   @Override
   public void renderComponent() {
      Color backgroundColor;
      if (this.isHovered) {
         if (this.mod.isToggled()) {
            backgroundColor = new Color(-14540254).darker();
         } else {
            backgroundColor = new Color(2960685).darker();
         }
      } else if (this.mod.isToggled()) {
         backgroundColor = new Color(-15658735);
      } else {
         backgroundColor = new Color(14, 14, 14);
      }

      Gui.drawRect(
         this.parent.getX(),
         this.parent.getY() + this.offset,
         this.parent.getX() + this.parent.getWidth(),
         this.parent.getY() + 12 + this.offset,
         backgroundColor.getRGB()
      );
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(
            this.mod.getName(),
            (float)((this.parent.getX() + 2) * 2),
            (float)((this.parent.getY() + this.offset + 2) * 2 + 4),
            !this.mod.isToggled() ? 10066329 : -1
         );
      if (this.subcomponents.size() > 1) {
         Minecraft.getMinecraft()
            .fontRendererObj
            .drawStringWithShadow(
               this.open ? "-" : "+",
               (float)((this.parent.getX() + this.parent.getWidth() - 10) * 2),
               (float)((this.parent.getY() + this.offset + 2) * 2 + 4),
               -1
            );
      }

      GL11.glPopMatrix();
      if (this.open && !this.subcomponents.isEmpty()) {
         for (Component comp : this.subcomponents) {
            comp.renderComponent();
         }
      }
   }

   @Override
   public int getHeight() {
      return this.open ? 12 * (this.subcomponents.size() + 1) : 12;
   }

   @Override
   public void updateComponent(int mouseX, int mouseY) throws IOException {
      this.isHovered = this.isMouseOnButton(mouseX, mouseY);
      if (!this.subcomponents.isEmpty()) {
         for (Component comp : this.subcomponents) {
            comp.updateComponent(mouseX, mouseY);
         }
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
      if (this.isMouseOnButton(mouseX, mouseY) && button == 0) {
         this.mod.toggle();
      }

      if (this.isMouseOnButton(mouseX, mouseY) && button == 1) {
         this.open = !this.open;
         this.parent.refresh();
      }

      for (Component comp : this.subcomponents) {
         comp.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      for (Component comp : this.subcomponents) {
         comp.mouseReleased(mouseX, mouseY, mouseButton);
      }
   }

   @Override
   public void keyTyped(char typedChar, int key) throws IOException {
      for (Component comp : this.subcomponents) {
         comp.keyTyped(typedChar, key);
      }
   }

   public boolean isMouseOnButton(int x, int y) {
      return x > this.parent.getX()
         && x < this.parent.getX() + this.parent.getWidth()
         && y > this.parent.getY() + this.offset
         && y < this.parent.getY() + 12 + this.offset;
   }
}
