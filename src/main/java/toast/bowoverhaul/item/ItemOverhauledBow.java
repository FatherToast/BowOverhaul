package toast.bowoverhaul.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ammo.AmmoData;
import toast.bowoverhaul.util.EventHandler;
import toast.bowoverhaul.util.ItemStackAndSlot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemOverhauledBow extends ItemBow {

    public IIcon[] iconArray;

    /** The material this bow is made from. */
    public Item.ToolMaterial toolMaterial;

    /** Array of tick values used mainly for animation.<br>
     * i - description<br>
     * 0 - after this many ticks, icon switches to pulling_1<br>
     * 1 - after this many ticks, icon switches to pulling_2<br>
     * 2 - the bow is fully charged once it has been in use this long */
    public int[] drawTimes;

	public ItemOverhauledBow(Item.ToolMaterial material) {
		this.toolMaterial = material;
        this.setMaxDamage(material.getMaxUses());

		switch (material) {
			case EMERALD:
				this.drawTimes = new int[] { 13, 17, 20 };
				break;
			case IRON:
				this.drawTimes = new int[] { 15, 20, 23 };
				break;
			case STONE:
				this.drawTimes = new int[] { 17, 23, 27 };
				break;
			case WOOD:
				this.drawTimes = new int[] { 20, 26, 30 };
				break;
			case GOLD:
				this.drawTimes = new int[] { 11, 14, 16 };
				break;
			default:
				this.drawTimes = new int[] { 13, 17, 20 };
		}
	}

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
	public void onPlayerStoppedUsing(ItemStack bow, World world, EntityPlayer player, int remainingDuration) {
        int charge = this.getMaxItemUseDuration(bow) - remainingDuration;

        ArrowLooseEvent event = new ArrowLooseEvent(player, bow, charge);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
			return;
        charge = event.charge;

        int unlimitedAmmo = player.capabilities.isCreativeMode ? 2 : EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0 ? 1 : 0;
        ItemStackAndSlot ammo = AmmoData.AMMO_IN_USE.get(player);
        if (ammo == null) {
        	ammo = AmmoData.getAmmoStack(bow, player);
	        if (ammo == null && unlimitedAmmo > 0) {
				ammo = new ItemStackAndSlot(new ItemStack(Items.arrow, 64), -1, AmmoData.INFINITY_AMMO);
			}
		}

        if (ammo != null) {
            float draw = charge / 20.0F;
            draw = (draw * draw + draw * this.toolMaterial.getEfficiencyOnProperMaterial() / 4.0F) / 3.0F;

            if (draw < 0.1F)
				return;
            if (draw > 1.0F) {
                draw = 1.0F;
            }

            Entity arrow = ammo.ammoData.shootItem(ammo.itemStack, world, player, bow, draw, draw * (1.75F + 0.125F * this.toolMaterial.getDamageVsEntity()), unlimitedAmmo);

        	if (ammo.quiverSlot >= 0) {
        		ItemStack quiver = player.inventory.getStackInSlot(ammo.quiverSlot);
        		InventoryQuiver quiverInventory = new InventoryQuiver(quiver);
        		quiverInventory.setInventorySlotContents(ammo.slot, ammo.itemStack.stackSize <= 0 ? null : ammo.itemStack);
        		quiverInventory.save();
        		player.inventory.setInventorySlotContents(ammo.quiverSlot, quiver);
        	}
        	else if (ammo.slot >= 0 && ammo.itemStack.stackSize <= 0) {
        		player.inventory.setInventorySlotContents(ammo.slot, null);
            }

            bow.damageItem(ammo.ammoData.getDurabilityDamage(ammo.itemStack, bow), player);
            world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (Item.itemRand.nextFloat() * 0.4F + 1.2F) + draw * 0.5F);

            if (!world.isRemote && arrow != null) {
                world.spawnEntityInWorld(arrow);
            }
        }
    }

    @Override
	public ItemStack onItemRightClick(ItemStack bow, World world, EntityPlayer player) {
        ArrowNockEvent event = new ArrowNockEvent(player, bow);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
			return event.result;

        int unlimitedAmmo = player.capabilities.isCreativeMode ? 2 : EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0 ? 1 : 0;
        ItemStackAndSlot ammo = AmmoData.getAmmoStack(bow, player);
        if (ammo == null && unlimitedAmmo > 0) {
			ammo = new ItemStackAndSlot(new ItemStack(Items.arrow, 64), -1, AmmoData.INFINITY_AMMO);
		}
        if (ammo != null) {
            player.setItemInUse(bow, this.getMaxItemUseDuration(bow));
            AmmoData.AMMO_IN_USE.put(player, ammo);
        }
        return bow;
    }

    @Override
	public int getItemEnchantability() {
        return this.toolMaterial.getEnchantability();
    }

    @Override
	public IIcon getIcon(ItemStack bow, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (usingItem != null) {
        	int ticksUsed = usingItem.getMaxItemUseDuration() - useRemaining;
        	ticksUsed = Math.round(ticksUsed * EventHandler.calculateDrawTimeMult(player));

	        if (ticksUsed > 0) {
	            if (ticksUsed > this.drawTimes[1])
					return this.iconArray[2];
	            if (ticksUsed > this.drawTimes[0])
					return this.iconArray[1];
	            return this.iconArray[0];
	        }
        }
        return this.itemIcon;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "_standby");

        this.iconArray = new IIcon[ItemBow.bowPullIconNameArray.length];
        for (int i = 0; i < this.iconArray.length; i++) {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "_" + ItemBow.bowPullIconNameArray[i]);
        }
    }

}
