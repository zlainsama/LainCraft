package lain.mods.molang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.Loader;

public class RemoteTranslationFileLoader
{

    private static File download(Fetchable f, int maxAttempts)
    {
        while (f.getNumAttempts() < maxAttempts)
        {
            try
            {
                System.out.println(String.format("fetching%s \'%s\' to \'%s\'", f.getNumAttempts() > 0 ? String.format(" (attempt %d)", f.getNumAttempts() + 1) : "", f.getRemoteFile(), f.getLocalFile()));
                System.out.println(f.fetch());
                return f.getLocalFile();
            }
            catch (Throwable t)
            {
                System.err.println(t.toString());
            }
        }
        return null;
    }

    private static String filterFilename(String filename)
    {
        if (filename == null)
            return null;
        StringBuilder s = new StringBuilder(filename.length());
        for (char c : filename.toCharArray())
            if (Character.isLetterOrDigit(c) || '-' == c || '_' == c || '.' == c)
                s.append(c);
            else
                s.append('_');
        return s.toString();
    }

    public static TranslationTable load(File baseDir) throws IOException
    {
        TranslationTable t = new TranslationTable();
        File dir = new File(baseDir, "langOnlineTemp");
        if (dir.exists() || dir.mkdirs())
        {
            for (String provider : Configs.OnlineProviders)
                load0(t, provider, dir);
        }
        return t;
    }

    private static void load0(TranslationTable t, String baseURL, File tempDir) throws IOException
    {
        String s = String.format(baseURL, "langlist.list");
        File fileList = download(new Fetchable(Proxy.NO_PROXY, new URL(s), new File(tempDir, filterFilename(s)), false), 5);
        if (fileList != null)
        {
            FileInputStream data = null;
            try
            {
                data = new FileInputStream(fileList);
                load1(t, baseURL, tempDir, CharStreams.readLines(new UnicodeInputStreamReader(data, "UTF-8"), new LineProcessor<Multimap<String, String>>()
                {

                    Multimap<String, String> files = HashMultimap.create();
                    boolean flag = Configs.OnlineSkipVerification;

                    @Override
                    public Multimap<String, String> getResult()
                    {
                        return files;
                    }

                    @Override
                    public boolean processLine(String line) throws IOException
                    {
                        if (!flag)
                            flag = line.equals("#langlist");
                        if (!flag)
                            return false;
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            return true;
                        String[] parts = line.split(" ");
                        if (parts.length != 4)
                            return true;
                        files.put(parts[0], String.format("%s/%s", parts[2], parts[1]));
                        return true;
                    }

                }));
            }
            finally
            {
                Closeables.close(data, true);
            }
        }
    }

    private static void load1(TranslationTable t, String baseURL, File tempDir, Multimap<String, String> files) throws IOException
    {
        for (String modname : files.keySet())
        {
            if (!modname.equals("*") && !Loader.isModLoaded(modname))
                continue;
            for (String n : files.get(modname))
            {
                String s = String.format(baseURL, n);
                File langFile = download(new Fetchable(Proxy.NO_PROXY, new URL(s), new File(tempDir, filterFilename(s)), true), 3);
                if (langFile != null)
                {
                    InputStream data = null;
                    try
                    {
                        data = new FileInputStream(langFile);
                        t.importTranslationFile(data, n.substring(0, n.indexOf("/")));
                    }
                    finally
                    {
                        Closeables.close(data, true);
                    }
                }
            }
        }
    }

}
