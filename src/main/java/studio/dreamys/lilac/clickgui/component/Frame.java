package studio.dreamys.lilac.clickgui.component;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.clickgui.ClickGUI;
import studio.dreamys.lilac.clickgui.component.components.Button;
import studio.dreamys.lilac.module.Module;

public class Frame implements IFrame {
   private final int width;
   private final int barHeight;
   public final ArrayList<Component> components = new ArrayList<>();
   public final String category;
   public int dragX;
   public int dragY;
   private boolean open;
   private int y;
   private int x;
   private boolean isDragging;

   public Frame(String cat) {
      this.category = cat;
      this.width = 88;
      this.x = 5;
      this.y = 5;
      this.barHeight = 13;
      this.dragX = 0;
      this.open = false;
      this.isDragging = false;
      int tY = this.barHeight;

      for (Module mod : lilac.getInstance().getModuleManager().getModulesInCategory(this.category)) {
         Button modButton = new Button(mod, this, tY);
         this.components.add(modButton);
         tY += 12;
      }
   }

   @Override
   public void setDragX(int dragX) {
      this.dragX = dragX;
   }

   @Override
   public void setDragY(int dragY) {
      this.dragY = dragY;
   }

   @Override
   public ArrayList<Component> getComponents() {
      return this.components;
   }

   @Override
   public void setDrag(boolean drag) {
      this.isDragging = drag;
   }

   @Override
   public boolean isOpen() {
      return this.open;
   }

   @Override
   public void setOpen(boolean open) {
      this.open = open;
   }

   @Override
   public void renderFrame(FontRenderer fontRenderer) {
      Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.barHeight, ClickGUI.color);
      GL11.glPushMatrix();
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      fontRenderer.drawStringWithShadow(this.category, (float)((this.x + 2) * 2 + 5), ((float)this.y + 2.5F) * 2.0F + 5.0F, -1);
      fontRenderer.drawStringWithShadow(this.open ? "-" : "+", (float)((this.x + this.width - 10) * 2 + 5), ((float)this.y + 2.5F) * 2.0F + 5.0F, -1);
      GL11.glPopMatrix();
      if (this.open && !this.components.isEmpty()) {
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         int dropdownHeight = 0;

         for (Component component : this.components) {
            dropdownHeight += component.getHeight();
         }

         int dropdownBackgroundColor = new Color(0, 0, 0, 128).getRGB();
         Gui.drawRect(this.x, this.y + this.barHeight, this.x + this.width, this.y + this.barHeight + dropdownHeight, dropdownBackgroundColor);

         for (Component component : this.components) {
            component.renderComponent();
         }

         GL11.glDisable(3042);
      }
   }

   @Override
   public void refresh() {
      int off = this.barHeight;

      for (Component comp : this.components) {
         comp.setOff(off);
         off += comp.getHeight();
      }
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public void setX(int newX) {
      this.x = newX;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setY(int newY) {
      this.y = newY;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public void updatePosition(int mouseX, int mouseY) {
      if (this.isDragging) {
         this.setX(mouseX - this.dragX);
         this.setY(mouseY - this.dragY);
      }
   }

   @Override
   public boolean isWithinHeader(int x, int y) {
      return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
   }
}
