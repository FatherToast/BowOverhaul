package toast.bowoverhaul.client;

import java.util.ArrayDeque;

import net.minecraft.entity.Entity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 *
 */
public class ClientTickHandler
{
    // Stack of events that need to be executed next tick.
    public static final ArrayDeque<DelayedEvent> eventStack = new ArrayDeque<DelayedEvent>();

    public ClientTickHandler() {
        FMLCommonHandler.instance().bus().register(this);
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
    public void onClientTick(TickEvent.ClientTickEvent event) {
    	/* Does not work yet
        if (event.phase == TickEvent.Phase.START) {
            if (!ClientTickHandler.eventStack.isEmpty()) {
                DelayedEvent delayedEvent;
                while (!ClientTickHandler.eventStack.isEmpty()) {
                	delayedEvent = ClientTickHandler.eventStack.pollFirst();
                    if (delayedEvent != null) {
                    	delayedEvent.execute();
                    }
                }
            }
        }
        */
    }

    public static void markForDelayedMotionY(Entity entity) {
        ClientTickHandler.eventStack.add(new DelayedMotionY(entity));
    }

    private static interface DelayedEvent {
    	void execute();
    }

    private static class DelayedMotionY implements DelayedEvent {

    	public final Entity theEntity;
    	public final double motionY;

    	public DelayedMotionY(Entity entity) {
    		this.theEntity = entity;
    		this.motionY = entity.motionY;
    		entity.motionY = 0.0;
    		entity.onGround = false;
    	}

		@Override
		public void execute() {
			this.theEntity.motionY = this.motionY;
			this.theEntity.onGround = false;
		}
    }
}
