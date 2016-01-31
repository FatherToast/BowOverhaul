package toast.bowoverhaul.entry;

import toast.bowoverhaul.entry.nbt.NBTStatsInfo;
import toast.bowoverhaul.util.IVerifiable;

public interface IProperty extends IVerifiable
{
    /// Adds any NBT tags to the list.
    public void addTags(NBTStatsInfo nbtStats);
}
