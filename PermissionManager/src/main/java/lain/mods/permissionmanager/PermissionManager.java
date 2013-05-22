package lain.mods.permissionmanager;

import java.io.File;
import lain.mods.laincraft.core.SharedConstants;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = "PermissionManager", name = "PermissionManager", version = "", dependencies = "required-after:LainCraftCore", useMetadata = true)
public class PermissionManager
{

    @Mod.Init
    public void load(FMLInitializationEvent event)
    {
        File dir = new File(SharedConstants.getLainCraftDirFile(), "Permissions");
        if (dir.exists() || dir.mkdirs() || dir.isDirectory())
            new lain.mods.permissionmanager.permission.PermissionManager(dir);
    }

}
