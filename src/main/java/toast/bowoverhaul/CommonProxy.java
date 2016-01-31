package toast.bowoverhaul;

/**
 * The proxy class used by dedicated servers. When not running in a dedicated environment, ClientProxy is used.<br>
 * As this is a client-side mod, none of these methods should do anything.
 */
@SuppressWarnings(value = { "all" })
public class CommonProxy {

    /** Registers renderers. */
    public void registerRenderers() {}

    /**
     * Returns a new render index to use and registers it with the game.
     * @param id The id of the texture. The filenames become id_1 (non-leg armor) and id_2 (leg armor).
     * @param defaultValue The render index to use if possible.
     * @return The render index assigned. This will be defaultValue if this is not the client side or if that index was available.
     */
    public int getRenderIndex(String id, int defaultValue) {
        return defaultValue;
    }

}
