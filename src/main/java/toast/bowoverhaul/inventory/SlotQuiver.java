package toast.bowoverhaul.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import toast.bowoverhaul.item.ammo.AmmoData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlotQuiver extends Slot
{
	public InventoryQuiver inventoryQuiver;

	public SlotQuiver(InventoryQuiver inventory, int id, int x, int y) {
		super(inventory, id, x, y);
		this.inventoryQuiver = inventory;
	}

    @Override
	public boolean isItemValid(ItemStack itemStack) {
        return AmmoData.isAmmo(itemStack);
    }

    @Override
	public void putStack(ItemStack itemStack) {
    	if (itemStack != null) {
	    	this.inventoryQuiver.quiverFilters[this.getSlotIndex()] = itemStack.copy();
	    	this.inventoryQuiver.quiverFilters[this.getSlotIndex()].stackSize = 1;
    	}
        super.putStack(itemStack);
    }

    @Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack itemStack) {
    	this.inventoryQuiver.quiverFilters[this.getSlotIndex()] = null;
        super.onPickupFromSlot(player, itemStack);
    }

    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex() {
        return this.inventoryQuiver.quiverFilters[this.getSlotIndex()] == null ? this.backgroundIcon : this.inventoryQuiver.quiverFilters[this.getSlotIndex()].getIconIndex();
    }
}
