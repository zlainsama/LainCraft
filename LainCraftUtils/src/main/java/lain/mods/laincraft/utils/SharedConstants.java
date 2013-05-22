package lain.mods.laincraft.utils;

import java.io.File;
import java.util.List;
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

    public static boolean getRuntimeDeobfuscationEnabled()
    {
        return runtimeDeobfuscationEnabled;
    }

    public static boolean isLain(String username)
    {
        return "zlainsama".equalsIgnoreCase(StringUtils.stripControlCodes(username));
    }

    public static void setActualClassLoader(RelaunchClassLoader classLoader)
    {
        actualClassLoader = classLoader;
    }

    public static void setCoreJarFile(File file)
    {
        coreJar = file;
    }

    public static void setCorePluginsList(List<IFMLLoadingPlugin> list)
    {
        corePlugins = list;
    }

    public static void setDeobfuscationFileName(String name)
    {
        deobfuscationFileName = name;
    }

    public static void setMinecraftDirFile(File file)
    {
        mcDir = file;
    }

    public static void setRuntimeDeobfuscationEnabled(boolean enabled)
    {
        runtimeDeobfuscationEnabled = enabled;
    }

}
