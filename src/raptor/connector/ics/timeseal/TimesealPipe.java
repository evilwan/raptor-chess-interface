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

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This code was reverse engineered from the JIN project. JIN is a gpled
 * project. Its url can be found here: http://www.jinchess.com/
 */
public class TimesealPipe {

	private final TimesealInputStream timesealInputStream;

	private final TimesealOutputStream timesealOutputStream;

	private volatile int _flddo;

	private final byte buffer[];

	private int _fldchar;

	private int _fldbyte;

	private boolean closedFlag;

	private boolean closedFlag2;

	private Object writeLock;

	private Object readLock;

	public TimesealPipe() {
		this(2048);
	}

	public TimesealPipe(int i) {
		_flddo = 0;
		_fldchar = 0;
		_fldbyte = 0;
		closedFlag = false;
		closedFlag2 = false;
		writeLock = new String("Write Lock for PipedStreams");
		readLock = new String("Read Lock for PipedStream");
		timesealInputStream = new TimesealInputStream(this);
		timesealOutputStream = new TimesealOutputStream(this);
		buffer = new byte[i];
	}

	synchronized int _mthcase() {
		if (closedFlag2)
			return 0;
		else
			return _mthint();
	}

	public int _mthdo() {
		return _flddo;
	}

	synchronized int _mthfor() throws IOException {
		synchronized (readLock) {
			if (closedFlag2)
				throw new IOException("Stream closed");
			long l = System.currentTimeMillis();
			while (_mthcase() == 0) {
				if (closedFlag) {
					byte byte0 = -1;
					return byte0;
				}
				long currentTime = System.currentTimeMillis();
				if (_flddo != 0 && currentTime - l >= _flddo)
					throw new InterruptedIOException();
				try {
					if (_flddo == 0)
						wait();
					else
						wait((_flddo + currentTime) - l);
				} catch (InterruptedException _ex) {
					throw new InterruptedIOException();
				}
				if (closedFlag2)
					throw new IOException("Stream closed");
			}
			byte byte1 = buffer[_fldchar++];
			if (_fldchar == buffer.length)
				_fldchar = 0;
			notifyAll();
			int i = byte1 >= 0 ? ((int) (byte1)) : byte1 + 256;
			return i;
		}
	}

	synchronized int _mthif(byte abyte0[], int i, int j) throws IOException {
		synchronized (readLock) {
			if (closedFlag2)
				throw new IOException("Stream closed");
			long currentTimeMillis = System.currentTimeMillis();
			while (_mthcase() == 0) {
				if (closedFlag) {
					byte byte0 = -1;
					return byte0;
				}
				long l1 = System.currentTimeMillis();
				if (_flddo != 0 && l1 - currentTimeMillis >= _flddo)
					throw new InterruptedIOException();
				try {
					if (_flddo == 0)
						wait();
					else
						wait((_flddo + l1) - currentTimeMillis);
				} catch (InterruptedException _ex) {
					throw new InterruptedIOException();
				}
				if (closedFlag2)
					throw new IOException("Stream closed");
			}
			int i1 = _mthcase();
			int j1 = j <= i1 ? j : i1;
			int k1 = buffer.length - _fldchar <= j1 ? buffer.length - _fldchar
					: j1;
			int i2 = j1 - k1 <= 0 ? 0 : j1 - k1;
			System.arraycopy(buffer, _fldchar, abyte0, i, k1);
			System.arraycopy(buffer, 0, abyte0, i + k1, i2);
			_fldchar = (_fldchar + j1) % buffer.length;
			notifyAll();
			int k = j1;
			return k;
		}
	}

	public void _mthif(int i) {
		synchronized (readLock) {
			_flddo = i;
		}
	}

	private int _mthint() {
		if (_fldbyte >= _fldchar)
			return _fldbyte - _fldchar;
		else
			return (_fldbyte + buffer.length) - _fldchar;
	}

	synchronized void _mthnew() {
		if (closedFlag2) {
			throw new IllegalStateException("Already closed");
		} else {
			closedFlag2 = true;
			notifyAll();
			return;
		}
	}

	synchronized void _mthtry() {
		if (closedFlag) {
			throw new IllegalStateException("Already closed");
		} else {
			closedFlag = true;
			notifyAll();
			return;
		}
	}

	synchronized void a(int i) throws IOException {
		synchronized (writeLock) {
			if (closedFlag2 || closedFlag)
				throw new IOException("Stream closed");
			while (duno() == 0)
				try {
					wait();
				} catch (InterruptedException _ex) {
					throw new InterruptedIOException();
				}
			if (closedFlag2 || closedFlag)
				throw new IOException("Stream closed");
			buffer[_fldbyte++] = (byte) (i & 0xff);
			if (_fldbyte == buffer.length)
				_fldbyte = 0;
			notifyAll();
		}
	}

	private int duno() {
		return buffer.length - _mthint() - 1;
	}

	public TimesealInputStream getTimesealInputStream() {
		return timesealInputStream;
	}

	public TimesealOutputStream getTimesealOutputStream() {
		return timesealOutputStream;
	}

	synchronized void write(byte bytes[], int i, int j) throws IOException {
		synchronized (writeLock) {
			if (closedFlag2 || closedFlag)
				throw new IOException("Stream closed");
			while (j > 0) {
				while (duno() == 0)
					try {
						wait();
					} catch (InterruptedException _ex) {
						throw new InterruptedIOException();
					}
				int k = duno();
				int l = j <= k ? j : k;
				int i1 = buffer.length - _fldbyte < l ? buffer.length
						- _fldbyte : l;
				int j1 = l - i1 <= 0 ? 0 : l - i1;
				System.arraycopy(bytes, i, buffer, _fldbyte, i1);
				System.arraycopy(bytes, i + i1, buffer, 0, j1);
				i += l;
				j -= l;
				_fldbyte = (_fldbyte + l) % buffer.length;
				notifyAll();
			}
		}
	}
}
