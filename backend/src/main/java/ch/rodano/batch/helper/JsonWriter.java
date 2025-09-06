package ch.rodano.batch.helper;

public class JsonWriter {

	public static String toJson(final Object o) {
		if(o == null) {
			return null;
		}
		try {
			return Jsons.write(o);
		}
		catch(Exception e) {
			return null;
		}
	}
}
