package studio.dreamys.lilac.clickgui.component.components;

import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.IFrame;

public class Text extends Component {
   public final IFrame parent;
   private final String text;
   public int offset;
   private boolean isHovered;

   public Text(IFrame parent, int offset, String text) {
      this.parent = parent;
      this.offset = offset;
      int opY = offset + 12;
      this.text = text;
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
      int opY = this.offset + 12;
   }

   @Override
   public void renderComponent() {
      Gui.drawRect(
         this.parent.getX(),
         this.parent.getY() + this.offset,
         this.parent.getX() + this.parent.getWidth(),
         this.parent.getY() + 12 + this.offset,
         new Color(14, 14, 14).getRGB()
      );
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(this.text, (float)((this.parent.getX() + 2) * 2), (float)((this.parent.getY() + this.offset + 2) * 2 + 4), 10066329);
      GL11.glPopMatrix();
   }

   @Override
   public int getHeight() {
      return 12;
   }

   @Override
   public void updateComponent(int mouseX, int mouseY) throws IOException {
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
      if (this.isMouseOnButton(mouseX, mouseY) && button == 0) {
      }

      if (this.isMouseOnButton(mouseX, mouseY) && button == 1) {
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
   }

   @Override
   public void keyTyped(char typedChar, int key) throws IOException {
   }

   public boolean isMouseOnButton(int x, int y) {
      return x > this.parent.getX()
         && x < this.parent.getX() + this.parent.getWidth()
         && y > this.parent.getY() + this.offset
         && y < this.parent.getY() + 12 + this.offset;
   }
}
