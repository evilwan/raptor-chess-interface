// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package raptor.connector.ics.timeseal;

import java.io.IOException;
import java.io.OutputStream;

// Referenced classes of package free.a:
//            c

/**
 * This code was reverse engineered from the JIN project. JIN is a gpled
 * project. Its url can be found here: http://www.jinchess.com/
 */
public class TimesealOutputStream extends OutputStream {

	private final TimesealPipe a;

	public TimesealOutputStream(TimesealPipe c1) {
		a = c1;
	}

	@Override
	public void close() throws IOException {
		a._mthtry();
	}

	@Override
	public void write(byte abyte0[], int i, int j) throws IOException {
		a.write(abyte0, i, j);
	}

	@Override
	public void write(int i) throws IOException {
		a.a(i);
	}
}
