package lain.mods.laincraft.util;

import java.io.File;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class MinecraftUtils
{

    public static File getActiveSaveDirectory()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null)
            return null;
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
            return new File(server.getFile("saves"), server.worldServerForDimension(0).getSaveHandler().getWorldDirectoryName());
        else
            return server.getFile(server.getFolderName());
    }

    public static File getCustomSaveDirectory(String name)
    {
        File dir = new File(new File(getActiveSaveDirectory(), "LainCraft"), name);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

}
