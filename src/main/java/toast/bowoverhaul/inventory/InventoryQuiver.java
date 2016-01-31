package toast.bowoverhaul.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.item.ammo.AmmoData;

public class InventoryQuiver implements IInventory
{
	public static final String TAG_BASE = "QuiverInventory";

	public static final String TAG_ITEMS = "Items";
	public static final String TAG_FILTERS = "Filters";

	public static final String TAG_ACTIVE_SLOT = "ActiveSlot";

	public static final String TAG_QUIVER_NAME = "QuiverName";

	public ItemStack theQuiver;

	private ItemStack[] quiverContents = new ItemStack[this.getSizeInventory()];
	public ItemStack[] quiverFilters = new ItemStack[this.getSizeInventory()];

	public int activeSlot;

	public String quiverName = "";

	public InventoryQuiver(ItemStack heldItem) {
		this.theQuiver = heldItem;
		this.load();
	}

	public boolean canFill(int slot, ItemStack ammo) {
		if (ammo == null)
			return true;
		return this.quiverFilters[slot] == null || this.quiverFilters[slot].getItem() == ammo.getItem() && (!this.quiverFilters[slot].getHasSubtypes() || this.quiverFilters[slot].getItemDamage() == ammo.getItemDamage()) && ItemStack.areItemStackTagsEqual(this.quiverFilters[slot], ammo);
	}

	/** Attempts to autofill an itemstack into this quiver inventory.
	 * @return True if the quiver has been changed */
	public boolean autoFill(ItemStack ammo) {
		if (ammo == null)
			return false;
		boolean quiverChanged = false;
		int combinedStackSize;
		for (int i = 0; ammo.stackSize > 0 && i < this.quiverFilters.length; i++) {
			if (this.canFill(i, ammo)) {
				if (this.quiverContents[i] == null) {
					this.setInventorySlotContents(i, ammo.copy());
					if (ammo.stackSize > ammo.getMaxStackSize()) {
						ammo.stackSize -= ammo.getMaxStackSize();
					}
					else {
						ammo.stackSize = 0;
					}
                	quiverChanged = true;
				}
				else if (this.quiverContents[i].getItem() == ammo.getItem() && (!ammo.getHasSubtypes() || ammo.getItemDamage() == this.quiverContents[i].getItemDamage()) && ItemStack.areItemStackTagsEqual(ammo, this.quiverContents[i])) {
                    combinedStackSize = this.quiverContents[i].stackSize + ammo.stackSize;

                    if (combinedStackSize <= ammo.getMaxStackSize()) {
                    	ammo.stackSize = 0;
                    	this.quiverContents[i].stackSize = combinedStackSize;
                    	quiverChanged = true;
                    }
                    else if (this.quiverContents[i].stackSize < ammo.getMaxStackSize()) {
                    	ammo.stackSize -= ammo.getMaxStackSize() - this.quiverContents[i].stackSize;
                    	this.quiverContents[i].stackSize = ammo.getMaxStackSize();
                    	quiverChanged = true;
                    }
                }
			}
		}
		if (quiverChanged) {
			this.save();
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		this.save();
		return this.theQuiver.stackTagCompound.toString();
	}

	@Override
	public int getSizeInventory() {
		return 3;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot < 0 || slot >= this.getSizeInventory() ? null : this.quiverContents[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
        if (this.quiverContents[slot] != null) {
            ItemStack itemStack;
            if (this.quiverContents[slot].stackSize <= amount) {
                itemStack = this.quiverContents[slot];
                this.quiverContents[slot] = null;
                return itemStack;
            }
            itemStack = this.quiverContents[slot].splitStack(amount);
            if (this.quiverContents[slot].stackSize == 0) {
                this.quiverContents[slot] = null;
            }
            return itemStack;
        }
        return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
        if (this.quiverContents[slot] != null) {
            ItemStack itemStack = this.quiverContents[slot];
            this.quiverContents[slot] = null;
            return itemStack;
        }
        return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
        this.quiverContents[slot] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
	}

	@Override
	public String getInventoryName() {
        return this.hasCustomInventoryName() ? this.theQuiver.getItem() == ItemManager.quiver ? this.theQuiver.getDisplayName() : this.quiverName : "container.quiver";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.theQuiver.getItem() == ItemManager.quiver ? this.theQuiver.hasDisplayName() : !"".equals(this.quiverName);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
        this.save();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return player.getHeldItem() != null &&  player.getHeldItem().getItem() == ItemManager.quiver;
	}

	@Override
	public void openInventory() {
        // Do nothing
	}
	@Override
	public void closeInventory() {
        this.save();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return AmmoData.isAmmo(itemStack);
	}

	public void save() {
		if (this.theQuiver != null) {
			this.saveTo(this.theQuiver);
		}
	}
	public void load() {
		if (this.theQuiver != null) {
			this.loadFrom(this.theQuiver);
		}
	}

	public void saveTo(ItemStack quiver) {
		if (quiver.stackTagCompound == null) {
			quiver.stackTagCompound = new NBTTagCompound();
		}
		NBTTagCompound tag = quiver.stackTagCompound.getCompoundTag(InventoryQuiver.TAG_BASE);
		if (!quiver.stackTagCompound.hasKey(InventoryQuiver.TAG_BASE)) {
			quiver.stackTagCompound.setTag(InventoryQuiver.TAG_BASE, tag);
		}

        NBTTagCompound itemTag;
        NBTTagList tagList;

        tagList = new NBTTagList();
        for (int i = 0; i < this.quiverContents.length; i++) {
            if (this.quiverContents[i] != null) {
            	itemTag = new NBTTagCompound();
            	itemTag.setByte("Slot", (byte) i);
                this.quiverContents[i].writeToNBT(itemTag);
                tagList.appendTag(itemTag);
            }
        }
        tag.setTag(InventoryQuiver.TAG_ITEMS, tagList);

        tagList = new NBTTagList();
        for (int i = 0; i < this.quiverFilters.length; i++) {
            if (this.quiverFilters[i] != null) {
            	itemTag = new NBTTagCompound();
            	itemTag.setByte("Slot", (byte) i);
                this.quiverFilters[i].writeToNBT(itemTag);
                tagList.appendTag(itemTag);
            }
        }
        tag.setTag(InventoryQuiver.TAG_FILTERS, tagList);

        tag.setInteger(InventoryQuiver.TAG_ACTIVE_SLOT, this.activeSlot);

        if (!"".equals(this.quiverName)) {
        	tag.setString(InventoryQuiver.TAG_QUIVER_NAME, this.quiverName);
        }
        else {
        	tag.removeTag(InventoryQuiver.TAG_QUIVER_NAME);
        }
	}
	public void loadFrom(ItemStack quiver) {
		if (quiver.stackTagCompound == null)
			return;

		if (quiver.stackTagCompound.hasKey(InventoryQuiver.TAG_BASE)) {
			NBTTagCompound tag = quiver.stackTagCompound.getCompoundTag(InventoryQuiver.TAG_BASE);

			if (tag.hasKey(InventoryQuiver.TAG_ITEMS)) {
				NBTTagList contents = tag.getTagList(InventoryQuiver.TAG_ITEMS, tag.getId());
		        this.quiverContents = new ItemStack[this.getSizeInventory()];

		        NBTTagCompound itemTag;
		        for (int i = 0; i < contents.tagCount(); i++) {
		        	itemTag = contents.getCompoundTagAt(i);
		            int slot = itemTag.getByte("Slot") & 0xff;

		            if (slot >= 0 && slot < this.quiverContents.length) {
		                this.quiverContents[slot] = ItemStack.loadItemStackFromNBT(itemTag);
		            }
		        }
			}

			if (tag.hasKey(InventoryQuiver.TAG_FILTERS)) {
				NBTTagList filters = tag.getTagList(InventoryQuiver.TAG_FILTERS, tag.getId());
		        this.quiverFilters = new ItemStack[this.getSizeInventory()];

		        NBTTagCompound itemTag;
		        for (int i = 0; i < filters.tagCount(); i++) {
		        	itemTag = filters.getCompoundTagAt(i);
		            int slot = itemTag.getByte("Slot") & 0xff;

		            if (slot >= 0 && slot < this.quiverFilters.length) {
		                this.quiverFilters[slot] = ItemStack.loadItemStackFromNBT(itemTag);
		            }
		        }
			}

			if (tag.hasKey(InventoryQuiver.TAG_ACTIVE_SLOT)) {
				this.activeSlot = tag.getInteger(InventoryQuiver.TAG_ACTIVE_SLOT);
			}

			if (tag.hasKey(InventoryQuiver.TAG_QUIVER_NAME)) {
				this.quiverName = tag.getString(InventoryQuiver.TAG_QUIVER_NAME);
			}
		}
	}
}
