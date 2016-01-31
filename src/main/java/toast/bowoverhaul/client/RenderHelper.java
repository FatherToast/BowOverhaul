package toast.bowoverhaul.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;

/**
 * Contains useful methods for rendering common objects.
 */
public class RenderHelper
{
    public static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public static RenderBlocks BLOCK_RENDERER = new RenderBlocks();

	public static void renderItem(EntityLivingBase entity, ItemStack itemStack, int renderPass) {
        TextureManager texturemanager = FMLClientHandler.instance().getClient().getTextureManager();
        Item item = itemStack.getItem();
        Block block = Block.getBlockFromItem(item);

        GL11.glPushMatrix();
		if (itemStack.getItemSpriteNumber() == 0 && item instanceof ItemBlock && RenderBlocks.renderItemIn3d(block.getRenderType())) {
            texturemanager.bindTexture(texturemanager.getResourceLocation(0));

            if (itemStack != null && block != null && block.getRenderBlockPass() != 0) {
                GL11.glDepthMask(false);
                RenderHelper.BLOCK_RENDERER.renderBlockAsItem(block, itemStack.getItemDamage(), 1.0F);
                GL11.glDepthMask(true);
            }
            else {
                RenderHelper.BLOCK_RENDERER.renderBlockAsItem(block, itemStack.getItemDamage(), 1.0F);
            }
        }
        else {
            IIcon iicon = entity.getItemIcon(itemStack, renderPass);
            if (iicon == null) {
                GL11.glPopMatrix();
                return;
            }

            texturemanager.bindTexture(texturemanager.getResourceLocation(itemStack.getItemSpriteNumber()));
            TextureUtil.func_152777_a(false, false, 1.0F);
            Tessellator tessellator = Tessellator.instance;
            float minU = iicon.getMinU();
            float maxU = iicon.getMaxU();
            float minV = iicon.getMinV();
            float maxV = iicon.getMaxV();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(0.0F, -0.3F, 0.0F);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            ItemRenderer.renderItemIn2D(tessellator, maxU, minV, minU, maxV, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625F);

            if (itemStack.hasEffect(renderPass)) {
                GL11.glDepthFunc(GL11.GL_EQUAL);
                GL11.glDisable(GL11.GL_LIGHTING);
                texturemanager.bindTexture(RenderHelper.RES_ITEM_GLINT);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(768, 1, 1, 0);
                GL11.glColor4f(0.38F, 0.19F, 0.608F, 1.0F);
                GL11.glMatrixMode(GL11.GL_TEXTURE);

                GL11.glPushMatrix();
                GL11.glScalef(0.125F, 0.125F, 0.125F);
                float animOffset = Minecraft.getSystemTime() % 3000L / 3000.0F * 8.0F;
                GL11.glTranslatef(animOffset, 0.0F, 0.0F);
                GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glScalef(0.125F, 0.125F, 0.125F);
                animOffset = Minecraft.getSystemTime() % 4873L / 4873.0F * 8.0F;
                GL11.glTranslatef(-animOffset, 0.0F, 0.0F);
                GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
                ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
                GL11.glPopMatrix();

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            texturemanager.bindTexture(texturemanager.getResourceLocation(itemStack.getItemSpriteNumber()));
            TextureUtil.func_147945_b();
        }
        if (itemStack != null && block != null && block.getRenderBlockPass() != 0) {
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL11.glPopMatrix();
	}

	private RenderHelper() {}
}
