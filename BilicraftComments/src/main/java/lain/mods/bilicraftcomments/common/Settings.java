package lain.mods.bilicraftcomments.common;

import lain.mods.laincraft.utils.configuration.Config;

public class Settings
{

    @Config.Property(defaultValue = "0;1;2")
    public static String allowedMode;

    @Config.Property(defaultValue = "40")
    public static int minLifespan;

    @Config.Property(defaultValue = "400")
    public static int maxLifespan;

    public static boolean isModeAllowed(int mode)
    {
        String a1 = Integer.toString(mode);
        String[] a2 = allowedMode.split(";");
        for (String a3 : a2)
            if (a1.equals(a3))
                return true;
        return false;
    }

}
