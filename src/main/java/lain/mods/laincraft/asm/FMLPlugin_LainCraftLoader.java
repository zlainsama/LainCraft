package lain.mods.laincraft.asm;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.laincraft.asm")
public class FMLPlugin_LainCraftLoader implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return null;
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
        return null;
    }

    @Override
    public void injectData(Map<String, Object> arg0)
    {
    }

}
