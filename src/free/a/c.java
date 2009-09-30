// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package free.a;

import java.io.IOException;
import java.io.InterruptedIOException;

// Referenced classes of package free.a:
//            a, b

public class c
{

    public c()
    {
        this(2048);
    }

    public c(int i)
    {
        _flddo = 0;
        _fldchar = 0;
        _fldbyte = 0;
        _fldint = false;
        _fldif = false;
        a = new String("Write Lock for PipedStreams");
        _fldtry = new String("Read Lock for PipedStream");
        _fldcase = new a(this);
        _fldnew = new b(this);
        _fldfor = new byte[i];
    }

    synchronized int _mthcase()
    {
        if(_fldif)
            return 0;
        else
            return _mthint();
    }

    private int _mthint()
    {
        if(_fldbyte >= _fldchar)
            return _fldbyte - _fldchar;
        else
            return (_fldbyte + _fldfor.length) - _fldchar;
    }

    private int a()
    {
        return _fldfor.length - _mthint() - 1;
    }

    synchronized void _mthnew()
    {
        if(_fldif)
        {
            throw new IllegalStateException("Already closed");
        } else
        {
            _fldif = true;
            notifyAll();
            return;
        }
    }

    synchronized void _mthtry()
    {
        if(_fldint)
        {
            throw new IllegalStateException("Already closed");
        } else
        {
            _fldint = true;
            notifyAll();
            return;
        }
    }

    public a _mthif()
    {
        return _fldcase;
    }

    public b _mthbyte()
    {
        return _fldnew;
    }

    public int _mthdo()
    {
        return _flddo;
    }

    synchronized int _mthfor()
        throws IOException
    {
        synchronized(_fldtry)
        {
            if(_fldif)
                throw new IOException("Stream closed");
            long l = System.currentTimeMillis();
            while(_mthcase() == 0) 
            {
                if(_fldint)
                {
                    byte byte0 = -1;
                    return byte0;
                }
                long l1 = System.currentTimeMillis();
                if(_flddo != 0 && l1 - l >= (long)_flddo)
                    throw new InterruptedIOException();
                try
                {
                    if(_flddo == 0)
                        wait();
                    else
                        wait(((long)_flddo + l1) - l);
                }
                catch(InterruptedException _ex)
                {
                    throw new InterruptedIOException();
                }
                if(_fldif)
                    throw new IOException("Stream closed");
            }
            byte byte1 = _fldfor[_fldchar++];
            if(_fldchar == _fldfor.length)
                _fldchar = 0;
            notifyAll();
            int i = byte1 >= 0 ? ((int) (byte1)) : byte1 + 256;
            return i;
        }
    }

    synchronized int _mthif(byte abyte0[], int i, int j)
        throws IOException
    {
        synchronized(_fldtry)
        {
            if(_fldif)
                throw new IOException("Stream closed");
            long l = System.currentTimeMillis();
            while(_mthcase() == 0) 
            {
                if(_fldint)
                {
                    byte byte0 = -1;
                    return byte0;
                }
                long l1 = System.currentTimeMillis();
                if(_flddo != 0 && l1 - l >= (long)_flddo)
                    throw new InterruptedIOException();
                try
                {
                    if(_flddo == 0)
                        wait();
                    else
                        wait(((long)_flddo + l1) - l);
                }
                catch(InterruptedException _ex)
                {
                    throw new InterruptedIOException();
                }
                if(_fldif)
                    throw new IOException("Stream closed");
            }
            int i1 = _mthcase();
            int j1 = j <= i1 ? j : i1;
            int k1 = _fldfor.length - _fldchar <= j1 ? _fldfor.length - _fldchar : j1;
            int i2 = j1 - k1 <= 0 ? 0 : j1 - k1;
            System.arraycopy(_fldfor, _fldchar, abyte0, i, k1);
            System.arraycopy(_fldfor, 0, abyte0, i + k1, i2);
            _fldchar = (_fldchar + j1) % _fldfor.length;
            notifyAll();
            int k = j1;
            return k;
        }
    }

    public void _mthif(int i)
    {
        synchronized(_fldtry)
        {
            _flddo = i;
        }
    }

    synchronized void a(int i)
        throws IOException
    {
        synchronized(a)
        {
            if(_fldif || _fldint)
                throw new IOException("Stream closed");
            while(a() == 0) 
                try
                {
                    wait();
                }
                catch(InterruptedException _ex)
                {
                    throw new InterruptedIOException();
                }
            if(_fldif || _fldint)
                throw new IOException("Stream closed");
            _fldfor[_fldbyte++] = (byte)(i & 0xff);
            if(_fldbyte == _fldfor.length)
                _fldbyte = 0;
            notifyAll();
        }
    }

    synchronized void a(byte abyte0[], int i, int j)
        throws IOException
    {
        synchronized(a)
        {
            if(_fldif || _fldint)
                throw new IOException("Stream closed");
            while(j > 0) 
            {
                while(a() == 0) 
                    try
                    {
                        wait();
                    }
                    catch(InterruptedException _ex)
                    {
                        throw new InterruptedIOException();
                    }
                int k = a();
                int l = j <= k ? j : k;
                int i1 = _fldfor.length - _fldbyte < l ? _fldfor.length - _fldbyte : l;
                int j1 = l - i1 <= 0 ? 0 : l - i1;
                System.arraycopy(abyte0, i, _fldfor, _fldbyte, i1);
                System.arraycopy(abyte0, i + i1, _fldfor, 0, j1);
                i += l;
                j -= l;
                _fldbyte = (_fldbyte + l) % _fldfor.length;
                notifyAll();
            }
        }
    }

    private final a _fldcase;
    private final b _fldnew;
    private volatile int _flddo;
    private final byte _fldfor[];
    private int _fldchar;
    private int _fldbyte;
    private boolean _fldint;
    private boolean _fldif;
    private Object a;
    private Object _fldtry;
}
