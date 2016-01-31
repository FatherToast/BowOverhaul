package toast.bowoverhaul.stats;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created to save and load stats of an individual arrow to and from its NBT data.
 */
public class ArrowStatsInstance {

	public static final String TAG_BASE = "BowOverhaulStats";

	public static final String TAG_SHOOTER = "shooter_";

	public static final String TAG_ORIGIN = "origin_";

	public static final String TAG_RAMP_UP_DIST_SQ = "damage_distance_sq";

	public static final String TAG_HEAD_DAMAGE = "head_damage";
	public static final String TAG_BODY_DAMAGE = "body_damage";

	public static final String TAG_HEAD_MULT = "head_multiplier";
	public static final String TAG_BODY_MULT = "body_multiplier";

	/**
	 * @return Creates a new arrow stats instance for an entity and loads any saved stats from the arrow.
	 */
	public static ArrowStatsInstance get(Entity arrow) {
		ArrowStatsInstance instance =  new ArrowStatsInstance(arrow);
		instance.load();
		return instance;
	}

	/**
	 * Saves a shooter's UUID to an arrow.
	 */
	public static void saveShooter(Entity arrow, Entity shooter) {
		NBTTagCompound tag = arrow.getEntityData().getCompoundTag(ArrowStatsInstance.TAG_BASE);
		if (!arrow.getEntityData().hasKey(ArrowStatsInstance.TAG_BASE)) {
			arrow.getEntityData().setTag(ArrowStatsInstance.TAG_BASE, tag);
		}

		UUID uuid = shooter.getUniqueID();
		tag.setLong(ArrowStatsInstance.TAG_SHOOTER + "msb", uuid.getMostSignificantBits());
		tag.setLong(ArrowStatsInstance.TAG_SHOOTER + "lsb", uuid.getLeastSignificantBits());
	}
	/**
	 * @return Attempts to load the arrow's shooter, returns null if the shooter can not be found
	 */
	public static Entity loadShooter(Entity arrow) {
		if (arrow.getEntityData().hasKey(ArrowStatsInstance.TAG_BASE)) {
			NBTTagCompound tag = arrow.getEntityData().getCompoundTag(ArrowStatsInstance.TAG_BASE);

			if (tag.hasKey(ArrowStatsInstance.TAG_SHOOTER + "msb")) {
				UUID uuid = new UUID(tag.getLong(ArrowStatsInstance.TAG_SHOOTER + "msb"), tag.getLong(ArrowStatsInstance.TAG_SHOOTER + "lsb"));
				Object entity;
				for (int i = 0; i < arrow.worldObj.loadedEntityList.size(); i++) {
					entity = arrow.worldObj.loadedEntityList.get(i);
					if (entity instanceof Entity && uuid.equals(((Entity) entity).getUniqueID()))
						return (Entity) entity;
				}
			}
		}
		return null;
	}

	public double originX;
	public double originY = Double.NaN;
	public double originZ;

	public Entity theArrow;

	/** The squared distance this must travel before the full damage is reached. */
	public double rampUpDistSq = 0.0;

	/** Flat damage added to headshots. */
	public double headDamage = 0.0;
	/** Flat damage added to non-headshots. */
	public double bodyDamage = 0.0;

	/** Damage multiplier applied to headshots. */
	public double headMultiplier = 1.0;
	/** Damage multiplier applied to non-headshots. */
	public double bodyMultiplier = 1.0;

	private ArrowStatsInstance(Entity arrow) {
		this.theArrow = arrow;
	}

	/** Checks if this instance needs to be initialized ands initializes it if needed.
	 * @return True if this instance needed to be initialized */
	public boolean ensureInitialized() {
		if (Double.isNaN(this.originY)) {
			this.originX = this.theArrow.posX;
			this.originY = this.theArrow.posY;
			this.originZ = this.theArrow.posZ;
			return true;
		}
		return false;
	}

	/** @return A number from 0.1 to 1.0, with 1.0 meaning the ramp up distance has been met or surpassed */
	public double getDistanceMult() {
		if (this.rampUpDistSq <= 0.0)
			return 1.0;
		return Math.max(0.1, Math.cbrt(this.getDistanceSq()));
	}

	/** @return The ratio of current distance squared to maximum distance squared, a value of 1.0 means the max distance has been met or surpassed */
	public double getDistanceSq() {
		if (this.rampUpDistSq <= 0.0)
			return 1.0;
		return Math.min(1.0, this.theArrow.getDistanceSq(this.originX - this.theArrow.motionX, this.originY - this.theArrow.motionY, this.originZ - this.theArrow.motionZ) / this.rampUpDistSq);
	}

	public void save() {
		if (this.theArrow == null)
			return;

		NBTTagCompound tag = this.theArrow.getEntityData().getCompoundTag(ArrowStatsInstance.TAG_BASE);
		if (!this.theArrow.getEntityData().hasKey(ArrowStatsInstance.TAG_BASE)) {
			this.theArrow.getEntityData().setTag(ArrowStatsInstance.TAG_BASE, tag);
		}

		this.ensureInitialized();
		tag.setDouble(ArrowStatsInstance.TAG_ORIGIN + "x", this.originX);
		tag.setDouble(ArrowStatsInstance.TAG_ORIGIN + "y", this.originY);
		tag.setDouble(ArrowStatsInstance.TAG_ORIGIN + "z", this.originZ);

		tag.setDouble(ArrowStatsInstance.TAG_RAMP_UP_DIST_SQ, this.rampUpDistSq);

		tag.setDouble(ArrowStatsInstance.TAG_HEAD_DAMAGE, this.headDamage);
		tag.setDouble(ArrowStatsInstance.TAG_BODY_DAMAGE, this.bodyDamage);

		tag.setDouble(ArrowStatsInstance.TAG_HEAD_MULT, this.headMultiplier);
		tag.setDouble(ArrowStatsInstance.TAG_BODY_MULT, this.bodyMultiplier);
	}
	public void load() {
		if (this.theArrow == null)
			return;

		if (this.theArrow.getEntityData().hasKey(ArrowStatsInstance.TAG_BASE)) {
			NBTTagCompound tag = this.theArrow.getEntityData().getCompoundTag(ArrowStatsInstance.TAG_BASE);

			if (tag.hasKey(ArrowStatsInstance.TAG_ORIGIN + "y")) {
				this.originX = tag.getDouble(ArrowStatsInstance.TAG_ORIGIN + "x");
				this.originY = tag.getDouble(ArrowStatsInstance.TAG_ORIGIN + "y");
				this.originZ = tag.getDouble(ArrowStatsInstance.TAG_ORIGIN + "z");
			}

			if (tag.hasKey(ArrowStatsInstance.TAG_RAMP_UP_DIST_SQ)) {
				this.rampUpDistSq = tag.getDouble(ArrowStatsInstance.TAG_RAMP_UP_DIST_SQ);
			}

			if (tag.hasKey(ArrowStatsInstance.TAG_HEAD_DAMAGE)) {
				this.headDamage = tag.getDouble(ArrowStatsInstance.TAG_HEAD_DAMAGE);
			}
			if (tag.hasKey(ArrowStatsInstance.TAG_BODY_DAMAGE)) {
				this.bodyDamage = tag.getDouble(ArrowStatsInstance.TAG_BODY_DAMAGE);
			}

			if (tag.hasKey(ArrowStatsInstance.TAG_HEAD_MULT)) {
				this.headMultiplier = tag.getDouble(ArrowStatsInstance.TAG_HEAD_MULT);
			}
			if (tag.hasKey(ArrowStatsInstance.TAG_BODY_MULT)) {
				this.bodyMultiplier = tag.getDouble(ArrowStatsInstance.TAG_BODY_MULT);
			}
		}
	}

	public void saveShooter(Entity shooter) {
		if (this.theArrow == null)
			return;

		ArrowStatsInstance.saveShooter(this.theArrow, shooter);
	}
	public Entity loadShooter() {
		if (this.theArrow == null)
			return null;

		return ArrowStatsInstance.loadShooter(this.theArrow);
	}

}
