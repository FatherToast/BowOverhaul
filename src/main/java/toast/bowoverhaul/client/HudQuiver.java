package toast.bowoverhaul.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import toast.bowoverhaul.inventory.InventoryQuiver;
import toast.bowoverhaul.item.ItemOverhauledBow;
import toast.bowoverhaul.item.ammo.AmmoData;
import toast.bowoverhaul.util.ItemStackAndSlot;
import toast.bowoverhaul.util.Properties;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * The heads-up display for ammo data.
 */
public class HudQuiver extends Gui
{
    public static final int ENABLED = HudQuiver.getEnabled();

    public static final int POSITION_X = HudQuiver.getPositionX();
    public static final int POSITION_Y = HudQuiver.getPositionY();
    public static final int OFFSET_X = Properties.getInt(Properties.QUIVER_HUD, "offset_h") * (HudQuiver.POSITION_X == 1 ? -1 : 1);
    public static final int OFFSET_Y = Properties.getInt(Properties.QUIVER_HUD, "offset_v") * (HudQuiver.POSITION_Y == 1 ? -1 : 1);

    public static final boolean VERTICAL_BAR = Properties.getBoolean(Properties.QUIVER_HUD, "vertical_bar");

    private static final RenderItem itemRender = new RenderItem();
    /** The client this is registered in. */
    private final Minecraft mc = FMLClientHandler.instance().getClient();

    private static int getEnabled() {
        String pos = Properties.getString(Properties.QUIVER_HUD, "_enabled");
        if (pos.equalsIgnoreCase("TRUE"))
            return 1;
        else if (pos.equalsIgnoreCase("FALSE"))
            return 0;
        return 2;
    }
    private static int getPositionX() {
        String pos = Properties.getString(Properties.QUIVER_HUD, "position_h");
        if (pos.equalsIgnoreCase("LEFT"))
            return 0;
        else if (pos.equalsIgnoreCase("RIGHT"))
            return 1;
        else if (pos.equalsIgnoreCase("CENTER"))
            return 2;
        return -1;
    }
    private static int getPositionY() {
        String pos = Properties.getString(Properties.QUIVER_HUD, "position_v");
        if (pos.equalsIgnoreCase("TOP"))
            return 0;
        else if (pos.equalsIgnoreCase("BOTTOM"))
            return 1;
        else if (pos.equalsIgnoreCase("CENTER"))
            return 2;
        return -1;
    }

    /**
     * Called by GuiIngame.____().
     * float partialTicks = the partial render tick
     * ScaledResolution resolution = resolution of the game
     * int mouseX = the current x position of the mouse
     * int mouseY = the current y position of the mouse
     * RenderGameOverlayEvent.ElementType type = the type of overlay being rendered
     *
     * @param event The event being triggered
	 */
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void afterRenderGameOverlay(RenderGameOverlayEvent.Post event) {
	    if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || HudQuiver.ENABLED == 0)
	        return;

	    ItemStack bow = this.mc.thePlayer.getHeldItem();
	    if (bow != null && !(bow.getItem() instanceof ItemOverhauledBow)) {
			bow = null; // This must be an overhauled bow
		}
	    if (bow == null && HudQuiver.ENABLED == 2)
	    	return;

	    ItemStackAndSlot ammo = AmmoData.getAmmoStack(bow, this.mc.thePlayer);
	    if (ammo == null)
	    	return;

	    ItemStack quiver;
    	if (ammo.quiverSlot >= 0) { // The current ammo is in a quiver
    		quiver = this.mc.thePlayer.inventory.getStackInSlot(ammo.quiverSlot);
    	}
    	else {
    		quiver = null;
    	}

        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();
        int barWidth = quiver != null && !HudQuiver.VERTICAL_BAR ? 62 : 22;
        int barHeight = quiver != null && HudQuiver.VERTICAL_BAR ? 62 : 22;

        int x, y;
        switch (HudQuiver.POSITION_X) {
            case 0:
                x = 2;
                break;
            case 1:
                x = width - barWidth - 2;
                break;
            case 2:
                x = width - barWidth >> 1;
                break;
            default:
                return;
        }
        switch (HudQuiver.POSITION_Y) {
            case 0:
                y = 2;
                break;
            case 1:
                y = height - barHeight - 2;
                break;
            case 2:
                y = height - barHeight >> 1;
                break;
            default:
                return;
        }
        x += HudQuiver.OFFSET_X;
        y += HudQuiver.OFFSET_Y;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiQuiver.TEXTURE);

        if (quiver != null) {
        	InventoryQuiver quiverInventory = new InventoryQuiver(quiver);

        	int u = HudQuiver.VERTICAL_BAR ? 0 : 24;
        	int unitX = HudQuiver.VERTICAL_BAR ? 0 : 1;
        	int unitY = HudQuiver.VERTICAL_BAR ? 1 : 0;
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        	this.drawTexturedModalRect(x, y, u, 157, barWidth, barHeight);
        	this.drawTexturedModalRect(x + 20 * quiverInventory.activeSlot * unitX - 1, y + 20 * quiverInventory.activeSlot * unitY - 1, u, 133, 24, 24);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

        	ItemStack itemStack;
        	for (int i = 0; i < quiverInventory.getSizeInventory(); i++) {
        		itemStack = quiverInventory.getStackInSlot(i);
        		if (itemStack != null) {
        	        this.drawItemStack(itemStack, x + 20 * i * unitX + 3, y + 20 * i * unitY + 3, event.partialTicks);
        		}
        	}
        }
        else {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

        	this.drawItemStack(ammo.itemStack, x + 3, y + 3, event.partialTicks);
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

    private void drawItemStack(ItemStack itemStack, int x, int y, float partialTicks) {
        FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
        if (font == null) {
			font = this.mc.fontRenderer;
		}

    	float animationOffset = itemStack.animationsToGo - partialTicks;

	    if (animationOffset > 0.0F) {
	        GL11.glPushMatrix();
	        float f2 = 1.0F + animationOffset / 5.0F;
	        GL11.glTranslatef(x + 8, y + 12, 0.0F);
	        GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
	        GL11.glTranslatef(-(x + 8), -(y + 12), 0.0F);
	    }

	    HudQuiver.itemRender.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemStack, x, y);

	    if (animationOffset > 0.0F) {
	        GL11.glPopMatrix();
	    }

	    HudQuiver.itemRender.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), itemStack, x, y);
    }

}
