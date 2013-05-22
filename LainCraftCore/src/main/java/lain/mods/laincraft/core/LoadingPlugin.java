package lain.mods.laincraft.core;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.laincraft.core")
public class LoadingPlugin implements IFMLLoadingPlugin, IFMLCallHook
{

    @Override
    public Void call() throws Exception
    {
        discoverTransformers(SharedConstants.getLainCraftDirFile());
        discoverTransformers(new File(SharedConstants.getMinecraftDirFile(), "coremods"));
        discoverTransformers(new File(SharedConstants.getMinecraftDirFile(), "mods"));
        return null;
    }

    private void discoverTransformers(File dir)
    {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File f : dir.listFiles())
            if (f.isFile() && f.canRead())
            {
                JarFile jar = null;
                try
                {
                    jar = new JarFile(f);
                    String transformer = jar.getManifest().getMainAttributes().getValue("ASMTransformer");
                    if (transformer != null && !transformer.isEmpty())
                    {
                        SharedConstants.getActualClassLoader().addURL(f.toURI().toURL());
                        SharedConstants.getActualClassLoader().registerTransformer(transformer);
                    }
                }
                catch (Throwable ignored)
                {
                }
                finally
                {
                    if (jar != null)
                        try
                        {
                            jar.close();
                        }
                        catch (Throwable ignored)
                        {
                        }
                }
            }
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.laincraft.core.ASMTransformer" };
    }

    @Override
    public String[] getLibraryRequestClass()
    {
        return null;
    }

    @Override
    public String getModContainerClass()
    {
        return "lain.mods.laincraft.core.LainCraftCore";
    }

    @Override
    public String getSetupClass()
    {
        return "lain.mods.laincraft.core.LoadingPlugin";
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
