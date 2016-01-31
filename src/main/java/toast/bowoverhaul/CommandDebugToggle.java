package toast.bowoverhaul;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class CommandDebugToggle extends CommandBase {
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
    }

    // The command name.
    @Override
    public String getCommandName() {
        return "bodebug";
    }

    // Returns the help string.
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bodebug - toggles bow overhaul's debug mode (does not change config setting).";
    }

    // Executes the command.
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
    	if (BowOverhaul.debug) {
			sender.addChatMessage(new ChatComponentText("[Bow Overhaul] Turning debug mode OFF!"));
		}
    	else {
			sender.addChatMessage(new ChatComponentText("[Bow Overhaul] Turning debug mode ON!"));
    	}
    	BowOverhaul.debug = !BowOverhaul.debug;
    }
}