package toast.bowoverhaul.item.ammo;

import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import toast.bowoverhaul.entity.EntityOverhauledArrow;
import toast.bowoverhaul.util.Properties;

/**
 *
 */
public class BehaviorOverhauledArrowDispense extends BehaviorProjectileDispense
{
	private static final float DISPENSER_CRIT_CHANCE = (float) Properties.getDouble(Properties.GENERAL, "dispenser_crit_chance");

	private final Item arrowItem;

	public BehaviorOverhauledArrowDispense(Item arrowItem) {
		this.arrowItem = arrowItem;
	}

	@Override
	protected IProjectile getProjectileEntity(World world, IPosition pos) {
        EntityOverhauledArrow arrow = new EntityOverhauledArrow(world, pos.getX(), pos.getY(), pos.getZ()).setType(this.arrowItem);
        arrow.canBePickedUp = 1;
        arrow.setDefaultLifespan();

        if (world.rand.nextFloat() < BehaviorOverhauledArrowDispense.DISPENSER_CRIT_CHANCE) {
            arrow.setIsCritical(true);
        }

        return arrow;
	}

    /** The variance (inaccuracy) of the projectile. */
    @Override
	protected float func_82498_a() {
        return 6.0F;
    }
    /** The initial speed of the projectile. */
    @Override
	protected float func_82500_b() {
        return 1.75F;
    }
}
