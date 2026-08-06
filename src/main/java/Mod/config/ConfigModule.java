package Mod.config;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import studio.dreamys.lilac.module.Module;
import studio.dreamys.lilac.setting.Setting;

/**
 * One generic module driven by a {@link ModConfig.ModuleEntry}, replacing what
 * used to be 58 near-identical hardcoded classes.
 *
 * Behaviour is unchanged from the originals:
 * toggling sends the toggle command, and each tick any setting whose value
 * changed since the last tick is pushed to the client as a chat command.
 */
public class ConfigModule extends Module {

    private final ModConfig config;
    private final ModConfig.ModuleEntry entry;

    /** Setting -> last value we sent, so we only send on change. */
    private final Map<Setting, Object> lastSent = new IdentityHashMap<Setting, Object>();

    public ConfigModule(ModConfig config, ModConfig.ModuleEntry entry) {
        super(entry.name, entry.category);
        this.config = config;
        this.entry = entry;

        for (ModConfig.SettingEntry s : entry.settings) {
            Setting setting = build(s);
            if (setting == null) continue;
            set(setting);
        }

        // seed the baseline so joining a world does not replay every default
        for (ModConfig.SettingEntry s : entry.settings) {
            Setting setting = getSetting(s.name);
            if (setting != null) lastSent.put(setting, currentValue(setting, s));
        }

        if (entry.key != 0) key(entry.key);
    }

    private Setting build(ModConfig.SettingEntry s) {
        if (s.isCheck()) {
            return new Setting(s.name, this, s.booleanValue());
        }
        if (s.isSlider()) {
            return new Setting(s.name, this, s.doubleValue(), s.min, s.max, s.onlyInt);
        }
        if (s.isCombo()) {
            return new Setting(s.name, this, s.stringValue(), new ArrayList<String>(s.options));
        }
        return null;
    }

    public ModConfig.ModuleEntry getEntry() {
        return entry;
    }

    // ----------------------------------------------------------------- toggle

    @Override
    public void onEnable() {
        super.onEnable();
        sendToggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        sendToggle();
    }

    private void sendToggle() {
        // the client uses one command for both directions
        String cmd = config.toggleCommandFor(entry);
        if (cmd != null && !cmd.trim().isEmpty()) sendChatCommand(cmd);
    }

    @Override
    protected String buildKeybindCommand(int key) {
        return config.keybindCommandFor(entry,
                Keyboard.getKeyName(key).toLowerCase(Locale.ROOT));
    }

    // ------------------------------------------------------------------- sync

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        for (ModConfig.SettingEntry s : entry.settings) {
            Setting setting = getSetting(s.name);
            if (setting == null) continue;

            Object now = currentValue(setting, s);
            Object before = lastSent.get(setting);
            if (now == null || now.equals(before)) continue;

            lastSent.put(setting, now);
            if (s.hasCommand()) {
                sendChatCommand(s.command.trim() + " " + format(now, s));
            }
        }
    }

    private Object currentValue(Setting setting, ModConfig.SettingEntry s) {
        if (s.isCheck()) return Boolean.valueOf(setting.getValBoolean());
        if (s.isCombo()) return setting.getValString();
        if (s.isSlider()) {
            return s.onlyInt
                    ? (Object) Integer.valueOf((int) setting.getValDouble())
                    : (Object) Double.valueOf(setting.getValDouble());
        }
        return null;
    }

    private String format(Object value, ModConfig.SettingEntry s) {
        if (s.isCombo()) {
            String v = String.valueOf(value);
            return s.lowercase ? v.toLowerCase(Locale.ROOT) : v;
        }
        if (s.isSlider() && !s.onlyInt) {
            int decimals = s.decimals == null ? 1 : Math.max(0, s.decimals.intValue());
            // Locale.ROOT so a comma-decimal system locale cannot produce "3,5"
            return String.format(Locale.ROOT, "%." + decimals + "f",
                    Double.valueOf(((Number) value).doubleValue()));
        }
        return String.valueOf(value);
    }
}
