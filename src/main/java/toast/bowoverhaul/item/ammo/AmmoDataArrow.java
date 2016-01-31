package toast.bowoverhaul.item.ammo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

/**
 * Used for basic vanilla-like arrows.
 */
public class AmmoDataArrow extends AmmoData {

	/**
	 * Override this to change the arrow entity spawned by this ammo data.
	 */
	public EntityArrow newArrow(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
		return new EntityArrow(world, shooter, shotPower);
	}

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	@Override
	public Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
        EntityArrow arrow = this.newArrow(ammo, world, shooter, bow, draw, shotPower, unlimitedAmmo);

        if (draw >= 1.0F && !shooter.isPotionActive(Potion.blindness)) {
            arrow.setIsCritical(true);
        }

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + power * 0.5 + 0.5);
        }
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            arrow.setFire(100);
        }

        if (unlimitedAmmo > 0) {
            arrow.canBePickedUp = 2;
        }
        else if (!world.isRemote) {
            ammo.stackSize--;
        }
        return arrow;
	}
}
