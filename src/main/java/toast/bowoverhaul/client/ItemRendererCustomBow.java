package toast.bowoverhaul.client;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRendererCustomBow implements IItemRenderer
{
    public static final ItemRendererCustomBow INSTANCE = new ItemRendererCustomBow();

    @Override
    public boolean handleRenderType(ItemStack itemStack, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data) {
        EntityLivingBase entity = (EntityLivingBase) data[1];

        GL11.glPopMatrix(); // Prevents Forge from pre-translating the item
        if(type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            RenderHelper.renderItem(entity, itemStack, 0);
        }
        else {
            GL11.glPushMatrix();

            float scale = 3.0F - 1.0F / 3.0F; // Translate the item from its standard translation and scale the bow
            GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(-0.25F, -0.1875F, 0.1875F);

            scale = 0.625F; // To render the item as 'real' bow
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(scale, -scale, scale);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

            RenderHelper.renderItem(entity, itemStack, 0);

            GL11.glPopMatrix();
        }
        GL11.glPushMatrix();
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack itemStack, ItemRendererHelper helper) {
        return false;
    }
}
