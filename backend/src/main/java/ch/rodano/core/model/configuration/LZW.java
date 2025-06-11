package ch.rodano.core.model.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LZW {

	private static final int INITIAL_DICTIONARY_SIZE = 256;
	private static final int MAXIMUM_DICTIONARY_SIZE = 65536;

	public static List<Integer> compress(final String string) {
		return compress(string, null);
	}

	public static List<Integer> compress(final String string, final Integer dictionnaryMaxSize) {
		//build dictionary
		final Map<String, Integer> dictionary = new TreeMap<>();
		for(int i = 0; i < INITIAL_DICTIONARY_SIZE; i++) {
			dictionary.put(Character.toString((char) i), i);
		}

		int currentCode = INITIAL_DICTIONARY_SIZE;
		final List<Integer> result = new ArrayList<>();

		StringBuilder phrase = new StringBuilder();

		for(final char character : string.toCharArray()) {
			final StringBuilder newPhrase = new StringBuilder().append(phrase).append(character);

			//there is a match in dictionary
			if(dictionary.containsKey(newPhrase.toString())) {
				phrase = newPhrase;
			}
			else {
				result.add(dictionary.get(phrase.toString()));
				// add new sequence to the dictionary
				if(dictionnaryMaxSize == null || currentCode < dictionnaryMaxSize) {
					dictionary.put(newPhrase.toString(), currentCode);
					currentCode++;
				}
				phrase.delete(0, phrase.length()).append(character);
			}
		}

		//add last characters to results
		if(phrase.length() > 0) {
			result.add(dictionary.get(phrase.toString()));
		}
		return result;
	}

	public static String compressToString(final String s) {
		final StringBuilder result = new StringBuilder();
		for(final Integer charCode : compress(s, MAXIMUM_DICTIONARY_SIZE)) {
			result.append((char) (int) charCode);
		}
		return result.toString();
	}

	public static String decompress(final List<Integer> integers) {
		return decompress(integers, null);
	}

	public static String decompress(final List<Integer> ints, final Integer dictionnaryMaxSize) {
		final List<Integer> integers = new ArrayList<>(ints);
		final Map<Integer, String> dictionary = new TreeMap<>();
		final StringBuilder result = new StringBuilder();

		//initialize "previous" data
		char previousCharacter = (char) (int) integers.remove(0);
		StringBuilder previousPhrase = new StringBuilder().append(previousCharacter);

		result.append(previousCharacter);

		int currentCode = INITIAL_DICTIONARY_SIZE;

		for(final int i : integers) {
			final char character = (char) i;

			final StringBuilder phrase = new StringBuilder();

			if(i < INITIAL_DICTIONARY_SIZE) {
				phrase.append(character);
			}
			//there is a match in dictionary
			else if(dictionary.containsKey(i)) {
				phrase.append(dictionary.get(i));
			}
			else {
				phrase.append(previousPhrase).append(previousCharacter);
			}
			result.append(phrase);

			previousCharacter = phrase.charAt(0);

			final StringBuilder term = new StringBuilder().append(previousPhrase).append(previousCharacter);
			if(dictionnaryMaxSize == null || currentCode < dictionnaryMaxSize) {
				dictionary.put(currentCode, term.toString());
				currentCode++;
			}
			previousPhrase = phrase;
		}
		return result.toString();
	}

	public static String decompressString(final String string) {
		final List<Integer> integers = new ArrayList<>();
		for(final char character : string.toCharArray()) {
			integers.add((int) character);
		}
		return decompress(integers, MAXIMUM_DICTIONARY_SIZE);
	}

}
