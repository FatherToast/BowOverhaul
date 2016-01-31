package toast.bowoverhaul.util;

import net.minecraft.item.ItemStack;
import toast.bowoverhaul.item.ammo.AmmoData;

/**
 * Unsurprisingly, this class wraps an item stack and also a slot.
 * Also, it includes ammo data sometimes, to help use the item as ammo.
 */
public class ItemStackAndSlot
{
	public final ItemStack itemStack;
	public final int slot;
	public final AmmoData ammoData;
	/** The slot the quiver is in, if this item is in a quiver. -1 otherwise. */
	public int quiverSlot = -1;

	public ItemStackAndSlot(ItemStack itemStack, int slot) {
		this.itemStack = itemStack;
		this.slot = slot;
		this.ammoData = null;
	}
	public ItemStackAndSlot(ItemStack itemStack, int slot, AmmoData ammoData) {
		this.itemStack = itemStack;
		this.slot = slot;
		this.ammoData = ammoData;
	}

	public ItemStackAndSlot setQuiverSlot(int slot) {
		this.quiverSlot = slot;
		return this;
	}
}
