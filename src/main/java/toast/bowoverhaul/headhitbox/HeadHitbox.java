package toast.bowoverhaul.headhitbox;

import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;
import toast.bowoverhaul.util.IVerifiable;

import com.google.gson.JsonObject;

/**
 * This represents the head hitbox for a single entity.
 */
public class HeadHitbox implements IVerifiable {

	private static final HashMap<String, HeadHitbox> HEAD_HITBOXES = new HashMap<String, HeadHitbox>();

	private static final AxisAlignedBB returnHeadHitbox = AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

	/** @return The entity's head hitbox */
	public static HeadHitbox getHeadHitbox(EntityLivingBase entity) {
		return entity instanceof EntityPlayer ? HeadHitbox.getHeadHitbox("Player") : HeadHitbox.getHeadHitbox(EntityList.getEntityString(entity));
	}
	/** @return The entity's head hitbox */
	public static HeadHitbox getHeadHitbox(String entityId) {
		return HeadHitbox.HEAD_HITBOXES.get(entityId);
	}

    /** Unloads all head hitboxes. */
    public static void unload() {
    	HeadHitbox.HEAD_HITBOXES.clear();
    }

    /** Turns a string of info into data. Crashes the game if something goes wrong. */
	public static void load(String path, JsonObject node) {
		new HeadHitbox(path, node, node);
	}

	public final String entityId;

	/** Scales all values, useful for mobs with weird render scales. */
	public final float scale;
	/** Scales all values when the entity is a child. */
	public final float childScale;

	/** Size in width, height, depth. */
	public final float[] size;
	/** Offset in strafe, height, foreward. */
	public final float[] offset;

	/** Flat damage added to headshots. */
	public final float headDamage;
	/** Flat damage added to non-headshots. */
	public final float bodyDamage;
	/** Damage multiplier applied to headshots. */
	public final float headMultiplier;
	/** Damage multiplier applied to non-headshots. */
	public final float bodyMultiplier;

	private HeadHitbox(String path, JsonObject root, JsonObject node) {
		FileHelper.verify(node, path, this);
		HeadHitboxDefault theDefault = HeadHitboxDefault.get(null);
		this.entityId = FileHelper.readText(node, path, "_name", "");

		this.scale = FileHelper.readFloat(node, path, "scale", theDefault.scale);
		this.childScale = FileHelper.readFloat(node, path, "child_scale", theDefault.childScale);

		this.size = FileHelper.readFloatArray(node, path, "size", Arrays.copyOf(theDefault.size, 3));
		this.offset = FileHelper.readFloatArray(node, path, "offset", Arrays.copyOf(theDefault.offset, 3));

		this.headDamage = FileHelper.readFloat(node, path, "head_damage", theDefault.headDamage);
		this.bodyDamage = FileHelper.readFloat(node, path, "body_damage", theDefault.bodyDamage);
		this.headMultiplier = FileHelper.readFloat(node, path, "head_multiplier", theDefault.headMultiplier);
		this.bodyMultiplier = FileHelper.readFloat(node, path, "body_multiplier", theDefault.bodyMultiplier);

		if (this.entityId != null) {
			if (HeadHitbox.HEAD_HITBOXES.containsKey(this.entityId))
				throw new BowOverhaulSettingsException("Duplicate head hitbox for entity id! (" + this.entityId + ")", path);

			HeadHitbox.HEAD_HITBOXES.put(this.entityId, this);
		}
	}

	@Override
	public String[] getRequiredFields() {
		return new String[] { "_name" };
	}
	@Override
	public String[] getOptionalFields() {
		return new String[] { "scale", "child_scale", "size", "offset", "head_damage", "body_damage", "head_multiplier", "body_multiplier" };
	}

	/** @param entity The entity to get the bounding box of this head for
	 * @return The bounding box of the entity's head - the same box is reused and returned every time. */
	public AxisAlignedBB getBoundingBox(EntityLivingBase entity) {
		float entityScale = this.scale;
		if (entity.isChild()) {
			entityScale *= this.childScale;
		}

		if (Double.isNaN(this.size[1])) {
			HeadHitbox.returnHeadHitbox.minY = entity.boundingBox.minY - 0.02;
			HeadHitbox.returnHeadHitbox.maxY = entity.boundingBox.maxY + 0.02;
		}
		else {
	        double y = entity.posY + entity.height - this.offset[1] * entity.height * entityScale;

	        HeadHitbox.returnHeadHitbox.minY = y - this.size[1] * entity.height * this.scale;
	        HeadHitbox.returnHeadHitbox.maxY = y + (this.offset[1] == 0.0 ? 0.02 : 0);
		}

        if (Double.isNaN(this.size[0]) || Double.isNaN(this.size[2])) {
			HeadHitbox.returnHeadHitbox.minX = entity.boundingBox.minX - 0.02;
			HeadHitbox.returnHeadHitbox.maxX = entity.boundingBox.maxX + 0.02;
			HeadHitbox.returnHeadHitbox.minZ = entity.boundingBox.minZ - 0.02;
			HeadHitbox.returnHeadHitbox.maxZ = entity.boundingBox.maxZ + 0.02;
        }
        else {
        	float radius = entity.width / 2.0F;
	        float sine = MathHelper.sin(entity.rotationYaw * (float) Math.PI / 180.0F);
	        float cosine = MathHelper.cos(entity.rotationYaw * (float) Math.PI / 180.0F);

	        double x = entity.posX - (this.offset[0] * cosine + this.offset[2] * sine) * radius * entityScale;
	        double z = entity.posZ - (this.offset[0] * sine - this.offset[2] * cosine) * radius * entityScale;

	        sine = Math.abs(sine);
	        cosine = Math.abs(cosine);
			double xR = Math.abs(this.size[0] * cosine + this.size[2] * sine) * radius * this.scale;
	        double zR = Math.abs(this.size[0] * sine + this.size[2] * cosine) * radius * this.scale;

	        HeadHitbox.returnHeadHitbox.minX = x - xR;
	        HeadHitbox.returnHeadHitbox.maxX = x + xR;
	        HeadHitbox.returnHeadHitbox.minZ = z - zR;
	        HeadHitbox.returnHeadHitbox.maxZ = z + zR;
        }

		return HeadHitbox.returnHeadHitbox;
	}

}
