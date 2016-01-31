package toast.bowoverhaul.util;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import toast.bowoverhaul.BowOverhaul;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * This is an
 */
public class HeadshotEvent {

	private final Object instance;
	private final Method callback;

	public HeadshotEvent(Object instance, String methodName) {
		Method method;
		try {
			method = instance.getClass().getDeclaredMethod(methodName, EntityLivingBase.class, Entity.class, Entity.class, boolean.class, float.class);
		}
		catch (Exception ex) {
			ModContainer activeMod = Loader.instance().activeModContainer();
			String modId = activeMod == null ? "" : " at modId:" + activeMod.getModId() + ",name:" + activeMod.getName() + ",version:" + activeMod.getVersion();

			BowOverhaul.logError("Failed to add headshot event listener! " + instance.getClass().getSimpleName() + "#" + methodName + modId, ex);
			method = null;
		}
		this.instance = instance;
		this.callback = method;
	}

	public float invoke(EntityLivingBase entityHit, Entity arrow, Entity shooter, boolean headshot, float damage) {
		try {
			return (Float) this.callback.invoke(this.instance, entityHit, arrow, shooter, headshot, damage);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return damage;
	}

}
