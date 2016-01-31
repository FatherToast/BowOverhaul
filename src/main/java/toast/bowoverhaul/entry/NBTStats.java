package toast.bowoverhaul.entry;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import toast.bowoverhaul.entry.nbt.EntryNBTByte;
import toast.bowoverhaul.entry.nbt.EntryNBTByteArray;
import toast.bowoverhaul.entry.nbt.EntryNBTChestLoot;
import toast.bowoverhaul.entry.nbt.EntryNBTCompound;
import toast.bowoverhaul.entry.nbt.EntryNBTDouble;
import toast.bowoverhaul.entry.nbt.EntryNBTEnchantId;
import toast.bowoverhaul.entry.nbt.EntryNBTFloat;
import toast.bowoverhaul.entry.nbt.EntryNBTInt;
import toast.bowoverhaul.entry.nbt.EntryNBTIntArray;
import toast.bowoverhaul.entry.nbt.EntryNBTItemId;
import toast.bowoverhaul.entry.nbt.EntryNBTList;
import toast.bowoverhaul.entry.nbt.EntryNBTLong;
import toast.bowoverhaul.entry.nbt.EntryNBTPotionId;
import toast.bowoverhaul.entry.nbt.EntryNBTShort;
import toast.bowoverhaul.entry.nbt.EntryNBTString;
import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.stats.ArrowStatsInstance;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NBTStats implements IPropertyReader {
    // The entry objects included in this property.
    public final IProperty[] entries;

    public NBTStats() { // This constructor is used instead of null nbt stats
    	this.entries = new IProperty[0];
    }
    public NBTStats(String tag, String path, JsonObject root, JsonObject node) {
        JsonArray nodes = node.getAsJsonArray(tag);
        if (nodes == null) {
            this.entries = new IProperty[0];
        }
        else {
            int length = nodes.size();
            this.entries = new IProperty[length];
            for (int i = 0; i < length; i++) {
                this.entries[i] = this.readLine(path, root, i, nodes.get(i));
            }
        }
    }

    // Returns true if this has any meaning.
    public boolean hasEntries() {
        return this.entries.length > 0;
    }

    // Generates and writes any nbt tags to the given compound and returns it.
    public NBTTagCompound generate(Entity shooter, ItemStack bow, NBTTagCompound compound, ArrowStatsInstance mobInfo) {
    	if (!this.hasEntries())
    		return compound;

        NBTStatsInfo info = new NBTStatsInfo(bow, shooter, mobInfo);
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.addTags(info);
            }
        }
        return info.writeTo(compound);
    }
    public NBTTagList generate(Entity shooter, ItemStack bow, NBTTagList list, ArrowStatsInstance mobInfo) {
    	if (!this.hasEntries())
    		return list;

        NBTStatsInfo info = new NBTStatsInfo(bow, shooter, mobInfo);
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.addTags(info);
            }
        }
        return info.writeTo(list);
    }
    public void generate(NBTStatsInfo info) {
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.addTags(info);
            }
        }
    }

    // Loads a line as a mob property.
    @Override
    public IProperty readLine(String path, JsonObject root, int index, JsonElement node) {
        path += "\\entry_" + (index + 1);
        if (!node.isJsonObject())
            throw new BowOverhaulSettingsException("Invalid node (object expected)!", path);
        JsonObject objNode = node.getAsJsonObject();
        String function = null;
        try {
            function = objNode.get("function").getAsString();
        }
        catch (NullPointerException ex) {
            // Do nothing
        }
        catch (IllegalArgumentException ex) {
            // Do nothing
        }
        if (function == null)
            throw new BowOverhaulSettingsException("Missing function name!", path);
        path += "(" + function + ")";

        if (function.equals("all"))
            return new PropertyGroup(path, root, index, objNode, this);
        if (function.equals("choose"))
            return new PropertyChoose(path, root, index, objNode, this);
        if (function.equals("external"))
            return new PropertyExternal(path, root, index, objNode, this);

        if (function.equals("compound"))
            return new EntryNBTCompound(path, root, objNode);
        if (function.equals("list"))
            return new EntryNBTList(path, root, objNode);
        if (function.equals("chest_loot"))
            return new EntryNBTChestLoot(path, root, index, objNode, this);
        if (function.equals("item_id"))
            return new EntryNBTItemId(path, root, index, objNode, this);
        if (function.equals("potion_id"))
            return new EntryNBTPotionId(path, root, index, objNode, this);
        if (function.equals("enchant_id"))
            return new EntryNBTEnchantId(path, root, index, objNode, this);

        if (function.equals("string"))
            return new EntryNBTString(path, root, index, objNode, this);
        if (function.equals("boolean"))
            return new EntryNBTByte(path, root, index, objNode, this, true);
        if (function.equals("byte"))
            return new EntryNBTByte(path, root, index, objNode, this, false);
        if (function.equals("byte_array"))
            return new EntryNBTByteArray(path, root, index, objNode, this);
        if (function.equals("short"))
            return new EntryNBTShort(path, root, index, objNode, this);
        if (function.equals("int"))
            return new EntryNBTInt(path, root, index, objNode, this);
        if (function.equals("int_array"))
            return new EntryNBTIntArray(path, root, index, objNode, this);
        if (function.equals("long"))
            return new EntryNBTLong(path, root, index, objNode, this);
        if (function.equals("float"))
            return new EntryNBTFloat(path, root, index, objNode, this);
        if (function.equals("double"))
            return new EntryNBTDouble(path, root, index, objNode, this);

        boolean inverted = false;
        if (function.startsWith(Character.toString(FileHelper.CHAR_INVERT))) {
            inverted = true;
            function = function.substring(1);
        }
        if (function.startsWith("if_"))
            return new PropertyGroupConditional(path, root, index, objNode, this, function.substring(3), inverted);
        throw new BowOverhaulSettingsException("Invalid function name!", path);
    }
}
