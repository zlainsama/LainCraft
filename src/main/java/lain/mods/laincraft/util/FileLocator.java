package lain.mods.laincraft.util;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

public class FileLocator
{

    public static Proxy proxy = Proxy.NO_PROXY;
    public static boolean useCache = true;
    public static int maxAttempts = 3;

    private static File download(String url) throws IOException
    {
        File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "lcf-cache");
        if (!tempDir.exists())
            tempDir.mkdirs();
        String id = filterFilename(url);
        File file = new File(tempDir, id);
        int max = maxAttempts;
        Fetchable job = new Fetchable(proxy, new URL(url), file, useCache);
        while (job.getNumAttempts() < max)
        {
            try
            {
                System.out.println(job.fetch());
                break;
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        return file;
    }

    public static String filterFilename(String filename)
    {
        if (filename == null)
            return null;
        StringBuilder var1 = new StringBuilder(filename.length());
        for (char c : filename.toCharArray())
            if (Character.isLetterOrDigit(c) || '-' == c || '_' == c || '.' == c)
                var1.append(c);
            else
                var1.append('_');
        return var1.toString();
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
