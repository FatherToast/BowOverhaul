package toast.bowoverhaul.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.entry.IProperty;
import toast.bowoverhaul.entry.PropertyExternal;
import toast.bowoverhaul.headhitbox.HeadHitbox;
import toast.bowoverhaul.headhitbox.HeadHitboxDefault;
import toast.bowoverhaul.stats.ArrowStats;
import toast.bowoverhaul.stats.BowStats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class FileHelper {
    // The directory for config files.
    public static File CONFIG_DIRECTORY;
    // The main directory for arrow stats.
    public static File ARROW_DIRECTORY;
    // The main directory for bow modifiers.
    public static File BOW_DIRECTORY;
    // The directory for head hitboxes.
    public static File HEAD_HITBOX_DIRECTORY;
    // The directory for external nbt functions.
    public static File EXTERNAL_DIRECTORY;

    // The file extention for mob properties files.
    public static final String FILE_EXT = ".json";
    // The json parser object for reading json.
    private static final JsonParser PARSER = new JsonParser();
    // The gson objects for writing json.
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	@SuppressWarnings("unused")
    private static final Gson GSON_COMPACT = new GsonBuilder().disableHtmlEscaping().create();

    // Special characters recognized by the property reader.
    public static final char CHAR_RAND = '~';
    public static final char CHAR_INVERT = '!';

    public static final HashSet<String> VANILLA_ENTITY_IDS = new HashSet<String>(Arrays.asList(
    		"Item", "XPOrb", "LeashKnot", "Painting", "Arrow", "Snowball", "Fireball", "SmallFireball", "ThrownEnderpearl",
    		"EyeOfEnderSignal", "ThrownPotion", "ThrownExpBottle", "ItemFrame", "WitherSkull", "PrimedTnt", "FallingSand",
    		"FireworksRocketEntity", "Boat", "MinecartRideable", "MinecartChest", "MinecartFurnace", "MinecartTNT", "MinecartHopper",
    		"MinecartSpawner", "MinecartCommandBlock", "Mob", "Monster", "Creeper", "Skeleton", "Spider", "Giant", "Zombie",
    		"Slime", "Ghast", "PigZombie", "Enderman", "CaveSpider", "Silverfish", "Blaze", "LavaSlime", "EnderDragon", "WitherBoss",
    		"Bat", "Witch", "Pig", "Sheep", "Cow", "Chicken", "Squid", "Wolf", "MushroomCow", "SnowMan", "Ozelot", "VillagerGolem",
    		"EntityHorse", "Villager", "EnderCrystal"
		));

    // Initializes this file helper.
    public static void init(File directory) {
        FileHelper.CONFIG_DIRECTORY = new File(directory, "BowOverhaul");
        FileHelper.ARROW_DIRECTORY = new File(FileHelper.CONFIG_DIRECTORY, "arrows");
        FileHelper.ARROW_DIRECTORY.mkdirs();
        FileHelper.BOW_DIRECTORY = new File(FileHelper.CONFIG_DIRECTORY, "bows");
        FileHelper.BOW_DIRECTORY.mkdirs();
        FileHelper.HEAD_HITBOX_DIRECTORY = new File(FileHelper.CONFIG_DIRECTORY, "head_hitboxes");
        FileHelper.HEAD_HITBOX_DIRECTORY.mkdirs();
        FileHelper.EXTERNAL_DIRECTORY = new File(FileHelper.CONFIG_DIRECTORY, "external");
        FileHelper.EXTERNAL_DIRECTORY.mkdirs();
    }

    // Loads all mob properties into memory.
    public static int load() {
        int filesLoaded = 0;
        File externalDir;
        for (String type : new String[] { "arrow", "bow", "nbt" }) {
        	externalDir = new File(FileHelper.EXTERNAL_DIRECTORY, type);
        	externalDir.mkdirs();
        	filesLoaded += FileHelper.loadExternalDirectory(type, externalDir);
        }
        FileHelper.ARROW_DIRECTORY.mkdirs();
        filesLoaded += FileHelper.loadArrowDirectory(FileHelper.ARROW_DIRECTORY);
        FileHelper.BOW_DIRECTORY.mkdirs();
        filesLoaded += FileHelper.loadBowDirectory(FileHelper.BOW_DIRECTORY);
        FileHelper.HEAD_HITBOX_DIRECTORY.mkdirs();
        filesLoaded += FileHelper.loadHeadHitboxDirectory(FileHelper.HEAD_HITBOX_DIRECTORY);
        return filesLoaded;
    }

    // Recursively loads the external functions in the given directory.
    private static int loadExternalDirectory(String type, File directory) {
        int filesLoaded = 0;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            PropertyExternal.load(type, propFile.getPath(), propFile.getName(), FileHelper.loadJsonFile(propFile));
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadExternalDirectory(type, subDirectory);
        }
        return filesLoaded;
    }

    // Recursively loads the arrow stats in the given directory.
    private static int loadArrowDirectory(File directory) {
        int filesLoaded = 0;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            ArrowStats.load(propFile.getPath(), FileHelper.loadJsonFile(propFile));
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadArrowDirectory(subDirectory);
        }
        return filesLoaded;
    }

    // Recursively loads the arrow stats in the given directory.
    private static int loadBowDirectory(File directory) {
        int filesLoaded = 0;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            BowStats.load(propFile.getPath(), FileHelper.loadJsonFile(propFile));
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadBowDirectory(subDirectory);
        }
        return filesLoaded;
    }

    // Recursively loads the head hitboxes in the given directory.
    private static int loadHeadHitboxDirectory(File directory) {
        int filesLoaded = 0;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            HeadHitbox.load(propFile.getPath(), FileHelper.loadJsonFile(propFile));
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadHeadHitboxDirectory(subDirectory);
        }
        return filesLoaded;
    }

    // Loads a file as a Json node object. Throws an exception if it fails.
    private static JsonObject loadJsonFile(File propFile) {
        JsonElement node = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(propFile)));
            node = FileHelper.PARSER.parse(in);
            in.close();
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Error reading file! (See \"Caused by:\" below for more info.)", propFile.getPath(), ex);
        }
        if (node == null)
            throw new BowOverhaulSettingsException("Failed to read file!", propFile.getPath());
        if (!node.isJsonObject())
            throw new BowOverhaulSettingsException("Invalid file! (non-object)", propFile.getPath());
        return node.getAsJsonObject();
    }

    // Loads a file as an NBT object. Throws an exception if it fails.
	@SuppressWarnings("unused")
	private static NBTTagCompound loadNBTFile(File nbtFile) {
        try {
            if (nbtFile.exists())
				return CompressedStreamTools.readCompressed(new FileInputStream(nbtFile));
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Error reading nbt file!", nbtFile.getPath(), ex);
        }
        throw new BowOverhaulSettingsException("Failed to read nbt file!", nbtFile.getPath());
    }

    // Loads an uncompressed file as an NBT object. Throws an exception if it fails.
	@SuppressWarnings("unused")
    private static NBTTagCompound loadUncompressedNBTFile(File nbtFile) {
        try {
            if (nbtFile.exists())
				return CompressedStreamTools.read(new DataInputStream(new FileInputStream(nbtFile)));
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Error reading uncompressed nbt file!", nbtFile.getPath(), ex);
        }
        throw new BowOverhaulSettingsException("Failed to read uncompressed nbt file!", nbtFile.getPath());
    }

    // Loads a file as a Json node object. Throws an exception if it fails.
    public static JsonObject loadJsonString(String path, String file) {
        JsonElement node = null;
        try {
            node = FileHelper.PARSER.parse(file);
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Error reading Json string! (See \"Caused by:\" below for more info.)", path, ex);
        }
        if (node == null)
            throw new BowOverhaulSettingsException("Failed to read Json string!", path);
        if (!node.isJsonObject())
            throw new BowOverhaulSettingsException("Invalid Json string! (non-object)", path);
        return node.getAsJsonObject();
    }

    // Generates a trivial property file for everything appropriate, if a file does not already exist.
    public static int generateDefaults() {
        int filesGenerated = 0;

        // Generate or load default files, then generate defaults as needed
        JsonObject defaultProp;
        File defaultFile;

        // Arrow stat defaults
        defaultFile = new File(FileHelper.CONFIG_DIRECTORY, "Arrow.json");
        if (!defaultFile.exists()) {
            defaultProp = new JsonObject();
            ArrowStats.populateDefault(defaultProp);
            try {
            	defaultFile.createNewFile();
                FileWriter out = new FileWriter(defaultFile);
                FileHelper.GSON_PRETTY.toJson(defaultProp, out);
                out.close();
                filesGenerated++;
            }
            catch (Exception ex) {
                BowOverhaul.logWarning("Failed to generate the default arrow stats file!");
            }
        }
        else {
            defaultProp = FileHelper.loadJsonFile(defaultFile);
        }
        // Generate
        for (Map.Entry<String, Class> mobEntry : ((Map<String, Class>) EntityList.stringToClassMapping).entrySet()) {
            if (ArrowStats.getArrowStats(mobEntry.getKey()) == null && EntityArrow.class.isAssignableFrom(mobEntry.getValue()) && !Modifier.isAbstract(mobEntry.getValue().getModifiers())) {
            	filesGenerated += FileHelper.generateDefaultArrow(defaultProp, mobEntry.getKey());
            }
        }

        // Bow stat defaults
        defaultFile = new File(FileHelper.CONFIG_DIRECTORY, "Bow.json");
        if (!defaultFile.exists()) {
            defaultProp = new JsonObject();
            BowStats.populateDefault(defaultProp);
            try {
            	defaultFile.createNewFile();
                FileWriter out = new FileWriter(defaultFile);
                FileHelper.GSON_PRETTY.toJson(defaultProp, out);
                out.close();
                filesGenerated++;
            }
            catch (Exception ex) {
                BowOverhaul.logWarning("Failed to generate the default bow stats file!");
            }
        }
        else {
            defaultProp = FileHelper.loadJsonFile(defaultFile);
        }
        // Generate
        Item item;
        for (String itemId : new HashSet<String>(Item.itemRegistry.getKeys())) {
        	item = (Item) Item.itemRegistry.getObject(itemId);
            if (BowStats.getBowStats(item) == null && ItemBow.class.isAssignableFrom(item.getClass())) {
            	filesGenerated += FileHelper.generateDefaultBow(defaultProp, itemId);
            }
        }

        // Head hitbox defaults
        defaultFile = new File(FileHelper.CONFIG_DIRECTORY, "HeadHitbox.json");
        if (!defaultFile.exists()) {
            defaultProp = new JsonObject();
            defaultProp.addProperty("_comment", "This is the default head hitbox file. When this mod generates a default hitbox file for any mob without a custom default hitbox, it will be an auto-formatted copy of this file. Remember, comments (such as this one) will not be copied.");
            HeadHitboxDefault.get(null).generate(defaultProp);
            try {
            	defaultFile.createNewFile();
                FileWriter out = new FileWriter(defaultFile);
                FileHelper.GSON_PRETTY.toJson(defaultProp, out);
                out.close();
                filesGenerated++;
            }
            catch (Exception ex) {
                BowOverhaul.logWarning("Failed to generate the default head hitbox file!");
            }
        }
        else {
            defaultProp = FileHelper.loadJsonFile(defaultFile);
        }
        // Generate
        if (HeadHitbox.getHeadHitbox("Player") == null) {
        	filesGenerated += FileHelper.generateDefaultHeadHitbox(defaultProp, "Player", null);
        }
        for (Map.Entry<String, Class> mobEntry : ((Map<String, Class>) EntityList.stringToClassMapping).entrySet()) {
            if (HeadHitbox.getHeadHitbox(mobEntry.getKey()) == null && EntityLivingBase.class.isAssignableFrom(mobEntry.getValue()) && !Modifier.isAbstract(mobEntry.getValue().getModifiers())) {
            	filesGenerated += FileHelper.generateDefaultHeadHitbox(defaultProp, mobEntry.getKey(), mobEntry.getValue());
            }
        }

        return filesGenerated;
    }
    private static int generateDefaultArrow(JsonObject defaultProp, String entityId) {
        int filesGenerated = 0;
        String fileName;
        File directory, propFile;
        JsonObject props;

    	directory = FileHelper.ARROW_DIRECTORY;
        fileName = entityId;
        String[] split;
        if (FileHelper.VANILLA_ENTITY_IDS.contains(fileName)) {
			split = new String[] { "Minecraft", fileName };
		}
        else {
			split = fileName.split("\\.", 2);
		}
        if (split.length > 1) {
            fileName = split[1];
            char[] dirNameArray = split[0].toCharArray();
            split[0] = "";
            for (char letter : dirNameArray) {
                split[0] += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
            }
            directory = new File(directory, split[0]);
            directory.mkdirs();
        }
        char[] fileNameArray = fileName.toCharArray();
        fileName = "";
        for (char letter : fileNameArray) {
            fileName += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
        }
        try {
            propFile = new File(directory, fileName + FileHelper.FILE_EXT);
            if (propFile.exists()) {
                int attempt = 0;
                for (; attempt < 100; attempt++)
                    if (! (propFile = new File(directory, fileName + attempt + FileHelper.FILE_EXT)).exists()) {
                        break;
                    }
                if (attempt > 99) {
                	BowOverhaul.logWarning("Failed to generate default arrow stats file for \"" + entityId + "\"!");
                    return filesGenerated;
                }
                fileName += attempt;
            }
            props = new JsonObject();
            props.addProperty("_name", entityId);
			for (Map.Entry<String, JsonElement> entry : defaultProp.entrySet()) {
			    if (entry.getKey() != null && !entry.getKey().equals("_name") && !entry.getKey().equals("_comment")) {
			        props.add(entry.getKey(), entry.getValue());
			    }
			}
            propFile.createNewFile();
            FileWriter out = new FileWriter(propFile);
            FileHelper.GSON_PRETTY.toJson(props, out);
            out.close();
            filesGenerated++;
            ArrowStats.load(propFile.getPath(), props);
        }
        catch (BowOverhaulSettingsException ex) {
            throw ex;
        }
        catch (Exception ex) {
        	BowOverhaul.logWarning("Failed to generate default arrow stats file for \"" + entityId + "\"!");
            ex.printStackTrace();
        }
        return filesGenerated;
    }
    private static int generateDefaultBow(JsonObject defaultProp, String itemId) {
        int filesGenerated = 0;
        String fileName;
        File directory, propFile;
        JsonObject props;

    	directory = FileHelper.BOW_DIRECTORY;
        fileName = itemId;
        String[] split = fileName.split(":", 2);
        if (split.length > 1) {
            fileName = split[1];
            char[] dirNameArray = split[0].toCharArray();
            split[0] = "";
            for (char letter : dirNameArray) {
                split[0] += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
            }
            directory = new File(directory, split[0]);
            directory.mkdirs();
        }
        char[] fileNameArray = fileName.toCharArray();
        fileName = "";
        for (char letter : fileNameArray) {
            fileName += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
        }
        try {
            propFile = new File(directory, fileName + FileHelper.FILE_EXT);
            if (propFile.exists()) {
                int attempt = 0;
                for (; attempt < 100; attempt++)
                    if (! (propFile = new File(directory, fileName + attempt + FileHelper.FILE_EXT)).exists()) {
                        break;
                    }
                if (attempt > 99) {
                	BowOverhaul.logWarning("Failed to generate default bow stats file for \"" + itemId + "\"!");
                    return filesGenerated;
                }
                fileName += attempt;
            }
            props = new JsonObject();
            props.addProperty("_name", itemId);
			for (Map.Entry<String, JsonElement> entry : defaultProp.entrySet()) {
			    if (entry.getKey() != null && !entry.getKey().equals("_name") && !entry.getKey().equals("_comment")) {
			        props.add(entry.getKey(), entry.getValue());
			    }
			}
            propFile.createNewFile();
            FileWriter out = new FileWriter(propFile);
            FileHelper.GSON_PRETTY.toJson(props, out);
            out.close();
            filesGenerated++;
            BowStats.load(propFile.getPath(), props);
        }
        catch (BowOverhaulSettingsException ex) {
            throw ex;
        }
        catch (Exception ex) {
        	BowOverhaul.logWarning("Failed to generate default bow stats file for \"" + itemId + "\"!");
            ex.printStackTrace();
        }
        return filesGenerated;
    }
    private static int generateDefaultHeadHitbox(JsonObject defaultProp, String entityId, Class entityClass) {
        int filesGenerated = 0;
        String fileName;
        File directory, propFile;
        JsonObject props;

    	directory = FileHelper.HEAD_HITBOX_DIRECTORY;
        fileName = entityId;
        String[] split;
        if ("Player".equals(fileName) || FileHelper.VANILLA_ENTITY_IDS.contains(fileName)) {
			split = new String[] { "Minecraft", fileName };
		}
        else {
			split = fileName.split("\\.", 2);
		}
        if (split.length > 1) {
            fileName = split[1];
            char[] dirNameArray = split[0].toCharArray();
            split[0] = "";
            for (char letter : dirNameArray) {
                split[0] += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
            }
            directory = new File(directory, split[0]);
            directory.mkdirs();
        }
        char[] fileNameArray = fileName.toCharArray();
        fileName = "";
        for (char letter : fileNameArray) {
            fileName += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
        }
        try {
            propFile = new File(directory, fileName + FileHelper.FILE_EXT);
            if (propFile.exists()) {
                int attempt = 0;
                for (; attempt < 100; attempt++)
                    if (! (propFile = new File(directory, fileName + attempt + FileHelper.FILE_EXT)).exists()) {
                        break;
                    }
                if (attempt > 99) {
                	BowOverhaul.logWarning("Failed to generate default head hitbox file for \"" + entityId + "\"!");
                    return filesGenerated;
                }
                fileName += attempt;
            }
            props = new JsonObject();
            props.addProperty("_name", entityId);
            HeadHitboxDefault customDefault = HeadHitboxDefault.get(entityClass);
            if (customDefault != null) {
            	customDefault.generate(props);
            }
            else {
				for (Map.Entry<String, JsonElement> entry : defaultProp.entrySet()) {
				    if (entry.getKey() != null && !entry.getKey().equals("_name") && !entry.getKey().equals("_comment")) {
				        props.add(entry.getKey(), entry.getValue());
				    }
				}
			}
            propFile.createNewFile();
            FileWriter out = new FileWriter(propFile);
            FileHelper.GSON_PRETTY.toJson(props, out);
            out.close();
            filesGenerated++;
            HeadHitbox.load(propFile.getPath(), props);
        }
        catch (BowOverhaulSettingsException ex) {
            throw ex;
        }
        catch (Exception ex) {
        	BowOverhaul.logWarning("Failed to generate default head hitbox file for \"" + entityId + "\"!");
            ex.printStackTrace();
        }
        return filesGenerated;
    }

    // Makes sure that only defined fields are being used.
    public static void verify(JsonObject node, String path, IVerifiable verifiable) {
        List<String> required = Arrays.asList(verifiable.getRequiredFields());
        List<String> optional = Arrays.asList(verifiable.getOptionalFields());
        HashSet<String> allowed = new HashSet<String>();
        allowed.addAll(required);
        allowed.addAll(optional);
        allowed.add("_comment");
        if (verifiable instanceof IProperty) {
			allowed.add("function");
		}

        try {
            Set<Map.Entry<String, JsonElement>> fields = node.entrySet();
            HashSet<String> fieldNames = new HashSet<String>();
            for (Map.Entry<String, JsonElement> entry : fields) {
                fieldNames.add(entry.getKey());
            }

            for (String name : required)
                if (!fieldNames.contains(name))
                    throw new BowOverhaulSettingsException("Verify error! Missing required field \"" + name + "\". (Required fields: " + Arrays.toString(verifiable.getRequiredFields()) + ")", path);
            for (String name : fieldNames)
                if (!allowed.contains(name))
                    throw new BowOverhaulSettingsException("Verify error! Invalid field \"" + name + "\". (Allowed fields: " + Arrays.toString(allowed.toArray(new String[0])) + ")", path);
        }
        catch (IllegalStateException ex) {
            throw new BowOverhaulSettingsException("Verify error! Wrong data type. (Object required)", path);
        }
    }

    // Returns a randomized double within the values' range.
    public static double getValue(double[] values) {
        return FileHelper.getCount(values, BowOverhaul.random);
    }
    public static double getValue(double[] values, Random random) {
        if (values[0] == values[1])
            return values[0];
        return random.nextDouble() * (values[1] - values[0]) + values[0];
    }
    // Returns a randomized integer within the counts' range.
    public static int getCount(double[] counts) {
        return FileHelper.getCount(counts, BowOverhaul.random);
    }
    public static int getCount(double[] counts, Random random) {
        double count = FileHelper.getValue(counts, random);
        int intCount = (int) Math.floor(count);
        count -= intCount;
        if (0.0 < count && random.nextDouble() < count) {
            intCount++;
        }
        return intCount;
    }
    // Returns a randomized long within the counts' range.
    public static long getLongCount(double[] counts) {
        return FileHelper.getLongCount(counts, BowOverhaul.random);
    }
    public static long getLongCount(double[] counts, Random random) {
        double count = FileHelper.getValue(counts, random);
        long longCount = (long) Math.floor(count);
        count -= longCount;
        if (0.0 < count && random.nextDouble() < count) {
            longCount++;
        }
        return longCount;
    }

    // Returns the text of the node, or the default.
    public static String readText(JsonObject node, String path, String tag, String defaultValue) {
        try {
            return node.get(tag).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid value for \"" + tag + "\"! (wrong node type)", path);
        }
    }

    // Returns the boolean value of the node, or the default.
    public static boolean readBoolean(JsonObject node, String path, String tag, boolean defaultValue) {
        String text = FileHelper.readText(node, path, tag, Boolean.toString(defaultValue));
        if (text.equals("false"))
            return false;
        else if (text.equals("true"))
            return true;
        throw new BowOverhaulSettingsException("Invalid boolean value! (" + text + ": must be true or false)", path);
    }

    // Reads the line part as a number range.
    public static double[] readCounts(JsonObject node, String path, String tag, int index, double defaultMin, double defaultMax) {
        path += "\\" + tag + "\\entry_" + (index + 1);
        String value;
        try {
            value = node.getAsJsonArray(tag).get(index).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return new double[] { defaultMin, defaultMax };
        }
        catch (IndexOutOfBoundsException ex) {
            throw new BowOverhaulSettingsException("Unexpected error! (array index out of bounds)", path);
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid number range! (wrong node type)", path);
        }
        return FileHelper.readCounts(value, path);
    }
    public static double[] readCounts(JsonObject node, String path, String tag, double defaultMin, double defaultMax) {
        path += "\\" + tag;
        String value;
        try {
            value = node.get(tag).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return new double[] { defaultMin, defaultMax };
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid number range! (wrong node type)", path);
        }
        return FileHelper.readCounts(value, path);
    }
    private static double[] readCounts(String value, String path) {
        double[] counts = new double[2];
        String[] subParts = value.split(Character.toString(FileHelper.CHAR_RAND));
        try {
            if (subParts[0].startsWith("0x")) {
                counts[0] = Integer.parseInt(subParts[0].substring(2), 16);
            }
            else {
                counts[0] = Double.valueOf(subParts[0]).doubleValue();
            }
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid number range! (" + subParts[0].trim() + ")", path);
        }
        if (subParts.length == 1) {
            counts[1] = counts[0];
        }
        else if (subParts.length == 2) {
            try {
                if (subParts[1].startsWith("0x")) {
                    counts[1] = Integer.parseInt(subParts[1].substring(2), 16);
                }
                else {
                    counts[1] = Double.valueOf(subParts[1]).doubleValue();
                }
            }
            catch (Exception ex) {
                throw new BowOverhaulSettingsException("Invalid number range! (" + subParts[1].trim() + ")", path);
            }
        }
        else
            throw new BowOverhaulSettingsException("Invalid number range! (too many \'~\'s)", path);
        if (Double.isNaN(counts[0]) || Double.isNaN(counts[1]) || Double.isInfinite(counts[0]) || Double.isInfinite(counts[1]))
            throw new BowOverhaulSettingsException("Invalid number range! (NaN and Infinity are not allowed)", path);
        if (counts[0] > counts[1]) {
            double temp = counts[0];
            counts[0] = counts[1];
            counts[1] = temp;
        }
        return counts;
    }

    // Reads the object's weight.
    public static int readWeight(JsonObject node, String path, int defaultValue) {
        String value = "";
        try {
            value = node.get("weight").getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new BowOverhaulSettingsException("Unexpected error! (array index out of bounds)", path);
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid number range! (wrong node type)", path);
        }

        try {
            int weight = Integer.parseInt(value);
            if (weight < 0)
                throw new BowOverhaulSettingsException("Invalid weight! (" + value + ": must be non-negative)", path);
            return weight;
        }
        catch (NumberFormatException ex) {
            throw new BowOverhaulSettingsException("Invalid weight! (" + value + ")", path, ex);
        }
    }

    // Reads the line part as an integer array.
    public static int[] readIntArray(JsonObject node, String path, String tag, int... defaultValue) {
        path += "\\" + tag;
        JsonArray arrayValue;
        try {
            arrayValue = node.get(tag).getAsJsonArray();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid integer array! (wrong node type)", path);
        }
        int length = arrayValue.size();
        if (length != defaultValue.length)
            throw new BowOverhaulSettingsException("Invalid integer array length (" + length + ")! (must be " + defaultValue.length + ")", path);

        for (int index = 0; index < length; index++) {
        	defaultValue[index] = FileHelper.readInteger(node, path, arrayValue, index, defaultValue[index]);
        }
        return defaultValue;
    }

    // Reads the line part as an integer.
    public static int readInteger(JsonObject node, String path, String tag, int defaultValue) {
        path += "\\" + tag;
        String value;
        try {
            value = node.get(tag).getAsString();
        }
        catch (NullPointerException ex) {
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid integer! (wrong node type)", path);
        }

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new BowOverhaulSettingsException("Invalid integer! (" + value + ")", path, ex);
        }
    }
    public static int readInteger(JsonObject node, String path, JsonArray array, int index, int defaultValue) {
        path += "\\entry_" + (index + 1);
        String value;
        try {
            value = array.get(index).getAsString();
        }
        catch (NullPointerException ex) {
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid integer array entry! (wrong node type)", path);
        }
        if ("".equals(value))
            return defaultValue;

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new BowOverhaulSettingsException("Invalid integer array entry! (" + value + ")", path, ex);
        }
    }

    // Reads the line part as a float array.
    public static float[] readFloatArray(JsonObject node, String path, String tag, float... defaultValue) {
        path += "\\" + tag;
        JsonArray arrayValue;
        try {
            arrayValue = node.get(tag).getAsJsonArray();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid integer array! (wrong node type)", path);
        }
        int length = arrayValue.size();
        if (length != defaultValue.length)
            throw new BowOverhaulSettingsException("Invalid integer array length (" + length + ")! (must be " + defaultValue.length + ")", path);

        for (int index = 0; index < length; index++) {
        	defaultValue[index] = FileHelper.readFloat(node, path, arrayValue, index, defaultValue[index]);
        }
        return defaultValue;
    }

    // Reads the line part as a float.
    public static float readFloat(JsonObject node, String path, String tag, float defaultValue) {
        path += "\\" + tag;
        String value;
        try {
            value = node.get(tag).getAsString();
        }
        catch (NullPointerException ex) {
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid float! (wrong node type)", path);
        }

        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            throw new BowOverhaulSettingsException("Invalid float! (" + value + ")", path, ex);
        }
    }
    public static float readFloat(JsonObject node, String path, JsonArray array, int index, float defaultValue) {
        path += "\\entry_" + (index + 1);
        String value;
        try {
            value = array.get(index).getAsString();
        }
        catch (NullPointerException ex) {
            return defaultValue;
        }
        catch (Exception ex) {
            throw new BowOverhaulSettingsException("Invalid float array entry! (wrong node type)", path);
        }
        if ("".equals(value))
            return defaultValue;

        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            throw new BowOverhaulSettingsException("Invalid float array entry! (" + value + ")", path, ex);
        }
    }

    // Reads the line and throws an exception if it does not represent a valid item.
    public static Item readItem(JsonObject node, String path, String tag) {
        return FileHelper.readItem(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid item.
    public static Item readItem(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readItem(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid item.
    public static Item readItem(String id, String path, boolean required) {
        Item item = (Item) Item.itemRegistry.getObject(id);

        // Compatibility with old numerical ids.
        if (item == null) {
            try {
                item = Item.getItemById(Integer.parseInt(id));
                if (item != null) {
                	BowOverhaul.logWarning("Usage of numerical item id! (" + id + "=\"" + Item.itemRegistry.getNameForObject(item) + "\") at " + path);
                }
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
        }

        if (required && item == null)
            throw new BowOverhaulSettingsException("Missing or invalid item! (" + id + ")", path);
        return item;
    }

    // Reads the line and throws an exception if it does not represent a valid block.
    public static Block readBlock(JsonObject node, String path, String tag) {
        return FileHelper.readBlock(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid block.
    public static Block readBlock(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readBlock(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid block.
    public static Block readBlock(String id, String path, boolean required) {
    	if (id.equals("air") || id.equals("minecraft:air"))
    		return Blocks.air;

        Block block = (Block) Block.blockRegistry.getObject(id);
        if (block == null || block == Blocks.air) {
            try {
                block = Block.getBlockById(Integer.parseInt(id));
                if (block != null && block != Blocks.air) {
                	BowOverhaul.logWarning("Usage of numerical block id! (" + id + "=\"" + Block.blockRegistry.getNameForObject(block) + "\") at " + path);
                }
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
        }
        if (required && (block == null || block == Blocks.air))
            throw new BowOverhaulSettingsException("Missing or invalid block! (" + id + ")", path);
        return block;
    }

    // Reads the line and throws an exception if it does not represent a valid potion.
    public static Potion readPotion(JsonObject node, String path, String tag) {
        return FileHelper.readPotion(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid potion.
    public static Potion readPotion(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readPotion(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid potion.
    public static Potion readPotion(String id, String path, boolean required) {
        Potion potion = null;
    	for (Potion potionType : Potion.potionTypes) {
    		if (potionType != null && id.equals(potionType.getName())) {
    			potion = potionType;
    			break;
    		}
    	}
    	if (potion == null) {
    		try {
    			potion = Potion.potionTypes[Integer.parseInt(id)];
                if (potion != null) {
                	BowOverhaul.logWarning("Usage of numerical potion id! (" + id + "=\"" + potion.getName() + "\") at " + path);
                }
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                // Do nothing
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
    	}
        if (required && potion == null)
			throw new BowOverhaulSettingsException("Missing or invalid potion! (" + id + ")", path);
        return potion;
    }

    // Reads the line and throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(JsonObject node, String path, String tag) {
        return FileHelper.readEnchant(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readEnchant(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(String id, String path, boolean required) {
    	Enchantment enchant = null;
    	for (Enchantment enchantType : Enchantment.enchantmentsList) {
    		if (enchantType != null && id.equals(enchantType.getName())) {
    			enchant = enchantType;
    			break;
    		}
    	}
    	if (enchant == null) {
    		try {
    			enchant = Enchantment.enchantmentsList[Integer.parseInt(id)];
                if (enchant != null) {
                	BowOverhaul.logWarning("Usage of numerical enchantment id! (" + id + "=\"" + enchant.getName() + "\") at " + path);
                }
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                // Do nothing
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
    	}
        if (required && enchant == null)
			throw new BowOverhaulSettingsException("Missing or invalid enchantment! (" + id + ")", path);
        return enchant;
    }

    /// All the file filters used.
    public static class ExtensionFilter implements FilenameFilter {
        // The file extension to accept.
        private final String extension;

        public ExtensionFilter(String ext) {
            this.extension = ext;
        }

        // Returns true if the file should be accepted.
        @Override
        public boolean accept(File file, String name) {
            return name.toLowerCase().endsWith(this.extension);
        }
    }
    public static class FolderFilter implements FileFilter {
        public FolderFilter() {
        }

        /// Returns true if the file should be accepted.
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }
}
