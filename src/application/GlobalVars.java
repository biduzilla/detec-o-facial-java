package application;

public class GlobalVars {
	private static String codAssoc;
	private static String path;
	private static int timerSet;
	private static int nuEquip;
	private static int confidenceInt;

	public static String getCodAssoc() {
		return codAssoc;
	}

	public static void setCodAssoc(String codAssoc) {
		GlobalVars.codAssoc = codAssoc;
	}

	public static String getPath() {
		return path;
	}

	public static void setPath(String path) {
		GlobalVars.path = path;
	}

	public static int getTimerSet() {
		return timerSet;
	}

	public static void setTimerSet(int timerSet) {
		GlobalVars.timerSet = timerSet;
	}

	public static int getNuEquip() {
		return nuEquip;
	}

	public static void setNuEquip(int nuEquip) {
		GlobalVars.nuEquip = nuEquip;
	}

	public static int getConfidenceInt() {
		return confidenceInt;
	}

	public static void setConfidenceInt(int confidenceInt) {
		GlobalVars.confidenceInt = confidenceInt;
	}

}
