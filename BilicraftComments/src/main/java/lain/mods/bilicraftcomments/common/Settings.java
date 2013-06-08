package lain.mods.bilicraftcomments.common;

import lain.mods.laincraft.utils.configuration.Config;

public class Settings
{
    
    @Config.SingleComment("You can change this and rejoin the server")
    @Config.Property(defaultValue = "true")
    public static String enabled;
    
}
