package lain.mods.laincraft.asm;

import java.io.File;
import java.util.List;
import java.util.Map;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.laincraft.asm")
public class LainCraftCorePlugin implements IFMLLoadingPlugin, IFMLCallHook
{

    @Override
    public Void call() throws Exception
    {
        SharedConstants.setup();
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.laincraft.asm.transformers.InputFix", "lain.mods.laincraft.asm.transformers.PlayerHooks", "lain.mods.laincraft.asm.transformers.ServerHooks" };
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
        return "lain.mods.laincraft.asm.LainCraftCorePlugin";
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        if (data.containsKey("mcLocation"))
            SharedConstants.setMinecraftDirFile((File) data.get("mcLocation"));
        if (data.containsKey("coremodList"))
            SharedConstants.setCorePluginsList((List<IFMLLoadingPlugin>) data.get("coremodList"));
        if (data.containsKey("runtimeDeobfuscationEnabled"))
            SharedConstants.setRuntimeDeobfuscationEnabled((Boolean) data.get("runtimeDeobfuscationEnabled"));
        if (data.containsKey("coremodLocation"))
            SharedConstants.setCoreJarFile((File) data.get("coremodLocation"));
        if (data.containsKey("classLoader"))
            SharedConstants.setActualClassLoader((RelaunchClassLoader) data.get("classLoader"));
        if (data.containsKey("deobfuscationFileName"))
            SharedConstants.setDeobfuscationFileName((String) data.get("deobfuscationFileName"));
    }

}
