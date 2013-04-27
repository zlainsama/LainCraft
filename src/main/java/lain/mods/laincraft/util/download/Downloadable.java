package lain.mods.laincraft.util.download;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Downloadable
{

    public static void closeSilently(Closeable closeable)
    {
        if (closeable != null)
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
            }
    }

    public static String copyAndDigest(InputStream inputStream, OutputStream outputStream) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[65536];
        try
        {
            int read = inputStream.read(buffer);
            while (read >= 1)
            {
                digest.update(buffer, 0, read);
                outputStream.write(buffer, 0, read);
                read = inputStream.read(buffer);
            }
        }
        finally
        {
            closeSilently(inputStream);
            closeSilently(outputStream);
        }

        return String.format("%1$032x", new BigInteger(1, digest.digest()));
    }

    public static String getEtag(HttpURLConnection connection)
    {
        return getEtag(connection.getHeaderField("ETag"));
    }

    public static String getEtag(String etag)
    {
        if (etag == null)
            etag = "-";
        else if ((etag.startsWith("\"")) && (etag.endsWith("\"")))
        {
            etag = etag.substring(1, etag.length() - 1);
        }

        return etag;
    }

    public static String getMD5(File file)
    {
        DigestInputStream stream = null;
        try
        {
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
            byte[] buffer = new byte[65536];

            int read = stream.read(buffer);
            while (read >= 1)
                read = stream.read(buffer);
        }
        catch (Exception ignored)
        {
            return null;
        }
        finally
        {
            closeSilently(stream);
        }

        return String.format("%1$032x", new BigInteger(1, stream.getMessageDigest().digest()));
    }

    private final URL url;
    private final File target;
    private final boolean forceDownload;
    private final Proxy proxy;

    private int numAttempts = 0;
    private boolean finished = false;

    public Downloadable(Proxy proxy, URL remoteFile, File localFile, boolean forceDownload)
    {
        this.proxy = proxy;
        url = remoteFile;
        target = localFile;
        this.forceDownload = forceDownload;
    }

    public String download() throws IOException
    {
        String localMd5 = null;
        numAttempts += 1;

        if ((target.getParentFile() != null) && (!target.getParentFile().isDirectory()))
        {
            target.getParentFile().mkdirs();
        }
        if ((!forceDownload) && (target.isFile()))
        {
            localMd5 = getMD5(target);
        }
        try
        {
            HttpURLConnection connection = makeConnection(localMd5);
            int status = connection.getResponseCode();

            if (status == 304)
                return "Used own copy as it matched etag";
            if (status / 100 == 2)
            {
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(target);
                String md5 = copyAndDigest(inputStream, outputStream);
                String etag = getEtag(connection);

                if (etag.contains("-"))
                {
                    return "Didn't have etag so assuming our copy is good";
                }
                if (etag.equalsIgnoreCase(md5))
                {
                    return "Downloaded successfully and etag matched";
                }
                throw new RuntimeException(String.format("E-tag did not match downloaded MD5 (ETag was %s, downloaded %s)", etag, md5));
            }
            if (target.isFile())
            {
                return "Couldn't connect to server (responded with " + status + ") but have local file, assuming it's good";
            }
            throw new RuntimeException("Server responded with " + status);
        }
        catch (IOException e)
        {
            if (target.isFile())
            {
                return "Couldn't connect to server (" + e.getClass().getSimpleName() + ": '" + e.getMessage() + "') but have local file, assuming it's good";
            }
            throw e;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Missing Digest.MD5", e);
        }
    }

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public File getTarget()
    {
        return target;
    }

    public URL getUrl()
    {
        return url;
    }

    protected HttpURLConnection makeConnection(String localMd5) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");
        if (localMd5 != null)
            connection.setRequestProperty("If-None-Match", localMd5);

        connection.connect();

        return connection;
    }

    public boolean shouldIgnoreLocal()
    {
        return forceDownload;
    }

}
