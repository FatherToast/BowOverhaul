package toast.bowoverhaul;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import toast.bowoverhaul.entry.PropertyExternal;
import toast.bowoverhaul.headhitbox.HeadHitbox;
import toast.bowoverhaul.stats.ArrowStats;
import toast.bowoverhaul.stats.BowStats;
import toast.bowoverhaul.util.FileHelper;
import toast.bowoverhaul.util.Properties;

public class CommandReload extends CommandBase {
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
    }

    // The command name.
    @Override
    public String getCommandName() {
        return "boreload";
    }

    // Returns the help string.
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/boreload - reloads all bow overhaul settings.";
    }

    // Executes the command.
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        sender.addChatMessage(new ChatComponentText("Reloading bow overhaul settings!"));

        BowOverhaul.log("Reloading bow overhaul settings...");
        ArrowStats.unload();
        BowStats.unload();
        HeadHitbox.unload();
        PropertyExternal.unload();
        BowOverhaul.log("Loaded " + FileHelper.load() + " bow overhaul settings!");
        if (Properties.getBoolean(Properties.GENERAL, "auto_generate_files")) {
            BowOverhaul.log("Generating default bow overhaul settings...");
            BowOverhaul.log("Generated " + FileHelper.generateDefaults() + " bow overhaul settings!");
        }
    }
}