package toast.bowoverhaul.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Message used to play graphical effects from the server side.
 */
public class MessageFX implements IMessage {

    public static enum EffectType {

        HEADSHOT(0);

        private static final EffectType[] allTypes = new EffectType[EffectType.values().length];

        private final byte TYPE_ID;

        EffectType(int id) {
            this.TYPE_ID = (byte) id;
        }

        /** Actually creates the effect in the world. */
        public void doEffect(WorldClient world, MessageFX message, MessageContext ctx) {
        	switch(this) {
        		case HEADSHOT:
        			float v = 1.5F;
        			for (int i = (int) message.size; i-- > 0;) {
	    				world.spawnParticle("crit", message.posX, message.posY, message.posZ,
	    						(world.rand.nextFloat() - 0.5F) * v, (world.rand.nextFloat() - 0.5F) * v, (world.rand.nextFloat() - 0.5F) * v);
	    			}
        			break;
        	}
        }

        // Returns this type's id.
        public byte getId() {
            return this.TYPE_ID;
        }

        // Returns the effect type with the given id.
        public static EffectType getType(byte id) {
            return EffectType.allTypes[id % EffectType.allTypes.length];
        }

        static {
            // Assign all enum types to an ordered array
            EffectType[] types = EffectType.values();
            int length = types.length;

            for (int i = 0; i < length; i++) {
                EffectType type = types[i];
                EffectType.allTypes[type.getId()] = type;
            }
        }
    }

    // This effect's type.
    public EffectType type;
    // State of the effect, used to define extra data.
    public float state;
    // The size of the effect, or used to define extra data.
    public float size;
    // The effect's coords.
    public double posX, posY, posZ;

    public MessageFX() {
    }
    public MessageFX(EffectType type, double x, double y, double z, float size, float state) {
        this.type = type;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.size = size;
        this.state = state;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
		try {
			this.type = EffectType.getType(buf.readByte());
			this.state = buf.readFloat();
			this.size = buf.readFloat();
			this.posX = buf.readDouble();
			this.posY = buf.readDouble();
			this.posZ = buf.readDouble();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    @Override
    public void toBytes(ByteBuf buf) {
		try {
			buf.writeByte(this.type.getId());
			buf.writeFloat(this.state);
			buf.writeFloat(this.size);
			buf.writeDouble(this.posX);
			buf.writeDouble(this.posY);
			buf.writeDouble(this.posZ);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    public static class Handler implements IMessageHandler<MessageFX, IMessage> {
        @Override
        public IMessage onMessage(MessageFX message, MessageContext ctx) {
            WorldClient world = FMLClientHandler.instance().getWorldClient();
            message.type.doEffect(world, message, ctx);
            return null;
        }

    }
}
