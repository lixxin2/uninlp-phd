package edu.hitsz.algorithm.hash;

import java.nio.charset.Charset;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

public class MyCheckSum {
	
	public static final Charset UTF8Charset = Charset.forName( "UTF-8" );

	
	public static int getAdler(byte[] bytes) {
		
		final Adler32 digester = new Adler32();
        digester.update(bytes);
        // digester.update( int ) processes only the low order 8-bits. It actually expects an unsigned byte.
        // getValue produces a long to conform to the Checksum interface.
        // Actual result is 32 bits long not 64.
        return ( int ) digester.getValue();
		
	}
	
	public static int sunCRC32( byte[] ba )
    {
	    // create a new CRC-calculating object
	    final CRC32 crc = new CRC32();
	    crc.update( ba );
	    // crc.update( int ) processes only the low order 8-bits. It actually expects an unsigned byte.
	    return ( int ) crc.getValue();
    }
	
	
	public static void main(String[] args) {
		
		final String s = "The quick brown fox jumped over the lazy dog's back";
        final byte[] ba = s.getBytes( UTF8Charset );
        int adlerSum = MyCheckSum.getAdler(ba);
        
        System.out.println( adlerSum );
	}
	

}
