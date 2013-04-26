package lain.mods.laincraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import com.google.common.base.CharMatcher;

public class FileLocator
{

    public static boolean useCache = true;

    private static File download(String url) throws IOException
    {
        File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "lcf-cache");
        if (!tempDir.exists())
            tempDir.mkdirs();
        String id = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("-_.")).negate().replaceFrom(url.toString(), '_');
        File file = new File(tempDir, id);
        if ((file.exists()) && (useCache))
        {
            return file;
        }
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try
        {
            rbc = Channels.newChannel(new URL(url.replace('\\', '/')).openStream());
            fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0L, 16777216L);
        }
        finally
        {
            if (rbc != null)
                rbc.close();
            if (fos != null)
                fos.close();
        }
        return file;
    }

    public static File getFile(String path) throws IOException
    {
        if (isHTTPURL(path))
            return download(path);
        return new File(path);
    }

    public static boolean isHTTPURL(String string)
    {
        return (string.startsWith("http://")) || (string.startsWith("https://"));
    }

}
