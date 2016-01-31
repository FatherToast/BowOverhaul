package toast.bowoverhaul.item;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/**
 * The actual recipe for crafting a quiver onto a chestplate.
 */
public class RecipeRemovePotionAmmo implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting craftMatrix, World world) {
		boolean hasPotion = false;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (!hasPotion && ingredient.getItem() == ItemManager.potionAmmo) {
            	hasPotion = true;
			}
            else
                return false;
        }
        return hasPotion;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craftMatrix) {
		ItemStack potion = null;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (potion == null && ingredient.getItem() == ItemManager.potionAmmo) {
            	potion = ingredient;
            }
            else
                return null;
        }

        if (potion != null) {
        	ItemStack result = potion.copy();
        	result.func_150996_a(Items.potionitem); // setItem
        	result.stackSize = 1;
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
