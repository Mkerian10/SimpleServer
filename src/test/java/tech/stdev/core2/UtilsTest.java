package tech.stdev.core2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest{
	
	@Test
	void testIntoToBytesToInt(){
		for(int i = 0; i < 5000; i++){
			int j = new Random().nextInt(2000000);
			assertEquals(j, bytesToInt(intToBytes(j)));
		}
	}
	
	byte[] intToBytes(int i){
		return Utils.intToBytes(i);
	}
	
	int bytesToInt(byte[] b){
		return Utils.bytesToInt(b);
	}
}