package toast.bowoverhaul.util;

import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.item.RecipeRemoveQuiver;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class EventHandlerFML
{
    public EventHandlerFML() {
        FMLCommonHandler.instance().bus().register(this);
    }

    /**
     * Called when a player crafts an item in SlotCrafting.onPickupFromSlot().
     * EntityPlayer player = The player crafting the item.
     * ItemStack crafting = The item being crafted.
     * IInventory craftMatrix = The crafting table's inventory.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
    	if (event.crafting != null && event.crafting.getItem() == ItemManager.quiver) {
    		RecipeRemoveQuiver.removeQuiverFromChestplate(event.craftMatrix, event.crafting, event.player);
    	}
    }

    /**
     * Called each tick.
     * TickEvent.Type type = the type of tick.
     * Side side = the side this tick is on.
     * TickEvent.Phase phase = the phase of this tick (START, END).
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
    	// Do nothing
    }
}
