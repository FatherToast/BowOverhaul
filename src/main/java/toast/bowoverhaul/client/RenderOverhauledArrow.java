package toast.bowoverhaul.client;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.entity.EntityOverhauledArrow;
import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.item.ItemOverhauledArrow;

/**
 *
 */
public class RenderOverhauledArrow extends RenderArrow {

    public static final ResourceLocation[] TEXTURES;

    @Override
	protected ResourceLocation getEntityTexture(EntityArrow arrow) {
    	Item arrowItem = ((EntityOverhauledArrow) arrow).getType();
        return arrowItem instanceof ItemOverhauledArrow ? RenderOverhauledArrow.TEXTURES[((ItemOverhauledArrow) arrowItem).toolMaterial.ordinal() + 2] : RenderOverhauledArrow.TEXTURES[arrowItem == ItemManager.flintArrow ? 1 : 0];
    }

    static {
    	String[] arrows = {
    			"infinity", "flint", "wooden", "stone", "iron", "diamond", "golden"
    	};
    	TEXTURES = new ResourceLocation[arrows.length];
    	for (int i = 0; i < arrows.length; i++) {
    		RenderOverhauledArrow.TEXTURES[i] = new ResourceLocation(BowOverhaul.TEXTURE_PATH + "entity/" + arrows[i] + "Arrow.png");
    	}
    }

}
