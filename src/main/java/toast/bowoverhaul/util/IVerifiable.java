package toast.bowoverhaul.util;

/**
 * Used for objects that can be represented in Json so that they can choose to require or allow only certain fields.<br>
 * "_comment" is always an optional field and should never be included or used.
 */
public interface IVerifiable {

	/** @return An array of fields that must be included in the Json object. */
	String[] getRequiredFields();
	/** @return An array of fields that can be included in the Json object, but are not required. */
	String[] getOptionalFields();

}
