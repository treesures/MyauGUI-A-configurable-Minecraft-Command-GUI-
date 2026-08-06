package Mod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The whole mod is driven by this file instead of hardcoded Java classes.
 * When the target client renames a command, adds a module or drops a setting,
 * you edit modules.json - no recompiling.
 *
 * Layout:
 * <pre>
 * {
 *   "toggleCommand": ".t {name}",
 *   "keybindCommand": ".bind {name} {key}",
 *   "categories": ["COMBAT", "..."],
 *   "modules": [
 *     { "name": "Killaura", "category": "COMBAT", "toggleName": "killaura",
 *       "settings": [
 *         { "name": "Attack Range", "type": "slider", "command": ".killaura attack-range",
 *           "value": 3.0, "min": 3.0, "max": 8.0, "decimals": 1 }
 *       ] }
 *   ]
 * }
 * </pre>
 */
public class ModConfig {

    /** Bumped only when the format changes in a way the loader must know about. */
    public static final int CURRENT_VERSION = 2;

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /** Comment block, kept so it survives a rewrite of the file. */
    public String[] _comment;

    public int version = CURRENT_VERSION;

    /** Sent on toggle. {name} is replaced with the module's toggleName. */
    public String toggleCommand = ".t {name}";

    /** Sent when a keybind is set. {name} = toggleName, {key} = key name. */
    public String keybindCommand = ".bind {name} {key}";

    /** Key that opens the ClickGUI, as an LWJGL keycode. 54 = right shift. */
    public int guiKey = 54;

    /** Frame order in the ClickGUI. A module whose category is missing here is hidden. */
    public List<String> categories = new ArrayList<String>();

    public List<ModuleEntry> modules = new ArrayList<ModuleEntry>();

    // ------------------------------------------------------------------ model

    public static class ModuleEntry {
        public String name;
        public String category;

        /**
         * Name used inside the toggle command. Defaults to {@link #name}.
         * Needed because they often differ, e.g. module "NoSlowdown" toggles as ".t NoSlow".
         */
        public String toggleName;

        /** "bridge" (default) sends chat commands; "clickgui" opens the GUI locally. */
        public String type;

        /** Default keybind as an LWJGL keycode, 0 for none. */
        public int key;

        public List<SettingEntry> settings = new ArrayList<SettingEntry>();

        public String toggleName() {
            return toggleName == null || toggleName.isEmpty() ? name : toggleName;
        }

        public boolean isClickGui() {
            return "clickgui".equalsIgnoreCase(type);
        }
    }

    public static class SettingEntry {
        public String name;

        /** check | slider | combo */
        public String type;

        /** Command prefix; the value is appended. Empty means GUI-only, sends nothing. */
        public String command;

        /** Default value. Boolean for check, number for slider, string for combo. */
        public Object value;

        // slider
        public Double min;
        public Double max;
        public boolean onlyInt;
        /** Decimal places when sending a slider value. Ignored when onlyInt. */
        public Integer decimals;

        // combo
        public List<String> options = new ArrayList<String>();
        /** Send the value lowercased, which is what the client expects for modes. */
        public boolean lowercase;

        public boolean hasCommand() {
            return command != null && !command.trim().isEmpty();
        }

        public boolean isCheck()  { return "check".equalsIgnoreCase(type); }
        public boolean isSlider() { return "slider".equalsIgnoreCase(type); }
        public boolean isCombo()  { return "combo".equalsIgnoreCase(type); }

        public boolean booleanValue() {
            if (value instanceof Boolean) return (Boolean) value;
            return value != null && Boolean.parseBoolean(String.valueOf(value));
        }

        public double doubleValue() {
            if (value instanceof Number) return ((Number) value).doubleValue();
            try {
                return value == null ? 0.0D : Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException e) {
                return 0.0D;
            }
        }

        public String stringValue() {
            return value == null ? "" : String.valueOf(value);
        }
    }

    // ------------------------------------------------------------------- load

    /**
     * Loads the config, writing the bundled default on first run.
     * Never throws: a broken file falls back to the bundled default so the
     * GUI still opens and the user can fix the file.
     */
    public static ModConfig load(File file) {
        if (!file.exists()) {
            ModConfig fresh = bundledDefault();
            if (fresh != null) {
                save(file, fresh);
                System.out.println("[weirdGUI] wrote default config to " + file);
                return fresh;
            }
            System.err.println("[weirdGUI] no bundled default found, starting empty");
            return new ModConfig();
        }

        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), UTF8);
            ModConfig cfg = GSON.fromJson(reader, ModConfig.class);
            if (cfg == null) throw new JsonSyntaxException("file is empty");
            cfg.normalize();
            System.out.println("[weirdGUI] loaded " + cfg.modules.size()
                    + " modules from " + file.getName());
            return cfg;
        } catch (Exception e) {
            System.err.println("[weirdGUI] failed to read " + file.getName()
                    + ": " + e.getMessage());
            System.err.println("[weirdGUI] falling back to the bundled default; "
                    + "your file was left untouched");
            ModConfig fallback = bundledDefault();
            return fallback != null ? fallback : new ModConfig();
        } finally {
            closeQuietly(reader);
        }
    }

    /** The default config shipped inside the jar. */
    public static ModConfig bundledDefault() {
        InputStream in = ModConfig.class.getResourceAsStream("/assets/weirdgui/modules.json");
        if (in == null) return null;
        Reader reader = new InputStreamReader(in, UTF8);
        try {
            ModConfig cfg = GSON.fromJson(reader, ModConfig.class);
            if (cfg != null) cfg.normalize();
            return cfg;
        } catch (Exception e) {
            System.err.println("[weirdGUI] bundled default is invalid: " + e.getMessage());
            return null;
        } finally {
            closeQuietly(reader);
        }
    }

    public static void save(File file, ModConfig cfg) {
        Writer writer = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                System.err.println("[weirdGUI] could not create " + parent);
            }
            writer = new OutputStreamWriter(new FileOutputStream(file), UTF8);
            GSON.toJson(cfg, writer);
        } catch (IOException e) {
            System.err.println("[weirdGUI] could not write " + file + ": " + e.getMessage());
        } finally {
            closeQuietly(writer);
        }
    }

    /**
     * Fills in defaults and drops entries that cannot work, so the rest of the
     * mod never has to null-check. Also makes sure every module's category has a
     * frame, otherwise the module would silently never render.
     */
    public void normalize() {
        if (toggleCommand == null) toggleCommand = ".t {name}";
        if (keybindCommand == null) keybindCommand = ".bind {name} {key}";
        if (categories == null) categories = new ArrayList<String>();
        if (modules == null) modules = new ArrayList<ModuleEntry>();

        List<ModuleEntry> valid = new ArrayList<ModuleEntry>();
        for (ModuleEntry m : modules) {
            if (m == null) continue;
            if (m.name == null || m.name.trim().isEmpty()) {
                System.err.println("[weirdGUI] skipping a module with no name");
                continue;
            }
            if (m.category == null || m.category.trim().isEmpty()) {
                m.category = categories.isEmpty() ? "OTHER" : categories.get(0);
            }
            if (m.settings == null) m.settings = new ArrayList<SettingEntry>();

            List<SettingEntry> okSettings = new ArrayList<SettingEntry>();
            for (SettingEntry s : m.settings) {
                if (s == null || s.name == null || s.name.trim().isEmpty()) continue;
                if (s.type == null) s.type = inferType(s);
                if (s.options == null) s.options = new ArrayList<String>();

                if (s.isSlider()) {
                    if (s.min == null) s.min = 0.0D;
                    if (s.max == null) s.max = Math.max(1.0D, s.doubleValue());
                    if (s.min > s.max) { double t = s.min; s.min = s.max; s.max = t; }
                } else if (s.isCombo()) {
                    if (s.options.isEmpty()) {
                        System.err.println("[weirdGUI] " + m.name + " -> " + s.name
                                + ": combo with no options, skipped");
                        continue;
                    }
                    if (!s.options.contains(s.stringValue())) s.value = s.options.get(0);
                } else if (!s.isCheck()) {
                    System.err.println("[weirdGUI] " + m.name + " -> " + s.name
                            + ": unknown type '" + s.type + "', skipped");
                    continue;
                }
                okSettings.add(s);
            }
            m.settings = okSettings;
            valid.add(m);
        }
        modules = valid;

        // any category referenced by a module but missing from the list would
        // leave that module unreachable, so append it rather than hide it
        LinkedHashSet<String> cats = new LinkedHashSet<String>(categories);
        for (ModuleEntry m : modules) {
            if (!cats.contains(m.category)) {
                cats.add(m.category);
                System.out.println("[weirdGUI] category '" + m.category
                        + "' was missing from \"categories\", added at the end");
            }
        }
        categories = new ArrayList<String>(cats);
    }

    private static String inferType(SettingEntry s) {
        if (s.options != null && !s.options.isEmpty()) return "combo";
        if (s.value instanceof Boolean) return "check";
        if (s.value instanceof Number) return "slider";
        return "check";
    }

    public String toggleCommandFor(ModuleEntry m) {
        return toggleCommand.replace("{name}", m.toggleName());
    }

    public String keybindCommandFor(ModuleEntry m, String keyName) {
        return keybindCommand
                .replace("{name}", m.toggleName())
                .replace("{key}", keyName);
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException ignored) {
        }
    }
}
