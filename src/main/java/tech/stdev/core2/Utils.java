package tech.stdev.core2;

public class Utils{
	
	public static byte[] intToBytes(int i){
		byte[] bytes = new byte[4];
		bytes[0] = (byte)((i >> 24) & 0xFF);
		bytes[1] = (byte)((i >> 16) & 0xFF);
		bytes[2] = (byte)((i >> 8) & 0xFF);
		bytes[3] = (byte)(i & 0xFF);
		return bytes;
	}
	
	public static int bytesToInt(byte[] bytes){
		int i = 0;
		i |= (bytes[0] << 24 & 0xFF000000);
		i |= (bytes[1] << 16 & 0xFF0000);
		i |= (bytes[2] << 8 & 0xFF00);
		i |= (int)(bytes[3] & 0xFF);
		return i;
	}
}
