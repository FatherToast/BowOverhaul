package toast.bowoverhaul.client;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ItemOverhauledBow;
import toast.bowoverhaul.item.ItemQuiver;
import toast.bowoverhaul.item.ammo.AmmoData;
import toast.bowoverhaul.network.MessageOpenQuiver;
import toast.bowoverhaul.network.MessageSwapArrow;
import toast.bowoverhaul.util.ItemStackAndSlot;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public class KeyHandler
{
	public static final int KEY_SWAP_ARROW = 0;
	public static final int KEY_OPEN_QUIVER = 1;

	public static final KeyBinding[] KEY_BINDINGS;

	static {
		String category = "key.bow_overhaul.category";
		String[] descriptions = { "key.swapActiveArrow.desc", "key.openQuiver.desc" };
		int[] defaultKeys = { Keyboard.KEY_R, Keyboard.KEY_G };

		KEY_BINDINGS = new KeyBinding[descriptions.length];
		for (int i = 0; i < descriptions.length; i++) {
			KeyHandler.KEY_BINDINGS[i] = new KeyBinding(descriptions[i], defaultKeys[i], category);
		}
	}

	public KeyHandler() {
		for (int i = 0; i < KeyHandler.KEY_BINDINGS.length; i++) {
			ClientRegistry.registerKeyBinding(KeyHandler.KEY_BINDINGS[i]);
		}
        FMLCommonHandler.instance().bus().register(this);
	}

    /**
     * Called when a key is pressed.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
    	if (!FMLClientHandler.instance().isGUIOpen(GuiChat.class)) {
	    	if (KeyHandler.KEY_BINDINGS[KeyHandler.KEY_SWAP_ARROW].isPressed()) {
	    		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();

	    	    ItemStack bow = player.getHeldItem();
	    	    if (bow != null && !(bow.getItem() instanceof ItemOverhauledBow)) {
	    			bow = null; // This must be an overhauled bow
	    		}

	    	    ItemStackAndSlot ammo = AmmoData.getAmmoStack(bow, player);
	    	    if (ammo != null && ammo.quiverSlot >= 0) {
	    	    	ItemStack quiver = player.inventory.getStackInSlot(ammo.quiverSlot);
	    	    	if (quiver != null && quiver.getItem() instanceof ItemQuiver) {
	    	    		InventoryQuiver quiverInventory = new InventoryQuiver(quiver);
	    	    		quiverInventory.activeSlot = (quiverInventory.activeSlot + 1) % quiverInventory.getSizeInventory();
	    	    		quiverInventory.save();

	    	    		BowOverhaul.CHANNEL.sendToServer(new MessageSwapArrow(ammo.quiverSlot, quiverInventory.activeSlot));
	    	    	}
	    	    }
	    	}
	    	else if (KeyHandler.KEY_BINDINGS[KeyHandler.KEY_OPEN_QUIVER].isPressed()) {
	    		EntityClientPlayerMP player = FMLClientHandler.instance().getClientPlayerEntity();

	    	    int quiverSlot = KeyHandler.getFirstQuiver(player.inventory);
	    	    if (quiverSlot >= 0) {
    	    		BowOverhaul.CHANNEL.sendToServer(new MessageOpenQuiver(quiverSlot));
	    	    }
	    	}
    	}
    }

    public static int getFirstQuiver(InventoryPlayer inventory) {
		for (int i = inventory.armorInventory.length; i-- > 0;) {
			if (inventory.armorInventory[i] != null && inventory.armorInventory[i].getItem() instanceof ItemQuiver)
				return i + inventory.mainInventory.length;
		}
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() instanceof ItemQuiver)
				return i;
		}
		return -1;
    }
}
