package lain.mods.accman;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import com.google.common.base.Objects;
import com.google.common.hash.Hashing;

public final class AccInfo implements Serializable
{

    private static final long serialVersionUID = 5713905927223396726L;

    public static AccInfo read(InputStream stream)
    {
        try
        {
            ObjectInputStream tmp = new ObjectInputStream(stream);
            return (AccInfo) tmp.readObject();
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static boolean write(OutputStream stream, AccInfo info)
    {
        try
        {
            ObjectOutputStream tmp = new ObjectOutputStream(stream);
            tmp.writeObject(info);
            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public final String name;
    public final String encryptedPW;
    public String lastIP = "";
    public long lastSeen = 0L;

    public AccInfo(String name, String password)
    {
        if (name == null)
            name = "";
        if (password == null)
            password = "";
        this.name = name;
        this.encryptedPW = Hashing.md5().hashString(String.format("-LI|%s=%s", name, password)).toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof AccInfo)
        {
            AccInfo tmp = (AccInfo) obj;
            return name.equals(tmp.name) && encryptedPW.equals(tmp.encryptedPW);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name, encryptedPW);
    }

    public AccInfo setPassword(String password)
    {
        AccInfo res = new AccInfo(name, password);
        res.lastIP = lastIP;
        res.lastSeen = lastSeen;
        return res;
    }

}
