package lain.mods.molang;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "MoLanguage", name = "MoLanguage", version = "1.6.x-v1", dependencies = "after:*")
public class MoLanguage
{

    public static TranslationTable tableLocal;
    public static TranslationTable tableRemote;

    private void dump() throws IOException
    {
        File dir = new File(Configs.baseDir, "langDump");
        if (dir.exists() || dir.mkdirs())
        {
            TranslationTable tableVanilla = new TranslationTable();
            TranslationTable tableActualGame = new TranslationTable();
            if (Configs.dumpLang.isEmpty())
            {
                ModCompatibilities.loadVanillaTable(tableVanilla);
                ModCompatibilities.loadActualGameTable(tableActualGame);
            }
            else
            {
                ModCompatibilities.loadVanillaTable(tableVanilla, Configs.dumpLang);
                ModCompatibilities.loadActualGameTable(tableActualGame, Configs.dumpLang);
            }
            for (String lang : tableActualGame.getTranslationTable().columnKeySet())
            {
                Map<String, String> m = tableActualGame.getTranslationTableForLang(lang);
                Map<String, String> toDump = Maps.newHashMap();
                for (String key : m.keySet())
                {
                    String value = m.get(key);
                    if (value == null)
                        continue; // just in case
                    if (Configs.dumpExtraOnly)
                    {
                        String valueVanilla = tableVanilla.translateKey(key, lang);
                        String valueLocal = tableLocal.translateKey(key, lang);
                        if (value.equals(valueVanilla) || value.equals(valueLocal))
                            continue;
                    }
                    toDump.put(key, value);
                }
                String newLine = System.getProperty("line.separator");
                FileOutputStream fos = new FileOutputStream(new File(dir, String.format("%s.lang", lang)));
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                buffer.write("#" + lang + newLine);
                buffer.write("# the first line (above this, which is a comment line) IS REQUIRED for MoLanguage to verify lang files" + newLine);
                buffer.write("# WARNING: this file was saved in UTF-8" + newLine + newLine);
                for (String key : new TreeSet<String>(toDump.keySet()))
                    buffer.write(key + " = " + toDump.get(key) + newLine);
                buffer.write(newLine + "# dump on " + DateFormat.getInstance().format(new Date()));
                buffer.close();
                fos.close();
            }
            Configs.dump = false;
            Configs.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isServer())
            return;
        final File baseDir = new File(event.getModConfigurationDirectory(), "MoLanguage");
        if (baseDir.exists() || baseDir.mkdirs())
        {
            Configs.baseDir = baseDir;
            Configs.load();
            Configs.save();
        }
    }

    @Mod.EventHandler
    public void modsLoaded(FMLPostInitializationEvent event)
    {
        if (event.getSide().isServer())
            return;
        if (!Configs.ModEnabled)
            return;
        try
        {
            tableLocal = LocalTranslationFileLoader.load(Configs.baseDir);
            ModCompatibilities.importTable(tableLocal);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        if (Configs.dump)
        {
            try
            {
                dump();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        if (Configs.OnlineEnabled)
        {
            try
            {
                new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        try
                        {
                            tableRemote = RemoteTranslationFileLoader.load(Configs.baseDir);
                            ModCompatibilities.importTable(tableRemote);
                            ModCompatibilities.importTable(tableLocal); // local overrides remote
                        }
                        catch (Throwable t)
                        {
                            t.printStackTrace();
                        }
                    }

                }).start();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

}
