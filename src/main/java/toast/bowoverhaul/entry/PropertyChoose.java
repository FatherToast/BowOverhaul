package toast.bowoverhaul.entry;

import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.entry.nbt.EntryAbstract;
import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PropertyChoose extends EntryAbstract {
    /// The min and max number of times to choose an object.
    private final double[] counts;
    /// The entry objects included in this property.
    private final IProperty[] entries;
    /// The individual weight for each object.
    private final int[] weights;
    /// The total weight of all objects.
    private final int totalWeight;

    public PropertyChoose(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        JsonArray nodes = node.getAsJsonArray("functions");
        if (nodes == null)
            throw new BowOverhaulSettingsException("Missing or invalid functions!", path);

        path += "\\functions";
        String subpath;
        int length = nodes.size();
        this.entries = new IProperty[length];
        this.weights = new int[length];
        JsonElement subnode;
        for (int i = 0; i < length; i++) {
            subnode = nodes.get(i);
            subpath = path + "\\entry_" + (i + 1);
            if (!subnode.isJsonObject())
                throw new BowOverhaulSettingsException("Invalid node (object expected)!", subpath);
            this.weights[i] = FileHelper.readWeight(subnode.getAsJsonObject(), subpath, 1);
            if (this.weights[i] <= 0)
                throw new BowOverhaulSettingsException("Invalid property weight! (" + this.weights[0] + ": must be a positive integer)", path);
            if (subnode.getAsJsonObject().has("function")) {
                this.entries[i] = loader.readLine(path, root, i, subnode);
            }
            else {
                this.entries[i] = null;
            }
        }

        int weight = 0;
        for (int i = length; i-- > 0;) {
            weight += this.weights[i];
        }
        this.totalWeight = weight;
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "functions" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        if (this.totalWeight <= 0)
            return;
        choose: for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            int choice = nbtStats.random.nextInt(this.totalWeight);
            for (int i = this.weights.length; i-- > 0;) {
                if ( (choice -= this.weights[i]) < 0) {
                    if (this.entries[i] != null) {
                        this.entries[i].addTags(nbtStats);
                    }
                    continue choose;
                }
            }
            BowOverhaul.logError("Error choosing weighted item! " + choice + "/" + this.totalWeight);
        }
    }
}
