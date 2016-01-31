package toast.bowoverhaul.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import toast.bowoverhaul.inventory.InventoryQuiver;

/**
 * The actual recipe for crafting a quiver onto a chestplate.
 */
public class RecipeRemoveQuiver implements IRecipe
{
	public static final String TAG_REMOVED_SLOT = "QuiverRemovedFrom";

	private static int getQuiverIndex(Item quiverArmor) {
		for (int i = 0; i < ItemManager.quiverArmors.length; i++) {
			if (quiverArmor == ItemManager.quiverArmors[i])
				return i;
		}
		return -1;
	}

	public static void removeQuiverFromChestplate(IInventory craftMatrix, ItemStack quiver, EntityPlayer player) {
		if (quiver.stackTagCompound != null && quiver.stackTagCompound.hasKey(RecipeRemoveQuiver.TAG_REMOVED_SLOT)) {
			int quiverSlot = quiver.stackTagCompound.getInteger(RecipeRemoveQuiver.TAG_REMOVED_SLOT);
			quiver.stackTagCompound.removeTag(RecipeRemoveQuiver.TAG_REMOVED_SLOT);

			try {
				ItemStack quiverArmor = craftMatrix.getStackInSlot(quiverSlot);

	        	ItemStack chestplate = quiverArmor.copy();
	        	chestplate.func_150996_a(ItemManager.quiverArmors[RecipeRemoveQuiver.getQuiverIndex(quiverArmor.getItem())].chestplateWithoutQuiver); // setItem
	        	chestplate.stackSize = 1;
	        	if (chestplate.stackTagCompound != null) {
	        		chestplate.stackTagCompound.removeTag(InventoryQuiver.TAG_BASE);
	        	}

	        	if (quiverArmor.stackSize == 1) {
	        		chestplate.stackSize++;
	        		craftMatrix.setInventorySlotContents(quiverSlot, chestplate);
	        	}
	        	else if (!player.inventory.addItemStackToInventory(chestplate)) {
	        		player.entityDropItem(chestplate, 0.0F);
	        	}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public boolean matches(InventoryCrafting craftMatrix, World world) {
		boolean hasQuiverArmor = false;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (!hasQuiverArmor && RecipeRemoveQuiver.getQuiverIndex(ingredient.getItem()) >= 0) {
            	hasQuiverArmor = true;
			}
            else
                return false;
        }
        return hasQuiverArmor;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craftMatrix) {
		ItemStack quiverArmor = null;
		int quiverSlot = -1;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (quiverArmor == null && RecipeRemoveQuiver.getQuiverIndex(ingredient.getItem()) >= 0) {
            	quiverArmor = ingredient;
            	quiverSlot = i;
			}
            else
                return null;
        }

        if (quiverArmor != null) {
        	InventoryQuiver quiverInventory = new InventoryQuiver(quiverArmor);

        	ItemStack result = new ItemStack(ItemManager.quiver);
        	if (!"".equals(quiverInventory.quiverName)) {
				result.setStackDisplayName(quiverInventory.quiverName);
				quiverInventory.quiverName = "";
			}
        	quiverInventory.saveTo(result);

        	result.stackTagCompound.setInteger(RecipeRemoveQuiver.TAG_REMOVED_SLOT, quiverSlot);
        	return result;
		}
        return null;
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
