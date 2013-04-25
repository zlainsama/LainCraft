package lain.mods.laincraft.asm;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.laincraft.asm")
public class FMLPlugin_LainCraftLoader implements IFMLLoadingPlugin, IFMLCallHook
{

    public static RelaunchClassLoader classLoader;

    @Override
    public Void call() throws Exception
    {
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]
            {
                "lain.mods.laincraft.asm.PlayerHooks"
            };
    }

    @Override
    public String[] getLibraryRequestClass()
    {
        return null;
    }

    @Override
    public String getModContainerClass()
    {
        return "lain.mods.laincraft.LainCraft";
    }

    @Override
    public String getSetupClass()
    {
        return "lain.mods.laincraft.asm.FMLPlugin_LainCraftLoader";
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        if (data.containsKey("classLoader"))
            classLoader = (RelaunchClassLoader) data.get("classLoader");
    }

}
