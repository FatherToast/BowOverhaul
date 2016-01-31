package toast.bowoverhaul.stats;

import toast.bowoverhaul.entry.NBTStats;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

/**
 * Abstract class representing something that changes all of an arrow's stats.
 */
public abstract class AbstractArrowStats implements IArrowStats
{
	/** The distance required to reach full damage. */
	public final double[] rampUpDist;

	/** Multiplier applied to initial speed. */
	public final double[] speedMultiplier;
	/** Modifier applied to accuracy (higher variance => lower accuracy). */
	public final double[] variance;

	/** Flat damage added to headshots. */
	public final double[] headDamage;
	/** Flat damage added to non-headshots. */
	public final double[] bodyDamage;
	/** Damage multiplier applied to headshots. */
	public final double[] headMultiplier;
	/** Damage multiplier applied to non-headshots. */
	public final double[] bodyMultiplier;

	/** The nbt stats to apply. */
    public final NBTStats nbtStats;

    public AbstractArrowStats() {
    	// The "null" stats, these should have no effect on an entity's stats

		this.rampUpDist = new double[] { 0.0, 0.0 };

		this.speedMultiplier = new double[] { 1.0, 1.0 };
		this.variance = new double[] { 0.0, 0.0 };

		this.headDamage = new double[] { 0.0, 0.0 };
		this.bodyDamage = new double[] { 0.0, 0.0 };
		this.headMultiplier = new double[] { 1.0, 1.0 };
		this.bodyMultiplier = new double[] { 1.0, 1.0 };

        this.nbtStats = new NBTStats();
    }
	public AbstractArrowStats(String path, JsonObject root, JsonObject node) {
		FileHelper.verify(node, path, this);

		this.rampUpDist = FileHelper.readCounts(node, path, "ramp_up_distance", 10.0, 10.0);

		this.speedMultiplier = FileHelper.readCounts(node, path, "speed_multiplier", 1.0, 1.0);
		this.variance = FileHelper.readCounts(node, path, "variance", 0.0, 0.0);

		this.headDamage = FileHelper.readCounts(node, path, "head_damage", 0.0, 0.0);
		this.bodyDamage = FileHelper.readCounts(node, path, "body_damage", 0.0, 0.0);
		this.headMultiplier = FileHelper.readCounts(node, path, "head_multiplier", 1.0, 1.0);
		this.bodyMultiplier = FileHelper.readCounts(node, path, "body_multiplier", 1.0, 1.0);

        this.nbtStats = new NBTStats("nbt", path, root, node);
	}

	@Override
	public String[] getRequiredFields() {
		return new String[] { "_name" };
	}
	@Override
	public String[] getOptionalFields() {
		return new String[] { "ramp_up_distance", "speed_multiplier", "variance", "head_damage", "body_damage", "head_multiplier", "body_multiplier", "nbt" };
	}
}
