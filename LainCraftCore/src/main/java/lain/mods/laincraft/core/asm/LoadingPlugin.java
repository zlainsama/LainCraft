package lain.mods.laincraft.core.asm;

import java.io.File;
import java.util.List;
import java.util.Map;
import lain.mods.laincraft.utils.SharedConstants;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.laincraft.core.asm")
public class LoadingPlugin implements IFMLLoadingPlugin, IFMLCallHook
{

    @Override
    public Void call() throws Exception
    {
        return null;
    }

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
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return "lain.mods.laincraft.core.asm.LoadingPlugin";
    }

    @Override
    public void injectData(Map<String, Object> arg0)
    {
        if (arg0.containsKey("mcLocation"))
            SharedConstants.setMinecraftDirFile((File) arg0.get("mcLocation"));
        if (arg0.containsKey("coremodList"))
            SharedConstants.setCorePluginsList((List<IFMLLoadingPlugin>) arg0.get("coremodList"));
        if (arg0.containsKey("runtimeDeobfuscationEnabled"))
            SharedConstants.setRuntimeDeobfuscationEnabled((Boolean) arg0.get("runtimeDeobfuscationEnabled"));
        if (arg0.containsKey("coremodLocation"))
            SharedConstants.setCoreJarFile((File) arg0.get("coremodLocation"));
        if (arg0.containsKey("classLoader"))
            SharedConstants.setActualClassLoader((RelaunchClassLoader) arg0.get("classLoader"));
        if (arg0.containsKey("deobfuscationFileName"))
            SharedConstants.setDeobfuscationFileName((String) arg0.get("deobfuscationFileName"));
    }

}
