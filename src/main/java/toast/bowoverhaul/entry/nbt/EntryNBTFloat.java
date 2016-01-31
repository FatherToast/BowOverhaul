package toast.bowoverhaul.entry.nbt;

import net.minecraft.nbt.NBTTagFloat;
import toast.bowoverhaul.entry.IPropertyReader;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTFloat extends EntryNBTNumber {
    public EntryNBTFloat(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(path, root, index, node, loader);
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        double value = FileHelper.getValue(this.values, nbtStats.random);
        nbtStats.addTag(this.name, new NBTTagFloat((float) value));
    }
}