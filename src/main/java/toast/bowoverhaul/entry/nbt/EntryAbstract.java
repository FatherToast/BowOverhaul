package toast.bowoverhaul.entry.nbt;

import toast.bowoverhaul.entry.IProperty;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public abstract class EntryAbstract implements IProperty {

    public EntryAbstract(JsonObject node, String path) {
        FileHelper.verify(node, path, this);
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        throw new UnsupportedOperationException("Non-nbt properties can not modify nbt!");
    }
}
