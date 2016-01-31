package toast.bowoverhaul.item.ammo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toast.bowoverhaul.stats.ArrowStatsInstance;

/**
 * Used for basic vanilla-like arrows.
 */
public abstract class AmmoDataMisc extends AmmoData {

	private final float speedMult;
	private final boolean flameOn;
	private final boolean infinitySaves;

	public AmmoDataMisc() {
		this(1.0F, false);
	}
	public AmmoDataMisc(float speed) {
		this(speed, false);
	}
	public AmmoDataMisc(boolean infinite) {
		this(1.0F, infinite);
	}
	public AmmoDataMisc(float speed, boolean infinite) {
		this.speedMult = speed;
		this.flameOn = true;
		this.infinitySaves = infinite;
	}
	public AmmoDataMisc(float speed, boolean flame, boolean infinite) {
		this.speedMult = speed;
		this.flameOn = flame;
		this.infinitySaves = infinite;
	}

	/**
	 * Override this to change the entity shot by this ammo data.
	 */
	public abstract Entity newProjectile(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo);

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	@Override
	public Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
        Entity projectile = this.newProjectile(ammo, world, shooter, bow, draw, shotPower, unlimitedAmmo);
        ArrowStatsInstance.saveShooter(projectile, shooter);

        projectile.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

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

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
            //?
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
            //?
        }
        if (this.flameOn && EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            projectile.setFire(100);
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
