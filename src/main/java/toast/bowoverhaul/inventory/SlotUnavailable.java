package toast.bowoverhaul.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotUnavailable extends Slot
{
	public SlotUnavailable(IInventory inventory, int id, int x, int y) {
		super(inventory, id, x, y);
	}

    @Override
	public boolean isItemValid(ItemStack itemStack) {
        return false;
    }

    @Override
	public boolean canTakeStack(EntityPlayer player) {
        return false;
    }
}
