package lain.mods.bilicraftcomments.common;

import java.util.HashSet;
import java.util.Set;
import lain.mods.laincraft.utils.configuration.Config;

public class Settings
{

    @Config.Property(defaultValue = "0;1;2")
    public static String allowedMode;

    @Config.Property(defaultValue = "40")
    public static int minLifespan;

    @Config.Property(defaultValue = "400")
    public static int maxLifespan;

    @Config.Property(defaultValue = "100")
    public static int commentInterval;

    @Config.Property(defaultValue = "true")
    public static boolean whitelistMode;

    private static final Set<Integer> modes = new HashSet();

    public static boolean isModeAllowed(int mode)
    {
        return modes.contains(mode);
    }

    public static void update()
    {
        for (String s : allowedMode.split(";"))
            try
            {
                modes.add(Integer.parseInt(s));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    }

}
