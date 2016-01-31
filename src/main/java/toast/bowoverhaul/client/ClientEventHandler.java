package toast.bowoverhaul.client;

import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import toast.bowoverhaul.item.ItemOverhauledBow;
import toast.bowoverhaul.util.EventHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 *
 */
public class ClientEventHandler {

    public ClientEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by EntityPlayerSP.getFOVMultiplier().
     * EntityPlayerSP entity = the player calculating FOV.
     * float fov = the original FOV.
     * float newfov = the resultant FOV.
     *
     * @param event the event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFOVUpdate(FOVUpdateEvent event) {
        if (event.entity.isUsingItem() && event.entity.getItemInUse() != null && event.entity.getItemInUse().getItem() instanceof ItemOverhauledBow) {
            int duration = Math.round(event.entity.getItemInUseDuration() * EventHandler.calculateDrawTimeMult(event.entity));
            float multiplier = duration / (float) ((ItemOverhauledBow) event.entity.getItemInUse().getItem()).drawTimes[2];

            if (multiplier > 1.0F) {
                multiplier = 1.0F;
            }
            else {
                multiplier *= multiplier;
            }

            event.newfov *= 1.0F - multiplier * 0.15F;
        }
    }

}
