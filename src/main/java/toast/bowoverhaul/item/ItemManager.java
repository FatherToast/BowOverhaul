package toast.bowoverhaul.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.potion.PotionHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.item.ammo.BehaviorOverhauledArrowDispense;
import toast.bowoverhaul.util.Properties;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Manages all of the items in this mod.
 */
public class ItemManager
{
	public static final ItemArmor.ArmorMaterial QUIVER_MATERIAL = EnumHelper.addArmorMaterial("QUIVER", 0, new int[] { 0, 0, 0, 0 }, 0);
	public static final ItemArmor.ArmorMaterial STUDDED_LEATHER_MATERIAL = EnumHelper.addArmorMaterial("STUDDED", 20, new int[] { 2, 6, 5, 2 }, 14);

    public static Item chainmail;
    public static Item studdedLeather;

	public static ItemCustomArmor[] studdedLeatherArmor = new ItemCustomArmor[4];

    public static ItemQuiver quiver;
    public static ItemQuiver[] quiverArmors = new ItemQuiver[3];

    public static Item flintArrow;
    public static ItemOverhauledArrow[] overhauledArrows = new ItemOverhauledArrow[5];

    public static ItemOverhauledBow[] overhauledBows = new ItemOverhauledBow[5];

    public static Item potionAmmo;
    public static Item snowAmmo;

    /** Registers all the items in this mod. */
    public static void registerItems() {
        int renderIndex;
    	String[] materialNames;

    	// Materials
		if (Properties.getBoolean(Properties.ITEMS, "chainmail")) {
			ItemManager.chainmail = ItemManager.register(new Item().setCreativeTab(CreativeTabs.tabMaterials), "chainmail");
		}
		if (Properties.getBoolean(Properties.ITEMS, "studded_leather")) {
			ItemManager.studdedLeather = ItemManager.register(new Item().setCreativeTab(CreativeTabs.tabMaterials), "studdedLeather");
		}

    	// Studded leather armor
        renderIndex = BowOverhaul.proxy.getRenderIndex("studded", 1);
    	String[] armorNames = { "Helmet", "Chestplate", "Leggings", "Boots" };

    	for (int i = 0; i < ItemManager.studdedLeatherArmor.length; i++) {
    		if (Properties.getBoolean(Properties.ITEMS, "studded_" + armorNames[i].toLowerCase())) {
				ItemManager.studdedLeatherArmor[i] = ItemManager.register(new ItemCustomArmor(ItemManager.STUDDED_LEATHER_MATERIAL, 1, i, "studded"), "studded" + armorNames[i]);
			}
		}

    	// Quiver and chestplates with quivers
        renderIndex = BowOverhaul.proxy.getRenderIndex("quiver", 1);
    	ItemArmor[] chestplates = {
    			Items.leather_chestplate, Items.chainmail_chestplate
    		};
    	materialNames = new String[] { "leather", "chain" };

		if (Properties.getBoolean(Properties.ITEMS, "quiver")) {
			ItemManager.quiver = ItemManager.register(new ItemQuiver(ItemManager.QUIVER_MATERIAL, renderIndex), "quiver");

	    	for (int i = 0; i < ItemManager.quiverArmors.length - 1; i++) {
	    		if (Properties.getBoolean(Properties.ITEMS, materialNames[i] + "_chest_with_quiver")) {
					ItemManager.quiverArmors[i] = ItemManager.register(new ItemQuiver(chestplates[i]), materialNames[i] + "Quiver");
				}
			}
	    	if (ItemManager.studdedLeatherArmor[1] != null && Properties.getBoolean(Properties.ITEMS, "studded_chest_with_quiver")) {
				ItemManager.quiverArmors[ItemManager.quiverArmors.length - 1] = ItemManager.register(new ItemQuiver(ItemManager.studdedLeatherArmor[1]), "studdedQuiver");
	    	}
		}

		// Arrows
    	Item.ToolMaterial[] toolMaterials = {
    			Item.ToolMaterial.WOOD, Item.ToolMaterial.STONE, Item.ToolMaterial.IRON, Item.ToolMaterial.EMERALD, Item.ToolMaterial.GOLD
			};
    	materialNames = new String[] { "wooden", "stone", "iron", "diamond", "golden" };

		if (Properties.getBoolean(Properties.ITEMS, "flint_arrow")) {
			ItemManager.flintArrow = ItemManager.register(new Item().setCreativeTab(CreativeTabs.tabCombat), "flintArrow");

			BlockDispenser.dispenseBehaviorRegistry.putObject(ItemManager.flintArrow, new BehaviorOverhauledArrowDispense(ItemManager.flintArrow));
		}
    	for (int i = 0; i < ItemManager.overhauledArrows.length; i++) {
    		if (Properties.getBoolean(Properties.ITEMS, materialNames[i] + "_arrow")) {
				ItemManager.overhauledArrows[i] = ItemManager.register((ItemOverhauledArrow) new ItemOverhauledArrow(toolMaterials[i]).setCreativeTab(CreativeTabs.tabCombat), materialNames[i] + "Arrow");

				BlockDispenser.dispenseBehaviorRegistry.putObject(ItemManager.overhauledArrows[i], new BehaviorOverhauledArrowDispense(ItemManager.overhauledArrows[i]));
			}
		}

    	// Bows
    	for (int i = 0; i < ItemManager.overhauledBows.length; i++) {
    		if (Properties.getBoolean(Properties.ITEMS, materialNames[i] + "_bow")) {
				ItemManager.overhauledBows[i] = ItemManager.register(new ItemOverhauledBow(toolMaterials[i]), materialNames[i] + "Bow");
			}
		}

    	// Misc. items
		if (Properties.getBoolean(Properties.ITEMS, "potion_ammo")) {
			ItemManager.potionAmmo = ItemManager.register(new ItemPotionAmmo().setCreativeTab(CreativeTabs.tabCombat).setMaxStackSize(Properties.getInt(Properties.ITEMS, "potion_ammo_stack_size")), "potionAmmo");

			BlockDispenser.dispenseBehaviorRegistry.putObject(ItemManager.potionAmmo, new IBehaviorDispenseItem() {
	            private final BehaviorDefaultDispenseItem defaultBehavior = new BehaviorDefaultDispenseItem();

	            @Override
				public ItemStack dispense(IBlockSource dispenser, final ItemStack itemStack) {
	                return ItemPotion.isSplash(itemStack.getItemDamage()) ? new BehaviorProjectileDispense() {
	                    @Override
						protected IProjectile getProjectileEntity(World world, IPosition pos) {
	                    	ItemStack shotStack = itemStack.copy();
	    					shotStack.func_150996_a(Items.potionitem); // setItem
	        				return new EntityPotion(world, pos.getX(), pos.getY(), pos.getZ(), shotStack);
	                    }

	                    /** The variance (inaccuracy) of the projectile. */
	                    @Override
						protected float func_82498_a() {
	                        return super.func_82498_a() * 0.5F;
	                    }
	                    /** The initial speed of the projectile. */
	                    @Override
						protected float func_82500_b() {
	                        return super.func_82500_b() * 1.25F;
	                    }
	                }.dispense(dispenser, itemStack) : this.defaultBehavior.dispense(dispenser, itemStack);
	            }
	        });
		}
		if (Properties.getBoolean(Properties.ITEMS, "snow_ammo")) {
			ItemManager.snowAmmo = ItemManager.register(new Item().setCreativeTab(CreativeTabs.tabCombat), "snowAmmo");

			BlockDispenser.dispenseBehaviorRegistry.putObject(ItemManager.snowAmmo, BlockDispenser.dispenseBehaviorRegistry.getObject(Items.snowball));
		}

        ItemManager.addRecipes();
    }

    private static void addRecipes() {
        String[][] armorRecipes = {
                { "###", "# #" },
                { "# #", "###", "###" },
                { "###", "# #", "# #" },
                { "# #", "# #" }
            };
        Item[][] combineItems = {
        		{ Items.leather_helmet, Items.chainmail_helmet },
        		{ Items.leather_chestplate, Items.chainmail_chestplate },
        		{ Items.leather_leggings, Items.chainmail_leggings },
        		{ Items.leather_boots, Items.chainmail_boots }
        };
        boolean replaceArrow = Properties.getBoolean(Properties.GENERAL, "replace_vanilla_arrow");
        boolean replaceBow = Properties.getBoolean(Properties.GENERAL, "replace_vanilla_bow");

        // Material recipes
        if (ItemManager.chainmail != null) {
			GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.chainmail), Items.iron_ingot);
			GameRegistry.addShapelessRecipe(new ItemStack(Items.iron_ingot), ItemManager.chainmail);

	        for (int i = 0; i < armorRecipes.length; i++) {
				GameRegistry.addRecipe(new ItemStack(combineItems[i][1]), new Object[] {
	                armorRecipes[i], '#', ItemManager.chainmail
	            });
	        }
		}
        if (ItemManager.studdedLeather != null) {
			GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.studdedLeather), Items.leather, Items.iron_ingot);
	        if (ItemManager.chainmail != null) {
				GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.studdedLeather), Items.leather, ItemManager.chainmail);
			}
		}

        // Studded leather recipes
        for (int i = 0; i < ItemManager.studdedLeatherArmor.length; i++) {
        	if (ItemManager.studdedLeatherArmor[i] != null) {
    			if (ItemManager.studdedLeather != null) {
					GameRegistry.addRecipe(new ItemStack(ItemManager.studdedLeatherArmor[i]), new Object[] {
	                    armorRecipes[i], '#', ItemManager.studdedLeather
	                });
	        	}
				GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.studdedLeatherArmor[i]), new ItemStack(combineItems[i][0], 1, 0), new ItemStack(combineItems[i][1], 1, 0));
	        }
        }

        // Quiver recipes
    	if (ItemManager.quiver != null) {
			GameRegistry.addRecipe(new ItemStack(ItemManager.quiver), new Object[] {
				"#@#", "# #", "###", '@', Items.string, '#', Items.leather
			});

			boolean canCraft = Properties.getBoolean(Properties.RECIPES, "chestplate_with_quiver");
			boolean canUncraft = Properties.getBoolean(Properties.RECIPES, "chestplate_with_quiver_uncrafting");
        	if (canCraft) {
        		RecipeSorter.register(BowOverhaul.MODID + ":quiverarmor", RecipeQuiverArmor.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");
				GameRegistry.addRecipe(new RecipeQuiverArmor());
			}
        	if (canUncraft) {
        		RecipeSorter.register(BowOverhaul.MODID + ":removequiver", RecipeRemoveQuiver.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");
				GameRegistry.addRecipe(new RecipeRemoveQuiver());
			}

	    	// Fake recipes to show up in NEI - these should never actually be matched
	    	ItemQuiver quiverArmor;
	        for (int i = 0; i < ItemManager.quiverArmors.length; i++) {
        		quiverArmor = ItemManager.quiverArmors[i];
	        	if (quiverArmor != null) {
	        		if (canCraft) {
						GameRegistry.addShapelessRecipe(new ItemStack(quiverArmor), new ItemStack(quiverArmor.chestplateWithoutQuiver, 1, 0), new ItemStack(ItemManager.quiver, 1, OreDictionary.WILDCARD_VALUE));
					}
	        		if (canUncraft) {
						GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.quiver), new ItemStack(quiverArmor, 1, OreDictionary.WILDCARD_VALUE));
					}
				}
	        }
		}

    	Item[] materials = {
    			Item.getItemFromBlock(Blocks.planks), Item.getItemFromBlock(Blocks.cobblestone), Items.iron_ingot, Items.diamond, Items.gold_ingot
    	};

    	// Arrow recipes
    	if (ItemManager.flintArrow != null) {
			OreDictionary.registerOre("arrow", ItemManager.flintArrow);
			GameRegistry.addRecipe(new ItemStack(ItemManager.flintArrow, 2, 0), new Object[] {
				"#", "|", "@", '@', Items.feather, '#', Items.flint, '|', Items.stick
			});
			if (!replaceArrow) {
				GameRegistry.addShapelessRecipe(new ItemStack(Items.arrow), ItemManager.flintArrow);
			}
		}
        for (int i = 0; i < ItemManager.overhauledArrows.length; i++) {
        	if (ItemManager.overhauledArrows[i] != null) {
    			OreDictionary.registerOre("arrow", ItemManager.overhauledArrows[i]);
				GameRegistry.addRecipe(new ItemStack(ItemManager.overhauledArrows[i], 2, 0), new Object[] {
					"#", "|", "@", '@', Items.feather, '#', new ItemStack(materials[i], 1, OreDictionary.WILDCARD_VALUE), '|', Items.stick
				});
			}
        }

        // Bow recipes
    	if (Properties.getBoolean(Properties.GENERAL, "wooden_bow_uses_vanilla_recipe")) {
    		materials[0] = Items.stick;
			if (!replaceBow && ItemManager.overhauledBows[0] != null) {
				GameRegistry.addShapelessRecipe(new ItemStack(Items.bow), new ItemStack(ItemManager.overhauledBows[0], 1, 0));
			}
    	}
        for (int i = 0; i < ItemManager.overhauledBows.length; i++) {
        	if (ItemManager.overhauledBows[i] != null) {
    			OreDictionary.registerOre("bow", ItemManager.overhauledBows[i]);
				GameRegistry.addRecipe(new ItemStack(ItemManager.overhauledBows[i], 1, 0), new Object[] {
					"@# ", "@ |", "@# ", '@', Items.string, '#', new ItemStack(materials[i], 1, OreDictionary.WILDCARD_VALUE), '|', Items.stick
				});

				GameRegistry.addRecipe(new ItemStack(Blocks.dispenser, 1, 0), new Object[] {
					"###", "#D#", "#@#", '#', new ItemStack(Blocks.cobblestone, 1, OreDictionary.WILDCARD_VALUE), 'D', new ItemStack(ItemManager.overhauledBows[i], 1, 0), '@', Items.redstone
				});
			}
        }

        // Misc. recipes
        if (ItemManager.potionAmmo != null) {
        	// Main recipes
        	boolean canUncraft = Properties.getBoolean(Properties.RECIPES, "potion_ammo_uncrafting");
        	if (RecipePotionAmmo.recipeItem != null) {
        		RecipeSorter.register(BowOverhaul.MODID + ":potionammo", RecipePotionAmmo.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");
				GameRegistry.addRecipe(new RecipePotionAmmo());
			}
        	if (canUncraft) {
        		RecipeSorter.register(BowOverhaul.MODID + ":potionammouncraft", RecipeRemovePotionAmmo.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");
				GameRegistry.addRecipe(new RecipeRemovePotionAmmo());
			}

        	// Individual recipes
            for (int effect = 0; effect <= 15; effect++) {
                for (int type = 0; type <= 3; type++) {
                    int damage = effect | 1 << 14;
                    if ((type & 1) != 0) { // Extended
                        damage |= 1 << 5;
                    }
                    if ((type & 2) != 0) { // Tier 2
                        damage |= 1 << 6;
                    }

                    List effects = PotionHelper.getPotionEffects(damage, false);
                    if (effects != null && !effects.isEmpty()) { // Check to be sure it is a valid potion
                    	if (RecipePotionAmmo.recipeItem != null) {
	                        ((ShapedRecipes) GameRegistry.addShapedRecipe(new ItemStack(ItemManager.potionAmmo, 1, damage), new Object[] {
	        					"@", "#", '#', new ItemStack(Items.potionitem, 1, damage), '@', new ItemStack(RecipePotionAmmo.recipeItem, 1, RecipePotionAmmo.recipeItemDamage < 0 ? OreDictionary.WILDCARD_VALUE : RecipePotionAmmo.recipeItemDamage)
	        				})).func_92100_c(); // Set the recipe to copy a stackTagCompound
                    	}
                    	if (canUncraft) {
	                        ((ShapedRecipes) GameRegistry.addShapedRecipe(new ItemStack(Items.potionitem, 1, damage), new Object[] {
	        					"#", '#', new ItemStack(ItemManager.potionAmmo, 1, damage)
	        				})).func_92100_c(); // Set the recipe to copy a stackTagCompound
                    	}
                    }
                }
            }
        }

        if (ItemManager.snowAmmo != null) {
			GameRegistry.addShapelessRecipe(new ItemStack(ItemManager.snowAmmo), Items.snowball);
        }

        // Remove recipes
    	Object obj;
    	ItemStack output;
    	for (Iterator iterator = CraftingManager.getInstance().getRecipeList().iterator(); iterator.hasNext();) {
    		obj = iterator.next();
    		if (obj instanceof IRecipe) {
    			output = ((IRecipe) obj).getRecipeOutput();
    			if (output != null) {
    				if (replaceBow && output.getItem() == Items.bow || replaceArrow && output.getItem() == Items.arrow) {
    					iterator.remove();
    				}
				}
    		}
    	}
    }

    private static <T extends Item> T register(T item, String itemName) {
    	item.setUnlocalizedName(itemName).setTextureName(BowOverhaul.MODID.toLowerCase() + ":" + itemName);
    	GameRegistry.registerItem(item, itemName);
    	return item;
    }

    private ItemManager() {}
}
