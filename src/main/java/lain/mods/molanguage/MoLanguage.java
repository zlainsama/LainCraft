package lain.mods.molanguage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lain.mods.laincraft.event.ClientPlayerSendMessageEvent;
import lain.mods.laincraft.util.FileLocator;
import lain.mods.laincraft.util.StreamUtils;
import lain.mods.laincraft.util.UnicodeInputStreamReader;
import lain.mods.laincraft.util.configuration.Config;
import lain.mods.molanguage.util.Localization;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "MoLanguage", name = "MoLanguage", version = "1.0", dependencies = "required-after:LainCraft;after:*", useMetadata = false)
public class MoLanguage
{

    @Config.SingleComment("just turn it off if you want")
    @Config.Property(name = "enabled", defaultValue = "true")
    private static boolean enabled;
    @Config.SingleComment("dump all mod language data? (auto-reset after one successful dump)")
    @Config.Property(name = "dump", defaultValue = "false")
    private static boolean dump;
    @Config.SingleComment("dump the specific language?")
    @Config.Property(name = "dumpLang", defaultValue = "")
    private static String dumpLang;
    @Config.SingleComment("dump data imported by this mod?")
    @Config.Property(name = "dumpExtra", defaultValue = "false")
    private static boolean dumpExtra;
    @Config.SingleComment("online lang-pack providers (split with \\n)")
    @Config.Property(name = "urlProviders", defaultValue = "http://www.mcbbc.com/lang/zh_CN/%s")
    private static String urlProviders;
    @Config.SingleComment("allow online lang-packs?")
    @Config.Property(name = "allowDownload", defaultValue = "true")
    private static boolean allowDownload;

    @Mod.Metadata
    public static ModMetadata metadata;

    private Config config;
    private Localization lo_vanilla;
    private Localization lo_extra;
    private Localization lo_online;
    private File baseDir;
    private Set<String> modsList;
    private Set<String> loadedFiles_local;
    private Set<String> loadedFiles_online;

    public void dump()
    {
        if (dump)
        {
            try
            {
                if (lo_vanilla == null)
                    lo_vanilla = new Localization();
                loadVanilla();
                for (String lang : (Set<String>) StringTranslate.getInstance().getLanguageList().keySet())
                    if (dumpLang.isEmpty() || dumpLang.equals(lang))
                        dump(lang);
                config.load();
                dump = false;
                config.save();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
            finally
            {
                lo_vanilla = null; // detach from JVM
            }
        }
    }

    public void dump(String lang) throws IOException
    {
        File dir = new File(baseDir, "langDump");
        if (!dir.exists() && !dir.mkdirs())
            throw new Error();
        File f = new File(dir, lang + ".lang");
        String prevLang = StringTranslate.getInstance().getCurrentLanguage();
        StringTranslate.getInstance().setLanguage(lang, true);
        Properties data = StringTranslate.getInstance().translateTable;
        String newLine = System.getProperty("line.separator");
        FileOutputStream fos = new FileOutputStream(f);
        BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
        buffer.write("#" + lang + newLine);
        buffer.write("# the first line (above this, which is a comment) IS REQUIRED for MoLanguage to verify lang files" + newLine);
        String[] keys = data.stringPropertyNames().toArray(new String[data.size()]);
        for (String k : keys)
        {
            String v = data.getProperty(k);
            if (v.equals(lo_vanilla.get(k, lang)))
                continue;
            if (!dumpExtra)
            {
                if (v.equals(lo_extra.get(k, lang)))
                    continue;
                if (lo_online != null && v.equals(lo_online.get(k, lang)))
                    continue;
            }
            buffer.write(k + "=" + v + newLine);
        }
        buffer.write("# dump on " + DateFormat.getInstance().format(new Date()));
        buffer.close();
        fos.close();
        StringTranslate.getInstance().setLanguage(prevLang, true);
    }

    public void importData()
    {
        if (lo_extra != null)
            importData(lo_extra);
        if (lo_online != null)
            importData(lo_online);
    }

    public void importData(Localization data)
    {
        for (String lang : data.getTableNames())
            importData(data.getTable(lang), lang);
    }

    public void importData(Map<String, String> data, String lang)
    {
        for (String k : data.keySet())
            LanguageRegistry.instance().addStringLocalization(k, lang, data.get(k));
    }

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        metadata.authorList = Arrays.asList("Lain");
        metadata.description = "";
        metadata.parent = "LainCraft";
        metadata.autogenerated = false;
        config = new Config(event.getSuggestedConfigurationFile());
        config.register(MoLanguage.class, null);
        config.load();
        config.save();
        if (enabled)
        {
            lo_extra = new Localization();
            if (allowDownload)
                lo_online = new Localization();
            baseDir = new File(event.getModConfigurationDirectory().getParentFile(), "MoLanguage");
            if (!baseDir.exists() && !baseDir.mkdirs())
                throw new Error();
            loadedFiles_local = new HashSet<String>();
            loadedFiles_online = new HashSet<String>();
        }
    }

    @Mod.Init
    public void load(FMLInitializationEvent event)
    {
        if (enabled)
        {
            MinecraftForge.EVENT_BUS.register(this);
            loadExtra();
            loadOnline();
        }
    }

    public void loadExtra()
    {
        loadedFiles_local.clear();
        if (lo_extra == null)
            lo_extra = new Localization();
        File dir = new File(baseDir, "lang");
        if (!dir.exists() && !dir.mkdirs())
            throw new Error();
        loadExtra(lo_extra, dir);
    }

    public void loadExtra(Localization data, File dir)
    {
        try
        {
            for (File f : dir.listFiles())
            {
                if (f.isDirectory())
                    loadExtra(data, f);
                else if (f.isFile())
                {
                    String n = f.getName().toLowerCase();
                    String p = f.getName();
                    if (n.endsWith(".zip") || n.endsWith(".jar"))
                    {
                        ZipInputStream zis = null;
                        try
                        {
                            zis = new ZipInputStream(new FileInputStream(f));
                            ZipEntry entry = null;
                            while ((entry = zis.getNextEntry()) != null)
                                if (entry.getName().toLowerCase().endsWith(".lang"))
                                {
                                    loadExtra(data, zis);
                                    if (data == lo_extra)
                                        loadedFiles_local.add(p + ":" + entry.getName());
                                    else if (data == lo_online)
                                        loadedFiles_online.add(p + ":" + entry.getName());
                                }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            if (zis != null)
                                try
                                {
                                    zis.close();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                        }
                    }
                    else if (n.endsWith(".lang"))
                    {
                        loadExtra(data, new FileInputStream(f));
                        if (data == lo_extra)
                            loadedFiles_local.add(p);
                        else if (data == lo_online)
                            loadedFiles_online.add(p);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public void loadExtra(Localization data, InputStream input)
    {
        BufferedReader buf = null;
        try
        {
            UnicodeInputStreamReader is = new UnicodeInputStreamReader(input, "UTF-8");
            buf = new BufferedReader(is);
            String lang = null;
            int num = 0;
            String line = null;
            String head = null, tail = null;
            while ((line = buf.readLine()) != null)
            {
                num++;
                line = line.trim();
                if (lang == null && line.startsWith("#"))
                {
                    lang = line.indexOf(" ") != -1 ? line.substring(1, line.indexOf(" ")) : line.substring(1);
                }
                else if (lang == null)
                {
                    break;
                }
                if (line.equals("#headclear"))
                    head = null;
                else if (line.equals("#tailclear"))
                    tail = null;
                else if (line.startsWith("#head="))
                    head = line.substring(6);
                else if (line.startsWith("#tail="))
                    tail = line.substring(6);
                else if (line.startsWith("#author="))
                    System.out.println(String.format("note: \'%s\' has contributed to this file", line.substring(8)));
                if (line.startsWith("#") || line.isEmpty() || line.indexOf("=") == -1)
                    continue;
                String k = line.substring(0, line.indexOf("=")).trim();
                String v = line.substring(line.indexOf("=") + 1).trim();
                if (data.get(k, lang) != null)
                    System.err.println(String.format("warning: line %d: found duplicate key \'%s\'", num, k));
                if (head != null)
                    v = head + v;
                if (tail != null)
                    v = v + tail;
                data.put(k, v, lang);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    public void loadOnline()
    {
        if (allowDownload)
        {
            loadedFiles_online.clear();
            if (lo_online == null)
                lo_online = new Localization();
            final File dir = new File(baseDir, "langOnlineTemp");
            if (dir.exists())
                dir.delete();
            if (!dir.exists() && !dir.mkdirs())
                throw new Error();
            modsList = new HashSet<String>();
            try
            {
                for (ModContainer mc : Loader.instance().getActiveModList())
                    modsList.add(mc.getModId());
            }
            catch (Throwable t)
            {
                t.printStackTrace();
                modsList.clear();
            }
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        for (String url0 : Config.lineSplitter.split(urlProviders))
                        {
                            try
                            {
                                String url = String.format(url0, "langlist");
                                FileLocator.useCache = false;
                                File list = FileLocator.getFile(url);
                                int retries = 0;
                                while (++retries <= 3)
                                {
                                    if (!list.exists())
                                    {
                                        FileLocator.useCache = false;
                                        list = FileLocator.getFile(url);
                                    }
                                    else
                                        break;
                                }
                                if (list.exists())
                                    loadOnline(list, url0, dir);
                            }
                            catch (Throwable t)
                            {
                                t.printStackTrace();
                            }
                        }
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                    finally
                    {
                        modsList = null; // detach from JVM
                        loadExtra(lo_online, dir);
                        importData();
                    }
                }
            }).run();
        }
    }

    public void loadOnline(File list, String root, File dir)
    {
        BufferedReader buf = null;
        try
        {
            buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(list), "UTF-8"));
            String line = null;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (!line.startsWith("#"))
                {
                    // modid filename lang md5
                    String[] parts = line.split(" ");
                    if (parts != null && parts.length == 4)
                    {
                        if (modsList.contains(parts[0]) || "*".equals(parts[0]))
                        {
                            FileLocator.useCache = true;
                            String url = String.format(root, parts[1]);
                            File f = FileLocator.getFile(url);
                            int retries = 0;
                            while (++retries <= 3)
                            {
                                if (!f.exists() || !StreamUtils.calc_md5(StreamUtils.readFully(new FileInputStream(f))).equals(parts[3].toLowerCase()))
                                {
                                    FileLocator.useCache = false;
                                    f = FileLocator.getFile(url);
                                }
                                else
                                    break;
                            }
                            if (f.exists() && StreamUtils.calc_md5(StreamUtils.readFully(new FileInputStream(f))).equals(parts[3].toLowerCase()))
                            {
                                File f1 = new File(dir, parts[1]);
                                if (f1.getParentFile() != null && !f1.getParentFile().exists())
                                    f1.mkdirs();
                                FileOutputStream fos = new FileOutputStream(f1);
                                fos.write(("#" + parts[2] + System.getProperty(System.getProperty("line.separator"))).getBytes("UTF-8"));
                                fos.write(StreamUtils.readFully(new FileInputStream(f)));
                                fos.close();
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    // TODO update to 1.6
    public void loadVanilla()
    {
        BufferedReader buf = null;
        try
        {
            buf = new BufferedReader(new UnicodeInputStreamReader(StringTranslate.class.getResourceAsStream("/lang/languages.txt"), "UTF-8"));
            String line = null;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (!line.startsWith("#"))
                {
                    String[] parts = line.split("=");
                    if (parts != null && parts.length == 2)
                        loadVanilla(parts[0]);
                }
            }
            loadVanilla("en_US");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    public void loadVanilla(String lang)
    {
        BufferedReader buf = null;
        try
        {
            buf = new BufferedReader(new UnicodeInputStreamReader(StringTranslate.class.getResourceAsStream("/lang/" + lang + ".lang"), "UTF-8"));
            String line = null;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (!line.startsWith("#"))
                {
                    String[] parts = line.split("=");
                    if (parts != null && parts.length == 2)
                        lo_vanilla.put(parts[0], parts[1], lang);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
        }
    }

    @Mod.PostInit
    public void modsLoaded(FMLPostInitializationEvent event)
    {
        if (enabled)
        {
            importData();
            dump();
        }
    }

    @ForgeSubscribe
    public void onSendMessage(ClientPlayerSendMessageEvent event)
    {
        if ("#molang reload".equals(event.message))
        {
            loadExtra();
            loadOnline();
            event.setCanceled(true);
        }
        else if ("#molang dump".equals(event.message))
        {
            dump = true;
            dump();
            event.setCanceled(true);
        }
        else if ("#molang list local".equals(event.message))
        {
            StringBuilder s = new StringBuilder("local language files:");
            if (loadedFiles_local.isEmpty())
                s.append(" none");
            else
                for (String f : loadedFiles_local)
                    s.append(" " + f);
            event.player.sendChatToPlayer(s.toString());
            event.setCanceled(true);
        }
        else if ("#molang list online".equals(event.message))
        {
            StringBuilder s = new StringBuilder("online language files:");
            if (loadedFiles_online.isEmpty())
                s.append(" none");
            else
                for (String f : loadedFiles_online)
                    s.append(" " + f);
            event.player.sendChatToPlayer(s.toString());
            event.setCanceled(true);
        }
    }

}
