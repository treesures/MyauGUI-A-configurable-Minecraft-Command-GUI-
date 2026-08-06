package Mod;

import Mod.config.ModConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import studio.dreamys.lilac.lilac;
import studio.dreamys.lilac.module.Module;

/**
 * /gui reload | list | path
 *
 * Client-side only. Lets the config be reloaded without restarting the game,
 * which is the point of moving the hardcoded data into a file.
 */
public class Command extends CommandBase {

   @Override
   public String getCommandName() {
      return "gui";
   }

   @Override
   public String getCommandUsage(ICommandSender sender) {
      return "/gui <reload|list|path>";
   }

   @Override
   public List<String> getCommandAliases() {
      return Arrays.asList("clickgui", "myau");
   }

   @Override
   public int getRequiredPermissionLevel() {
      return 0;
   }

   @Override
   public boolean canCommandSenderUseCommand(ICommandSender sender) {
      return true;
   }

   @Override
   public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, "reload", "list", "path");
      }
      return null;
   }

   @Override
   public void processCommand(ICommandSender sender, String[] args) throws CommandException {
      String sub = args.length == 0 ? "help" : args[0].toLowerCase();

      if ("reload".equals(sub)) {
         try {
            Main.reload();
            ModConfig cfg = Main.getConfig();
            info(sender, "Reloaded " + cfg.modules.size() + " modules in "
               + cfg.categories.size() + " categories.");
         } catch (Exception e) {
            error(sender, "Reload failed: " + e);
            error(sender, "The previous setup is still active.");
         }
         return;
      }

      if ("list".equals(sub)) {
         for (String category : lilac.getInstance().getCategories()) {
            ArrayList<Module> mods = lilac.getInstance().getModuleManager()
               .getModulesInCategory(category);
            StringBuilder sb = new StringBuilder();
            for (Module m : mods) {
               if (sb.length() > 0) sb.append(", ");
               sb.append(m.isToggled() ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY)
                 .append(m.getName()).append(EnumChatFormatting.RESET);
            }
            info(sender, EnumChatFormatting.AQUA + category + EnumChatFormatting.RESET
               + " (" + mods.size() + "): " + sb);
         }
         return;
      }

      if ("path".equals(sub)) {
         info(sender, "Config: " + Main.getConfigFile().getAbsolutePath());
         return;
      }

      info(sender, "Usage: " + getCommandUsage(sender));
      info(sender, EnumChatFormatting.GRAY + "reload" + EnumChatFormatting.RESET
         + " - re-read the config file and rebuild the GUI");
      info(sender, EnumChatFormatting.GRAY + "list" + EnumChatFormatting.RESET
         + " - show loaded modules per category");
      info(sender, EnumChatFormatting.GRAY + "path" + EnumChatFormatting.RESET
         + " - print the config file location");
   }

   private void info(ICommandSender sender, String msg) {
      sender.addChatMessage(new ChatComponentText(
         EnumChatFormatting.LIGHT_PURPLE + "[GUI] " + EnumChatFormatting.RESET + msg));
   }

   private void error(ICommandSender sender, String msg) {
      sender.addChatMessage(new ChatComponentText(
         EnumChatFormatting.RED + "[GUI] " + msg));
   }
}
