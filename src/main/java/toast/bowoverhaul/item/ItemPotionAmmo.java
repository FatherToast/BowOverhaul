package toast.bowoverhaul.item;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPotionAmmo extends ItemPotion
{
    public static final Map<List, Integer> creativePotions = new LinkedHashMap<List, Integer>();

	public IIcon itemIconOverlay;

	public ItemPotionAmmo() {}

    @Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        return itemStack;
    }

    @Override
	public String getItemStackDisplayName(ItemStack itemStack) {
    	if (!ItemPotion.isSplash(itemStack.getItemDamage()))
			return super.getItemStackDisplayName(itemStack);

    	String name = super.getItemStackDisplayName(itemStack);
    	String potionLoc = StatCollector.translateToLocal(Items.potionitem.getUnlocalizedName() + ".name").trim();
    	int index = name.indexOf(potionLoc);
    	if (index >= 0) {
    		name = name.substring(0, index) + StatCollector.translateToLocal(this.getUnlocalizedName() + ".name").trim() + name.substring(index + potionLoc.length(), name.length());
    	}
    	return name;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.itemIconOverlay = iconRegister.registerIcon(this.getIconString() + "_overlay");
    }

    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return this.itemIcon;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return pass == 0 ? this.itemIconOverlay : this.itemIcon;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack itemStack, int pass) {
        return super.hasEffect(itemStack, pass) && pass == 0;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List creativeList) {
        if (ItemPotionAmmo.creativePotions.isEmpty()) {
            for (int effect = 0; effect <= 15; effect++) {
                for (int type = 0; type <= 2; type++) {
                    int damage = effect | 16384;
                    if (type == 1) { // Extended
                        damage |= 32;
                    }
                    else if (type == 2) { // Tier 2
                        damage |= 64;
                    }

                    List effects = PotionHelper.getPotionEffects(damage, false);
                    if (effects != null && !effects.isEmpty()) { // Check to be sure it is a valid potion
                        ItemPotionAmmo.creativePotions.put(effects, Integer.valueOf(damage));
                    }
                }
            }
        }

        Iterator<Integer> iterator = ItemPotionAmmo.creativePotions.values().iterator();
        while (iterator.hasNext()) {
            creativeList.add(new ItemStack(item, 1, iterator.next().intValue()));
        }
    }
}
