package toast.bowoverhaul.entry.nbt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import toast.bowoverhaul.stats.ArrowStatsInstance;

public class NBTStatsInfo {
    /** The mob info this is a part of. */
    public final ArrowStatsInstance parent;
    /** The entity shooting the arrow. */
    public final Entity theShooter;
    /** The bow shooting the arrow. */
    public final ItemStack theBow;
    /** The arrow that is being affected. */
    public final Entity theArrow;
    /** The random number generator. */
    public final Random random;

    /** List containing all tags that will be added to the arrow. */
    private final ArrayList<NBTWrapper> tags = new ArrayList<NBTWrapper>();

    public NBTStatsInfo(ItemStack bow, Entity shooter, ArrowStatsInstance mobInfo) {
        this.parent = mobInfo;
        this.theShooter = shooter;
        this.theBow = bow;
        this.theArrow = mobInfo.theArrow;
        this.random = mobInfo.theArrow.worldObj.rand;
    }

    // Adds a tag to this info.
    public void addTag(String name, NBTBase tag) {
        this.tags.add(new NBTWrapper(name, tag));
    }

    // Writes all tags to the given tag compound and returns that compound.
    public NBTTagCompound writeTo(NBTTagCompound compound) {
        for (NBTWrapper wrapper : this.tags) {
            if (wrapper.getTag().getClass() == NBTTagCompound.class) {
                this.writeCompound(compound, wrapper);
            }
            else {
                compound.setTag(wrapper.getName(), wrapper.getTag());
            }
        }
        return compound;
    }

    public NBTTagList writeTo(NBTTagList list) {
        for (NBTWrapper wrapper : this.tags) {
            list.appendTag(wrapper.getTag());
        }
        return list;
    }

    // Called recursively to copy all the NBT tags from a wrapped compound.
    private void writeCompound(NBTTagCompound compound, NBTWrapper wrapper) {
        NBTTagCompound copyTo = compound.getCompoundTag(wrapper.getName());
        if (!compound.hasKey(wrapper.getName())) {
            compound.setTag(wrapper.getName(), copyTo);
        }

        NBTTagCompound copyFrom = (NBTTagCompound) wrapper.getTag();
        for (String name : (Collection<String>) copyFrom.func_150296_c()) {
            NBTBase tag = copyFrom.getTag(name);
            if (tag.getClass() == NBTTagCompound.class) {
                this.writeCompound(copyTo, new NBTWrapper(name, tag));
            }
            else {
                copyTo.setTag(name, tag.copy());
            }
        }
    }

    /** Wrapper class to store an NBT tag with its name. */
    private static class NBTWrapper {
        private final String name;
        private final NBTBase tag;

        public NBTWrapper(String name, NBTBase tag) {
            if (tag == null)
                throw new IllegalArgumentException("NBTBase cannot be null!");
            this.name = name;
            this.tag = tag;
        }

        /** @return the name tag for the wrapped NBTBase. Empty string only if in a list. */
        public String getName() {
            return this.name;
        }

        /** @return the wrapped NBTBase instance. */
        public NBTBase getTag() {
            return this.tag;
        }
    }
}
