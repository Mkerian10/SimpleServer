package tech.stdev.core2;

import java.util.Arrays;
import java.util.Random;

public class TestUtils{
	
	public static byte[] randomBytes(int maxLength){
		Random r = new Random();
		
		int size = r.nextInt(maxLength - 1) + 1;
		byte[] bytes = new byte[size];
		r.nextBytes(bytes);
		return bytes;
	}
}
