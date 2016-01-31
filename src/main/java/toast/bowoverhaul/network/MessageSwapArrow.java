package toast.bowoverhaul.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ItemQuiver;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Message used to swap the active arrow from the client side.
 */
public class MessageSwapArrow implements IMessage
{
	public int quiverSlot;
	public int activeSlot;

    public MessageSwapArrow() {
    }
    public MessageSwapArrow(int quiver, int active) {
    	this.quiverSlot = quiver;
    	this.activeSlot = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
		try {
			this.quiverSlot = buf.readInt();
			this.activeSlot = buf.readByte();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    @Override
    public void toBytes(ByteBuf buf) {
		try {
			buf.writeInt(this.quiverSlot);
			buf.writeByte((byte) this.activeSlot);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    public static class Handler implements IMessageHandler<MessageSwapArrow, IMessage> {
        @Override
        public IMessage onMessage(MessageSwapArrow message, MessageContext ctx) {
        	EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        	ItemStack quiver = player.inventory.getStackInSlot(message.quiverSlot);
	    	if (quiver != null && quiver.getItem() instanceof ItemQuiver) {
	    		InventoryQuiver quiverInventory = new InventoryQuiver(quiver);
	    		quiverInventory.activeSlot = message.activeSlot;
	    		quiverInventory.save();
	    		player.inventory.setInventorySlotContents(message.quiverSlot, quiver);
	    	}
            return null;
        }

    }
}
