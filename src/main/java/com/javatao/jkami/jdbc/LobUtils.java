package com.javatao.jkami.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;

/**
 * Clob,Blob
 * 
 * @author TLF
 *
 */
public class LobUtils {

	public static String toStr(Clob clob) {
		StringBuffer sb = new StringBuffer();
		try {
			java.io.Reader is = clob.getCharacterStream();
			BufferedReader br = new BufferedReader(is);
			String s = br.readLine();
			while (s != null) {
				sb.append(s);
				s = br.readLine();
			}
			
		} catch (Exception e) {
			throw new  RuntimeException(e);
		}
		return sb.toString();
	}
	
	public static String toStr(Blob blob) {
		return new String(toBytes(blob));
	}

	public static byte[] toBytes(Clob clob) {
		return toBytesObj(clob);
	}

	public static byte[] toBytes(Blob blob) {
		return toBytesObj(blob);
	}

	private static byte[] toBytesObj(Object obj) {
		BufferedInputStream is = null;
		try {
			if (obj instanceof Blob) {
				is = new BufferedInputStream(((Blob) obj).getBinaryStream());
			}
			if (obj instanceof Clob) {
				is = new BufferedInputStream(((Clob) obj).getAsciiStream());
			}
			byte[] bytes = new byte[1024];
			int len = bytes.length;
			int offset = 0;
			int read = 0;
			while (offset < len && (read = is.read(bytes, offset, len - offset)) >= 0) {
				offset += read;
			}
			return bytes;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				return null;
			}

		}
	}
}
