package com.orientechnologies.binary.old;

/**
 * 
 * @author Steve Coughlan
 *
 */
public class VarInt8 {

	/**
	 * decode the byte(s) into an int or a long
	 * @return
	 */
	public int getValue() {
		return -1;
	}
	
	public int getBytesLength() {
		return 1;
	}
	
	public static VarInt8 read(byte[] bytes, int offset) {
		//not implemented
		return null;
	}

}
