package toast.bowoverhaul.item;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import toast.bowoverhaul.util.FileHelper;
import toast.bowoverhaul.util.Properties;

/**
 * The actual recipe for crafting a quiver onto a chestplate.
 */
public class RecipePotionAmmo implements IRecipe
{
	public static final Item recipeItem = FileHelper.readItem(Properties.getString(Properties.RECIPES, "potion_ammo_item"), "config\\BowOverhaul.cfg\\" + Properties.RECIPES + "\\potion_ammo_item", false);
	public static final int recipeItemDamage = Properties.getInt(Properties.RECIPES, "potion_ammo_damage");

	@Override
	public boolean matches(InventoryCrafting craftMatrix, World world) {
		boolean hasPotion = false;
		boolean hasCombineItem = false;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (!hasCombineItem && ingredient.getItem() == RecipePotionAmmo.recipeItem && (RecipePotionAmmo.recipeItemDamage < 0 || RecipePotionAmmo.recipeItemDamage == ingredient.getItemDamage())) {
            	hasCombineItem = true;
            }
            else if (!hasPotion && ingredient.getItem() == Items.potionitem && ItemPotion.isSplash(ingredient.getItemDamage())) {
            	hasPotion = true;
			}
            else
                return false;
        }
        return hasPotion && hasCombineItem;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craftMatrix) {
		ItemStack potion = null;
		boolean hasCombineItem = false;

        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
            ItemStack ingredient = craftMatrix.getStackInSlot(i);
            if (ingredient == null) {
				continue;
			}
            if (!hasCombineItem && ingredient.getItem() == RecipePotionAmmo.recipeItem && (RecipePotionAmmo.recipeItemDamage < 0 || RecipePotionAmmo.recipeItemDamage == ingredient.getItemDamage())) {
            	hasCombineItem = true;
            }
            else if (potion == null && ingredient.getItem() == Items.potionitem && ItemPotion.isSplash(ingredient.getItemDamage())) {
            	potion = ingredient;
            }
            else
                return null;
        }

        if (potion != null && hasCombineItem) {
        	ItemStack result = potion.copy();
        	result.func_150996_a(ItemManager.potionAmmo); // setItem
        	result.stackSize = 1;
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
