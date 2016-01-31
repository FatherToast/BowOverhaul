package toast.bowoverhaul.entry;

import toast.bowoverhaul.entry.nbt.EntryAbstract;
import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PropertyGroup extends EntryAbstract {
    // The min and max number of times to perform all tasks.
    private final double[] counts;
    // The entry objects included in this property.
    private final IProperty[] entries;

    public PropertyGroup(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        JsonArray nodes = node.getAsJsonArray("functions");
        if (nodes == null)
            throw new BowOverhaulSettingsException("Missing or invalid functions!", path);

        path += "\\functions";
        int length = nodes.size();
        this.entries = new IProperty[length];
        for (int i = 0; i < length; i++) {
            this.entries[i] = loader.readLine(path, root, i, nodes.get(i));
        }
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "functions" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
                    entry.addTags(nbtStats);
                }
            }
        }
    }
}
