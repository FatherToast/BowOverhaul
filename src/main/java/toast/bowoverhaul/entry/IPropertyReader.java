package toast.bowoverhaul.entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface IPropertyReader {
    /// Loads a line as a property.
    public IProperty readLine(String path, JsonObject root, int index, JsonElement node);
}