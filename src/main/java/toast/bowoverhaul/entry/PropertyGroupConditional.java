package toast.bowoverhaul.entry;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.EnumDifficulty;
import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.stats.ArrowStatsInstance;
import toast.bowoverhaul.util.FileHelper;

import com.google.gson.JsonObject;

public class PropertyGroupConditional extends PropertyGroup {
    // The method to call for difficulty checks, if found.
    private static Method worldDifficultyMethod;

    static {
        try {
            PropertyGroupConditional.worldDifficultyMethod = Class.forName("toast.apocalypse.WorldDifficultyManager").getMethod("getWorldDifficulty");
            BowOverhaul.log("Successfully hooked into Apocalypse's world difficulty!");
        }
        catch (Exception ex) {
            // Do nothing
        }
    }

    // Returns true if the category can be executed.
    public static boolean isCategoryActive(boolean invert, String category, ArrowStatsInstance statsInstance, Entity shooter, ItemStack bow) {
    	Entity arrow = statsInstance.theArrow;
        if (category.startsWith("damage_is_")) {
            try {
                return (bow != null && bow.getItemDamage() == Integer.parseInt(category.substring(10))) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("damage_over_")) {
            try {
                return (bow != null && bow.getItemDamage() > Integer.parseInt(category.substring(10))) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

    	if (category.startsWith("shot_by_")) {
            try {
                return (shooter != null && ((Class) EntityList.stringToClassMapping.get(category.substring(10))).isAssignableFrom(shooter.getClass())) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("on_ground"))
            return (shooter != null && shooter.onGround) != invert;
        if (category.equals("burning"))
            return (shooter != null && shooter.isBurning()) != invert;
        if (category.equals("wet"))
            return (shooter != null && shooter.isWet()) != invert;
        if (category.equals("submerged"))
            return (shooter instanceof EntityLivingBase && shooter.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier((EntityLivingBase) shooter)) != invert;
        if (category.startsWith("has_potion_")) {
            try {
                return (shooter instanceof EntityLivingBase && ((EntityLivingBase) shooter).isPotionActive(FileHelper.readPotion(category.substring(11), shooter.getClass().getName(), true))) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("check_nbt_")) {
            try {
                String[] path = category.substring(10).split("/");
                String[] data = PropertyGroupConditional.getOperatorData(path[path.length - 1]);
                path[path.length - 1] = data[0];

                return (shooter != null && PropertyGroupConditional.compareNBT(shooter, path, data)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("wither_skeleton"))
            return (shooter instanceof EntitySkeleton && ((EntitySkeleton) shooter).getSkeletonType() == 1) != invert;
        if (category.startsWith("on_block_")) {
            try {
                Block block = FileHelper.readBlock(category.substring(9), shooter.getClass().getName(), true);
                int x = (int) Math.floor(shooter.posX);
                int yMin = (int) Math.floor(shooter.posY) - 1;
                int yMax = (int) Math.floor(shooter.posY) + (int) Math.floor(shooter.height);
                int z = (int) Math.floor(shooter.posZ);
                for (int y = yMin; y <= yMax; y++)
                    if (shooter.worldObj.getBlock(x, y, z) == block != invert)
                        return true;
                return invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("below_")) {
            try {
                return (int) shooter.posY < Integer.parseInt(category.substring(6)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.equals("raining"))
            return arrow.worldObj.isRaining() != invert;
        if (category.equals("thundering"))
            return arrow.worldObj.isThundering() != invert;
        if (category.equals("can_see_sky"))
            return arrow.worldObj.canBlockSeeTheSky((int) Math.floor(arrow.posX), (int) Math.floor(arrow.posY), (int) Math.floor(arrow.posZ)) != invert;
        if (category.startsWith("moon_phase_"))
            return arrow.worldObj.provider.getMoonPhase(arrow.worldObj.getWorldTime()) == PropertyGroupConditional.getMoonPhaseId(category.substring(11)) != invert;
        if (category.startsWith("beyond_")) {
            try {
                double distance = Double.parseDouble(category.substring(7));
                ChunkCoordinates spawnPoint = arrow.worldObj.getSpawnPoint();
                return arrow.getDistanceSq(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ) > distance * distance != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("difficulty_"))
            return arrow.worldObj.difficultySetting == PropertyGroupConditional.getDifficulty(category.substring(11)) != invert;
        if (category.startsWith("past_world_difficulty_")) {
            try {
                long difficulty = (long) (Double.parseDouble(category.substring(22)) * 24000L);
                if (PropertyGroupConditional.worldDifficultyMethod == null) {
                    category = "past_world_time_" + difficulty;
                }
                else
                    return ((Long) PropertyGroupConditional.worldDifficultyMethod.invoke(null)).longValue() > difficulty != invert;
            }
            catch (Exception ex) {
                return invert;
            }
        }
        if (category.startsWith("past_day_time_")) {
            try {
                return (int) (arrow.worldObj.getWorldInfo().getWorldTime() % 24000L) > Integer.parseInt(category.substring(14)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("past_world_time_")) {
            try {
                return arrow.worldObj.getWorldInfo().getWorldTime() > Long.parseLong(category.substring(16)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("in_dimension_")) {
            try {
                return arrow.dimension == Integer.parseInt(category.substring(13)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("in_biome_")) {
            try {
                return arrow.worldObj.getBiomeGenForCoords((int) Math.floor(arrow.posX), (int) Math.floor(arrow.posZ)).biomeID == Integer.parseInt(category.substring(9)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.startsWith("player_online_")) {
            try {
                return arrow.worldObj.getPlayerEntityByName(category.substring(14)) != null != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        throw new RuntimeException("[ERROR] Conditional property has invalid condition! for " + arrow.getClass().getName());
    }

    // Returns a string array with the path end for the left-hand operand, the operator string, and the right-hand operand.
    private static String[] getOperatorData(String last) {
        String[] operators = { "==", ">", "<", ">=", "<=" };
        String[] split;
        for (String operator : operators) {
            split = last.split(operator, 2);
            if (split.length == 2)
                return new String[] { split[0], operator, split[1] };
        }
        return new String[] { last, null, null };
    }

    // Compares the actual value given by the NBT path to a value, based on the operator string in the data array.
    private static boolean compareNBT(Entity entity, String[] path, String[] data) {
        // Step to tag at the end, tag becomes null if the target does not exist
        NBTBase tag = new NBTTagCompound();
        entity.writeToNBT((NBTTagCompound) tag);
        for (String pathStep : path) {
            if (tag instanceof NBTTagCompound) {
                if (!((NBTTagCompound) tag).hasKey(pathStep)) {
                    tag = null;
                    break;
                }
                tag = ((NBTTagCompound) tag).getTag(pathStep);
            }
            else if (tag instanceof NBTTagList) {
                int index = Integer.parseInt(pathStep);
                if (((NBTTagList) tag).tagCount() <= index) {
                    tag = null;
                    break;
                }
                // Only way to directly get an element from the list
                // The entity is not read from the tag again, anyway
                tag = ((NBTTagList) tag).removeTag(index);
            }
            else
                return false;
        }

        // Compare the actual to the value
        if (data[1] == null) // boolean check
            return tag != null && ((NBTBase.NBTPrimitive) tag).func_150290_f() == 1;
        double value;
        try {
        	value = Double.parseDouble(data[2]);
        }
        catch (NumberFormatException ex) { // String check
        	return data[1].equals("==") && data[2].equals(((NBTTagString) tag).func_150285_a_());
        }
        double actual;
        if (tag == null) {
            actual = 0.0;
        }
        else {
            actual = ((NBTBase.NBTPrimitive) tag).func_150286_g();
        }

        if (data[1].equals("=="))
            return actual == value;
        if (data[1].equals(">"))
            return actual > value;
        if (data[1].equals("<"))
            return actual < value;
        if (data[1].equals(">="))
            return actual >= value;
        if (data[1].equals("<="))
            return actual <= value;
        return false;
    }

    // Returns the moon phase id from the given string.
    private static int getMoonPhaseId(String phase) {
        if (phase.equalsIgnoreCase("FULL"))
            return 0;
        if (phase.equalsIgnoreCase("WANING_GIBBOUS"))
            return 1;
        if (phase.equalsIgnoreCase("THIRD_QUARTER") || phase.equalsIgnoreCase("WANING_HALF"))
            return 2;
        if (phase.equalsIgnoreCase("WANING_CRESCENT"))
            return 3;
        if (phase.equalsIgnoreCase("NEW"))
            return 4;
        if (phase.equalsIgnoreCase("WAXING_CRESCENT"))
            return 5;
        if (phase.equalsIgnoreCase("FIRST_QUARTER") || phase.equalsIgnoreCase("WAXING_HALF"))
            return 6;
        if (phase.equalsIgnoreCase("WAXING_GIBBOUS"))
            return 7;
        try {
            return Integer.parseInt(phase) % 8;
        }
        catch (Exception ex) {
            return -1;
        }
    }

    // Parses the world difficulty from a string.
    private static EnumDifficulty getDifficulty(String id) {
        if (id.equalsIgnoreCase("PEACEFUL"))
            return EnumDifficulty.PEACEFUL;
        else if (id.equalsIgnoreCase("EASY"))
            return EnumDifficulty.EASY;
        else if (id.equalsIgnoreCase("NORMAL"))
            return EnumDifficulty.NORMAL;
        else if (id.equalsIgnoreCase("HARD"))
            return EnumDifficulty.HARD;
        else {
            try {
                return EnumDifficulty.getDifficultyEnum(Integer.parseInt(id));
            }
            catch (Exception ex) {
                return null;
            }
        }
    }

    // True if this should only execute when its category would not.
    private final boolean inverted;
    // The category name. Used to check if this property should execute.
    private final String category;

    public PropertyGroupConditional(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader, String function, boolean invert) {
        super(path, root, index, node, loader);
        this.inverted = invert;
        this.category = function;
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, nbtStats.parent, nbtStats.theShooter, nbtStats.theBow)) {
            super.addTags(nbtStats);
        }
    }
}
