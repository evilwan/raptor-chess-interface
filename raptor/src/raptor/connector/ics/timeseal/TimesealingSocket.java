/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
package raptor.connector.ics.timeseal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This code was reverse engineered from the JIN project. JIN is a GPLed
 * project. Its URL can be found here: http://www.jinchess.com/
 * 
 * Variable names have been changed to be more readable.
 * 
 * It has also been modified to change the initial connect string. The original
 * version sent the operating system information stored in java system
 * properties.
 * 
 * This class isn't so bad but the TimesealPipe is horrendous. What it does
 * actually impacts the timeseal protocol and you can't just use the crypt
 * method in this class and do it yourself.
 * 
 * I would like to rewrite this using ChannelSocket in Raptor to speed it up.
 */
public class TimesealingSocket extends Socket implements Runnable {
	private class CryptOutputStream extends OutputStream {

		private final byte timesealKey[] = "Timestamp (FICS) v1.0 - programmed by Henrik Gram."
				.getBytes();

		private byte buffer[];

		private final OutputStream outputStreamToDecorate;
		private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		public CryptOutputStream(OutputStream outputstream) {
			buffer = new byte[10000];
			outputStreamToDecorate = outputstream;
		}

		private int crypt(byte stringToWriteBytes[], long timestamp) {
			int bytesInLength = stringToWriteBytes.length;
			System.arraycopy(stringToWriteBytes, 0, buffer, 0,
					stringToWriteBytes.length);
			buffer[bytesInLength++] = 24;
			byte abyte1[] = Long.toString(timestamp).getBytes();
			System.arraycopy(abyte1, 0, buffer, bytesInLength, abyte1.length);
			bytesInLength += abyte1.length;
			buffer[bytesInLength++] = 25;
			int j = bytesInLength;
			for (bytesInLength += 12 - bytesInLength % 12; j < bytesInLength;) {
				buffer[j++] = 49;
			}

			for (int k = 0; k < bytesInLength; k++) {
				buffer[k] = (byte) (buffer[k] | 0x80);
			}

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

			for (int k1 = 0; k1 < bytesInLength; k1++) {
				buffer[k1] = (byte) (buffer[k1] - 32);
			}

			buffer[bytesInLength++] = -128;
			buffer[bytesInLength++] = 10;
			return bytesInLength;
		}

		@Override
		public void write(int i) throws IOException {
			if (i == 10) {
				synchronized (TimesealingSocket.this) {
					int resultLength = crypt(byteArrayOutputStream
							.toByteArray(), System.currentTimeMillis()
							- initialTime);
					outputStreamToDecorate.write(buffer, 0, resultLength);
					outputStreamToDecorate.flush();
					byteArrayOutputStream.reset();
				}
			} else {
				byteArrayOutputStream.write(i);
			}
		}
	}

	private static final Log LOG = LogFactory.getLog(TimesealingSocket.class);

	private volatile long initialTime;

	private String initialTimesealString = null;

	private final TimesealPipe timesealPipe;

	private CryptOutputStream cryptedOutputStream;

	private volatile Thread thread;

	public TimesealingSocket(InetAddress inetaddress, int i,
			String intialTimestampString) throws IOException {
		super(inetaddress, i);
		initialTimesealString = intialTimestampString;
		timesealPipe = new TimesealPipe(10000);
		init();
	}

	public TimesealingSocket(String s, int i, String intialTimestampString)
			throws IOException {
		super(s, i);
		timesealPipe = new TimesealPipe(10000);
		initialTimesealString = intialTimestampString;
		init();
	}

	@Override
	public void close() throws IOException {
		super.close();
		thread = null;
	}

	@Override
	public InputStream getInputStream() {
		return timesealPipe.getTimesealInputStream();
	}

	@Override
	public CryptOutputStream getOutputStream() throws IOException {
		return cryptedOutputStream;
	}

	private void init() throws IOException {
		initialTime = System.currentTimeMillis();
		cryptedOutputStream = new CryptOutputStream(super.getOutputStream());
		writeInitialTimesealString();
		thread = new Thread(this, "Timeseal thread");
		thread.start();
	}

	public void run() {
		try {
			BufferedInputStream bufferedinputstream = new BufferedInputStream(
					super.getInputStream());
			raptor.connector.ics.timeseal.TimesealOutputStream timesealOutputStream = timesealPipe
					.getTimesealOutputStream();
			String timesealRequest = "\n\r[G]\n\r";
			byte timesealRequestBytes[] = new byte[timesealRequest.length()];
			int i = 0;
			int j = 0;
			while (thread != null) {
				int k;
				if (i != 0) {
					k = timesealRequestBytes[0];
					if (k < 0) {
						k += 256;
					}
					for (int l = 0; l < i; l++) {
						timesealRequestBytes[l] = timesealRequestBytes[l + 1];
					}

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
					timesealOutputStream
							.write((byte) timesealRequest.charAt(0));
					for (int i1 = 0; i1 < j - 1; i1++) {
						timesealRequestBytes[i1] = (byte) timesealRequest
								.charAt(i1 + 1);
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
				cryptedOutputStream.close();
			} catch (IOException ioexception) {
				LOG.debug("Failed to close PipedStream");
				ioexception.printStackTrace();
			}
		} finally {
			try {
				raptor.connector.ics.timeseal.TimesealOutputStream timesealOutputStream = timesealPipe
						.getTimesealOutputStream();
				timesealOutputStream.close();
			} catch (IOException _ex) {
			}
		}
	}

	private void writeInitialTimesealString() throws IOException {

		// BICS can't handle speedy connections so this slows it down a bit.
		try {
			Thread.sleep(100);
		} catch (InterruptedException ie) {
		}

		OutputStream outputstream = getOutputStream();
		synchronized (outputstream) {
			outputstream.write(initialTimesealString.getBytes());
			outputstream.write(10);
		}
	}

}
