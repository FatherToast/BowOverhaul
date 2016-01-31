package toast.bowoverhaul.headhitbox;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A class holding all the default head hitboxes for vanilla entities.
 */
public class HeadHitboxDefault {

	private static final HeadHitboxDefault MASTER_DEFAULT = new HeadHitboxDefault();
	private static final ArrayList<HeadHitboxDefault> DEFAULT_HEAD_HITBOXES = new ArrayList<HeadHitboxDefault>();

	public static HeadHitboxDefault get(Class<? extends EntityLivingBase> entityClass) {
		if (entityClass == null)
			return HeadHitboxDefault.MASTER_DEFAULT;

		HeadHitboxDefault hitbox;
		HeadHitboxDefault closestHitbox = null;
		Class closestClass = EntityLivingBase.class;
		for (int i = 0; i < HeadHitboxDefault.DEFAULT_HEAD_HITBOXES.size(); i++) {
			hitbox = HeadHitboxDefault.DEFAULT_HEAD_HITBOXES.get(i);
			if (closestClass.isAssignableFrom(hitbox.entityClass) && hitbox.entityClass.isAssignableFrom(entityClass)) {
				closestClass = hitbox.entityClass;
				closestHitbox = hitbox;
			}
		}
		return closestHitbox;
	}

	public static HeadHitboxDefault add(Class entityClass) {
		HeadHitboxDefault hitbox = new HeadHitboxDefault();
		hitbox.entityClass = entityClass;
		HeadHitboxDefault.DEFAULT_HEAD_HITBOXES.add(hitbox);
		return hitbox;
	}
	public static HeadHitboxDefault add(Class entityClass, HeadHitboxDefault hitbox) {
		hitbox.entityClass = entityClass;
		HeadHitboxDefault.DEFAULT_HEAD_HITBOXES.add(hitbox);
		return hitbox;
	}

	/**
	 * API method to add custom default hitboxes.
	 *
	 * @param entityClass The entity class to add the custom default for (also applies to classes extending this one)
	 * @return The added default hitbox object
	 */
	public static HeadHitboxDefault add(Class entityClass, float scale, float childScale, float[] size, float[] offset, float headDamage, float bodyDamage, float headMultiplier, float bodyMultiplier) {
		HeadHitboxDefault hitbox = new HeadHitboxDefault(scale, childScale, size, offset, headDamage, bodyDamage, headMultiplier, bodyMultiplier);
		hitbox.entityClass = entityClass;
		HeadHitboxDefault.DEFAULT_HEAD_HITBOXES.add(hitbox);
		return hitbox;
	}

	/** The class this applies to (also covers all entities extending). */
	public Class entityClass;

	/** Scales all values, useful for mobs with weird render scales. */
	public float scale = 1.0F;
	/** Scales all values when the entity is a child. */
	public float childScale = 1.0F;

	/** Size in width, height, depth. */
	public float[] size = { Float.NaN, 0.2F, Float.NaN };
	/** Offset in strafe, height, foreward. */
	public float[] offset = { 0.0F, -0.1F, 0.0F };

	/** Flat damage added to headshots. */
	public float headDamage = 1.0F;
	/** Flat damage added to non-headshots. */
	public float bodyDamage = 0.0F;
	/** Damage multiplier applied to headshots. */
	public float headMultiplier = 2.0F;
	/** Damage multiplier applied to non-headshots. */
	public float bodyMultiplier = 1.0F;

	private HeadHitboxDefault() {
	}
	private HeadHitboxDefault(float headDamage, float headMultiplier) {
		this.headDamage = headDamage;
		this.headMultiplier = headMultiplier;
	}
	private HeadHitboxDefault(float height) {
		this.size = new float[] { Float.NaN, height, Float.NaN };
	}
	private HeadHitboxDefault(float height, float headDamage, float headMultiplier) {
		this.size = new float[] { Float.NaN, height, Float.NaN };
		this.headDamage = headDamage;
		this.headMultiplier = headMultiplier;
	}
	private HeadHitboxDefault(float sizeW, float sizeH, float sizeD, float offsetW, float offsetH, float offsetD) {
		this.size = new float[] { sizeW, sizeH, sizeD };
		this.offset = new float[] { offsetW, offsetH, offsetD };
	}
	private HeadHitboxDefault(float sizeW, float sizeH, float sizeD, float offsetW, float offsetH, float offsetD, float headDamage, float bodyDamage, float headMultiplier, float bodyMultiplier) {
		this.size = new float[] { sizeW, sizeH, sizeD };
		this.offset = new float[] { offsetW, offsetH, offsetD };
		this.headDamage = headDamage;
		this.bodyDamage = bodyDamage;
		this.headMultiplier = headMultiplier;
		this.bodyMultiplier = bodyMultiplier;
	}
	private HeadHitboxDefault(float scale, float childScale, float[] size, float[] offset, float headDamage, float bodyDamage, float headMultiplier, float bodyMultiplier) {
		this.scale = scale;
		this.childScale = childScale;
		this.size = size;
		this.offset = offset;
		this.headDamage = headDamage;
		this.bodyDamage = bodyDamage;
		this.headMultiplier = headMultiplier;
		this.bodyMultiplier = bodyMultiplier;
	}

	/** @return Returns a deep copy of this default head hitbox. */
	public HeadHitboxDefault copy() {
		return new HeadHitboxDefault(this.scale, this.childScale,
				new float[] { this.size[0], this.size[1], this.size[2] },
				new float[] { this.offset[0], this.offset[1], this.offset[2] },
				this.headDamage, this.bodyDamage, this.headMultiplier, this.bodyMultiplier);
	}

	public void generate(JsonObject node) {
        JsonArray dummyArray;
        node.addProperty("scale", this.scale);
        node.addProperty("child_scale", this.childScale);
        dummyArray = new JsonArray();
        dummyArray.add(new JsonPrimitive(this.size[0]));
        dummyArray.add(new JsonPrimitive(this.size[1]));
        dummyArray.add(new JsonPrimitive(this.size[2]));
        node.add("size", dummyArray);
        dummyArray = new JsonArray();
        dummyArray.add(new JsonPrimitive(this.offset[0]));
        dummyArray.add(new JsonPrimitive(this.offset[1]));
        dummyArray.add(new JsonPrimitive(this.offset[2]));
        node.add("offset", dummyArray);
        node.addProperty("head_damage", this.headDamage);
        node.addProperty("body_damage", this.bodyDamage);
        node.addProperty("head_multiplier", this.headMultiplier);
        node.addProperty("body_multiplier", this.bodyMultiplier);
	}

	static {
		// Populate the defaults map
		HeadHitboxDefault hitbox;

		HeadHitboxDefault.add(EntityCreeper.class, new HeadHitboxDefault(0.35F));
		HeadHitboxDefault.add(EntitySkeleton.class);
		HeadHitboxDefault.add(EntitySpider.class, new HeadHitboxDefault(0.4F, 0.5F, 0.4F, 0.0F, -0.1F, 0.75F));
		HeadHitboxDefault.add(EntityGiantZombie.class);
		HeadHitboxDefault.add(EntityZombie.class);
		HeadHitboxDefault.add(EntitySlime.class, new HeadHitboxDefault(0.35F, 0.35F, 0.35F, 0.0F, 0.3F, 1.0F, 1.0F, 0.0F, 1.0F, 0.3F));
		HeadHitboxDefault.add(EntityGhast.class, new HeadHitboxDefault(0.35F, 0.35F, 0.35F, 0.0F, 0.3F, 1.0F));
		hitbox = HeadHitboxDefault.add(EntityEnderman.class);
		hitbox.offset[1] = -0.02F;
		HeadHitboxDefault.add(EntitySilverfish.class, new HeadHitboxDefault(1.0F, Float.NaN, 1.0F, 0.0F, 0.0F, 1.0F));
		HeadHitboxDefault.add(EntityBlaze.class, new HeadHitboxDefault(0.35F));
		HeadHitboxDefault.add(EntityDragon.class, new HeadHitboxDefault(0.4F, Float.NaN, 0.4F, 0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 1.0F, 1.0F));
		hitbox = HeadHitboxDefault.add(EntityWither.class, new HeadHitboxDefault(0.4F, 1.0F, 1.5F));
		hitbox.offset[1] = -0.02F;
		HeadHitboxDefault.add(EntityBat.class, new HeadHitboxDefault(0.5F));
		HeadHitboxDefault.add(EntityWitch.class, new HeadHitboxDefault(0.25F));
		HeadHitboxDefault.add(EntityPig.class, new HeadHitboxDefault(0.5F, 0.5F, 0.5F, 0.0F, -0.1F, 1.0F));
		HeadHitboxDefault.add(EntitySheep.class, new HeadHitboxDefault(0.5F, 0.3F, 0.5F, 0.0F, -0.1F, 1.0F));
		HeadHitboxDefault.add(EntityCow.class, new HeadHitboxDefault(0.5F, 0.3F, 0.4F, 0.0F, -0.1F, 1.0F));
		HeadHitboxDefault.add(EntityChicken.class, new HeadHitboxDefault(1.0F, 0.8F, 1.0F, 0.0F, -0.4F, 1.6F));
		HeadHitboxDefault.add(EntitySquid.class, new HeadHitboxDefault(0.5F, Float.NaN, 0.3F, 0.0F, 0.0F, -1.0F));
		HeadHitboxDefault.add(EntityWolf.class, new HeadHitboxDefault(0.5F, 0.5F, 0.3F, 0.0F, -0.1F, 1.0F));
		HeadHitboxDefault.add(EntitySnowman.class, new HeadHitboxDefault(0.35F, -1.0F, 0.3F));
		HeadHitboxDefault.add(EntityOcelot.class, new HeadHitboxDefault(0.4F, 0.5F, 0.4F, 0.0F, -0.1F, 1.2F));
		HeadHitboxDefault.add(EntityIronGolem.class, new HeadHitboxDefault(0.35F, 1.0F, 1.5F));
		HeadHitboxDefault.add(EntityHorse.class, new HeadHitboxDefault(0.4F, 0.5F, 0.4F, 0.0F, -0.3F, 0.75F));
		HeadHitboxDefault.add(EntityVillager.class, HeadHitboxDefault.get(EntityWitch.class).copy());
	}

}
