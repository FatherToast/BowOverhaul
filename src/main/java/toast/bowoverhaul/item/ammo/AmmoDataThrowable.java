package toast.bowoverhaul.item.ammo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Used for basic vanilla-like arrows.
 */
public class AmmoDataThrowable extends AmmoData {

	private final float speedMult;
	private final boolean flameOn;
	private final boolean infinitySaves;

	public AmmoDataThrowable() {
		this(1.0F, false);
	}
	public AmmoDataThrowable(float speed) {
		this(speed, false);
	}
	public AmmoDataThrowable(boolean infinite) {
		this(1.0F, infinite);
	}
	public AmmoDataThrowable(float speed, boolean infinite) {
		this.speedMult = speed + 0.5F;
		this.flameOn = false;
		this.infinitySaves = infinite;
	}
	public AmmoDataThrowable(float speed, boolean flame, boolean infinite) {
		this.speedMult = speed + 0.5F;
		this.flameOn = flame;
		this.infinitySaves = infinite;
	}

	/**
	 * Override this to change the arrow entity spawned by this ammo data.
	 */
	public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
		return new EntitySnowball(world, shooter);
	}

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	@Override
	public Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
		EntityThrowable throwable = this.newThrowable(ammo, world, shooter, bow, draw, shotPower, unlimitedAmmo);

		throwable.motionX *= this.speedMult * shotPower;
		throwable.motionY *= this.speedMult * shotPower;
		throwable.motionZ *= this.speedMult * shotPower;

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
        	//?
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
        	//?
        }
        if (this.flameOn && EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            throwable.setFire(100);
        }

        if (unlimitedAmmo > 1 || unlimitedAmmo > 0 && this.infinitySaves) {
        	//?
        }
        else if (!world.isRemote) {
            ammo.stackSize--;
        }
        return throwable;
	}
}
