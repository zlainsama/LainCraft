package lain.mods.laincraft.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
        try
        {
            byte[] data = new byte[65536];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int len = -1;
            while ((len = stream.read(data)) != -1)
                if (len > 0)
                    buffer.write(data, 0, len);
            return buffer.toByteArray();
        }
        finally
        {
            stream.close();
        }
    }

    public static boolean saveAsFile(InputStream stream, File file)
    {
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try
        {
            rbc = Channels.newChannel(stream);
            fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, 16777216L);
            return true;
        }
        catch (IOException ignored)
        {
            return false;
        }
        finally
        {
            if (rbc != null)
                try
                {
                    rbc.close();
                }
                catch (IOException ignored)
                {
                }
            if (fos != null)
                try
                {
                    fos.close();
                }
                catch (IOException ignored)
                {
                }
        }
    }

}
