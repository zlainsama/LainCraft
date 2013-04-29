package lain.mods.molanguage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.event.ClientPlayerSendMessageEvent;
import lain.mods.laincraft.util.FileLocator;
import lain.mods.laincraft.util.StreamUtils;
import lain.mods.laincraft.util.UnicodeInputStreamReader;
import lain.mods.laincraft.util.configuration.Config;
import lain.mods.molanguage.util.Localization;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class MoLanguage extends Plugin
{

    @Config.SingleComment("just turn it off if you want")
    @Config.Property(defaultValue = "true")
    private static boolean enabled;
    @Config.SingleComment("dump all mod language data? (auto-reset after one successful dump)")
    @Config.Property(defaultValue = "false")
    private static boolean dump;
    @Config.SingleComment("dump the specific language?")
    @Config.Property(defaultValue = "")
    private static String dumpLang;
    @Config.SingleComment("dump data imported by this mod?")
    @Config.Property(defaultValue = "false")
    private static boolean dumpExtra;
    @Config.SingleComment("online lang-pack providers (split with \\n)")
    @Config.Property(defaultValue = "http://tab.mcbbc.com/lang/%s")
    private static String urlProviders;
    @Config.SingleComment("allow online lang-packs?")
    @Config.Property(defaultValue = "true")
    private static boolean allowDownload;

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
        buffer.write("# WARNING: this file was saved in UTF-8" + newLine + newLine);
        for (String k : new TreeSet<String>(data.stringPropertyNames()))
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
        buffer.write(newLine + "# dump on " + DateFormat.getInstance().format(new Date()));
        buffer.close();
        fos.close();
        StringTranslate.getInstance().setLanguage(prevLang, true);
    }

    @Override
    public String getName()
    {
        return "MoLanguage";
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

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
        config = new Config(new File(event.getModConfigurationDirectory(), "MoLanguage.cfg"));
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

    @Subscribe
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
                        ZipFile zip = null;
                        try
                        {
                            zip = new ZipFile(f);
                            for (ZipEntry entry : Collections.list(zip.entries()))
                            {
                                if (entry.getName().toLowerCase().endsWith(".lang"))
                                {
                                    loadExtra(data, zip.getInputStream(entry));
                                    String p1 = entry.getName();
                                    if (p1.lastIndexOf("/") != -1)
                                        p1 = p1.substring(p1.lastIndexOf("/") + 1);
                                    if (data == lo_extra)
                                        loadedFiles_local.add(p1 + "(" + p + ")");
                                    else if (data == lo_online)
                                        loadedFiles_online.add(p1 + "(" + p + ")");
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            if (zip != null)
                                try
                                {
                                    zip.close();
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
                                String url = String.format(url0, "langlist.list");
                                File list = FileLocator.getFile(url);
                                if (list.exists() && verifyList(list))
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
                            String url = String.format(root, parts[2] + "/" + parts[1]);
                            File f = FileLocator.getFile(url);
                            if (f.exists())
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

    @Subscribe
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
            StringBuilder s = new StringBuilder("[L] " + loadedFiles_local.size() + " lang-packs:");
            if (loadedFiles_local.isEmpty())
                s.append(" none");
            else
                for (String f : new TreeSet<String>(loadedFiles_local))
                    s.append(" " + f);
            event.player.sendChatToPlayer(s.toString());
            event.setCanceled(true);
        }
        else if ("#molang list online".equals(event.message))
        {
            StringBuilder s = new StringBuilder("[O] " + +loadedFiles_online.size() + " lang-packs:");
            if (loadedFiles_online.isEmpty())
                s.append(" none");
            else
                for (String f : new TreeSet<String>(loadedFiles_online))
                    s.append(" " + f);
            event.player.sendChatToPlayer(s.toString());
            event.setCanceled(true);
        }
        else if (event.message.startsWith("#molang ") || "#molang".equals(event.message))
        {
            event.player.sendChatToPlayer("#molang reload");
            event.player.sendChatToPlayer("#molang dump");
            event.player.sendChatToPlayer("#molang list local");
            event.player.sendChatToPlayer("#molang list online");
            event.setCanceled(true);
        }
    }

    public boolean verifyList(File list)
    {
        BufferedReader buf = null;
        try
        {
            buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(list), "UTF-8"));
            return buf.readLine().equals("#langlist");
        }
        catch (Exception ignored)
        {
            return false;
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (Exception ignored)
                {
                }
        }

    }

}
