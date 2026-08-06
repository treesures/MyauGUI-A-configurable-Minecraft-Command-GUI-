package studio.dreamys.lilac.clickgui.component.components.sub;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.components.Button;

public class Checkbox extends Component {
   private final studio.dreamys.lilac.setting.Setting op;
   private final Button parent;
   private boolean hovered;
   private int offset;
   private int x;
   private int y;

   public Checkbox(studio.dreamys.lilac.setting.Setting option, Button button, int offset) {
      this.op = option;
      this.parent = button;
      this.x = button.parent.getX() + button.parent.getWidth();
      this.y = button.parent.getY() + button.offset;
      this.offset = offset;
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
            this.op.getName(), (float)((this.parent.parent.getX() + 10 + 4) * 2 + 5), (float)((this.parent.parent.getY() + this.offset + 2) * 2 + 4), -1
         );
      GL11.glPopMatrix();
      Gui.drawRect(
         this.parent.parent.getX() + 3 + 4,
         this.parent.parent.getY() + this.offset + 3,
         this.parent.parent.getX() + 9 + 4,
         this.parent.parent.getY() + this.offset + 9,
         -6710887
      );
      if (this.op.getValBoolean()) {
         Gui.drawRect(
            this.parent.parent.getX() + 4 + 4,
            this.parent.parent.getY() + this.offset + 4,
            this.parent.parent.getX() + 8 + 4,
            this.parent.parent.getY() + this.offset + 8,
            new Color(128, 51, 205).getRGB()
         );
      }
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
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
         this.op.setValBoolean(!this.op.getValBoolean());
      }
   }

   public boolean isMouseOnButton(int x, int y) {
      return x > this.x && x < this.x + 88 && y > this.y && y < this.y + 12;
   }
}
