package toast.bowoverhaul.entry.nbt;

import net.minecraft.nbt.NBTTagShort;
import toast.bowoverhaul.entry.IPropertyReader;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTShort extends EntryNBTNumber {
    public EntryNBTShort(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(path, root, index, node, loader);
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        int value = FileHelper.getCount(this.values, nbtStats.random);
        nbtStats.addTag(this.name, new NBTTagShort((short) value));
    }
}