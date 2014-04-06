package com.orientechnologies.binary.util;

import java.util.List;

/**
 * Set of utility methods for setting, clearing, toggling and reading bits.
 * @author Steve Coughlan
 *
 */
public class Bits {

	
	/**
	 * return the nth power of 2.
	 * @param n
	 * @return
	 */
	public static long pow2(int n) {
		if (n > 64)
			throw new IllegalArgumentException("power greater than 64 will overflow.");
		return 1 << n;
	}
	
	/**
	 * return true if the number is a power of 2.
	 * @param num
	 * @return
	 */
	public static boolean isPow2(long num) {
		return (num & (num - 1)) == 0;
	}
	
	/**
	 * return true if the number is a power of 2.
	 * @param num
	 * @return
	 */
	public static boolean isPow2(int num) {
		return (num & (num - 1)) == 0;
	}

	/*
	 * bytes
	 */
	
	/**
	 * Converts an array of booleans to bits
	 * @param bools
	 * @return
	 */
	public static byte[] toBits(boolean[] bools) {
		byte[] bits = new byte[bools.length + 7 / 8];
		for (int i = 0; i < bools.length; i++)
			if (bools[i])
				setBit(bits, i);
		return bits;
	}
	
	/**
	 * Converts a list of booleans to bits.  nulls not allowed
	 * @param bools
	 * @return
	 */
	public static byte[] toBits(List<Boolean> bools) {
		byte[] bits = new byte[(bools.size() + 7) / 8];
		for (int i = 0; i < bools.size(); i++)
			if (bools.get(i))
				setBit(bits, i);
		return bits;
	}
	
	public static boolean[] toBooleanArray(byte[] bits) {
		return toBooleanArray(bits, bits.length * 8);
	}
	
	public static boolean[] toBooleanArray(byte[] bits, int size) {
		boolean[] bools = new boolean[size];
		for (int i = 0; i < bools.length; i++)
			bools[i] = isSet(bits, i);
		return bools;
	}
	
	/**
	 * Check if the nth but is set
	 * @param bytes the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 * @return true if bit is set to 1.
	 */
	public static boolean isSet(byte[] bytes, int bit) {
		return isSet(bytes[bit / 8], bit % 8);
	}
	
	/**
	 * Sets the nth bit to 1.
	 * @param bytes the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static void setBit(byte[] bytes, int bit) {
		int i = bit / 8;
		bytes[i] = (byte) setBit(bytes[i], bit % 8); 
	}
	
	/**
	 * Sets the nth bit to 0.
	 * @param bytes the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static void clearBit(byte[] bytes, int bit) {
		int i = bit / 8;
		bytes[i] = (byte) clearBit(bytes[i], bit % 8);
	}
	
	/**
	 * Toggles the nth bit.
	 * @param bytes the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static void toggleBit(byte[] bytes, int bit) {
		int i = bit / 8;
		bytes[i] = (byte) toggleBit(bytes[i], bit % 8);
	}
	
	/*
	 * byte
	 */
	
	/**
	 * Check if the nth but is set
	 * @param bytes the primitive containing the bit sequence
	 * @param n the number of bits from the right-most bit (0 is the LSB)
	 * @return true if bit is set to 1.
	 */
	public static boolean isSet(byte b, int bit) {
		return (b & (1 << bit)) != 0;
	}
	
	/**
	 * Sets the nth bit to 1.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int setBit(byte b, int n) {
		return b | (1 << n);
	}
	
	/**
	 * Sets the nth bit to 0.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int clearBit(byte b, int n) {
		return b & ~(1 << n);
	}
	
	/**
	 * Toggles the nth bit.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int toggleBit(byte b, int n) {
		return  b ^ (1 << n);
	}
	
	/*
	 * int
	 */
	
	/**
	 * Check if the nth but is set
	 * @param bytes the primitive containing the bit sequence
	 * @param n the number of bits from the right-most bit (0 is the LSB)
	 * @return true if bit is set to 1.
	 */
	public static boolean isSet(int b, int bit) {
		return (b & (1 << bit)) != 0;
	}
	
	/**
	 * Sets the nth bit to 1.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int setBit(int b, int n) {
		return b | (1 << n);
	}
	
	/**
	 * Sets the nth bit to 0.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int clearBit(int b, int n) {
		return b & ~(1 << n);
	}
	
	/**
	 * Toggles the nth bit.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static int toggleBit(int b, int n) {
		return  b ^ (1 << n);
	}
	
	/*
	 * long 
	 */
	
	/**
	 * Check if the nth but is set
	 * @param bytes the primitive containing the bit sequence
	 * @param n the number of bits from the right-most bit (0 is the LSB)
	 * @return true if bit is set to 1.
	 */
	public static boolean isSet(long b, int bit) {
		return (b & (1 << bit)) != 0;
	}
	
	/**
	 * Sets the nth bit to 1.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static long setBit(long b, int n) {
		return b | (1 << n);
	}
	
	/**
	 * Sets the nth bit to 0.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static long clearBit(long b, int n) {
		return b & ~(1 << n);
	}
	
	/**
	 * Toggles the nth bit.
	 * @param b the primitive containing the bit sequence
	 * @param bit the number of bits from the right-most bit (0 is the LSB)
	 */
	public static long toggleBit(long b, int n) {
		return  b ^ (1 << n);
	}
	

}
