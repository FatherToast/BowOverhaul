package toast.bowoverhaul.item.ammo;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.entity.EntityOverhauledArrow;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.item.ItemQuiver;
import toast.bowoverhaul.util.ItemStackAndSlot;

/**
 *
 */
public abstract class AmmoData {

	public static final HashMap<EntityPlayer, ItemStackAndSlot> AMMO_IN_USE = new HashMap<EntityPlayer, ItemStackAndSlot>();

	public static final AmmoData INFINITY_AMMO;

	private static final HashMap<AmmoDataHash, AmmoData> AMMO_DATA_MAP = new HashMap<AmmoDataHash, AmmoData>();

	public static ItemStackAndSlot getAmmoStack(ItemStack bow, EntityPlayer player) {
		ItemStackAndSlot ammoStack;
		for (int i = player.inventory.armorInventory.length; i-- > 0;) {
			if (player.inventory.armorInventory[i] != null && player.inventory.armorInventory[i].getItem() instanceof ItemQuiver) {
				ammoStack = AmmoData.getAmmoStack(bow, new ItemStackAndSlot(player.inventory.armorInventory[i], i + player.inventory.mainInventory.length));
				if (ammoStack != null)
					return ammoStack;
			}
		}
		for (int i = 0; i < player.inventory.mainInventory.length; i++) {
			if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof ItemQuiver) {
				ammoStack = AmmoData.getAmmoStack(bow, new ItemStackAndSlot(player.inventory.mainInventory[i], i));
				if (ammoStack != null)
					return ammoStack;
			}
		}
		return AmmoData.getAmmoStack(bow, player.inventory);
	}
	public static ItemStackAndSlot getAmmoStack(ItemStack bow, ItemStackAndSlot quiver) {
		InventoryQuiver quiverInventory = new InventoryQuiver(quiver.itemStack);
		ItemStack stack = quiverInventory.getStackInSlot(quiverInventory.activeSlot);
		AmmoData ammoData = AmmoData.get(stack);
		if (ammoData != null && ammoData.canBeShotBy(stack, bow))
			return new ItemStackAndSlot(stack, quiverInventory.activeSlot, ammoData).setQuiverSlot(quiver.slot);
		ItemStackAndSlot ammoStack = AmmoData.getAmmoStack(bow, quiverInventory);
		if (ammoStack != null)
			return ammoStack.setQuiverSlot(quiver.slot);
		return null;
	}
	public static ItemStackAndSlot getAmmoStack(ItemStack bow, IInventory inventory) {
		ItemStack stack;
		AmmoData ammoData;
		int size = inventory.getSizeInventory();
		for (int slot = 0; slot < size; slot++) {
			stack = inventory.getStackInSlot(slot);
			ammoData = AmmoData.get(stack);
			if (ammoData != null && ammoData.canBeShotBy(stack, bow))
				return new ItemStackAndSlot(stack, slot, ammoData);
		}
		return null;
	}

	public static boolean isAmmo(ItemStack itemStack) {
		AmmoData ammoData = AmmoData.get(itemStack);
		return ammoData != null && ammoData.canBeShotBy(itemStack, null);
	}
	public static AmmoData get(ItemStack ammo) {
		return ammo == null ? null : AmmoData.AMMO_DATA_MAP.get(new AmmoDataHash(ammo));
	}

	private static void add(Block ammo, AmmoData bowData) {
		AmmoData.add(Item.getItemFromBlock(ammo), bowData);
	}
	private static void add(Item ammo, AmmoData bowData) {
		AmmoData.add(new AmmoDataHash(ammo), bowData);
	}
	private static void add(Block ammo, int damage, AmmoData bowData) {
		AmmoData.add(Item.getItemFromBlock(ammo), damage, bowData);
	}
	private static void add(Item ammo, int damage, AmmoData bowData) {
		AmmoData.add(new AmmoDataHash(ammo, damage), bowData);
	}
	private static void add(ItemStack ammo, AmmoData bowData) {
		AmmoData.add(new AmmoDataHash(ammo), bowData);
	}
	private static void add(AmmoDataHash ammo, AmmoData bowData) {
		if (AmmoData.AMMO_DATA_MAP.containsKey(ammo)) {
			BowOverhaul.logError("Duplicate ammo data! " + ammo.ammoItem.toString());
		}
		AmmoData.AMMO_DATA_MAP.put(ammo, bowData);
	}

	public int durabilityDamage = 1;

	public AmmoData setDurabilityDamage(int damage) {
		this.durabilityDamage = damage;
		return this;
	}

	/**
	 * @param bow The bow trying to shoot, null if checking for a quiver
	 * @return True if this ammo can be shot by the bow
	 */
	public boolean canBeShotBy(ItemStack ammo, ItemStack bow) {
		return true;
	}

	/**
	 * @param unlimitedAmmo 0 - normal ammo consumption, 1 - infinity bow, 2 - creative mode
	 * @return The projectile to spawn
	 */
	public abstract Entity shootItem(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo);

	public int getDurabilityDamage(ItemStack ammo, ItemStack bow) {
		return this.durabilityDamage;
	}

	private static class AmmoDataHash {

		public final Item ammoItem;
		public final int ammoId;
		public final int ammoDamage;

		public AmmoDataHash(ItemStack ammoStack) {
			this(ammoStack.getItem(), ammoStack.getItemDamage());
		}
		public AmmoDataHash(Item ammo) {
			this(ammo, OreDictionary.WILDCARD_VALUE);
		}
		public AmmoDataHash(Item ammo, int damage) {
			this.ammoItem = ammo;
			this.ammoId = Item.getIdFromItem(ammo);
			this.ammoDamage = damage;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof AmmoDataHash && this.ammoId == ((AmmoDataHash) obj).ammoId && (this.ammoDamage == OreDictionary.WILDCARD_VALUE || ((AmmoDataHash) obj).ammoDamage == OreDictionary.WILDCARD_VALUE || this.ammoDamage == ((AmmoDataHash) obj).ammoDamage);
		}
		@Override
		public int hashCode() {
			return this.ammoId;
		}
	}

	static {
		// Populate bow data map
		INFINITY_AMMO = new AmmoDataArrow() {
			@Override
			public EntityArrow newArrow(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				return new EntityOverhauledArrow(world, shooter, shotPower).setType(Items.arrow);
			}
		}.setDurabilityDamage(2);

		AmmoData overhauledArrowAmmo = new AmmoDataArrow() {
			@Override
			public EntityArrow newArrow(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				return new EntityOverhauledArrow(world, shooter, shotPower).setType(ammo.getItem());
			}
		};

		// Arrows
		AmmoData.add(Items.arrow, new AmmoDataArrow());
		if (ItemManager.flintArrow != null) {
			AmmoData.add(ItemManager.flintArrow, overhauledArrowAmmo);
		}
		for (int i = 0; i < ItemManager.overhauledArrows.length; i++) {
			if (ItemManager.overhauledArrows[i] != null) {
				AmmoData.add(ItemManager.overhauledArrows[i], overhauledArrowAmmo);
			}
		}

		// Fireballs
		AmmoData.add(Items.fire_charge, new AmmoDataFireball(true) {
			@Override
			public boolean canBeShotBy(ItemStack ammo, ItemStack bow) {
				return bow == null || EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0;
			}
		}.setDurabilityDamage(3));

		// Throwables
		AmmoData.add(Items.snowball, new AmmoDataThrowable(true));
		if (ItemManager.snowAmmo != null) {
			AmmoData.add(ItemManager.snowAmmo, AmmoData.get(new ItemStack(Items.snowball)));
		}
		AmmoData.add(Items.egg, new AmmoDataThrowable() {
			@Override
			public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				return new EntityEgg(world, shooter);
			}
		});
		AmmoData.add(Items.ender_pearl, new AmmoDataThrowable(0.5F) {
			@Override
			public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				return new EntityEnderPearl(world, shooter);
			}
		}.setDurabilityDamage(6));
		AmmoData.add(Items.experience_bottle, new AmmoDataThrowable(0.5F) {
			@Override
			public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				return new EntityExpBottle(world, shooter);
			}
		});
		AmmoData.add(Items.potionitem, new AmmoDataThrowable(0.5F) {
			@Override
			public boolean canBeShotBy(ItemStack ammo, ItemStack bow) {
				return ItemPotion.isSplash(ammo.getItemDamage());
			}

			@Override
			public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
				ItemStack shotStack = ammo.copy();
				shotStack.stackSize = 1;
				return new EntityPotion(world, shooter, shotStack);
			}
		}.setDurabilityDamage(4));
		if (ItemManager.potionAmmo != null) {
			AmmoData.add(ItemManager.potionAmmo, new AmmoDataThrowable(0.5F) {
				@Override
				public boolean canBeShotBy(ItemStack ammo, ItemStack bow) {
					return ItemPotion.isSplash(ammo.getItemDamage());
				}

				@Override
				public EntityThrowable newThrowable(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
					ItemStack shotStack = ammo.copy();
					shotStack.func_150996_a(Items.potionitem); // setItem
					shotStack.stackSize = 1;
					return new EntityPotion(world, shooter, shotStack);
				}
			}.setDurabilityDamage(4));
		}

		// Misc.
		AmmoData.add(Items.spawn_egg, new AmmoDataSpawnEgg().setDurabilityDamage(6));
		AmmoData.add(Blocks.tnt, new AmmoDataMisc(0.5F, false, false) {
			@Override
			public boolean canBeShotBy(ItemStack ammo, ItemStack bow) {
				return bow == null || EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0;
			}

			@Override
			public Entity newProjectile(ItemStack ammo, World world, EntityLivingBase shooter, ItemStack bow, float draw, float shotPower, int unlimitedAmmo) {
	            world.playSoundAtEntity(shooter, "game.tnt.primed", 1.0F, 1.0F);
				return new EntityTNTPrimed(world, shooter.posX, shooter.posY, shooter.posZ, shooter);
			}
		}.setDurabilityDamage(6));
	}
}
