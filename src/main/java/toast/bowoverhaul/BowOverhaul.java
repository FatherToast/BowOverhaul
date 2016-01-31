package toast.bowoverhaul;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.config.Configuration;
import toast.bowoverhaul.entity.EntityOverhauledArrow;
import toast.bowoverhaul.item.ItemManager;
import toast.bowoverhaul.network.GuiHelper;
import toast.bowoverhaul.network.MessageFX;
import toast.bowoverhaul.network.MessageOpenQuiver;
import toast.bowoverhaul.network.MessageSwapArrow;
import toast.bowoverhaul.util.EventHandler;
import toast.bowoverhaul.util.EventHandlerFML;
import toast.bowoverhaul.util.FileHelper;
import toast.bowoverhaul.util.HeadshotEvent;
import toast.bowoverhaul.util.Properties;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = BowOverhaul.MODID, name = "Bow Overhaul", version = BowOverhaul.VERSION)
public class BowOverhaul {
    /* TO DO *\
     *
     * Arrow replacement
     * 	> Replace villager trades
     *  > Replace chest loot
     *  > Auto-replace recipes
     *
     * Bow replacement
     *  > Replace equipped bows and drops on mobs
     * 	> Replace villager trades
     *  > Replace chest loot
     *  > Replace fishing loot
     *  > Auto-replace recipes
     *
    \* ** ** */
    // This mod's id.
    public static final String MODID = "BowOverhaul";
    // This mod's version.
    public static final String VERSION = "0.0.0";

    /** If true, this mod starts up in debug mode. */
    public static boolean debug = false;
    /** The sided proxy. This points to a "common" proxy if and only if we are on a dedicated
     * server. Otherwise, it points to a client proxy. */
    @SidedProxy(clientSide = "toast.bowoverhaul.client.ClientProxy", serverSide = "toast.bowoverhaul.CommonProxy")
    public static CommonProxy proxy;
    /** The mod's random number generator. */
    public static final Random random = new Random();
    /** The network channel for this mod. */
    public static SimpleNetworkWrapper CHANNEL;

    /** The path to the textures folder. */
    public static final String TEXTURE_PATH = BowOverhaul.MODID + ":textures/";

    /** Allows proper calculation of headshot damage. */
    public static Field entityLivingBaseLastDamage;

    private static final ArrayList<HeadshotEvent> HEADSHOT_EVENT_LISTENERS = new ArrayList<HeadshotEvent>();

    /**
     * Registers an object as a headshot event listener. The method with the signature<br>
     * methodName(EntityLivingBase entityHit, Entity arrow, Entity shooter, boolean headshot, float damage)<br>
     * in the event listener object will be called every time damage occurs that has to potential to score a headshot
     * (within Forge's LivingHurtEvent at EventPriority.HIGH).
     * The method must return a float value, which will become the new damage dealt by the arrow.
     * The shooter may be null.<br>
     * <br>
     * Event listener method parameters:<br>
     * * EntityLivingBase entityHit - The entity being dealt damage.<br>
     * * Entity arrow - The arrow entity dealing damage.<br>
     * * Entity shooter - The entity which fired the arrow.<br>
     * * boolean headshot - True if this hit scored a headshot; false otherwise.<br>
     * * float damage - The amount of damage being dealt.<br>
     * <br>
     * Event listener return value: Returns the new damage to deal (usually will just return the damage parameter, modified if needed).
     *
     * @param instance The object you want to recieve the events from
     * @param methodName The name of the method to call within the instance - the method must return a float and have parameters (EntityLivingBase, Entity, Entity, boolean, float)
     */
    public static void registerHeadshotEvent(Object instance, String methodName) {
    	BowOverhaul.HEADSHOT_EVENT_LISTENERS.add(new HeadshotEvent(instance, methodName));
    }

    public static float fireHeadshotEvent(EntityLivingBase entityHit, Entity arrow, Entity shooter, boolean headshot, float damage) {
    	for (int i = 0; i < BowOverhaul.HEADSHOT_EVENT_LISTENERS.size(); i++) {
    		damage = BowOverhaul.HEADSHOT_EVENT_LISTENERS.get(i).invoke(entityHit, arrow, shooter, headshot, damage);
    	}
    	return damage;
    }

    // Called before initialization. Loads the properties/configurations.
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Properties.init(new Configuration(event.getSuggestedConfigurationFile()));
        BowOverhaul.logDebug("Loading in debug mode!");
        FileHelper.init(event.getModConfigurationDirectory());

        int id = 0;
        BowOverhaul.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("BO|FX");
        BowOverhaul.CHANNEL.registerMessage(MessageSwapArrow.Handler.class, MessageSwapArrow.class, id++, Side.SERVER);
        BowOverhaul.CHANNEL.registerMessage(MessageOpenQuiver.Handler.class, MessageOpenQuiver.class, id++, Side.SERVER);
        if (event.getSide() == Side.CLIENT) {
            BowOverhaul.CHANNEL.registerMessage(MessageFX.Handler.class, MessageFX.class, id++, Side.CLIENT);
        }

        ItemManager.registerItems();
    }

    // Called during initialization. Registers entities, mob spawns, and renderers.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new EventHandler();
        new EventHandlerFML();

        EntityRegistry.registerModEntity(EntityOverhauledArrow.class, "OverhauledArrow", 0, this, 64, 20, true);

        BowOverhaul.proxy.registerRenderers();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHelper());
    }

    // Called after initialization. Used to check for dependencies.
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        BowOverhaul.log("Loading bow overhaul settings...");
        BowOverhaul.log("Loaded " + FileHelper.load() + " bow overhaul settings!");
        if (Properties.getBoolean(Properties.GENERAL, "auto_generate_files")) {
            BowOverhaul.log("Generating default bow overhaul settings...");
            BowOverhaul.log("Generated " + FileHelper.generateDefaults() + " bow overhaul settings!");
        }
    }

    // Called as the server is starting.
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandReload());
        commandManager.registerCommand(new CommandDebugToggle());
    }

    // Makes the first letter upper case.
    public static String cap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toUpperCase();
        return Character.toString(Character.toUpperCase(string.charAt(0))) + string.substring(1);
    }

    // Makes the first letter lower case.
    public static String decap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toLowerCase();
        return Character.toString(Character.toLowerCase(string.charAt(0))) + string.substring(1);
    }

    /** Prints the message to the console with this mod's name tag. */
    public static void log(String message) {
        System.out.println("[" + BowOverhaul.MODID + "] " + message);
    }
    /** Prints the message to the console with this mod's name tag if debugging is enabled. */
    public static void logDebug(String message) {
        if (BowOverhaul.debug) {
            System.out.println("[" + BowOverhaul.MODID + "] [debug] " + message);
        }
    }
    /** Prints the message to the console with this mod's name tag and a warning tag. */
    public static void logWarning(String message) {
        System.out.println("[" + BowOverhaul.MODID + "] [WARNING] " + message);
    }
    /** Prints the message to the console with this mod's name tag and a warning tag. */
    public static void logWarning(String message, Exception ex) {
        System.out.println("[" + BowOverhaul.MODID + "] [WARNING] " + message);
        ex.printStackTrace();
    }
    /** Prints the message to the console with this mod's name tag and an error tag.<br>
     * Throws a runtime exception with a message and this mod's name tag if debugging is enabled. */
    public static void logError(String message) {
        if (BowOverhaul.debug)
            throw new RuntimeException("[" + BowOverhaul.MODID + "] " + message);
        BowOverhaul.log("[ERROR] " + message);
    }
    /** Prints the message to the console with this mod's name tag and an error tag.<br>
     * Throws a runtime exception with a message and this mod's name tag if debugging is enabled. */
    public static void logError(String message, Exception ex) {
        if (BowOverhaul.debug)
            throw new RuntimeException("[" + BowOverhaul.MODID + "] " + message, ex);
        BowOverhaul.log("[ERROR] " + message);
        ex.printStackTrace();
    }
    /** Throws a runtime exception with a message and this mod's name tag. */
    public static void exception(String message) {
        throw new RuntimeException("[" + BowOverhaul.MODID + "] " + message);
    }
    /** Throws a runtime exception with a message and this mod's name tag. */
    public static void exception(String message, Exception ex) {
        throw new RuntimeException("[" + BowOverhaul.MODID + "] " + message, ex);
    }

    static {
    	try {
    		BowOverhaul.entityLivingBaseLastDamage = ReflectionHelper.findField(EntityLivingBase.class, "bc", "field_110153_bc", "lastDamage");
    		BowOverhaul.entityLivingBaseLastDamage.setAccessible(true);
    	}
    	catch (Exception ex) {
    		BowOverhaul.logError("Unable to find EntityLivingBase#lastDamage!", ex);
    	}
    }
}