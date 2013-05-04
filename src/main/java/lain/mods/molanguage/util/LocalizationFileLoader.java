package lain.mods.molanguage.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import lain.mods.laincraft.util.UnicodeInputStreamReader;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public abstract class LocalizationFileLoader
{

    public static final List<LocalizationFileLoader> loaders = new ArrayList<LocalizationFileLoader>();

    public static boolean loadDefault(InputStream stream, Localization container, Properties env, Set<String> authors)
    {
        BufferedReader buf = null;
        try
        {
            List<ModContainer> mods = Loader.instance().getActiveModList();
            UnicodeInputStreamReader is = new UnicodeInputStreamReader(stream, env.getProperty("defaultEncoding", "UTF-8"));
            buf = new BufferedReader(is);
            String line = null;
            int num = 0;
            while ((line = buf.readLine()) != null)
            {
                num++;
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (line.startsWith("#"))
                {
                    if (env.getProperty("lang") == null && num == 1)
                        env.setProperty("lang", line.indexOf(" ") != -1 ? line.substring(1, line.indexOf(" ")) : line.substring(1));
                    else if (line.equals("#langclear"))
                        env.remove("lang");
                    else if (line.equals("#headclear"))
                        env.remove("head");
                    else if (line.equals("#tailclear"))
                        env.remove("tail");
                    else if (line.equals("#modclear"))
                        env.remove("mod");
                    else if (line.equals("#versionclear"))
                        env.remove("version");
                    else if (line.startsWith("#lang="))
                        env.setProperty("lang", line.substring(6));
                    else if (line.startsWith("#head="))
                        env.setProperty("head", line.substring(6));
                    else if (line.startsWith("#tail="))
                        env.setProperty("tail", line.substring(6));
                    else if (line.startsWith("#mod="))
                        env.setProperty("mod", line.substring(5));
                    else if (line.startsWith("#version="))
                        env.setProperty("version", line.substring(9));
                    else if (line.startsWith("#author="))
                        authors.add(line.substring(8));
                }
                else if (line.indexOf("=") != -1)
                {
                    String lang = env.getProperty("lang");
                    String head = env.getProperty("head");
                    String tail = env.getProperty("tail");
                    String mod = env.getProperty("mod");
                    String version = env.getProperty("version");
                    String k = line.substring(0, line.indexOf("=")).trim();
                    String v = line.substring(line.indexOf("=") + 1).trim();
                    if (lang == null || lang.isEmpty())
                        continue;
                    if (head != null && !head.isEmpty())
                        v = head + v;
                    if (tail != null && !tail.isEmpty())
                        v = v + tail;
                    if (mod != null && !mod.isEmpty())
                    {
                        ModContainer found = null;
                        for (ModContainer mc : mods)
                            if (mod.equals(mc.getModId()))
                            {
                                found = mc;
                                break;
                            }
                        if (found == null)
                            continue;
                        if (version != null && !version.isEmpty())
                            if (!version.equals(found.getVersion()))
                                continue;
                    }
                    container.put(k, v, lang);
                }
            }
            return true;
        }
        catch (Throwable t)
        {
            System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
            return false;
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                    System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
        }
    }

    public abstract boolean load(InputStream stream, Localization container, Properties env, Set<String> authors);

}
