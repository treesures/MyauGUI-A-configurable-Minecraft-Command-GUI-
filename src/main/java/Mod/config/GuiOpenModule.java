package Mod.config;

import net.minecraft.client.Minecraft;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.module.Module;

/**
 * A button that opens the ClickGUI instead of talking to the client.
 * Declared in the config with "type": "clickgui".
 */
public class GuiOpenModule extends Module {

    public GuiOpenModule(ModConfig.ModuleEntry entry) {
        super(entry.name, entry.category);
        if (entry.key != 0) key(entry.key);
    }

    @Override
    public void onEnable() {
        Minecraft.getMinecraft().displayGuiScreen(lilac.getInstance().getClickGUI());
        // it is a button, not a state
        setToggled(false);
    }

    @Override
    protected String buildKeybindCommand(int key) {
        // purely local, the client has nothing to bind
        return null;
    }
}
