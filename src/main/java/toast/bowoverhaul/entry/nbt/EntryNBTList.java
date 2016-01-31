package toast.bowoverhaul.entry.nbt;

import net.minecraft.nbt.NBTTagList;
import toast.bowoverhaul.entry.NBTStats;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTList extends EntryAbstract {
    /// The name of this tag.
    private final String name;
    /// The entry objects included in this property.
    private final NBTStats nbtStatsObj;

    public EntryNBTList(String path, JsonObject root, JsonObject node) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "");
        this.nbtStatsObj = new NBTStats("tags", path, root, node);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name", "tags" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        NBTTagList tag = new NBTTagList();
        this.nbtStatsObj.generate(nbtStats.theArrow, nbtStats.theBow, tag, nbtStats.parent);
        nbtStats.addTag(this.name, tag);
    }
}