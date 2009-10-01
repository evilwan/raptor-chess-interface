// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package free;

import java.io.IOException;
import java.io.InputStream;

// Referenced classes of package free.a:
//            c

class TimesealInputStream extends InputStream
{

    public TimesealInputStream(TimesealPipe c1)
    {
        a = c1;
    }

    public int available()
    {
        return a._mthcase();
    }

    public void close()
        throws IOException
    {
        a._mthnew();
    }

    public int read()
        throws IOException
    {
        return a._mthfor();
    }

    public int read(byte abyte0[], int i, int j)
        throws IOException
    {
        return a._mthif(abyte0, i, j);
    }

    private final TimesealPipe a;
}
