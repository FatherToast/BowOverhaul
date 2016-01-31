package toast.bowoverhaul.stats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The general settings for a particular type of bow.
 */
public class BowStats extends AbstractArrowStats
{
	private static final HashMap<Item, BowStats> BOW_STATS = new HashMap<Item, BowStats>();

	/** @return The item's bow stats */
	public static BowStats getBowStats(ItemStack itemStack) {
		BowStats stats = BowStats.loadFromNBT(itemStack);
		if (stats == null && itemStack != null)
			return BowStats.getBowStats(itemStack.getItem());
		return stats;
	}
	/** @return The item's bow stats */
	public static BowStats getBowStats(Item item) {
		return BowStats.BOW_STATS.get(item);
	}

    /** Unloads all bow stats. */
    public static void unload() {
    	BowStats.BOW_STATS.clear();
    }

    /** Turns a string of info into data. Crashes the game if something goes wrong. */
	public static void load(String path, JsonObject node) {
		new BowStats(path, node, node);
	}

    /** Attempts to load bow stats for the bow from NBT, returns null if there are no valid stats saved. */
	public static BowStats loadFromNBT(ItemStack bow) {
		try {
			if (bow != null && bow.stackTagCompound != null && bow.stackTagCompound.hasKey("BowOverhaulStats")) {
				String path = bow.toString();
				JsonObject node = FileHelper.loadJsonString(bow.toString(), bow.stackTagCompound.getString("BowOverhaulStats"));
				node.addProperty("_name", "\fnbt");
				return new BowStats(path, node, node);
			}
		}
		catch (Exception ex) {
			BowOverhaul.logWarning("Exception caught while loading bow stats from NBT!", ex);
		}
		return null;
	}

	public static void populateDefault(JsonObject defaultProp) {
        defaultProp.addProperty("_comment", "This is the default bow stats file. When this mod generates default bow stats for any item, it will be an auto-formatted copy of this file. Remember, comments (such as this one) will not be copied.");

        defaultProp.addProperty("draw_speed", "1.0~1.0");
        defaultProp.addProperty("max_draw", Float.POSITIVE_INFINITY);

        defaultProp.addProperty("ramp_up_distance", "0.0~0.0");
        defaultProp.addProperty("speed_multiplier", "1.0~1.0");
        defaultProp.addProperty("variance", "0.0~0.0");
        defaultProp.addProperty("head_damage", "0.0~0.0");
        defaultProp.addProperty("body_damage", "0.0~0.0");
        defaultProp.addProperty("head_multiplier", "1.0~1.0");
        defaultProp.addProperty("body_multiplier", "1.0~1.0");
        defaultProp.add("nbt", new JsonArray());
	}

	public final Item item;

	/** Multiplier applied to draw speed. */
	public final double[] drawSpeed;
	/** Maximum draw time allowed. */
	public final float maxDraw;

	private BowStats(String path, JsonObject root, JsonObject node) {
		super(path, root, node);
		this.item = FileHelper.readItem(node, path, "_name", false);

		this.drawSpeed = FileHelper.readCounts(node, path, "draw_speed", 1.0, 1.0);
		this.maxDraw = FileHelper.readFloat(node, path, "max_draw", Float.POSITIVE_INFINITY);

		if (BowStats.BOW_STATS.containsKey(this.item))
			throw new BowOverhaulSettingsException("Duplicate bow stats for item id! (" + Item.itemRegistry.getNameForObject(this.item) + ")", path);

		if (this.item != null) {
			BowStats.BOW_STATS.put(this.item, this);
		}
		else if (!FileHelper.readText(node, path, "_name", "").equals("\fnbt")) {
			BowOverhaul.logWarning("Missing or invalid item id! at " + path);
		}
	}

	@Override
	public String[] getOptionalFields() {
		String[] extra = { "draw_speed", "max_draw" };
		String[] base = super.getOptionalFields();
		String[] fields = Arrays.copyOf(base, base.length + extra.length);
		System.arraycopy(extra, 0, fields, base.length, extra.length);
		return fields;
	}

	/** Do NOT call this method. Use ArrowStats.initArrow() to initialize an arrow. */
	@Override
	public void initArrow(ArrowStatsInstance statsInstance, Entity shooter, ItemStack bow, BowStats bowStats, Random rand) {
		double d = FileHelper.getValue(bowStats.rampUpDist, rand);
		statsInstance.rampUpDistSq += d < 0.0 ? -d * d : d * d;

		// speedMultiplier and variance are handled in ArrowStats.initArrow()

		statsInstance.headMultiplier *= FileHelper.getValue(bowStats.headMultiplier, rand);
		statsInstance.bodyMultiplier *= FileHelper.getValue(bowStats.bodyMultiplier, rand);

		// nbtStats is handled in ArrowStats.initArrow()
	}
}
