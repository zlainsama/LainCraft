package lain.mods.notooltips;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("NoToolTips")
@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.notooltips.")
public class NoToolTips implements IFMLLoadingPlugin
{

    public static boolean RUNTIME_DEOBF = true;

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.notooltips.NoToolTipsTransformer" };
    }

    @Override
    public String[] getLibraryRequestClass()
    {
        return null;
    }

    @Override
    public String getModContainerClass()
    {
        return "lain.mods.notooltips.NoToolTipsDummyContainer";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        RUNTIME_DEOBF = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

}
