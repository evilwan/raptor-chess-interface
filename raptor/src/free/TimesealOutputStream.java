// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package free;

import java.io.IOException;
import java.io.OutputStream;

// Referenced classes of package free.a:
//            c

public class TimesealOutputStream extends OutputStream
{

    public TimesealOutputStream(TimesealPipe c1)
    {
        a = c1;
    }

    public void close()
        throws IOException
    {
        a._mthtry();
    }

    public void write(int i)
        throws IOException
    {
        a.a(i);
    }

    public void write(byte abyte0[], int i, int j)
        throws IOException
    {
        a.write(abyte0, i, j);
    }

    private final TimesealPipe a;
}
