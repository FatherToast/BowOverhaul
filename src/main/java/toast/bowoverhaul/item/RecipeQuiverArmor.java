package toast.bowoverhaul.item;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import toast.bowoverhaul.inventory.InventoryQuiver;

/**
 * The actual recipe for crafting a quiver onto a chestplate.
 */
public class RecipeQuiverArmor implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting craftMatrix, World world) {
		boolean hasQuiver = false;
		boolean hasChestplate = false;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (!hasQuiver && ingredient.getItem() == ItemManager.quiver) {
            	hasQuiver = true;
            }
            else if (!hasChestplate && ItemQuiver.getChestplateIndex(ingredient.getItem()) >= 0) {
            	hasChestplate = true;
			}
            else
                return false;
        }
        return hasQuiver && hasChestplate;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craftMatrix) {
		ItemStack quiver = null;
		ItemStack chestplate = null;
		int quiverArmorIndex = -1;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (quiver == null && ingredient.getItem() == ItemManager.quiver) {
            	quiver = ingredient;
            }
            else if (chestplate == null) {
            	quiverArmorIndex = ItemQuiver.getChestplateIndex(ingredient.getItem());
            	if (quiverArmorIndex < 0)
            		return null;
            	chestplate = ingredient;
			}
            else
                return null;
        }

        if (quiver != null && chestplate != null) {
        	InventoryQuiver quiverInventory = new InventoryQuiver(quiver);
        	if (quiver.hasDisplayName()) {
				quiverInventory.quiverName = quiver.getDisplayName();
			}
        	else {
        		quiverInventory.quiverName = "";
        	}

        	ItemStack result = chestplate.copy();
        	result.func_150996_a(ItemManager.quiverArmors[quiverArmorIndex]); // setItem
        	result.stackSize = 1;
        	quiverInventory.saveTo(result);

        	return result;
		}
        return null;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
