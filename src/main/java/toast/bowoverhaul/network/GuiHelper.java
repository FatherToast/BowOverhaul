package toast.bowoverhaul.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.client.GuiQuiver;
import toast.bowoverhaul.inventory.ContainerQuiver;
import toast.bowoverhaul.item.ItemQuiver;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHelper implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int slot, EntityPlayer player, World world, int x, int y, int z) {
    	if (player.inventory.getStackInSlot(slot) != null && player.inventory.getStackInSlot(slot).getItem() instanceof ItemQuiver)
    		return new ContainerQuiver(player, slot);
    	return null;
    }

    @Override
    public Object getClientGuiElement(int slot, EntityPlayer player, World world, int x, int y, int z) {
    	if (player.inventory.getStackInSlot(slot) != null && player.inventory.getStackInSlot(slot).getItem() instanceof ItemQuiver)
    		return new GuiQuiver(player, slot);
    	return null;
    }

    public static void displayGUIQuiver(EntityPlayer player, int slot) {
        if (player instanceof EntityPlayerMP) {
            player.openGui(BowOverhaul.MODID, slot, player.worldObj, 0, 0, 0);
        }
    }
}
