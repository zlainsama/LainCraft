package lain.mods.laincraft.util;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.util.download.DownloadJob;
import lain.mods.laincraft.util.download.DownloadListener;
import lain.mods.laincraft.util.download.Downloadable;

public class FileLocator
{

    public static Proxy proxy = Proxy.NO_PROXY;
    public static boolean useCache = true;

    private static File download(String url) throws IOException
    {
        File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "lcf-cache");
        if (!tempDir.exists())
            tempDir.mkdirs();
        String id = filterFilename(url);
        File file = new File(tempDir, id);
        final boolean[] flag = new boolean[] { false };
        DownloadJob job = new DownloadJob(url, true, new DownloadListener()
        {
            @Override
            public void onDownloadJobFinished(DownloadJob job)
            {
                flag[0] = true;
            }

            @Override
            public void onDownloadJobProgressChanged(DownloadJob job)
            {
            }
        }, Collections.singleton(new Downloadable(proxy, new URL(url), file, !useCache)));
        job.startDownloading(LainCraft.getExecutorService());
        while (!flag[0])
            try
            {
                Thread.sleep(100L);
            }
            catch (InterruptedException e)
            {
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
