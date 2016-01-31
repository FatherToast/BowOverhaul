package toast.bowoverhaul.client;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import toast.bowoverhaul.CommonProxy;
import toast.bowoverhaul.entity.EntityOverhauledArrow;
import toast.bowoverhaul.item.ItemManager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /// Registers render files if this is the client side.
    @Override
    public void registerRenderers() {
    	new ClientEventHandler();
    	new ClientTickHandler();
    	new KeyHandler();

        MinecraftForge.EVENT_BUS.register(new RenderHeadHitbox());
        MinecraftForge.EVENT_BUS.register(new HudQuiver());

        for (int i = 0; i < ItemManager.overhauledBows.length; i++) {
        	if (ItemManager.overhauledBows[i] != null) {
				MinecraftForgeClient.registerItemRenderer(ItemManager.overhauledBows[i], ItemRendererCustomBow.INSTANCE);
			}
		}

        RenderingRegistry.registerEntityRenderingHandler(EntityOverhauledArrow.class, new RenderOverhauledArrow());
    }

    @Override
    public int getRenderIndex(String id, int defaultValue) {
        return RenderingRegistry.addNewArmourRendererPrefix(id);
    }

}