package toast.bowoverhaul.entry;

import java.util.HashMap;

import toast.bowoverhaul.entry.nbt.EntryAbstract;
import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.util.BowOverhaulSettingsException;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PropertyExternal extends EntryAbstract {
    /** Mapping of all loaded external arrow functions to their file name. */
    //private static final HashMap<String, ArrowStatsExternal> ARROW_MAP = new HashMap<String, ArrowStatsExternal>();
    /** Mapping of all loaded external bow functions to their file name. */
    //private static final HashMap<String, BowStatsExternal> BOW_MAP = new HashMap<String, BowStatsExternal>();
    /** Mapping of all loaded external nbt functions to their file name. */
    private static final HashMap<String, NBTStats> NBT_MAP = new HashMap<String, NBTStats>();

    // Unloads all properties.
    public static void unload() {
        //PropertyExternal.ARROW_MAP.clear();
        //PropertyExternal.BOW_MAP.clear();
        PropertyExternal.NBT_MAP.clear();
    }

    // Loads a single external function from Json.
    public static void load(String type, String path, String fileName, JsonObject node) {
        String name = fileName.substring(0, fileName.length() - FileHelper.FILE_EXT.length());
        if ("arrow".equals(type)) {
			PropertyExternal.loadArrow(path, name, node);
		}
        else if ("bow".equals(type)) {
			PropertyExternal.loadBow(path, name, node);
		}
        else if ("nbt".equals(type)) {
			PropertyExternal.loadNbt(path, name, node);
		}
    }
    private static void loadArrow(String path, String name, JsonObject node) {
        //if (PropertyExternal.ARROW_MAP.containsKey(name))
        //    throw new BowOverhaulSettingsException("Duplicate external arrow stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("functions", dummyArray);
        //PropertyExternal.NBT_MAP.put(name, new ArrowStatsExternal(path, dummyRoot, dummyRoot));
    }
    private static void loadBow(String path, String name, JsonObject node) {
        //if (PropertyExternal.BOW_MAP.containsKey(name))
        //    throw new BowOverhaulSettingsException("Duplicate external bow stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("functions", dummyArray);
        //PropertyExternal.NBT_MAP.put(name, new BowStatsExternal(path, dummyRoot, dummyRoot));
    }
    private static void loadNbt(String path, String name, JsonObject node) {
        if (PropertyExternal.NBT_MAP.containsKey(name))
            throw new BowOverhaulSettingsException("Duplicate external nbt stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("nbt", dummyArray);
        PropertyExternal.NBT_MAP.put(name, new NBTStats("nbt", path, dummyRoot, dummyRoot));
    }

    // The min and max number of times to perform the task.
    private final double[] counts;
    // The name of the external function to use.
    private final String externalFunction;

    public PropertyExternal(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        this.externalFunction = FileHelper.readText(node, path, "file", "");
        if (this.externalFunction == "")
            throw new BowOverhaulSettingsException("Missing or invalid external file name!", path);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "file" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        NBTStats stats = PropertyExternal.NBT_MAP.get(this.externalFunction);
        if (stats != null) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                stats.generate(nbtStats);
            }
        }
        else {
            super.addTags(nbtStats);
        }
    }
}
