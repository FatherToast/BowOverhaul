package toast.bowoverhaul.stats;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The general settings for a particular type of arrow.
 */
public class ArrowStats extends AbstractArrowStats
{
	public static final ArrowStats NULL_ARROW_STATS = new ArrowStats();

	private static final HashMap<String, ArrowStats> ARROW_STATS = new HashMap<String, ArrowStats>();

	/** @return The entity's arrow stats */
	public static ArrowStats getArrowStats(Entity entity) {
		return ArrowStats.getArrowStats(EntityList.getEntityString(entity));
	}
	/** @return The entity's arrow stats */
	public static ArrowStats getArrowStats(String entityId) {
		return ArrowStats.ARROW_STATS.get(entityId);
	}

    /** Unloads all arrow stats. */
    public static void unload() {
    	ArrowStats.ARROW_STATS.clear();
    }

    /** Turns a string of info into data. Crashes the game if something goes wrong. */
	public static void load(String path, JsonObject node) {
		new ArrowStats(path, node, node);
	}

	public static void populateDefault(JsonObject defaultProp) {
        defaultProp.addProperty("_comment", "This is the default arrow stats file. When this mod generates default arrow stats for any projectile, it will be an auto-formatted copy of this file. Remember, comments (such as this one) will not be copied.");

        defaultProp.addProperty("ramp_up_distance", "10.0~10.0");
        defaultProp.addProperty("speed_multiplier", "1.0~1.0");
        defaultProp.addProperty("variance", "0.0~0.0");
        defaultProp.addProperty("head_damage", "0.0~0.0");
        defaultProp.addProperty("body_damage", "0.0~0.0");
        defaultProp.addProperty("head_multiplier", "1.0~1.0");
        defaultProp.addProperty("body_multiplier", "1.0~1.0");
        defaultProp.add("nbt", new JsonArray());
	}

	public final String entityId;

	private ArrowStats() {
		this.entityId = "";
	}
	private ArrowStats(String path, JsonObject root, JsonObject node) {
		super(path, root, node);
		this.entityId = FileHelper.readText(node, path, "_name", "");

		if (this.entityId != null) {
			if (ArrowStats.ARROW_STATS.containsKey(this.entityId))
				throw new BowOverhaulSettingsException("Duplicate arrow stats for entity id! (" + this.entityId + ")", path);

			ArrowStats.ARROW_STATS.put(this.entityId, this);
		}
	}

	/** Initializes an arrow entity. */
	@Override
	public void initArrow(ArrowStatsInstance statsInstance, Entity shooter, ItemStack bow, BowStats bowStats, Random rand) {
		if (bowStats != null) {
			bowStats.initArrow(statsInstance, shooter, bow, bowStats, rand);
		}

		double d = FileHelper.getValue(this.rampUpDist, rand);
		statsInstance.rampUpDistSq += d < 0.0 ? -d * d : d * d;

		double v = Math.sqrt(statsInstance.theArrow.motionX * statsInstance.theArrow.motionX + statsInstance.theArrow.motionY * statsInstance.theArrow.motionY + statsInstance.theArrow.motionZ * statsInstance.theArrow.motionZ);

		double vM = FileHelper.getValue(this.speedMultiplier, rand);
		double var = FileHelper.getValue(this.variance, rand);
		if (bowStats != null) {
			vM *= FileHelper.getValue(bowStats.speedMultiplier, rand);
			var += FileHelper.getValue(bowStats.variance, rand);
		}
		if (var > 0.0) {
			statsInstance.theArrow.motionX = vM * (statsInstance.theArrow.motionX + v * var * 0.0075 * rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1));
			statsInstance.theArrow.motionY = vM * (statsInstance.theArrow.motionY + v * var * 0.0075 * rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1));
			statsInstance.theArrow.motionZ = vM * (statsInstance.theArrow.motionZ + v * var * 0.0075 * rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1));
		}
		else if (vM != 1.0) {
			statsInstance.theArrow.motionX *= vM;
			statsInstance.theArrow.motionY *= vM;
			statsInstance.theArrow.motionZ *= vM;
		}

		statsInstance.headMultiplier *= FileHelper.getValue(this.headMultiplier, rand);
		statsInstance.bodyMultiplier *= FileHelper.getValue(this.bodyMultiplier, rand);

        NBTTagCompound tag = new NBTTagCompound();
        statsInstance.theArrow.writeToNBT(tag);

        NBTTagList tagList = tag.getTagList("Pos", new NBTTagDouble(0.0).getId());
        double x = tagList.func_150309_d(0);
        double y = tagList.func_150309_d(1);
        double z = tagList.func_150309_d(2);
        tagList.func_150304_a(0, new NBTTagDouble(0.0));
        tagList.func_150304_a(1, new NBTTagDouble(0.0));
        tagList.func_150304_a(2, new NBTTagDouble(0.0));

        tagList = tag.getTagList("Rotation", new NBTTagFloat(0.0F).getId());
        float yaw = tagList.func_150308_e(0);
        tagList.func_150304_a(0, new NBTTagFloat(0.0F));

		if (bowStats != null) {
			bowStats.nbtStats.generate(shooter, bow, tag, statsInstance);
		}
		this.nbtStats.generate(shooter, bow, tag, statsInstance);

        tagList = tag.getTagList("Pos", new NBTTagDouble(0.0).getId());
        tagList.func_150304_a(0, new NBTTagDouble(tagList.func_150309_d(0) + x));
        tagList.func_150304_a(1, new NBTTagDouble(tagList.func_150309_d(1) + y));
        tagList.func_150304_a(2, new NBTTagDouble(tagList.func_150309_d(2) + z));

        tagList = tag.getTagList("Rotation", new NBTTagFloat(0.0F).getId());
        tagList.func_150304_a(0, new NBTTagFloat(tagList.func_150308_e(0) + yaw));

        statsInstance.theArrow.readFromNBT(tag);
	}

}
