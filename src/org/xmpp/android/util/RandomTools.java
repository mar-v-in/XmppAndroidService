package org.xmpp.android.util;

import java.util.Random;

public class RandomTools {
	private static Random random = new Random();

	public static String generateString(String characters, int length) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text);
	}

	public static String generateAlnumString(int length) {
		return generateString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890", length);
	}
}
