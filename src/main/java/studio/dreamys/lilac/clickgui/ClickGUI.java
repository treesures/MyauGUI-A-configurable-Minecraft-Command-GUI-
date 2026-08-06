package studio.dreamys.lilac.clickgui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.clickgui.component.Component;
import studio.dreamys.lilac.clickgui.component.Frame;
import studio.dreamys.lilac.clickgui.component.IFrame;
import studio.dreamys.lilac.clickgui.component.ProfilesFrame;

public class ClickGUI extends GuiScreen {
   public static ArrayList<IFrame> IFrames;
   public static final int color = new Color(128, 51, 205).getRGB();

   public ClickGUI() {
      IFrames = new ArrayList<>();
      int frameX = 5;

      for (String category : lilac.getInstance().getCategories()) {
         IFrame IFrame = new Frame(category);
         IFrame.setX(frameX);
         IFrames.add(IFrame);
         frameX += IFrame.getWidth() + 1;
      }

      ProfilesFrame profilesFrame = new ProfilesFrame("PROFILES");
      profilesFrame.setX(frameX);
      IFrames.add(profilesFrame);
   }

   public void initGui() {
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();

      for (IFrame frame : IFrames) {
         frame.updatePosition(mouseX, mouseY);

         for (Component comp : frame.getComponents()) {
            try {
               comp.updateComponent(mouseX, mouseY);
            } catch (IOException var9) {
               var9.printStackTrace();
            }
         }

         frame.renderFrame(this.fontRendererObj);
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      for (IFrame frame : IFrames) {
         if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 0) {
            frame.setDrag(true);
            frame.setDragX(mouseX - frame.getX());
            frame.setDragY(mouseY - frame.getY());
         }

         if (frame.isWithinHeader(mouseX, mouseY) && mouseButton == 1) {
            frame.setOpen(!frame.isOpen());
         }

         if (frame.isOpen() && !frame.getComponents().isEmpty()) {
            for (Component component : frame.getComponents()) {
               component.mouseClicked(mouseX, mouseY, mouseButton);
            }
         }
      }
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      for (IFrame IFrame : IFrames) {
         if (IFrame.isOpen() && keyCode != 1 && !IFrame.getComponents().isEmpty()) {
            for (Component component : IFrame.getComponents()) {
               component.keyTyped(typedChar, keyCode);
            }
         }
      }

      if (keyCode == 1) {
         this.mc.displayGuiScreen(null);
      }
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      for (IFrame IFrame : IFrames) {
         IFrame.setDrag(false);
      }

      for (IFrame IFrame : IFrames) {
         if (IFrame.isOpen() && !IFrame.getComponents().isEmpty()) {
            for (Component component : IFrame.getComponents()) {
               component.mouseReleased(mouseX, mouseY, state);
            }
         }
      }
   }

   public boolean doesGuiPauseGame() {
      return true;
   }
}
