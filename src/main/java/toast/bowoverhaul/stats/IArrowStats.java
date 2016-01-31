package toast.bowoverhaul.stats;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.util.IVerifiable;

/**
 * Interface representing something that can modify an arrow's stats.
 */
public interface IArrowStats extends IVerifiable
{
	/** Called upon each arrow the first time it is spawned to set its base stats. */
	public void initArrow(ArrowStatsInstance statsInstance, Entity shooter, ItemStack bow, BowStats bowStats, Random rand);
}
