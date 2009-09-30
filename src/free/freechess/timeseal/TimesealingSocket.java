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

import free.a.c;
import free.a.b;

public class TimesealingSocket extends Socket implements Runnable {
	private class a extends OutputStream {

		private int a(byte abyte0[], long l) {
			int i = abyte0.length;
			System.arraycopy(abyte0, 0, a, 0, abyte0.length);
			a[i++] = 24;
			byte abyte1[] = Long.toString(l).getBytes();
			System.arraycopy(abyte1, 0, a, i, abyte1.length);
			i += abyte1.length;
			a[i++] = 25;
			int j = i;
			for (i += 12 - i % 12; j < i;)
				a[j++] = 49;

			for (int k = 0; k < i; k++)
				a[k] = (byte) (a[k] | 0x80);

			for (int i1 = 0; i1 < i; i1 += 12) {
				byte byte0 = a[i1 + 11];
				a[i1 + 11] = a[i1];
				a[i1] = byte0;
				byte0 = a[i1 + 9];
				a[i1 + 9] = a[i1 + 2];
				a[i1 + 2] = byte0;
				byte0 = a[i1 + 7];
				a[i1 + 7] = a[i1 + 4];
				a[i1 + 4] = byte0;
			}

			int l1 = 0;
			for (int j1 = 0; j1 < i; j1++) {
				a[j1] = (byte) (a[j1] ^ initialTIme[l1]);
				l1 = (l1 + 1) % initialTIme.length;
			}

			for (int k1 = 0; k1 < i; k1++)
				a[k1] = (byte) (a[k1] - 32);

			a[i++] = -128;
			a[i++] = 10;
			return i;
		}

		public void write(int i) throws IOException {
			if (i == 10)
				synchronized (TimesealingSocket.this) {
					int j = a(_fldif.toByteArray(), System.currentTimeMillis()
							- TimesealingSocket.this.initialTIme);
					_flddo.write(a, 0, j);
					_flddo.flush();
					_fldif.reset();
				}
			else
				_fldif.write(i);
		}

		private final byte initialTIme[] = "Timestamp (FICS) v1.0 - programmed by Henrik Gram."
				.getBytes();
		private byte a[];
		private final OutputStream _flddo;
		private final ByteArrayOutputStream _fldif = new ByteArrayOutputStream();

		public a(OutputStream outputstream) {
			a = new byte[10000];
			_flddo = outputstream;
		}
	}

	public TimesealingSocket(String s, int i) throws IOException {
		super(s, i);
		_flddo = new c(10000);
		_mthif();
	}

	public TimesealingSocket(InetAddress inetaddress, int i) throws IOException {
		super(inetaddress, i);
		_flddo = new c(10000);
		_mthif();
	}

	public void close() throws IOException {
		super.close();
		a = null;
	}

	public InputStream getInputStream() {
		return _flddo._mthif();
	}

	public OutputStream getOutputStream() throws IOException {
		return _fldif;
	}

	private void a() throws IOException {
		OutputStream outputstream = getOutputStream();
		synchronized (outputstream) {
			outputstream.write(key.getBytes());
			outputstream.write(10);
		}
	}

	private void _mthif() throws IOException {
		initialTIme = System.currentTimeMillis();
		_fldif = new a(super.getOutputStream());
		a();
		a = new Thread(this, "Timeseal thread");
		a.start();
	}

	public void run() {
		try {
			BufferedInputStream bufferedinputstream = new BufferedInputStream(
					super.getInputStream());
			free.a.b b1 = _flddo._mthbyte();
			String s = "\n\r[G]\n\r";
			byte abyte0[] = new byte[s.length()];
			int i = 0;
			int j = 0;
			while (a != null) {
				int k;
				if (i != 0) {
					k = abyte0[0];
					if (k < 0)
						k += 256;
					for (int l = 0; l < i; l++)
						abyte0[l] = abyte0[l + 1];

					i--;
				} else {
					k = bufferedinputstream.read();
				}
				if (s.charAt(j) == k) {
					if (++j == s.length()) {
						j = 0;
						i = 0;
						synchronized (this) {
							getOutputStream().write("\0029\n".getBytes());
						}
					}
				} else if (j != 0) {
					b1.write((byte) s.charAt(0));
					for (int i1 = 0; i1 < j - 1; i1++) {
						abyte0[i1] = (byte) s.charAt(i1 + 1);
						i++;
					}

					abyte0[i++] = (byte) k;
					j = 0;
				} else {
					if (k < 0) {
						b1.close();
						return;
					}
					b1.write(k);
				}
			}
		} catch (IOException _ex) {
			try {
				_fldif.close();
			} catch (IOException ioexception) {
				System.err.println("Failed to close PipedStream");
				ioexception.printStackTrace();
			}
		} finally {
			try {
				free.a.b b = _flddo._mthbyte();
				b.close();
			} catch (IOException _ex) {
			}
		}
	}

	private volatile long initialTIme;
	private static final String key = "TIMESTAMP|iv|IHATEJIN|";
	private final c _flddo;
	private OutputStream _fldif;
	private volatile Thread a;

}
