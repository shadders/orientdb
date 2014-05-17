package com.orientechnologies.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * A byte array output stream that offers access to and control of the backing byte buffer at the
 * expense of some safety.
 * 
 * This class is not threadsafe.
 * 
 * @author Steve Coughlan
 *
 */
public class UnsafeByteArrayOutputStream extends OutputStream {

    /**
     * The buffer where data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
	public UnsafeByteArrayOutputStream() {
		this(32);
	}
	
    /**
     * Creates a new byte array output stream. Using the given byte array as the backing buffer.
     * 
     */
	public UnsafeByteArrayOutputStream(byte[] buf, int size) {
		this.buf = buf;
		this.count = size;
	}

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
	public UnsafeByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                                               + size);
        }
        buf = new byte[size];
	}
	
    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
     * interpreted as a request for the unsatisfiably large capacity
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }
    
    /**
     * Forces the backing array to the given size.
     * @param capacity
     */
    public void setCapacity(int capacity) {
    	if (capacity == count)
    		return;
    	buf = Arrays.copyOf(buf, capacity);
    }
    
    /**
     * Forces the backing array to the current size + extra bytes.  If you don't know the
     * final byte array length until part way through the write process you may be able to 
     * save an array allocation by setting capacity before finishing with the stream.
     * @param extraBytes
     */
    public void addCapacity(int extraBytes) {
    	setCapacity(count + extraBytes);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity < 0) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        buf = Arrays.copyOf(buf, newCapacity);
    }
    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param      out   the output stream to which to write the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * Resets the <code>count</code> field of this byte array output
     * stream to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     *
     * @see     java.io.ByteArrayInputStream#count
     */
    public void reset() {
        count = 0;
    }
    
    /**
     * Alias for reset()
     */
    public void clear() {
    	count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public byte toByteArraySafe()[] {
        return Arrays.copyOf(buf, count);
    }
    
    /**
     * If the backing byte array length is equal to the stream size() returns the backing
     * byte array without copying, otherwise returns a newly allocated array
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public byte toByteArrayUnsafe()[] {
        if (count == buf.length)
        	return buf;
    	return Arrays.copyOf(buf, count);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     * @see     java.io.ByteArrayOutputStream#count
     */
    public int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string decoding bytes using the
     * platform's default character set. The length of the new <tt>String</tt>
     * is a function of the character set, and hence may not be equal to the
     * size of the buffer.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with the default replacement string for the platform's
     * default character set. The {@linkplain java.nio.charset.CharsetDecoder}
     * class should be used when more control over the decoding process is
     * required.
     *
     * @return String decoded from the buffer's contents.
     * @since  JDK1.1
     */
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string by decoding the bytes using
     * the specified {@link java.nio.charset.Charset charsetName}. The length of
     * the new <tt>String</tt> is a function of the charset, and hence may not be
     * equal to the length of the byte array.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string. The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     *
     * @param  charsetName  the name of a supported
     *              {@linkplain java.nio.charset.Charset </code>charset<code>}
     * @return String decoded from the buffer's contents.
     * @exception  UnsupportedEncodingException
     *             If the named charset is not supported
     * @since   JDK1.1
     */
    public String toString(String charsetName)
        throws UnsupportedEncodingException
    {
        return new String(buf, 0, count, charsetName);
    }

	
	/**
	 * @return the current size of the backing array
	 */
	public int capacity() {
		return buf.length;
	}
	
	/**
	 * Sets the size either by adding zeros or by truncating.
	 * @param size
	 */
	public void setSize(int size) {
		if (size > count) {
			//force it to grow by adding zeroes
			try {
				write(new byte[size - count]);
			} catch (IOException e) {
				//never happens
				e.printStackTrace();
			}
		}
		count = size;	
	}
	
	/**
	 * Set a byte in the backing array allowing random access.
	 * @param index
	 * @param value
	 */
	public void set(int index, byte value) {
		buf[index] = value;
	}
	
	/**
	 * get the byte at the given index
	 * @param index
	 * @return
	 */
	public byte get(int index) {
		return buf[index];
	}
	
	/**
	 * Returns a new byte array that is the specified range of the backing byte buffer.
	 * @param index
	 * @param length
	 * @return
	 */
	public byte[] getRange(int index, int length) {
		if (index + length > count)
			throw new ArrayIndexOutOfBoundsException((index + length) + " is greater than size " + count);
		return Arrays.copyOfRange(buf, index, index + length);
	}
	
	/**
	 * Returns the range index to index + size()
	 * @param index
	 * @return
	 */
	public byte[] getRangeFrom(int index) {
		if (index > count)
			throw new ArrayIndexOutOfBoundsException((index) + " is greater than size " + count);
		return Arrays.copyOfRange(buf, index, count);
	}
	
	/**
	 * Returns the range 0 - index
	 * @param index
	 * @return
	 */
	public byte[] getRangeTo(int index) {
		if (index > count)
			throw new ArrayIndexOutOfBoundsException((index) + " is greater than size " + count);
		return Arrays.copyOfRange(buf, 0, index);
	}
	
	/**
	 * Returns the backing byte array.  What you do with it afterwards is your responsibility!
	 * @return
	 */
	public byte[] getBuffer() {
		return buf;
	}
	
	/**
	 * Writes to a specified index.
	 * 
	 * Note that no checks are performed to ensure that index is < size().  This method will
	 * allow you to write beyond the end of the stream.
	 * 
	 * @param index
	 * @param value
	 */
	public void write(int index, int value) {
		int oldCount = count;
		count = index;
		write(value);
		if (count < oldCount)
			count = oldCount;
	}
	
	/**
	 * Writes to specified index, if the the number of bytes written is less than those remaining
	 * the size() will not be affected.  If it is greater then size() will grow accordingly
	 *  
	 * Note that no checks are performed to ensure that index is < size().  This method will
	 * allow you to write beyond the end of the stream.
	 * 
	 * @param index
	 * @param bytes
	 * @param off
	 * @param len
	 */
	public void write(int index, byte[] bytes, int off, int len) {
		int oldCount = count;
		count = index;
		write(bytes, off, len);
		if (count < oldCount)
			count = oldCount;
	}
	
	/**
	 * Writes to specified index, if the the number of bytes written is less than those remaining
	 * the size() will not be affected.  If it is greater then size() will grow accordingly
	 * 
	 * Note that no checks are performed to ensure that index is < size().  This method will
	 * allow you to write beyond the end of the stream. 
	 * 
	 * @param index
	 * @param bytes
	 */
	public void write(int index, byte[] bytes) {
		write(index, bytes, 0, bytes.length);
	}
	
    /**
     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p>
     *
     */
    public void close() throws IOException {
    }

}
