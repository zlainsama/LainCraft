package lain.mods.molang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Fetchable
{

    private final Proxy proxy;
    private final URL remoteFile;
    private final File localFile;

    private final boolean useCaches;

    private int numAttempts = 0;

    public Fetchable(Proxy proxy, URL remoteFile, File localFile, boolean useCaches)
    {
        if (proxy == null)
            proxy = Proxy.NO_PROXY;
        this.proxy = proxy;
        this.remoteFile = remoteFile;
        this.localFile = localFile;
        this.useCaches = useCaches;
    }

    public String fetch() throws IOException
    {
        numAttempts += 1;
        if (localFile.getParentFile() != null && !localFile.getParentFile().isDirectory())
            localFile.getParentFile().mkdirs();
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try
        {
            URLConnection conn = makeConnection();
            if (conn instanceof HttpURLConnection)
            {
                int status = ((HttpURLConnection) conn).getResponseCode();
                if (status == 304)
                    return "Server responded with " + status + ", using cached localFile";
                if (status / 100 == 2)
                {
                    rbc = Channels.newChannel(conn.getInputStream());
                    fos = new FileOutputStream(localFile);
                    fos.getChannel().transferFrom(rbc, 0, 16777216L);
                    localFile.setLastModified(conn.getLastModified());
                }
                if (localFile.isFile())
                    return "Downloaded successfully, using localFile";
                throw new RuntimeException("Server responded with " + status);
            }
            rbc = Channels.newChannel(conn.getInputStream());
            fos = new FileOutputStream(localFile);
            fos.getChannel().transferFrom(rbc, 0, 16777216L);
            localFile.setLastModified(conn.getLastModified());
            if (localFile.isFile())
                return "Downloaded successfully, using localFile";
            throw new RuntimeException("Failed to fetch remoteFile");
        }
        catch (IOException e)
        {
            if (localFile.isFile())
                return "Failed to fetch remoteFile (" + e.getClass().getSimpleName() + ": '" + e.getMessage() + "') but localFile exists, using cached localFile";
            throw e;
        }
        finally
        {
            if (rbc != null)
                rbc.close();
            if (fos != null)
                fos.close();
        }
    }

    public File getLocalFile()
    {
        return localFile;
    }

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public URL getRemoteFile()
    {
        return remoteFile;
    }

    private URLConnection makeConnection() throws IOException
    {
        URLConnection conn = remoteFile.openConnection();
        conn.setUseCaches(false);
        conn.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        conn.setRequestProperty("Expires", "0");
        conn.setRequestProperty("Pragma", "no-cache");
        if (useCaches && localFile.isFile())
            conn.setIfModifiedSince(localFile.lastModified());
        conn.connect();
        return conn;
    }

    public void resetNumAttempts()
    {
        numAttempts = 0;
    }

}
