package process;

public class ReplyHeaderValidator {
	// Checking if it is a 2 digit string and in the valid range (10-59)
	public static Integer verifyStatCode(String header) {
		String statCode = header.split(" ", 2)[0];
		try {
			int statCodeInt = Integer.parseInt(statCode);
			if (statCode.length() == 2 && (statCodeInt >= 10 && statCodeInt <= 59))
				return statCodeInt;
			return null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// Checking if there is an error code for which we don't care if meta is empty
	public static boolean isErrorOrRedirect(String statCode) {
		return statCode.matches("3[0-9]")
			|| statCode.matches("4[0-25-9]")
			|| statCode.matches("5[0-9]");
	}

	// Checking if meta is present
	public static String verifyMetaPresence(String header) {
		String[] headerParts = header.split(" ", 2);
		if (headerParts.length == 2)
			return headerParts[1];
		return null;
	}

	public static boolean verifyMimeType(String meta) {
		meta = meta.toLowerCase();
		return meta.equalsIgnoreCase("text/gemini")
			|| meta.equalsIgnoreCase("text/plain")
			|| meta.equalsIgnoreCase("application/octet-stream");
	}
}
