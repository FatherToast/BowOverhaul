package toast.bowoverhaul.util;

public class BowOverhaulSettingsException extends RuntimeException
{
    public BowOverhaulSettingsException(String comment, String path) {
        super(comment + " at " + path);
    }
    
    public BowOverhaulSettingsException(String comment, String path, Exception ex) {
        super(comment + " at " + path, ex);
    }
}