package toast.bowoverhaul.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import toast.bowoverhaul.item.ItemQuiver;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Message used to swap the active arrow from the client side.
 */
public class MessageOpenQuiver implements IMessage
{
	public int quiverSlot;

    public MessageOpenQuiver() {
    }
    public MessageOpenQuiver(int quiver) {
    	this.quiverSlot = quiver;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
		try {
			this.quiverSlot = buf.readInt();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    @Override
    public void toBytes(ByteBuf buf) {
		try {
			buf.writeInt(this.quiverSlot);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    public static class Handler implements IMessageHandler<MessageOpenQuiver, IMessage> {
        @Override
        public IMessage onMessage(MessageOpenQuiver message, MessageContext ctx) {
        	EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        	ItemStack quiver = player.inventory.getStackInSlot(message.quiverSlot);
	    	if (quiver != null && quiver.getItem() instanceof ItemQuiver) {
	            GuiHelper.displayGUIQuiver(player, message.quiverSlot);
	    	}
            return null;
        }

    }
}
