package studio.dreamys.lilac.clickgui.component.components.sub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.components.Button;

public class Keybind extends Component {
   private final Button parent;
   private boolean hovered;
   private boolean binding;
   private int offset;
   private int x;
   private int y;

   public Keybind(Button button, int offset) {
      this.parent = button;
      this.x = button.parent.getX() + button.parent.getWidth();
      this.y = button.parent.getY() + button.offset;
      this.offset = offset;
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
   }

   @Override
   public void renderComponent() {
      Gui.drawRect(
         this.parent.parent.getX() + 2,
         this.parent.parent.getY() + this.offset,
         this.parent.parent.getX() + this.parent.parent.getWidth(),
         this.parent.parent.getY() + this.offset + 12,
         this.hovered ? -14540254 : -15658735
      );
      Gui.drawRect(
         this.parent.parent.getX(),
         this.parent.parent.getY() + this.offset,
         this.parent.parent.getX() + 2,
         this.parent.parent.getY() + this.offset + 12,
         -15658735
      );
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(
            this.binding ? "Press a key..." : "Key: " + Keyboard.getKeyName(this.parent.mod.getKey()),
            (float)((this.parent.parent.getX() + 7) * 2),
            (float)((this.parent.parent.getY() + this.offset + 2) * 2 + 5),
            -1
         );
      GL11.glPopMatrix();
   }

   @Override
   public void updateComponent(int mouseX, int mouseY) {
      this.hovered = this.isMouseOnButton(mouseX, mouseY);
      this.y = this.parent.parent.getY() + this.offset;
      this.x = this.parent.parent.getX();
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.isMouseOnButton(mouseX, mouseY) && button == 0 && this.parent.open) {
         this.binding = !this.binding;
      }
   }

   @Override
   public void keyTyped(char typedChar, int key) {
      if (this.binding) {
         this.parent.mod.key(key);
         this.binding = false;
      }
   }

   public boolean isMouseOnButton(int x, int y) {
      return x > this.x && x < this.x + 88 && y > this.y && y < this.y + 12;
   }
}
