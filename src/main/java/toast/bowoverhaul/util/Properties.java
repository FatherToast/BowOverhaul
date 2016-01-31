package toast.bowoverhaul.util;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.config.Configuration;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.item.ItemManager;

/**
 * This helper class automatically creates, stores, and retrieves properties.
 * Supported data types:
 * String, boolean, int, double
 *
 * Any property can be retrieved as an Object or String.
 * Any non-String property can also be retrieved as any other non-String property.
 * Retrieving a number as a boolean will produce a randomized output depending on the value.
 */
public abstract class Properties {
    /// Mapping of all properties in the mod to their values.
    private static final HashMap<String, Object> map = new HashMap();
    /// Common category names.
    public static final String GENERAL = "_general";
    public static final String ITEMS = "items";
    public static final String QUIVER_HUD = "quiver_hud";
    public static final String RECIPES = "recipes";
    public static final String SLOWDOWN_ARMOR = "slowdown_armor";

    /// Initializes these properties.
    public static void init(Configuration config) {
        config.load();
        BowOverhaul.debug = Boolean.valueOf(config.get(Properties.GENERAL, "debug", false, "If this is true, the mod will run in debug mode. Default is false.").getBoolean(false));

        Properties.add(config, Properties.GENERAL, "auto_generate_files", true, "If this is true, an empty properties file will be generated for every registered entity id. Default is true.");
        Properties.add(config, Properties.GENERAL, "dispenser_crit_chance", 0.05, "The chance for an arrow from this mod to deal critical damage when shot from a dispenser. Default is 5%.");
        Properties.add(config, Properties.GENERAL, "replace_vanilla_arrow", true, "If this is true, the recipe for the vanilla arrow will be removed. Default is true.");
        Properties.add(config, Properties.GENERAL, "replace_vanilla_arrow_drops", BowOverhaul.MODID + ":ironArrow", "If this is used, all vanilla arrow drops will be replaced by this item. Default is " + BowOverhaul.MODID + ":ironArrow.");
        Properties.add(config, Properties.GENERAL, "replace_vanilla_bow", true, "If this is true, the recipe for the vanilla bow will be removed. Default is true.");
        Properties.add(config, Properties.GENERAL, "wooden_bow_uses_vanilla_recipe", false, "If this is true, the wooden bow will take the vanilla bow's recipe. Default is false.");

    	String[] materialNames;

    	Properties.add(config, Properties.ITEMS, "potion_ammo", true, "If this is true, potion ammo will be enabled (splash potions that stack, but can only be used as bow ammo). Default is true.");
    	Properties.add(config, Properties.ITEMS, "potion_ammo_stack_size", 16, "The maximum stack size for potion ammo. Default is 16.");
    	Properties.add(config, Properties.ITEMS, "snow_ammo", true, "If this is true, snow ammo will be enabled (snowballs that stack to 64, can only be used as bow ammo). Default is true.");

		Properties.add(config, Properties.ITEMS, "chainmail", true, "If this is true, chain links will be enabled (used to craft chainmail armor). Default is true.");
		Properties.add(config, Properties.ITEMS, "studded_leather", true, "If this is true, studded leather will be enabled (used to craft studded leather armor). Default is true.");

    	String[] armorNames = { "helmet", "chestplate", "leggings", "boots" };
    	for (String armor : armorNames) {
			Properties.add(config, Properties.ITEMS, "studded_" + armor, true, "If this is true, the studded leather " + armor + " will be enabled. Default is true.");
		}

    	materialNames = new String[] { "leather", "chain", "studded" };
    	Properties.add(config, Properties.ITEMS, "quiver", true, "If this is true, the standard quiver will be enabled. Default is true.");
    	for (String material : materialNames) {
			Properties.add(config, Properties.ITEMS, material + "_chest_with_quiver", true, "(Requires quiver to be enabled) If this is true, the " + material + " chestplate with a quiver will be enabled. Default is true.");
		}

    	materialNames = new String[] { "wooden", "stone", "iron", "diamond", "golden" };
		Properties.add(config, Properties.ITEMS, "flint_arrow", true, "If this is true, the flint arrow will be enabled. Default is true.");
    	for (String material : materialNames) {
			Properties.add(config, Properties.ITEMS, material + "_arrow", true, "If this is true, the " + material + " arrow will be enabled. Default is true.");
		}
    	for (String material : materialNames) {
			Properties.add(config, Properties.ITEMS, material + "_bow", true, "If this is true, the " + material + " bow will be enabled. Default is true.");
		}

    	Properties.add(config, Properties.QUIVER_HUD, "_enabled", "holding_bow", "(true/false/holding_bow) If this is holding_bow, the hud will only be displayed while holding a bow. True/false enables/disables the hud always. Default is holding_bow.");
        Properties.add(config, Properties.QUIVER_HUD, "offset_h", 0, "The horizontal offset (in pixels) of the timer from the nearest edge of the screen. If centered, a negative number will shift the timer leftward. Default is 0.");
        Properties.add(config, Properties.QUIVER_HUD, "offset_v", 0, "The vertical offset (in pixels) of the timer from the nearest edge of the screen. If centered, a negative number will shift the timer upward. Default is 0.");
        Properties.add(config, Properties.QUIVER_HUD, "position_h", "left", "(left/center/right) The horizontal orientation for the timer. Default is left.");
        Properties.add(config, Properties.QUIVER_HUD, "position_v", "center", "(top/center/bottom) The vertical orientation for the timer. Default is center.");
    	Properties.add(config, Properties.QUIVER_HUD, "vertical_bar", true, "(true/false) If true, quiver bars will be displayed vertically. Default is true.");

		Properties.add(config, Properties.RECIPES, "chestplate_with_quiver", true, "If this is true, you will be able to craft quivers onto supported chestplates. Default is true.");
		Properties.add(config, Properties.RECIPES, "chestplate_with_quiver_uncrafting", true, "If this is true, you will be able to remove quivers from chestplates that have them attached. Default is true.");
		Properties.add(config, Properties.RECIPES, "potion_ammo_damage", -1, "The damage for the potion_ammo_item required to craft potion ammo. Default is -1 (any damage value).");
		Properties.add(config, Properties.RECIPES, "potion_ammo_item", Properties.str(Items.string), "If this is used, potion ammo will be craftable with the item specified. Default is " + Properties.str(Items.string) + ".");
		Properties.add(config, Properties.RECIPES, "potion_ammo_uncrafting", true, "If this is true, potion ammo will be un-craftable back into regular potions. Default is true.");

		materialNames = new String[] {
				ItemArmor.ArmorMaterial.CLOTH.toString(), ItemArmor.ArmorMaterial.CHAIN.toString(), ItemManager.QUIVER_MATERIAL.toString(), ItemManager.STUDDED_LEATHER_MATERIAL.toString()
		};
		String lightArmorMaterials = "";
		for (int i = 0; i < materialNames.length; i++) {
			lightArmorMaterials += "," + materialNames[i];
		}
		lightArmorMaterials = lightArmorMaterials.substring(1); // Remove initial comma

		armorNames = new String[] {
				Properties.str(Blocks.pumpkin), Properties.str(Items.skull)
		};
		String lightArmors = "";
		for (int i = 0; i < armorNames.length; i++) {
			lightArmors += "," + armorNames[i];
		}
		lightArmors = lightArmors.substring(1); // Remove initial comma

		Properties.add(config, Properties.SLOWDOWN_ARMOR, "_light_armor_materials", lightArmorMaterials, "The comma-separated list of materials which do NOT slow down draw speed when worn. Default is " + lightArmorMaterials + " (leather, chainmail, quivers, and studded leather).");
    	Properties.add(config, Properties.SLOWDOWN_ARMOR, "_light_armors", lightArmors, "The comma-separated list of specific armors which do NOT slow down draw speed when worn. Default is " + lightArmors + ".");
		Properties.add(config, Properties.SLOWDOWN_ARMOR, "helm_slow", 0.08, "The amount by which wearing heavy boots will slow draw speed. Default is 8%.");
    	Properties.add(config, Properties.SLOWDOWN_ARMOR, "chest_slow", 0.14, "The amount by which wearing heavy boots will slow draw speed. Default is 14%.");
    	Properties.add(config, Properties.SLOWDOWN_ARMOR, "legs_slow", 0.11, "The amount by which wearing heavy boots will slow draw speed. Default is 11%.");
    	Properties.add(config, Properties.SLOWDOWN_ARMOR, "boots_slow", 0.06, "The amount by which wearing heavy boots will slow draw speed. Default is 6%.");

        config.addCustomCategoryComment(Properties.GENERAL, "General and/or miscellaneous options.");
        config.addCustomCategoryComment(Properties.ITEMS, "Options for disabling each added item.");
        config.addCustomCategoryComment(Properties.QUIVER_HUD, "Client-side options for the display of the ammo heads-up display.");
        config.addCustomCategoryComment(Properties.RECIPES, "Options for special (not otherwise editable) recipes added by this mod.");
        config.addCustomCategoryComment(Properties.SLOWDOWN_ARMOR, "The amount by which each type of \"heavy\" armor slows down draw speed.");
        config.save();
    }

    private static String str(Block block) {
    	return Properties.str(Item.getItemFromBlock(block));
    }
    private static String str(Item item) {
    	return Item.itemRegistry.getNameForObject(item);
    }

    /// Gets the mod's random number generator.
    public static Random random() {
        return BowOverhaul.random;
    }

    /// Passes to the mod.
    public static void debugException(String message) {
        BowOverhaul.logError(message);
    }

    /// Loads the property as the specified value.
    public static void add(Configuration config, String category, String field, String defaultValue, String comment) {
        Properties.map.put(category + "@" + field, config.get(category, field, defaultValue, comment).getString());
    }

    public static void add(Configuration config, String category, String field, int defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Integer.valueOf(config.get(category, field, defaultValue, comment).getInt(defaultValue)));
    }

    public static void add(Configuration config, String category, String field, boolean defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Boolean.valueOf(config.get(category, field, defaultValue, comment).getBoolean(defaultValue)));
    }

    public static void add(Configuration config, String category, String field, double defaultValue, String comment) {
        Properties.map.put(category + "@" + field, Double.valueOf(config.get(category, field, defaultValue, comment).getDouble(defaultValue)));
    }

    /// Gets the Object property.
    public static Object getProperty(String category, String field) {
        return Properties.map.get(category + "@" + field);
    }

    /// Gets the value of the property (instead of an Object representing it).
    public static String getString(String category, String field) {
        return Properties.getProperty(category, field).toString();
    }
    public static boolean getBoolean(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue();
        if (property instanceof Integer)
            return Properties.random().nextInt( ((Number) property).intValue()) == 0;
        if (property instanceof Double)
            return Properties.random().nextDouble() < ((Number) property).doubleValue();
        Properties.debugException("Tried to get boolean for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return false;
    }
    public static int getInt(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Number)
            return ((Number) property).intValue();
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue() ? 1 : 0;
        Properties.debugException("Tried to get int for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0;
    }
    public static double getDouble(String category, String field) {
        Object property = Properties.getProperty(category, field);
        if (property instanceof Number)
            return ((Number) property).doubleValue();
        if (property instanceof Boolean)
            return ((Boolean) property).booleanValue() ? 1.0 : 0.0;
        Properties.debugException("Tried to get double for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
        return 0.0;
    }
}