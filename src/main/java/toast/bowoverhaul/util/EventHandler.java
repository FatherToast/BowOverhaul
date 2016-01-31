package toast.bowoverhaul.util;

import java.util.HashSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.headhitbox.HeadHitbox;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ItemQuiver;
import toast.bowoverhaul.item.ammo.AmmoData;
import toast.bowoverhaul.network.MessageFX;
import toast.bowoverhaul.stats.ArrowStats;
import toast.bowoverhaul.stats.ArrowStatsInstance;
import toast.bowoverhaul.stats.BowStats;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

	public static final HashSet<ItemArmor.ArmorMaterial> LIGHT_ARMOR_MATERIALS = new HashSet<ItemArmor.ArmorMaterial>();
	public static final HashSet<Item> LIGHT_ARMORS = new HashSet<Item>();
	public static final float[] ARMOR_DRAW_TIME_MULTS = {
		1.0F - (float) Properties.getDouble(Properties.SLOWDOWN_ARMOR, "boots_slow"),
		1.0F - (float) Properties.getDouble(Properties.SLOWDOWN_ARMOR, "legs_slow"),
		1.0F - (float) Properties.getDouble(Properties.SLOWDOWN_ARMOR, "chest_slow"),
		1.0F - (float) Properties.getDouble(Properties.SLOWDOWN_ARMOR, "helm_slow")
	};
	public static final Item ARROW_DROP_REPLACEMENT = FileHelper.readItem(Properties.getString(Properties.GENERAL, "replace_vanilla_arrow_drops"), "config\\BowOverhaul.cfg\\" + Properties.GENERAL + "\\replace_vanilla_arrow_drops", false);

    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /** Called by <unknown>.
        EntityPlayer entityPlayer = the player picking up the item.
        EntityItem item = the item being picked up.
    */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        if (event.entityPlayer == null || event.item == null)
            return;
        ItemStack itemStack = event.item.getEntityItem();
        if (AmmoData.isAmmo(itemStack) && EventHandler.addToQuiver(event.entityPlayer, itemStack)) {
            event.item.setEntityItemStack(itemStack);
        }
    }

    public static boolean addToQuiver(EntityPlayer player, ItemStack ammo) {
    	InventoryQuiver quiverInventory;
    	boolean inventoryChanged = false;

		for (int i = player.inventory.armorInventory.length; ammo.stackSize > 0 && i-- > 0;) {
			if (player.inventory.armorInventory[i] != null && player.inventory.armorInventory[i].getItem() instanceof ItemQuiver) {
				quiverInventory = new InventoryQuiver(player.inventory.armorInventory[i]);
				if (quiverInventory.autoFill(ammo)) {
					player.inventory.setInventorySlotContents(i + player.inventory.mainInventory.length, player.inventory.armorInventory[i]);
					inventoryChanged = true;
				}
			}
		}
		for (int i = 0; ammo.stackSize > 0 && i < player.inventory.mainInventory.length; i++) {
			if (player.inventory.mainInventory[i] != null && player.inventory.mainInventory[i].getItem() instanceof ItemQuiver) {
				quiverInventory = new InventoryQuiver(player.inventory.mainInventory[i]);
				if (quiverInventory.autoFill(ammo)) {
					player.inventory.setInventorySlotContents(i, player.inventory.mainInventory[i]);
					inventoryChanged = true;
				}
			}
		}
		return inventoryChanged;
    }

    /**
     * Called by ItemBow.onPlayerStoppedUsing().
     * EntityPlayer entityPlayer = the player firing the bow.
     * ItemStack bow = the bow being fired.
     * int charge = the number of ticks the bow has been charged for.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onArrowLoose(ArrowLooseEvent event) {
    	event.charge = Math.round(event.charge * EventHandler.calculateDrawTimeMult(event.entityLiving));

		BowStats bowStats = BowStats.getBowStats(event.bow);
		if (bowStats != null) {
			event.charge = (int) Math.round(event.charge * FileHelper.getValue(bowStats.drawSpeed, event.entity != null ? event.entity.worldObj.rand : BowOverhaul.random));
			if (event.charge > bowStats.maxDraw) {
				event.charge = (int) bowStats.maxDraw;
			}
		}
    }

    /**
     * @return Draw time multiplier of the entity. Used to slow down draw speed when armored, and possibly increase it with enchantments.
     */
    public static float calculateDrawTimeMult(EntityLivingBase entity) {
    	float mult = 1.0F;

		boolean isLight;
    	ItemStack armor;
    	for (int i = 1; i < 5; i++) {
    		armor = entity.getEquipmentInSlot(i);
    		if (armor != null) {
    			isLight = false;

    			if (EventHandler.LIGHT_ARMORS.contains(armor.getItem())) {
    				isLight = true;
    			}
    			else if (armor.getItem() instanceof ItemArmor) {
	    			if (EventHandler.LIGHT_ARMOR_MATERIALS.contains(((ItemArmor) armor.getItem()).getArmorMaterial())) {
	    				isLight = true;
	    			}
				}

	    		if (!isLight) {
					mult *= EventHandler.ARMOR_DRAW_TIME_MULTS[i % EventHandler.ARMOR_DRAW_TIME_MULTS.length];
	    		}
    		}
    	}

    	return mult;
    }

    /**
     * Called by World.spawnEntityInWorld().
     * Entity entity = the entity joining the world.
     * World world = the world the entity is joining.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
    	if (event.world.isRemote)
			/* Does not work yet
    		// Fix floor glitch
    		if (event.entity instanceof EntityLivingBase && event.entity.motionY > 0.0) {
    			ClientTickHandler.markForDelayedMotionY(event.entity);
    		}
    		 */
    		return;

		// Find the shooter
    	boolean foundShooterField = true;
		Entity shootingEntity;
		if (event.entity instanceof EntityArrow) {
			shootingEntity = ((EntityArrow) event.entity).shootingEntity;
        }
        else if (event.entity instanceof EntityFireball) {
			shootingEntity = ((EntityFireball) event.entity).shootingEntity;
		}
        else if (event.entity instanceof EntityThrowable) {
			shootingEntity = ((EntityThrowable) event.entity).getThrower();
		}
		else if (event.entity instanceof EntityFishHook) {
			shootingEntity = ((EntityFishHook) event.entity).field_146042_b; // angler
		}
		else {
        	shootingEntity = null;
        	foundShooterField = false;
        }
		if (shootingEntity == null) {
			shootingEntity = ArrowStatsInstance.loadShooter(event.entity);
		}
		// Find the bow (always fails if shooter is not found)
		ItemStack bow;
		BowStats bowStats;
		if (shootingEntity instanceof EntityLivingBase) {
			bow = ((EntityLivingBase) shootingEntity).getHeldItem();
			bowStats = BowStats.getBowStats(bow);
		}
		else {
			bow = null;
			bowStats = null;
		}

		// Find the arrow stats
		ArrowStats arrowStats = ArrowStats.getArrowStats(event.entity);
		if (bowStats != null && arrowStats == null) {
			arrowStats = ArrowStats.NULL_ARROW_STATS;
		}

		// Arrow stat initialization
		if (arrowStats != null) {
    		ArrowStatsInstance statsInstance = ArrowStatsInstance.get(event.entity);
    		if (statsInstance.ensureInitialized()) {
				if (!foundShooterField && shootingEntity == null) {
					BowOverhaul.logDebug("Failed to get shooter for arrow! Can not apply bow effects! at " + event.entity.toString());
				}
				arrowStats.initArrow(statsInstance, shootingEntity, bow, bowStats, event.world.rand);
    			statsInstance.save();
    		}
    	}
    }

    /**
     * Called by EntityLivingBase.attackEntityFrom().
     * EntityLivingBase entityLiving = the entity being damaged.
     * DamageSource source = the source of the damage.
     * int ammount = the amount of damage to be applied.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.source != null) {
        	// Headshot calculation
        	Entity arrow = event.source.getSourceOfDamage();
        	if (arrow != null && arrow != event.entityLiving) {
        		// Get arrow stats, cancel if not valid
        		ArrowStats stats = ArrowStats.getArrowStats(arrow);
        		if (stats != null) {
	        		float newDamage = event.ammount;
	        		float lastDamage;
	        		if (BowOverhaul.entityLivingBaseLastDamage != null && event.entityLiving.hurtResistantTime > event.entityLiving.maxHurtResistantTime / 2.0F) {
	        			// If within the hurt resistance time, check for the last damage
	        			try {
	        				lastDamage = (Float) BowOverhaul.entityLivingBaseLastDamage.get(event.entityLiving);
	        				newDamage += lastDamage;
	        			}
	        			catch (Exception ex) {
	        				lastDamage = 0.0F;
	        			}
	        		}
	        		else {
	        			lastDamage = 0.0F;
	        		}

        			float totalMultiplier = 1.0F;
        			float totalFlatDamage = 0.0F;

        			// Apply arrow-specific stats
	        		ArrowStatsInstance statsInstance = ArrowStatsInstance.get(arrow);
	        		if (statsInstance.ensureInitialized()) {
	    				stats.initArrow(statsInstance, null, null, null, arrow.worldObj.rand); // Safety measure
	        		}
	        		double distMult = statsInstance.getDistanceMult();
	        		if (BowOverhaul.debug) {
	        			BowOverhaul.logDebug("DistanceMultiplier:" + distMult + ",Distance:" + Math.sqrt(statsInstance.getDistanceSq()) * 100.0 + "%");
	        		}
	        		totalMultiplier *= distMult;

	        		// Determine if the projectile has caused a headshot
	        		boolean headshot = false;
	        		HeadHitbox headHitbox = HeadHitbox.getHeadHitbox(event.entityLiving);
	        		if (headHitbox != null) {
	        			AxisAlignedBB hitbox = headHitbox.getBoundingBox(event.entityLiving);
	        			Vec3 posVec = Vec3.createVectorHelper(arrow.posX, arrow.posY, arrow.posZ);
	        			Vec3 motionVec = posVec.addVector(arrow.motionX, arrow.motionY, arrow.motionZ);

	        			MovingObjectPosition intercept = hitbox.calculateIntercept(posVec, motionVec);
						MovingObjectPosition interferrence = event.entityLiving.boundingBox.calculateIntercept(posVec, intercept != null ? intercept.hitVec : motionVec);
	        			if (intercept != null) {
							if (interferrence == null || posVec.squareDistanceTo(intercept.hitVec) - posVec.squareDistanceTo(interferrence.hitVec) < posVec.squareDistanceTo(motionVec) * 0.1) {
								headshot = true;
							}
	        			}
	        			else if (interferrence == null) {
	        				intercept = this.expandHeadHitbox(hitbox, event.entityLiving.boundingBox).calculateIntercept(posVec, motionVec);
	        				if (intercept != null) {
								headshot = true;
							}
	        			}
	        			// Apply appropriate head hitbox multiplier
	                    if (headshot) {
	                    	totalFlatDamage += headHitbox.headDamage;
	                    	totalMultiplier *= headHitbox.headMultiplier;

	                    	//TODO apply helmet damage reduction?

	            			BowOverhaul.CHANNEL.sendToDimension(new MessageFX(MessageFX.EffectType.HEADSHOT,
	        						hitbox.minX + (hitbox.maxX - hitbox.minX) / 2.0, hitbox.maxY, hitbox.minZ + (hitbox.maxZ - hitbox.minZ) / 2.0,
	            					40.0F, 0.0F), event.entityLiving.dimension);
	                    	BowOverhaul.logDebug("BOOM! HEADSHOT!");
	            		}
	            		else {
	                    	totalFlatDamage += headHitbox.bodyDamage;
	            			totalMultiplier *= headHitbox.bodyMultiplier;
	            		}
	        		}
	        		// Apply appropriate arrow-specific multiplier
	        		if (headshot) {
                    	totalFlatDamage += statsInstance.headDamage;
	        			totalMultiplier *= statsInstance.headMultiplier;
	        		}
	        		else {
                    	totalFlatDamage += statsInstance.bodyDamage;
	        			totalMultiplier *= statsInstance.bodyMultiplier;
	        		}

	        		newDamage *= totalMultiplier;
	        		newDamage += totalFlatDamage;
	        		newDamage = BowOverhaul.fireHeadshotEvent(event.entityLiving, arrow, event.source.getEntity(), headshot, newDamage);

	        		newDamage -= lastDamage;
        			if (newDamage != event.ammount) {
        				event.ammount = newDamage;
        				// Update last damage
        				try {
	        				BowOverhaul.entityLivingBaseLastDamage.set(event.entityLiving, newDamage + lastDamage);
	        			}
	        			catch (Exception ex) {
	        				// Do nothing
	        			}
        			}
        		}
        	}
        }
    }

    private AxisAlignedBB expandHeadHitbox(AxisAlignedBB headHitbox, AxisAlignedBB entityHitbox) {
    	float standardExpansion = 0.3F;
    	AxisAlignedBB expandedHitbox = headHitbox.copy();
    	if (headHitbox.minX <= entityHitbox.minX) {
			expandedHitbox.minX -= standardExpansion;
		}
    	if (headHitbox.maxX >= entityHitbox.maxX) {
			expandedHitbox.maxX += standardExpansion;
		}
    	if (headHitbox.minY <= entityHitbox.minY) {
			expandedHitbox.minY -= standardExpansion;
		}
    	if (headHitbox.maxY >= entityHitbox.maxY) {
			expandedHitbox.maxY += standardExpansion;
		}
    	if (headHitbox.minZ <= entityHitbox.minZ) {
			expandedHitbox.minZ -= standardExpansion;
		}
    	if (headHitbox.maxZ >= entityHitbox.maxZ) {
			expandedHitbox.maxZ += standardExpansion;
		}
    	return expandedHitbox;
    }

    /**
     * Called by EntityLiving.onDeath().
     * EntityLivingBase entityLiving = the entity dropping the items.
     * DamageSource source = the source of the lethal damage.
     * ArrayList<EntityItem> drops = the items being dropped.
     * int lootingLevel = the attacker's looting level.
     * boolean recentlyHit = if the entity was recently hit by another player.
     * int specialDropValue = recentlyHit ? entityLiving.getRNG().nextInt(200) - lootingLevel : 0.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingDrops(LivingDropsEvent event) {
        if (EventHandler.ARROW_DROP_REPLACEMENT != null && !event.entityLiving.worldObj.isRemote) {
        	EntityItem drop;
        	ItemStack itemStack;
            for (int i = 0; i < event.drops.size(); i++) {
            	drop = event.drops.get(i);
            	if (drop != null) {
            		itemStack = drop.getEntityItem();
            		if (itemStack != null && itemStack.getItem() == Items.arrow) {
            			itemStack.func_150996_a(EventHandler.ARROW_DROP_REPLACEMENT); // setItem
            			drop.setEntityItemStack(itemStack);
            		}
            	}
            }
        }
    }

    static {
    	String[] list;
    	list = Properties.getString(Properties.SLOWDOWN_ARMOR, "_light_armor_materials").split(",");
    	for (String entry : list) {
    		if (!"".equals(entry)) {
    			FIND_MATERIAL: {
	    			for (ItemArmor.ArmorMaterial material : ItemArmor.ArmorMaterial.values()) {
	    				if (entry.equals(material.toString())) {
	    					EventHandler.LIGHT_ARMOR_MATERIALS.add(material);
	    					break FIND_MATERIAL;
	    				}
	    			}
    				BowOverhaul.logWarning("Could not parse material type (" + entry + ") at config\\BowOverhaul.cfg\\" + Properties.SLOWDOWN_ARMOR + "\\_light_armor_materials");
	    		}
    		}
    	}

    	list = Properties.getString(Properties.SLOWDOWN_ARMOR, "_light_armors").split(",");
    	Item armor;
    	for (String entry : list) {
    		if (!"".equals(entry)) {
    			armor = FileHelper.readItem(entry, "config\\BowOverhaul.cfg\\" + Properties.SLOWDOWN_ARMOR + "\\_light_armors", false);
    			if (armor != null) {
					EventHandler.LIGHT_ARMORS.add(armor);
				}
    			else {
    				BowOverhaul.logWarning("Could not parse item id (" + entry + ") at config\\BowOverhaul.cfg\\" + Properties.SLOWDOWN_ARMOR + "\\_light_armors");
    			}
    		}
    	}
    }
}