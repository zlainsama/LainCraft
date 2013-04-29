package lain.mods.laincraft.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class StreamUtils
{

    public static String compute_md5(byte[] bytes)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            return String.format("%1$032x", new BigInteger(1, digest.digest()));
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    public static byte[] readFully(InputStream stream) throws IOException
    {
        byte[] data = new byte[65536];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int len = -1;
        while ((len = stream.read(data)) != -1)
            if (len > 0)
                buffer.write(data, 0, len);
        return buffer.toByteArray();
    }

}
