package toast.bowoverhaul.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.item.ammo.AmmoData;
import toast.bowoverhaul.util.ItemStackAndSlot;

public class ContainerQuiver extends Container
{
    public final EntityPlayer thePlayer;
    public InventoryQuiver inventoryQuiver;
    public ItemStackAndSlot theQuiver;

    public ContainerQuiver(EntityPlayer player, int slot) {
        this.thePlayer = player;
        this.theQuiver = new ItemStackAndSlot(player.inventory.getStackInSlot(slot), slot);
        this.inventoryQuiver = new InventoryQuiver(this.theQuiver.itemStack);

        int s;
        for (s = 0; s < 3; s++) {
            this.addSlotToContainer(new SlotQuiver(this.inventoryQuiver, s, 62 + s * 18, 20));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
            	s = x + y * 9 + 9;
            	if (this.theQuiver.slot == s) { // The quiver should be unavailable
            		this.addSlotToContainer(new SlotUnavailable(player.inventory, s, 8 + x * 18, y * 18 + 51));
            	}
            	else {
    				this.addSlotToContainer(new Slot(player.inventory, s, 8 + x * 18, y * 18 + 51));
    			}
            }
        }
        for (s = 0; s < 9; s++) {
        	if (this.theQuiver.slot == s) { // The quiver should be unavailable
				this.addSlotToContainer(new SlotUnavailable(player.inventory, s, 8 + s * 18, 109));
        	}
        	else {
				this.addSlotToContainer(new Slot(player.inventory, s, 8 + s * 18, 109));
			}
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
    	player.inventory.setInventorySlotContents(this.theQuiver.slot, this.theQuiver.itemStack);
        super.onContainerClosed(player);
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    /** Called when a player shift-clicks on a slot. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemStack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemStackInSlot = slot.getStack();
            itemStack = itemStackInSlot.copy();
            if (slotIndex >= 3) {
                if (AmmoData.isAmmo(itemStackInSlot)) {
                    // Merge from player inventory to quiver inventory
                    if (!this.mergeItemStack(itemStackInSlot, 0, 3, false))
                        return null;
                }
                else if (slotIndex >= 3 && slotIndex < 30) {
                    // Merge from main inventory to hotbar
                    if (!this.mergeItemStack(itemStackInSlot, 30, 39, false))
                        return null;
                }
                else if (slotIndex >= 30 && slotIndex < 39) {
                    // Merge from hotbar to main inventory
                    if (!this.mergeItemStack(itemStackInSlot, 3, 30, false))
                        return null;
                }
            }
            else {
                // Merge from quiver inventory to player inventory
                if (!this.mergeItemStack(itemStackInSlot, 3, 39, false))
                    return null;
            }

            if (itemStackInSlot.stackSize == 0) {
                slot.putStack((ItemStack) null);
            }
            else {
                slot.onSlotChanged();
            }

            if (itemStackInSlot.stackSize == itemStack.stackSize)
                return null;
            slot.onPickupFromSlot(player, itemStackInSlot);
        }
        return itemStack;
    }
}
