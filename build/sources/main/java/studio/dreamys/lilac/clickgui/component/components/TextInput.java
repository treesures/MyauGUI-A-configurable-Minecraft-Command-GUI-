package studio.dreamys.lilac.clickgui.component.components;

import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.IFrame;

public class TextInput extends Component {
   public final IFrame parent;
   private String text;
   public int offset;
   private boolean isHovered;
   private boolean focus = false;

   public TextInput(IFrame parent, int offse, String text) {
      this.parent = parent;
      this.offset = this.offset;
      int opY = this.offset + 12;
      this.text = text;
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
      int opY = this.offset + 12;
   }

   public String getText() {
      return this.text;
   }

   @Override
   public void renderComponent() {
      Gui.drawRect(
         this.parent.getX(),
         this.parent.getY() + this.offset,
         this.parent.getX() + this.parent.getWidth(),
         this.parent.getY() + 12 + this.offset,
         this.isHovered ? new Color(2960685).darker().getRGB() : new Color(14, 14, 14).getRGB()
      );
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      String t = this.text;
      if (this.focus && System.currentTimeMillis() % 500L > 250L) {
         t = t + "_";
      }

      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(t, (float)((this.parent.getX() + 2) * 2), (float)((this.parent.getY() + this.offset + 2) * 2 + 4), 10066329);
      GL11.glPopMatrix();
   }

   @Override
   public int getHeight() {
      return 12;
   }

   @Override
   public void updateComponent(int mouseX, int mouseY) throws IOException {
      this.isHovered = this.isMouseOnButton(mouseX, mouseY);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
      if (!this.isMouseOnButton(mouseX, mouseY)) {
         this.focus = false;
      } else if (button == 0) {
         this.focus = true;
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
   }

   @Override
   public void keyTyped(char typedChar, int key) throws IOException {
      if (this.focus) {
         if (typedChar == '\b') {
            if (!this.text.isEmpty()) {
               this.text = this.text.substring(0, this.text.length() - 1);
            }
         } else if (typedChar >= ' ' && typedChar <= '~') {
            this.text = this.text + typedChar;
         }
      }
   }

   public boolean isMouseOnButton(int x, int y) {
      return x > this.parent.getX()
         && x < this.parent.getX() + this.parent.getWidth()
         && y > this.parent.getY() + this.offset
         && y < this.parent.getY() + 12 + this.offset;
   }
}
