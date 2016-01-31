package toast.bowoverhaul.client;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderLivingEvent;

import org.lwjgl.opengl.GL11;

import toast.bowoverhaul.BowOverhaul;
import toast.bowoverhaul.headhitbox.HeadHitbox;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Debug hitbox renderer.
 */
@SideOnly(Side.CLIENT)
public class RenderHeadHitbox
{
	public RenderHeadHitbox() {}

	/**
     * Called by RenderPlayer.doRender().
     * EntityPlayer entityPlayer = the player being rendered.
     * RenderPlayer renderer = the render object.
     * float partialRenderTick = client partial ticks.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void afterRenderEntity(RenderLivingEvent.Post event) {
        if (BowOverhaul.debug && RenderManager.instance.renderEngine != null) {
			this.renderHeadHitbox(event.entity, event.x, event.y, event.z);
        }
    }

    public void renderHeadHitbox(EntityLivingBase entity, double x, double y, double z) {
    	HeadHitbox headHitbox = HeadHitbox.getHeadHitbox(entity);
    	if (entity == null || headHitbox == null)
    		return;

        GL11.glLineWidth(4.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        AxisAlignedBB hitbox = headHitbox.getBoundingBox(entity).offset(x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
        RenderGlobal.drawOutlinedBoundingBox(hitbox, 0xff0000);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glLineWidth(2.0F);
    }
}
