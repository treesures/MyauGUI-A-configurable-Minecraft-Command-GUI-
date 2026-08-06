package studio.dreamys.lilac.clickgui.component;

import java.util.ArrayList;
import net.minecraft.client.gui.FontRenderer;

public interface IFrame {
   ArrayList<Component> getComponents();

   void setDrag(boolean var1);

   boolean isOpen();

   void setOpen(boolean var1);

   void renderFrame(FontRenderer var1);

   void refresh();

   int getX();

   void setX(int var1);

   int getY();

   void setY(int var1);

   int getWidth();

   void updatePosition(int var1, int var2);

   boolean isWithinHeader(int var1, int var2);

   void setDragX(int var1);

   void setDragY(int var1);
}
