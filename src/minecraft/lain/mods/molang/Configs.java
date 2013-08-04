package lain.mods.molang;

import java.io.File;
import java.util.Arrays;
import net.minecraftforge.common.Configuration;

public class Configs
{

    public static File baseDir;
    public static Configuration config;

    public static boolean ModEnabled = true;
    public static boolean dump = false;
    public static String dumpLang = "";
    public static boolean dumpExtraOnly = true;
    public static boolean OnlineEnabled = true;
    public static boolean OnlineSkipVerification = true;
    public static String[] OnlineProviders = { "http://tab.mcbbc.com/lang/%s" };

    private static String[] decode(String[] array)
    {
        String[] a = Arrays.copyOf(array, array.length);
        for (int i = 0; i < a.length; i++)
            a[i] = a[i].replace("0S1L2A2S1H0", "/").replace("0P1E2R3C3E2N1T0", "%");
        return a;
    }

    private static String[] encode(String[] array)
    {
        String[] a = Arrays.copyOf(array, array.length);
        for (int i = 0; i < a.length; i++)
            a[i] = a[i].replace("/", "0S1L2A2S1H0").replace("%", "0P1E2R3C3E2N1T0");
        return a;
    }

    public static void load()
    {
        if (config == null && baseDir == null)
            return;
        if (config == null)
            config = new Configuration(new File(baseDir, "MoLanguage.cfg"));
        config.load();
        ModEnabled = config.get(Configuration.CATEGORY_GENERAL, "enabled", ModEnabled).getBoolean(ModEnabled);
        dump = config.get("Advanced", "dump", dump).getBoolean(dump);
        dumpLang = config.get("Advanced", "dumpLang", dumpLang).getString();
        dumpExtraOnly = config.get("Advanced", "dumpExtraOnly", dumpExtraOnly).getBoolean(dumpExtraOnly);
        OnlineEnabled = config.get("OnlineTranslation", "enabled", OnlineEnabled).getBoolean(OnlineEnabled);
        OnlineSkipVerification = config.get("OnlineTranslation", "skipVerification", OnlineSkipVerification).getBoolean(OnlineSkipVerification);
        OnlineProviders = encode(OnlineProviders);
        OnlineProviders = config.get("OnlineTranslation", "providers", OnlineProviders).getStringList();
        OnlineProviders = decode(OnlineProviders);
    }

    public static void save()
    {
        if (config == null && baseDir == null)
            return;
        if (config == null)
            config = new Configuration(new File(baseDir, "MoLanguage.cfg"));
        config.get(Configuration.CATEGORY_GENERAL, "enabled", ModEnabled).set(ModEnabled);
        config.get("Advanced", "dump", dump).set(dump);
        config.get("Advanced", "dumpLang", dumpLang).set(dumpLang);
        config.get("Advanced", "dumpExtraOnly", dumpExtraOnly).set(dumpExtraOnly);
        config.get("OnlineTranslation", "enabled", OnlineEnabled).set(OnlineEnabled);
        config.get("OnlineTranslation", "skipVerification", OnlineSkipVerification).set(OnlineSkipVerification);
        OnlineProviders = encode(OnlineProviders);
        config.get("OnlineTranslation", "providers", OnlineProviders).set(OnlineProviders);
        OnlineProviders = decode(OnlineProviders);
        config.save();
    }

}
