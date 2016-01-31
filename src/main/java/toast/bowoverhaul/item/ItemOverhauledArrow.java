package toast.bowoverhaul.item;

import net.minecraft.item.Item;

public class ItemOverhauledArrow extends Item {

	public static float getDamage(Item.ToolMaterial material) {
		if (material == null)
			return 2.0F;
		return 1.5F + 0.25F * material.getDamageVsEntity();
	}

	/**
	 * Standard break chances:<br>
	 * vanilla = 100%<br>
	 * flint   = 26.67% (1 / 3.75)<br>
	 * wooden  = 100%<br>
	 * stone   = 50.89% (1 / 1.965)<br>
	 * iron    = 26.67% (1 / 3.75)<br>
	 * diamond = 4.27% (1 / 23.415)<br>
	 * golden  = 100%
	 *
	 * @return The chance for this arrow to break upon hit
	 */
	public static float getBreakChance(Item.ToolMaterial material) {
		if (material == null)
			return 1.0F;
		return 1.0F / (0.015F * material.getMaxUses());
	}

    /** The material this bow is made from. */
    public Item.ToolMaterial toolMaterial;

	public ItemOverhauledArrow(Item.ToolMaterial material) {
		super();
		this.toolMaterial = material;
	}

	public float getDamage() {
		return ItemOverhauledArrow.getDamage(this.toolMaterial);
	}

	public float getBreakChance() {
		return ItemOverhauledArrow.getBreakChance(this.toolMaterial);
	}

}
