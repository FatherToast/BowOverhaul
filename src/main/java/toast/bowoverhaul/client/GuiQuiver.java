package toast.bowoverhaul.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.inventory.ContainerQuiver;
import toast.bowoverhaul.inventory.InventoryQuiver;

/**
 * The gui used for managing a quiver's inventory.
 */
public class GuiQuiver extends GuiContainer
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(BowOverhaul.TEXTURE_PATH + "gui/quiver.png");

    public EntityPlayer thePlayer;
    public InventoryQuiver quiverInventory;
    public int theSlot;

    public GuiQuiver(EntityPlayer player, int slot) {
        super(new ContainerQuiver(player, slot));
        this.thePlayer = player;
        this.quiverInventory = ((ContainerQuiver) this.inventorySlots).inventoryQuiver;
        this.theSlot = slot;
        this.allowUserInput = false;
        this.ySize = 133;
    }

    // Draw the foreground layer for the GuiContainer (everything in front of the items).
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(this.quiverInventory.hasCustomInventoryName() ? this.quiverInventory.getInventoryName() : I18n.format(this.quiverInventory.getInventoryName(), new Object[0]), 8, 6, 4210752);
        this.fontRendererObj.drawString(this.thePlayer.inventory.hasCustomInventoryName() ? this.thePlayer.inventory.getInventoryName() : I18n.format(this.thePlayer.inventory.getInventoryName(), new Object[0]), 8, this.ySize - 96 + 2, 4210752);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.65F);
        this.mc.getTextureManager().bindTexture(GuiQuiver.TEXTURE);

        for (int i = 0; i < 3; i++) {
        	if (this.quiverInventory.getStackInSlot(i) == null && this.quiverInventory.quiverFilters[i] != null) {
				this.drawTexturedModalRect(62 + i * 18, 20, 8, 109, 16, 16);
			}
        }
        if (this.theSlot >= 0 && this.theSlot < 9) {
			this.drawTexturedModalRect(8 + this.theSlot * 18, 109, 8, 109, 16, 16);
        }
        else if (this.theSlot < this.thePlayer.inventory.mainInventory.length) {
        	int x = (this.theSlot - 9) % 9;
        	int y = (this.theSlot - 9) / 9;
			this.drawTexturedModalRect(8 + x * 18, y * 18 + 51, 8, 109, 16, 16);
		}

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiQuiver.TEXTURE);
        int x = this.width - this.xSize >> 1;
        int y = this.height - this.ySize >> 1;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }
}
