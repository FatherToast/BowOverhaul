package toast.bowoverhaul.item.ammo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toast.bowoverhaul.stats.ArrowStatsInstance;

/**
 * Used for spawn eggs.
 */
public class AmmoDataSpawnEgg extends AmmoData {

	private final float speedMult;
	private final boolean flameOn;
	private final boolean infinitySaves;

	public AmmoDataSpawnEgg() {
		this(1.0F, false);
	}
	public AmmoDataSpawnEgg(float speed) {
		this(speed, false);
	}
	public AmmoDataSpawnEgg(boolean infinite) {
		this(1.0F, infinite);
	}
	public AmmoDataSpawnEgg(float speed, boolean infinite) {
		this.speedMult = speed;
		this.flameOn = true;
		this.infinitySaves = infinite;
	}
	public AmmoDataSpawnEgg(float speed, boolean flame, boolean infinite) {
		this.speedMult = speed;
		this.flameOn = flame;
		this.infinitySaves = infinite;
	}

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	@Override
	public Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
        Entity projectile = EntityList.createEntityByID(ammo.getItemDamage(), world);
        if (projectile == null) {
        	ItemStack shotStack = ammo.copy();
        	shotStack.stackSize = 1;
        	projectile = new EntityItem(world, shooter.posX, shooter.posY, shooter.posZ, shotStack);
        	((EntityItem) projectile).delayBeforeCanPickup = 20;
        }
        if (projectile instanceof EntityLivingBase) {
	    	((EntityLivingBase) projectile).attackTime = 20;

	        if (projectile instanceof EntityLiving) {
	            if (ammo.hasDisplayName()) {
	                ((EntityLiving) projectile).setCustomNameTag(ammo.getDisplayName());
	            }
	            ((EntityLiving) projectile).onSpawnWithEgg((IEntityLivingData) null);
	            ((EntityLiving) projectile).playLivingSound();
	        }
        }
        ArrowStatsInstance.saveShooter(projectile, shooter);

        projectile.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight() / 2.0F, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

        float sinYaw = MathHelper.sin(projectile.rotationYaw / 180.0F * (float) Math.PI);
        float cosYaw = MathHelper.cos(projectile.rotationYaw / 180.0F * (float) Math.PI);
        float sinPitch = MathHelper.sin(projectile.rotationPitch / 180.0F * (float) Math.PI);
        float cosPitch = MathHelper.cos(projectile.rotationPitch / 180.0F * (float) Math.PI);

        projectile.posX -= cosYaw * 0.16F;
        projectile.posY -= 0.1;
        projectile.posZ -= sinYaw * 0.16F;
        projectile.setPosition(projectile.posX, projectile.posY, projectile.posZ);

        projectile.motionX = -sinYaw * cosPitch;
        projectile.motionY = -sinPitch;
        projectile.motionZ = cosYaw * cosPitch;
        projectile.motionX += world.rand.nextGaussian() * 0.0075;
        projectile.motionY += world.rand.nextGaussian() * 0.0075;
        projectile.motionZ += world.rand.nextGaussian() * 0.0075;
        projectile.motionX *= shotPower * this.speedMult;
        projectile.motionY *= shotPower * this.speedMult;
        projectile.motionZ *= shotPower * this.speedMult;
        projectile.onGround = false;

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
            //?
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
            //?
        }
        if (this.flameOn && EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            projectile.setFire(8);

            if (projectile instanceof EntityCreeper) {
            	((EntityCreeper) projectile).func_146079_cb(); // Ignite creeper
            }
        }

        if (unlimitedAmmo > 1 || unlimitedAmmo > 0 && this.infinitySaves) {
            //?
        }
        else if (!world.isRemote) {
            ammo.stackSize--;
        }

        return projectile;
	}
}
