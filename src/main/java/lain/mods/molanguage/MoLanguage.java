package lain.mods.molanguage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.event.ClientPlayerSendMessageEvent;
import lain.mods.laincraft.util.configuration.Config;
import lain.mods.laincraft.util.io.FileLocator;
import lain.mods.laincraft.util.io.StreamUtils;
import lain.mods.laincraft.util.io.UnicodeInputStreamReader;
import lain.mods.molanguage.util.Localization;
import lain.mods.molanguage.util.LocalizationAdapter;
import lain.mods.molanguage.util.LocalizationFile;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class MoLanguage extends Plugin implements IScheduledTickHandler
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
    private Thread threadDownload;

    public void clearDirectory(File dir)
    {
        for (File f : dir.listFiles())
        {
            if (f.isDirectory())
            {
                clearDirectory(f);
                f.delete();
            }
            else
            {
                f.delete();
            }
        }
    }

    public void cmd_dump()
    {
        dump = true;
        dump();
    }

    public void cmd_list()
    {
        try
        {
            String newLine = "\r\n";
            StringBuilder text = new StringBuilder();
            text.append("Local: total " + loadedFiles_local.size() + " entries" + newLine);
            if (loadedFiles_local.isEmpty())
                text.append("-  none" + newLine);
            else
                for (String f : new TreeSet<String>(loadedFiles_local))
                    text.append("-  " + f + newLine);
            text.append("Online: total " + loadedFiles_online.size() + " entries" + newLine);
            if (loadedFiles_online.isEmpty())
                text.append("-  none" + newLine);
            else
                for (String f : new TreeSet<String>(loadedFiles_online))
                    text.append("-  " + f + newLine);
            Class classGuiApiHelper = Class.forName("sharose.mods.guiapi.GuiApiHelper");
            Method methodMakeTextDisplayAndGoBack = classGuiApiHelper.getDeclaredMethod("makeTextDisplayAndGoBack", String.class, String.class, String.class, Boolean.class);
            Object objectTextDisplay = methodMakeTextDisplayAndGoBack.invoke(null, "Current loaded lang-packs", text.toString(), "Back", false);
            Class classWidget = Class.forName("de.matthiasmann.twl.Widget");
            Class classGuiModScreen = Class.forName("sharose.mods.guiapi.GuiModScreen");
            Method methodShow = classGuiModScreen.getDeclaredMethod("show", classWidget);
            methodShow.invoke(null, objectTextDisplay);
        }
        catch (Throwable t)
        {
            System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
        }

    }

    public void cmd_reload()
    {
        loadExtra();
        if (threadDownload == null || !threadDownload.isAlive())
            loadOnline();
        for (LocalizationAdapter adapter : LocalizationAdapter.adapters)
            adapter.update(true);
    }

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
        for (LocalizationAdapter adapter : LocalizationAdapter.adapters)
            adapter.update(true);
        Properties data = new Properties();
        for (LocalizationAdapter adapter : LocalizationAdapter.adapters)
            adapter.fillProperties(data);
        String newLine = Config.newLine;
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
    public String getLabel()
    {
        return getName();
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
        {
            String v = data.get(k);
            for (LocalizationAdapter adapter : LocalizationAdapter.adapters)
                adapter.addLocalization(k, lang, v);
        }
    }

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
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

    public boolean initGuiAPI()
    {
        try
        {
            Class classModSettingScreen = Class.forName("sharose.mods.guiapi.ModSettingScreen");
            Object objectModSettingScreen = classModSettingScreen.getDeclaredConstructor(String.class).newInstance(getName());
            Method methodSetSingleColumn = classModSettingScreen.getDeclaredMethod("setSingleColumn", Boolean.class);
            methodSetSingleColumn.invoke(objectModSettingScreen, true);
            Class classGuiApiHelper = Class.forName("sharose.mods.guiapi.GuiApiHelper");
            Method methodMakeButton = classGuiApiHelper.getDeclaredMethod("makeButton", String.class, String.class, Object.class, Boolean.class);
            Object objectButton_Reload = methodMakeButton.invoke(null, "Reload lang-packs", "cmd_reload", this, true);
            Object objectButton_Dump = methodMakeButton.invoke(null, "Dump lang-packs", "cmd_dump", this, true);
            Object objectButton_List = methodMakeButton.invoke(null, "List loaded lang-packs", "cmd_list", this, true);
            Class classWidget = Class.forName("de.matthiasmann.twl.Widget");
            Method methodAppend = classModSettingScreen.getDeclaredMethod("append", classWidget);
            methodAppend.invoke(objectModSettingScreen, objectButton_Reload);
            methodAppend.invoke(objectModSettingScreen, objectButton_Dump);
            methodAppend.invoke(objectModSettingScreen, objectButton_List);
            return true;
        }
        catch (Throwable t)
        {
            System.err.println(t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        return false;
    }

    @Subscribe
    public void load(FMLInitializationEvent event)
    {
        if (enabled)
        {
            TickRegistry.registerScheduledTickHandler(this, Side.CLIENT);
            TickRegistry.registerScheduledTickHandler(this, Side.SERVER);
            loadExtra();
            loadOnline();
            if (!initGuiAPI())
                MinecraftForge.EVENT_BUS.register(this);
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
                                    if (loadExtra(data, zip.getInputStream(entry), null))
                                    {
                                        String p1 = entry.getName();
                                        if (p1.lastIndexOf("/") != -1)
                                            p1 = p1.substring(p1.lastIndexOf("/") + 1);
                                        if (data == lo_extra)
                                            loadedFiles_local.add(p1 + "(" + f.getName() + ")");
                                        else if (data == lo_online)
                                            loadedFiles_online.add(p1 + "(" + f.getName() + ")");
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
                        if (loadExtra(data, new FileInputStream(f), null))
                        {
                            if (data == lo_extra)
                                loadedFiles_local.add(f.getName());
                            else if (data == lo_online)
                                loadedFiles_online.add(f.getName());
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    public boolean loadExtra(Localization data, InputStream input, Properties env)
    {
        LocalizationFile tmp = new LocalizationFile();
        if (env != null)
            tmp.env = env;
        return tmp.load(input, data);
    }

    public void loadOnline()
    {
        if (allowDownload)
        {
            loadedFiles_online.clear();
            if (lo_online == null)
                lo_online = new Localization();
            final File dir = new File(baseDir, "langOnlineTemp");
            if (!dir.exists() && !dir.mkdirs())
                throw new Error("failed to create directory \'" + dir + "\'");
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
            threadDownload = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        boolean flag = false;
                        for (String url0 : Config.lineSplitter.split(urlProviders))
                        {
                            try
                            {
                                String url = String.format(url0, "langlist.list");
                                File list = FileLocator.getFile(url);
                                if (list.exists() && verifyList(list))
                                {
                                    if (!flag)
                                    {
                                        clearDirectory(dir);
                                        flag = true;
                                    }
                                    loadOnline(lo_online, list, url0, dir);
                                }
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
                        importData();
                    }
                }
            });
            threadDownload.start();
        }
    }

    public void loadOnline(Localization data, File list, String root, File dir)
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
                                File f1 = new File(dir, parts[2]);
                                if (!f1.exists() && !f1.mkdirs())
                                    throw new Error("failed to create directory \'" + f1 + "\'");
                                File f2 = new File(f1, parts[1]);
                                StreamUtils.saveAsFile(new FileInputStream(f), f2);
                                Properties tmp = new Properties();
                                tmp.setProperty("lang", parts[2]);
                                if (!"*".equals(parts[0]))
                                    tmp.setProperty("mod", parts[0]);
                                if (loadExtra(data, new FileInputStream(f), tmp))
                                {
                                    if (data == lo_extra)
                                        loadedFiles_local.add(f2.getName());
                                    else if (data == lo_online)
                                        loadedFiles_online.add(f2.getName());
                                }
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

    @Override
    public int nextTickSpacing()
    {
        return 10;
    }

    @Override
    public void onDisable()
    {
    }

    @Override
    public void onEnable()
    {
        config = getConfig();
        config.register(MoLanguage.class, null);
        config.load();
        config.save();
    }

    @ForgeSubscribe
    public void onSendMessage(ClientPlayerSendMessageEvent event)
    {
        if ("#molang reload".equals(event.message))
        {
            cmd_reload();
            event.setCanceled(true);
        }
        else if ("#molang dump".equals(event.message))
        {
            cmd_dump();
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

    @Override
    public void tickEnd(EnumSet<TickType> par1, Object... par2)
    {
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.CLIENT, TickType.SERVER);
    }

    @Override
    public void tickStart(EnumSet<TickType> par1, Object... par2)
    {
        if (par1.contains(TickType.CLIENT) || par1.contains(TickType.SERVER))
            for (LocalizationAdapter adapter : LocalizationAdapter.adapters)
                adapter.update(false);
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
