package toast.bowoverhaul.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.network.GuiHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemQuiver extends ItemArmor
{
	public static int getChestplateIndex(Item chestplate) {
		for (int i = 0; i < ItemManager.quiverArmors.length; i++) {
			if (ItemManager.quiverArmors[i] != null && chestplate == ItemManager.quiverArmors[i].chestplateWithoutQuiver)
				return i;
		}
		return -1;
	}

    public IIcon overlayIcon;
    public ItemArmor chestplateWithoutQuiver;

	public ItemQuiver(ItemArmor.ArmorMaterial material, int renderIndex) {
		super(material, renderIndex, 1);
	}
	public ItemQuiver(ItemArmor chestplate) {
		super(chestplate.getArmorMaterial(), chestplate.renderIndex, 1);
		this.chestplateWithoutQuiver = chestplate;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            GuiHelper.displayGUIQuiver(player, player.inventory.currentItem);
        }
        return itemStack;
    }

    @Override
	public void setDamage(ItemStack quiver, int damage) {
        if (damage > quiver.getMaxDamage()) {
        	damage = 0;

        	InventoryQuiver quiverInventory = new InventoryQuiver(quiver);
        	quiver.func_150996_a(ItemManager.quiver); // setItem
        	if (!"".equals(quiverInventory.quiverName)) {
				quiver.setStackDisplayName(quiverInventory.quiverName);
				quiverInventory.quiverName = "";
			}
        	quiver.stackTagCompound = new NBTTagCompound();
        	quiverInventory.save();

        }
        super.setDamage(quiver, damage);
    }

    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return pass == 1 ? this.overlayIcon : super.getIconFromDamageForRenderPass(damage, pass);
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);

        if (this.getArmorMaterial() == ItemArmor.ArmorMaterial.CLOTH) {
            this.overlayIcon = iconRegister.registerIcon(this.getIconString() + "_overlay");
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int type, String name) {
        return this.getArmorMaterial() == ItemManager.QUIVER_MATERIAL ? BowOverhaul.TEXTURE_PATH + "models/armor/quiver_layer_1.png" : this.getArmorMaterial() == ItemManager.STUDDED_LEATHER_MATERIAL ? ItemManager.studdedLeatherArmor[1].modelTexture : super.getArmorTexture(stack, entity, type, name);
    }
}
