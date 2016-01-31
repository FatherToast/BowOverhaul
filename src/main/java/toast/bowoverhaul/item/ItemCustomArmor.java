package toast.bowoverhaul.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.BowOverhaul;

/**
 *
 */
public class ItemCustomArmor extends ItemArmor
{
	public String modelTexture;

	public ItemCustomArmor(ItemArmor.ArmorMaterial material, int renderIndex, int type, String name) {
		super(material, renderIndex, type);
		this.modelTexture = BowOverhaul.TEXTURE_PATH + "models/armor/" + name + "_layer_" + Integer.toString(type == 2 ? 2 : 1) + ".png";
	}

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int type, String name) {
        return this.modelTexture;
    }
}
