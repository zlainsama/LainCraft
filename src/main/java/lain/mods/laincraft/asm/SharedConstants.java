package lain.mods.laincraft.asm;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.util.configuration.Config;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

public class SharedConstants
{

    private static File mcDir;
    private static List<IFMLLoadingPlugin> corePlugins;
    private static boolean runtimeDeobfuscationEnabled;
    private static File coreJar;
    private static RelaunchClassLoader actualClassLoader;
    private static String deobfuscationFileName;
    private static Set<Plugin> plugins;

    public static RelaunchClassLoader getActualClassLoader()
    {
        return actualClassLoader;
    }

    public static File getCoreJarFile()
    {
        return coreJar;
    }

    public static List<IFMLLoadingPlugin> getCorePluginsList()
    {
        return corePlugins;
    }

    public static String getDeobfuscationFileName()
    {
        return deobfuscationFileName;
    }

    public static File getLainCraftDirFile()
    {
        if (mcDir == null)
            return null;
        File dir = new File(mcDir, "LainCraft");
        if (!dir.exists() && !dir.mkdirs())
            throw new RuntimeException("cannot create directory \'" + dir.getAbsolutePath() + "\'");
        if (!dir.isDirectory())
            throw new RuntimeException("found a file called \'LainCraft\' in working directory");
        return dir;
    }

    public static File getMinecraftDirFile()
    {
        return mcDir;
    }

    public static Set<Plugin> getPlugins()
    {
        return new TreeSet<Plugin>(plugins);
    }

    public static boolean getRuntimeDeobfuscationEnabled()
    {
        return runtimeDeobfuscationEnabled;
    }

    public static boolean isLain(String username)
    {
        return "zlainsama".equalsIgnoreCase(StringUtils.stripControlCodes(username));
    }

    private static Plugin loadPlugin(String className)
    {
        try
        {
            Plugin plugin = (Plugin) Class.forName(className).newInstance();
            plugin.setConfig(new Config(new File(getLainCraftDirFile(), plugin.getName() + ".cfg")));
            return plugin;
        }
        catch (Throwable t)
        {
            System.err.println(t.toString());
            return null;
        }
    }

    private static void loadPlugins(File archive)
    {
        ZipFile zip = null;
        try
        {
            zip = new ZipFile(archive);
            Set<String> list = readPluginList(zip.getInputStream(zip.getEntry("lcps.list")));
            if (!list.isEmpty() && archive != coreJar)
                actualClassLoader.addURL(archive.toURI().toURL());
            for (String className : list)
            {
                Plugin plugin = loadPlugin(className);
                if (plugin != null)
                    plugins.add(plugin);
            }
        }
        catch (Throwable t)
        {
            System.err.println(t.toString());
        }
        finally
        {
            if (zip != null)
                try
                {
                    zip.close();
                }
                catch (Throwable t)
                {
                    System.err.println(t.toString());
                }
        }
    }

    private static Set<String> readPluginList(InputStream input)
    {
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(input);
            scanner.useDelimiter(Pattern.compile("\r?\n"));
            Set<String> list = new HashSet<String>();
            while (scanner.hasNext())
            {
                String entry = scanner.next().trim();
                if (entry.isEmpty() || entry.startsWith("#"))
                    continue;
                list.add(entry);
            }
            return list;
        }
        catch (Throwable t)
        {
            System.err.println(t.toString());
            return Collections.emptySet();
        }
        finally
        {
            if (scanner != null)
                try
                {
                    scanner.close();
                }
                catch (Throwable t)
                {
                    System.err.println(t.toString());
                }
        }
    }

    protected static void setActualClassLoader(RelaunchClassLoader classLoader)
    {
        actualClassLoader = classLoader;
    }

    protected static void setCoreJarFile(File file)
    {
        coreJar = file;
    }

    protected static void setCorePluginsList(List<IFMLLoadingPlugin> list)
    {
        corePlugins = list;
    }

    protected static void setDeobfuscationFileName(String name)
    {
        deobfuscationFileName = name;
    }

    protected static void setMinecraftDirFile(File file)
    {
        mcDir = file;
    }

    protected static void setRuntimeDeobfuscationEnabled(boolean enabled)
    {
        runtimeDeobfuscationEnabled = enabled;
    }

    protected static void setup()
    {
        plugins = new HashSet<Plugin>();
        plugins.add(loadPlugin("lain.mods.inputfix.InputFix"));
        loadPlugins(coreJar);
        File pluginsDir = new File(getLainCraftDirFile(), "Plugins");
        if (pluginsDir.exists() || pluginsDir.mkdirs() || pluginsDir.isDirectory())
            for (File file : pluginsDir.listFiles())
                if (file.isFile())
                    loadPlugins(file);
    }

}
