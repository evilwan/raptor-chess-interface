// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package free.freechess.timeseal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import free.TimesealPipe;

public class TimesealingSocket extends Socket implements Runnable {
	private class CryptOutputStream extends OutputStream {

		private int crypt(byte bytesIn[], long l) {
			int bytesInLength = bytesIn.length;
			System.arraycopy(bytesIn, 0, buffer, 0, bytesIn.length);
			buffer[bytesInLength++] = 24;
			byte abyte1[] = Long.toString(l).getBytes();
			System.arraycopy(abyte1, 0, buffer, bytesInLength, abyte1.length);
			bytesInLength += abyte1.length;
			buffer[bytesInLength++] = 25;
			int j = bytesInLength;
			for (bytesInLength += 12 - bytesInLength % 12; j < bytesInLength;)
				buffer[j++] = 49;

			for (int k = 0; k < bytesInLength; k++)
				buffer[k] = (byte) (buffer[k] | 0x80);

			for (int i1 = 0; i1 < bytesInLength; i1 += 12) {
				byte byte0 = buffer[i1 + 11];
				buffer[i1 + 11] = buffer[i1];
				buffer[i1] = byte0;
				byte0 = buffer[i1 + 9];
				buffer[i1 + 9] = buffer[i1 + 2];
				buffer[i1 + 2] = byte0;
				byte0 = buffer[i1 + 7];
				buffer[i1 + 7] = buffer[i1 + 4];
				buffer[i1 + 4] = byte0;
			}

			int l1 = 0;
			for (int j1 = 0; j1 < bytesInLength; j1++) {
				buffer[j1] = (byte) (buffer[j1] ^ timesealKey[l1]);
				l1 = (l1 + 1) % timesealKey.length;
			}

			for (int k1 = 0; k1 < bytesInLength; k1++)
				buffer[k1] = (byte) (buffer[k1] - 32);

			buffer[bytesInLength++] = -128;
			buffer[bytesInLength++] = 10;
			return bytesInLength;
		}

		public void write(int i) throws IOException {
			if (i == 10)
				synchronized (TimesealingSocket.this) {
					int resultLength = crypt(byteArrayOutputStream.toByteArray(), System
							.currentTimeMillis()
							- TimesealingSocket.this.initialTime);
					outputStream.write(buffer, 0, resultLength);
					outputStream.flush();
					byteArrayOutputStream.reset();
				}
			else
				byteArrayOutputStream.write(i);
		}

		private final byte timesealKey[] = "Timestamp (FICS) v1.0 - programmed by Henrik Gram."
				.getBytes();
		private byte buffer[];
		private final OutputStream outputStream;
		private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		public CryptOutputStream(OutputStream outputstream) {
			buffer = new byte[10000];
			outputStream = outputstream;
		}
	}

	public TimesealingSocket(String s, int i) throws IOException {
		super(s, i);
		timesealPipe = new TimesealPipe(10000);
		init();
	}

	public TimesealingSocket(InetAddress inetaddress, int i) throws IOException {
		super(inetaddress, i);
		timesealPipe = new TimesealPipe(10000);
		init();
	}

	public void close() throws IOException {
		super.close();
		thread = null;
	}

	public InputStream getInputStream() {
		return timesealPipe.getTimesealInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	private void writeInitialTimesealString() throws IOException {
		OutputStream outputstream = getOutputStream();
		synchronized (outputstream) {
			outputstream.write(initialTimesealString.getBytes());
			outputstream.write(10);
		}
	}

	private void init() throws IOException {
		initialTime = System.currentTimeMillis();
		outputStream = new CryptOutputStream(super.getOutputStream());
		writeInitialTimesealString();
		thread = new Thread(this, "Timeseal thread");
		thread.start();
	}

	public void run() {
		try {
			BufferedInputStream bufferedinputstream = new BufferedInputStream(
					super.getInputStream());
			free.TimesealOutputStream timesealOutputStream = timesealPipe.getTimesealOutputStream();
			String timesealRequest = "\n\r[G]\n\r";
			byte timesealRequestBytes[] = new byte[timesealRequest.length()];
			int i = 0;
			int j = 0;
			while (thread != null) {
				int k;
				if (i != 0) {
					k = timesealRequestBytes[0];
					if (k < 0)
						k += 256;
					for (int l = 0; l < i; l++)
						timesealRequestBytes[l] = timesealRequestBytes[l + 1];

					i--;
				} else {
					k = bufferedinputstream.read();
				}
				if (timesealRequest.charAt(j) == k) {
					if (++j == timesealRequest.length()) {
						j = 0;
						i = 0;
						synchronized (this) {
							getOutputStream().write("\0029\n".getBytes());
						}
					}
				} else if (j != 0) {
					timesealOutputStream.write((byte) timesealRequest.charAt(0));
					for (int i1 = 0; i1 < j - 1; i1++) {
						timesealRequestBytes[i1] = (byte) timesealRequest.charAt(i1 + 1);
						i++;
					}

					timesealRequestBytes[i++] = (byte) k;
					j = 0;
				} else {
					if (k < 0) {
						timesealOutputStream.close();
						return;
					}
					timesealOutputStream.write(k);
				}
			}
		} catch (IOException _ex) {
			try {
				outputStream.close();
			} catch (IOException ioexception) {
				System.err.println("Failed to close PipedStream");
				ioexception.printStackTrace();
			}
		} finally {
			try {
				free.TimesealOutputStream timesealOutputStream = timesealPipe.getTimesealOutputStream();
				timesealOutputStream.close();
			} catch (IOException _ex) {
			}
		}
	}

	private volatile long initialTime;
	private static final String initialTimesealString = "TIMESTAMP|iv|IHATEJIN|";
	private final TimesealPipe timesealPipe;
	private OutputStream outputStream;
	private volatile Thread thread;

}
