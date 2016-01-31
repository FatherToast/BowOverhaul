package toast.bowoverhaul.item.ammo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Used for basic vanilla-like arrows.
 */
public class AmmoDataFireball extends AmmoData {

	private final float speedMult;
	private final boolean infinitySaves;

	public AmmoDataFireball() {
		this(1.0F, false);
	}
	public AmmoDataFireball(float speed) {
		this(speed, false);
	}
	public AmmoDataFireball(boolean infinite) {
		this(1.0F, infinite);
	}
	public AmmoDataFireball(float speed, boolean infinite) {
		this.speedMult = speed * 6.0F;
		this.infinitySaves = infinite;
	}

	/**
	 * Override this to change the arrow entity spawned by this ammo data.
	 * @param accelZ
	 * @param accelY
	 * @param accelX
	 */
	private EntityFireball newFireball(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo, float accelX, float accelY, float accelZ) {
		return new EntitySmallFireball(world, shooter, accelX, accelY, accelZ);
	}

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	@Override
	public Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
        float sinYaw = MathHelper.sin(shooter.rotationYaw / 180.0F * (float) Math.PI);
        float cosYaw = MathHelper.cos(shooter.rotationYaw / 180.0F * (float) Math.PI);
        float sinPitch = MathHelper.sin(shooter.rotationPitch / 180.0F * (float) Math.PI);
        float cosPitch = MathHelper.cos(shooter.rotationPitch / 180.0F * (float) Math.PI);

        float accelX = -sinYaw * cosPitch * shotPower * this.speedMult;
        float accelY = -sinPitch * shotPower * this.speedMult;
        float accelZ = cosYaw * cosPitch * shotPower * this.speedMult;

		EntityFireball fireball = this.newFireball(ammo, world, shooter, bow, draw, shotPower, unlimitedAmmo, accelX, accelY, accelZ);

        world.playAuxSFXAtEntity((EntityPlayer) null, 1009, (int) fireball.posX, (int) fireball.posY, (int) fireball.posZ, 0);

        fireball.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);

        fireball.posX -= cosYaw * 0.16F;
        fireball.posY -= 0.1;
        fireball.posZ -= sinYaw * 0.16F;
        fireball.setPosition(fireball.posX, fireball.posY, fireball.posZ);

        if (draw >= 1.0F && !shooter.isPotionActive(Potion.blindness)) {
        	//?
        }

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
        	//?
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
        	//?
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
        	//?
        }

        if (unlimitedAmmo > 1 || unlimitedAmmo > 0 && this.infinitySaves) {
            //?
        }
        else if (!world.isRemote) {
            ammo.stackSize--;
        }
        return fireball;
	}
}
