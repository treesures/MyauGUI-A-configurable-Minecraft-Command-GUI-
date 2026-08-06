package studio.dreamys.lilac.clickgui.component.components.sub;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.components.Button;

public class Slider extends Component {
   private final studio.dreamys.lilac.setting.Setting set;
   private final Button parent;
   private boolean hovered;
   private int offset;
   private int x;
   private int y;
   private boolean dragging;
   private double renderWidth;

   public Slider(studio.dreamys.lilac.setting.Setting value, Button button, int offset) {
      this.set = value;
      this.parent = button;
      this.x = button.parent.getX() + button.parent.getWidth();
      this.y = button.parent.getY() + button.offset;
      this.offset = offset;
   }

   private static double roundToPlace(double value) {
      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(2, RoundingMode.HALF_UP);
      return bd.doubleValue();
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
         this.parent.parent.getX() + 2,
         this.parent.parent.getY() + this.offset,
         this.parent.parent.getX() + (int)this.renderWidth,
         this.parent.parent.getY() + this.offset + 12,
         this.hovered ? -11184811 : -12303292
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
            this.set.getName() + ": " + this.set.getValDouble(),
            (float)(this.parent.parent.getX() * 2 + 15),
            (float)((this.parent.parent.getY() + this.offset + 2) * 2 + 5),
            -1
         );
      GL11.glPopMatrix();
   }

   @Override
   public void setOff(int newOff) {
      this.offset = newOff;
   }

   @Override
   public void updateComponent(int mouseX, int mouseY) {
      this.hovered = this.isMouseOnButtonD(mouseX, mouseY) || this.isMouseOnButtonI(mouseX, mouseY);
      this.y = this.parent.parent.getY() + this.offset;
      this.x = this.parent.parent.getX();
      double diff = (double)Math.min(88, Math.max(0, mouseX - this.x));
      double min = this.set.getMin();
      double max = this.set.getMax();
      this.renderWidth = 88.0 * (this.set.getValDouble() - min) / (max - min);
      if (this.dragging) {
         if (diff == 0.0) {
            this.set.setValDouble(this.set.getMin());
         } else {
            double newValue = roundToPlace(diff / 88.0 * (max - min) + min);
            this.set.setValDouble(newValue);
         }
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if ((this.isMouseOnButtonD(mouseX, mouseY) || this.isMouseOnButtonI(mouseX, mouseY)) && button == 0 && this.parent.open) {
         this.dragging = true;
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      this.dragging = false;
   }

   public boolean isMouseOnButtonD(int x, int y) {
      return x > this.x && x < this.x + this.parent.parent.getWidth() / 2 + 1 && y > this.y && y < this.y + 12;
   }

   public boolean isMouseOnButtonI(int x, int y) {
      return x > this.x + this.parent.parent.getWidth() / 2 && x < this.x + this.parent.parent.getWidth() && y > this.y && y < this.y + 12;
   }
}
