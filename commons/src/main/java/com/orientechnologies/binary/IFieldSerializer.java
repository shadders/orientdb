package com.orientechnologies.binary;

public interface IFieldSerializer<T> {

	  /**
	   * Serialize a single field object to a byte array
	   *
	   * @param object        is the object to serialize
	   */
	public byte[] serialize(String fieldName, T object);

	  /**
	   * Reads object from the stream starting from the startPosition
	   *
	   * @param stream        is the stream from object will be read
	   * @param offset is the position to start reading from
	   * @param length of the serialized data if known (read from the header)
	   * @return instance of the deserialized object
	   */
	public T deserialize(String fieldName, byte[] stream, int offset, int length);

	  /**
	   * @return <code>true</code> if binary presentation of object always has the same length.
	   */
	public boolean isFixedLength();

	  /**
	   * @return Length of serialized data if {@link #isFixedLength()} method returns <code>true</code>. If {@link #isFixedLength()}
	   *         method return <code>false</code> returned value is undefined.
	   */
	public int getFixedLength();

	
}
