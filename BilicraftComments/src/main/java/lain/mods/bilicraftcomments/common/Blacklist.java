package lain.mods.bilicraftcomments.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.io.UnicodeInputStreamReader;
import net.minecraft.util.StringUtils;

public class Blacklist
{

    private static Blacklist instance;

    public static void add(String username)
    {
        if (instance != null)
            instance.list.add(StringUtils.stripControlCodes(username).toLowerCase());
    }

    public static boolean contains(String username)
    {
        if (instance != null)
            return instance.list.contains(StringUtils.stripControlCodes(username).toLowerCase());
        return false;
    }

    public static void load()
    {
        File f = new File(SharedConstants.getMinecraftDirFile(), "BcC_Blacklist.txt");
        if (!f.exists())
            try
            {
                f.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        Blacklist.load(f);
    }

    public static void load(File file)
    {
        BufferedReader buf = null;
        Blacklist backup = instance;
        try
        {
            instance = new Blacklist();
            buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                add(line);
            }
        }
        catch (Exception e)
        {
            instance = backup;
            System.err.println("error loading comment blacklist: " + e.toString());
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException ignored)
                {
                }
        }
    }

    public static void remove(String username)
    {
        if (instance != null)
            instance.list.remove(StringUtils.stripControlCodes(username).toLowerCase());
    }

    public static void save()
    {
        File f = new File(SharedConstants.getMinecraftDirFile(), "BcC_Blacklist.txt");
        if (!f.exists())
            try
            {
                f.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        Blacklist.save(f);
    }

    public static void save(File file)
    {
        if (instance != null)
        {
            BufferedWriter buf = null;
            try
            {
                buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                buf.write("# This file was saved in UTF-8 format!");
                buf.newLine();
                for (String n : new TreeSet<String>(instance.list))
                {
                    buf.write(n);
                    buf.newLine();
                }
            }
            catch (Exception e)
            {
                System.err.println("error saving comment blacklist: " + e.toString());
            }
            finally
            {
                if (buf != null)
                    try
                    {
                        buf.close();
                    }
                    catch (IOException ignored)
                    {
                    }
            }
        }
    }

    private Set<String> list = new HashSet();

}
