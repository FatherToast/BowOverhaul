package toast.bowoverhaul.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.item.ItemOverhauledArrow;
import toast.bowoverhaul.util.EventHandler;
import toast.bowoverhaul.util.FileHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The arrow entity used for the overhauled arrow items.
 */
public class EntityOverhauledArrow extends EntityArrow {

    public static final byte DW_ARROW_ID = 24;
    public static final byte DW_KNOCKBACK = 25;
    public static final byte DW_AIR_FRICTION = 26;
    public static final byte DW_WATER_FRICTION = 27;
    public static final byte DW_GRAVITY = 28;

	public Item arrowItem = ItemManager.flintArrow;

	public boolean inGround;
	public Block inBlock;
	public int inData;
	public int inBlockX;
	public int inBlockY;
	public int inBlockZ;

	public int ticksInAir;
	public int ticksInGround;
	public int lifespan;

	public float breakChance = -1.0F;

    public EntityOverhauledArrow(World world) {
        super(world);
		this.setDamage(1.75);
		this.setDefaultLifespan();
    }
    public EntityOverhauledArrow(World world, double x, double y, double z) {
        super(world, x, y, z);
		this.setDamage(1.75);
		this.setDefaultLifespan();
    }
    public EntityOverhauledArrow(World world, EntityLivingBase shooter, EntityLivingBase target, float shotPower, float variance) {
        super(world, shooter, target, shotPower, variance);
		this.setDamage(1.75);
		this.setDefaultLifespan();
    }
    public EntityOverhauledArrow(World world, EntityLivingBase shooter, float shotPower) {
        super(world, shooter, shotPower);
		this.setDamage(1.75);
		this.setDefaultLifespan();
    }

    public void setDefaultLifespan() {
		this.lifespan = this.canBePickedUp == 1 ? 6000 : 1200;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(EntityOverhauledArrow.DW_ARROW_ID, Integer.valueOf(0));

        this.dataWatcher.addObject(EntityOverhauledArrow.DW_KNOCKBACK, Float.valueOf(0.0F));

        this.dataWatcher.addObject(EntityOverhauledArrow.DW_AIR_FRICTION, Float.valueOf(0.99F));
        this.dataWatcher.addObject(EntityOverhauledArrow.DW_WATER_FRICTION, Float.valueOf(0.8F));
        this.dataWatcher.addObject(EntityOverhauledArrow.DW_GRAVITY, Float.valueOf(0.05F));
    }

    public EntityOverhauledArrow setType(Item arrow) {
    	this.arrowItem = arrow;
    	if (arrow instanceof ItemOverhauledArrow) {
    		this.setDamage(((ItemOverhauledArrow) arrow).getDamage());
    	}
    	else if (arrow == Items.arrow) { // Infinity ammo
    		this.setDamage(2.0);
    		this.lifespan = 100;
    	}
    	else {
    		this.setDamage(1.75);
    	}
        this.dataWatcher.updateObject(EntityOverhauledArrow.DW_ARROW_ID, Integer.valueOf(Item.getIdFromItem(arrow)));
    	return this;
    }

    public void setKnockback(float value) {
		this.dataWatcher.updateObject(EntityOverhauledArrow.DW_KNOCKBACK, Float.valueOf(value));
    }
    public float getKnockback() {
		return this.dataWatcher.getWatchableObjectFloat(EntityOverhauledArrow.DW_KNOCKBACK);
    }

    public void setAirFriction(float value) {
		this.dataWatcher.updateObject(EntityOverhauledArrow.DW_AIR_FRICTION, Float.valueOf(value));
    }
    public float getAirFriction() {
		return this.dataWatcher.getWatchableObjectFloat(EntityOverhauledArrow.DW_AIR_FRICTION);
    }
    public void setWaterFriction(float value) {
		this.dataWatcher.updateObject(EntityOverhauledArrow.DW_WATER_FRICTION, Float.valueOf(value));
    }
    public float getWaterFriction() {
		return this.dataWatcher.getWatchableObjectFloat(EntityOverhauledArrow.DW_WATER_FRICTION);
    }
    public void setGravity(float value) {
		this.dataWatcher.updateObject(EntityOverhauledArrow.DW_GRAVITY, Float.valueOf(value));
    }
    public float getGravity() {
		return this.dataWatcher.getWatchableObjectFloat(EntityOverhauledArrow.DW_GRAVITY);
    }

    @SideOnly(Side.CLIENT)
    public Item getType() {
    	return Item.getItemById(this.dataWatcher.getWatchableObjectInt(EntityOverhauledArrow.DW_ARROW_ID));
    }

    @Override
	public void onUpdate() {
        this.onEntityUpdate(); // Circumvent EntityArrow's onUpdate

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0 / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, f) * 180.0 / Math.PI);
        }

        Block block = this.worldObj.getBlock(this.inBlockX, this.inBlockY, this.inBlockZ);
        if (block.getMaterial() != Material.air) {
            block.setBlockBoundsBasedOnState(this.worldObj, this.inBlockX, this.inBlockY, this.inBlockZ);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.inBlockX, this.inBlockY, this.inBlockZ);
            if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ))) {
                this.inGround = true;
            }
        }
        if (this.arrowShake > 0) {
            this.arrowShake--;
        }
        if (this.inGround) {
        	// Check to be sure the block this arrow is stuck in is still there
            int data = this.worldObj.getBlockMetadata(this.inBlockX, this.inBlockY, this.inBlockZ);
            if (block == this.inBlock && data == this.inData) {
                this.ticksInGround++;
                if (!this.worldObj.isRemote && this.lifespan >= 0 && this.ticksInGround >= this.lifespan) {
                    this.setDead();
                }
            }
            else {
            	// The block is no longer the same
                this.inGround = false;
                this.motionX *= this.rand.nextFloat() * 0.2F;
                this.motionY *= this.rand.nextFloat() * 0.2F;
                this.motionZ *= this.rand.nextFloat() * 0.2F;
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        }
        else {
            this.ticksInAir++;
            if (!this.worldObj.isRemote && this.posY > 2.5 * this.worldObj.getHeight() && this.lifespan >= 0 && ++this.ticksInGround >= this.lifespan) {
                this.setDead(); // Despawn if the entity has been very high in the sky for a long time
            }

            Vec3 posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 motionVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            // Ray trace block collision
            MovingObjectPosition closestHitObject = this.worldObj.func_147447_a(posVec, motionVec, false, true, false);
            posVec = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            motionVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (closestHitObject != null) {
                motionVec = Vec3.createVectorHelper(closestHitObject.hitVec.xCoord, closestHitObject.hitVec.yCoord, closestHitObject.hitVec.zCoord);
            }

            // Do entity collision
            List entitiesInRange = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            Entity closestEntity = null;
            double closestDistance = Double.POSITIVE_INFINITY;
            Entity entity;
            double distance;
            AxisAlignedBB hitbox;
            MovingObjectPosition hitObject;
            for (int i = 0; i < entitiesInRange.size(); ++i) {
                entity = (Entity) entitiesInRange.get(i);
                if (entity.canBeCollidedWith() && (entity != this.shootingEntity || this.ticksInAir >= 5)) {
                	hitbox = entity.boundingBox.expand(0.3, 0.3, 0.3);
                    hitObject = hitbox.calculateIntercept(posVec, motionVec);
                    if (hitObject != null) {
                    	distance = posVec.distanceTo(hitObject.hitVec);
                        if (distance < closestDistance) {
                            closestEntity = entity;
                            closestDistance = distance;
                        }
                    }
                }
            }
            if (closestEntity != null) {
                closestHitObject = new MovingObjectPosition(closestEntity);
            }

            if (closestHitObject != null && closestHitObject.entityHit != null && closestHitObject.entityHit instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) closestHitObject.entityHit;
                if (player.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(player)) {
                    closestHitObject = null;
                }
            }

            if (closestHitObject != null) {
                if (closestHitObject.entityHit != null) {
                	// Hit an entity
                    float velocity = (float) Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int damageToDeal = (int) Math.ceil(velocity * this.getDamage());
                    if (this.getIsCritical()) {
                        damageToDeal += this.rand.nextInt(damageToDeal / 2 + 2);
                    }

                    if (this.isBurning() && !(closestHitObject.entityHit instanceof EntityEnderman)) {
                        closestHitObject.entityHit.setFire(5);
                    }

                    DamageSource damageSource;
                    if (this.shootingEntity == null) {
                        damageSource = DamageSource.causeArrowDamage(this, this);
                    }
                    else {
                        damageSource = DamageSource.causeArrowDamage(this, this.shootingEntity);
                    }
                    boolean isBroken = closestHitObject.entityHit.attackEntityFrom(damageSource, damageToDeal);

                    if (isBroken) {
                    	if (this.breakChance >= 0.0F) {
                    		isBroken = this.rand.nextFloat() < this.breakChance;
                    	}
                    	else {
                    		isBroken = this.rand.nextFloat() < (this.arrowItem instanceof ItemOverhauledArrow ? ((ItemOverhauledArrow) this.arrowItem).getBreakChance() : ItemOverhauledArrow.getBreakChance(Item.ToolMaterial.IRON));
                    	}

                        if (closestHitObject.entityHit instanceof EntityLivingBase) {
                            EntityLivingBase entityHit = (EntityLivingBase) closestHitObject.entityHit;
                            if (isBroken && !this.worldObj.isRemote) {
                                entityHit.setArrowCountInEntity(entityHit.getArrowCountInEntity() + 1);
                            }

                            float knockback = this.getKnockback();
                            if (knockback != 0.0F) {
                                float vH = (float) Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                                if (vH > 0.0F) {
                                    closestHitObject.entityHit.addVelocity(this.motionX * knockback * 0.6 / vH, 0.1, this.motionZ * knockback * 0.6 / vH);
                                }
                            }

                            if (this.shootingEntity != null && this.shootingEntity instanceof EntityLivingBase) {
                            	// Apply enchantments
                                EnchantmentHelper.func_151384_a(entityHit, this.shootingEntity);
                                EnchantmentHelper.func_151385_b((EntityLivingBase) this.shootingEntity, entityHit);
                            }

                            if (this.shootingEntity != null && closestHitObject.entityHit != this.shootingEntity && closestHitObject.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                                ((EntityPlayerMP) this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

                        if (isBroken && !(closestHitObject.entityHit instanceof EntityEnderman)) {
                            this.setDead();
                        }
                    }
                    if (!isBroken) {
                        this.motionX *= -0.1;
                        this.motionY *= -0.1;
                        this.motionZ *= -0.1;
                        this.rotationYaw += 180.0F;
                        this.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                    }
                }
                else {
                	// Hit a block
                    this.inBlockX = closestHitObject.blockX;
                    this.inBlockY = closestHitObject.blockY;
                    this.inBlockZ = closestHitObject.blockZ;
                    this.inBlock = this.worldObj.getBlock(this.inBlockX, this.inBlockY, this.inBlockZ);
                    this.inData = this.worldObj.getBlockMetadata(this.inBlockX, this.inBlockY, this.inBlockZ);

                    this.motionX = (float)(closestHitObject.hitVec.xCoord - this.posX);
                    this.motionY = (float)(closestHitObject.hitVec.yCoord - this.posY);
                    this.motionZ = (float)(closestHitObject.hitVec.zCoord - this.posZ);
                    float velocity = (float) Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

                    this.posX -= this.motionX / velocity * 0.05;
                    this.posY -= this.motionY / velocity * 0.05;
                    this.posZ -= this.motionZ / velocity * 0.05;

                    this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.setIsCritical(false);

                    if (this.inBlock.getMaterial() != Material.air) {
                        this.inBlock.onEntityCollidedWithBlock(this.worldObj, this.inBlockX, this.inBlockY, this.inBlockZ, this);
                    }
                }
            }

            if (this.getIsCritical()) {
                for (int i = 0; i < 4; ++i) {
                    this.worldObj.spawnParticle("crit", this.posX + this.motionX * i / 4.0, this.posY + this.motionY * i / 4.0, this.posZ + this.motionZ * i / 4.0, -this.motionX, -this.motionY + 0.2, -this.motionZ);
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float velocity = (float) Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0 / Math.PI);
            for (this.rotationPitch = (float)(Math.atan2(this.motionY, velocity) * 180.0 / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            	// Do nothing
            }
            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }
            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

            float frictionMult = this.getAirFriction();
            if (this.isInWater()) {
                for (int i = 0; i < 4; i++) {
                    this.worldObj.spawnParticle("bubble", this.posX - this.motionX * 0.25F, this.posY - this.motionY * 0.25F, this.posZ - this.motionZ * 0.25F, this.motionX, this.motionY, this.motionZ);
                }
                frictionMult = this.getWaterFriction();
            }
            if (this.isWet()) {
                this.extinguish();
            }
            this.motionX *= frictionMult;
            this.motionY *= frictionMult;
            this.motionZ *= frictionMult;

            this.motionY -= this.getGravity();
            this.setPosition(this.posX, this.posY, this.posZ);

            this.func_145775_I(); // Do block collision
        }
    }

    @Override
	public void setKnockbackStrength(int knockback) {
        this.setKnockback(knockback);
    }

    @Override
	public void onCollideWithPlayer(EntityPlayer player) {
        if (!this.worldObj.isRemote && this.inGround && this.arrowShake <= 0) {
            boolean pickedUp = this.canBePickedUp == 1 || this.canBePickedUp == 2 && player.capabilities.isCreativeMode;
            if (this.canBePickedUp == 1 && !this.addToInventory(player)) {
                pickedUp = false;
            }
            if (pickedUp) {
                this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                player.onItemPickup(this, 1);
                this.setDead();
            }
        }
    }

    public boolean addToInventory(EntityPlayer player) {
    	if (this.arrowItem == Items.arrow)
    		return false;
    	ItemStack ammo = new ItemStack(this.arrowItem, 1);
    	return EventHandler.addToQuiver(player, ammo) || player.inventory.addItemStackToInventory(ammo);
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound tag) {
    	tag.setString("arrowItem", Item.itemRegistry.getNameForObject(this.arrowItem));

        tag.setByte("inGround", (byte) (this.inGround ? 1 : 0));
        tag.setByte("inTile", (byte) Block.getIdFromBlock(this.inBlock));
        tag.setByte("inData", (byte) this.inData);
        tag.setShort("xTile", (short) this.inBlockX);
        tag.setShort("yTile", (short) this.inBlockY);
        tag.setShort("zTile", (short) this.inBlockZ);

        tag.setShort("life", (short) this.ticksInGround);
        tag.setShort("lifespan", (short) this.lifespan);

        tag.setByte("shake", (byte) this.arrowShake);
        tag.setByte("pickup", (byte) this.canBePickedUp);

        tag.setDouble("damage", this.getDamage());

        tag.setFloat("breakChance", this.breakChance);

        tag.setFloat("knockback", this.getKnockback());

        tag.setFloat("friction", 1.0F - this.getAirFriction());
        tag.setFloat("waterFriction", 1.0F - this.getWaterFriction());
        tag.setFloat("gravity", this.getGravity());
    }
    @Override
	public void readEntityFromNBT(NBTTagCompound tag) {
    	this.arrowItem = FileHelper.readItem(tag.getString("arrowItem"), "EntityOverhauledArrow", false);
    	if (!this.worldObj.isRemote) {
			this.dataWatcher.updateObject(EntityOverhauledArrow.DW_ARROW_ID, Integer.valueOf(Item.getIdFromItem(this.arrowItem)));
		}

        this.inGround = tag.getByte("inGround") == 1;
        this.inBlock = Block.getBlockById(tag.getByte("inTile") & 255);
        this.inData = tag.getByte("inData") & 255;
        this.inBlockX = tag.getShort("xTile");
        this.inBlockY = tag.getShort("yTile");
        this.inBlockZ = tag.getShort("zTile");

        this.ticksInGround = tag.getShort("life");
        this.lifespan = tag.getShort("lifespan");

        this.arrowShake = tag.getByte("shake") & 255;
        if (tag.hasKey("pickup", 99)) {
            this.canBePickedUp = tag.getByte("pickup");
        }
        else if (tag.hasKey("player", 99)) {
            this.canBePickedUp = tag.getBoolean("player") ? 1 : 0;
        }

        if (tag.hasKey("damage", 99)) {
            this.setDamage(tag.getDouble("damage"));
        }

        if (tag.hasKey("breakChance")) {
			this.breakChance = tag.getFloat("breakChance");
		}

        if (tag.hasKey("knockback")) {
        	this.setKnockback(tag.getFloat("knockback"));
        }

        if (tag.hasKey("friction")) {
			this.setAirFriction(1.0F - tag.getFloat("friction"));
		}
        if (tag.hasKey("waterFriction")) {
			this.setWaterFriction(1.0F - tag.getFloat("waterFriction"));
		}
        if (tag.hasKey("gravity")) {
			this.setGravity(tag.getFloat("gravity"));
		}
    }

}
