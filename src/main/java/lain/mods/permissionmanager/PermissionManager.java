package lain.mods.permissionmanager;

import java.io.File;
import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.asm.SharedConstants;
import lain.mods.laincraft.util.configuration.Config;

public class PermissionManager extends Plugin
{

    @Config.SingleComment("enable PermissionManager?")
    @Config.Property(defaultValue = "false")
    public static boolean enablePermissionManager;

    @Override
    public String getName()
    {
        return "PermissionManager";
    }

    @Override
    public void onDisable()
    {
    }

    @Override
    public void onEnable()
    {
        Config config = getConfig();
        config.register(PermissionManager.class, null);
        config.load();
        config.save();
        if (!enablePermissionManager)
            setEnabled(false);
        else
        {
            File dir = new File(SharedConstants.getLainCraftDirFile(), "Permissions");
            if (dir.exists() || dir.mkdirs() || dir.isDirectory())
                new lain.mods.permissionmanager.permission.PermissionManager(dir);
        }
    }

}
