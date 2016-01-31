package toast.bowoverhaul.entry.nbt;

import net.minecraft.nbt.NBTTagShort;
import toast.bowoverhaul.entry.IPropertyReader;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTEnchantId extends EntryAbstract {
    /// The name of this tag.
    protected final String name;
    /// The value of this tag.
    protected final short value;

    public EntryNBTEnchantId(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "id");
        this.value = (short) FileHelper.readEnchant(node, path, "value").effectId;
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        nbtStats.addTag(this.name, new NBTTagShort(this.value));
    }
}