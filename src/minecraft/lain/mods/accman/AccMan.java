package lain.mods.accman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraftforge.common.Configuration;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = "AccMan", name = "Account Manager", version = "1.6.x-v1")
public class AccMan
{

    private Logger logger;
    private File AccSaveDir;
    private File BackupDir;

    private boolean enabled;
    private boolean disableInSinglePlayerMode;
    private boolean autoBackup;

    private Map<String, AccInfo> accs;

    private void backup(File dir, File backupDir)
    {
        pack(dir, new File(backupDir, String.format("%s.backup-%s.zip", dir.getName(), new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))));
    }

    private void clear(File dir)
    {
        for (File f : dir.listFiles())
            if (f.isFile())
                f.delete();
            else if (f.isDirectory())
            {
                clear(f);
                f.delete();
            }
    }

    private void load(File dir)
    {
        for (File f : dir.listFiles())
            if (f.isFile() && f.getName().toLowerCase().endsWith(".accinfo"))
            {
                AccInfo i = null;
                FileInputStream stream = null;
                try
                {
                    stream = new FileInputStream(f);
                    i = AccInfo.read(stream);
                }
                catch (IOException e)
                {
                    i = null;
                    logger.severe(String.format("Error reading \'%s\': %s", f.getPath(), e.toString()));
                }
                finally
                {
                    try
                    {
                        Closeables.close(stream, true);
                    }
                    catch (IOException ignored)
                    {
                    }
                }
                if (i != null)
                    accs.put(i.name, i);
            }
    }

    @Mod.EventHandler
    public void onModInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        File dir = new File(event.getModConfigurationDirectory(), event.getModMetadata().modId);
        if (!dir.exists())
            dir.mkdir();
        File configFile = new File(dir, "Main.cfg");
        AccSaveDir = new File(dir, "Accounts");
        if (!AccSaveDir.exists())
            AccSaveDir.mkdir();
        BackupDir = new File(dir, "Accounts-Backups");
        if (!BackupDir.exists())
            BackupDir.mkdir();
        Configuration config = null;
        try
        {
            config = new Configuration(configFile);
            enabled = config.get(Configuration.CATEGORY_GENERAL, "enabled", true).getBoolean(true);
            disableInSinglePlayerMode = config.get(Configuration.CATEGORY_GENERAL, "disableInSinglePlayerMode", true).getBoolean(true);
            autoBackup = config.get(Configuration.CATEGORY_GENERAL, "autoBackup", true).getBoolean(true);
        }
        catch (Exception e)
        {
            logger.warning(String.format("Error loading configuration: %s", e.toString()));
        }
        finally
        {
            if (config != null)
                config.save();
        }
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        if (enabled && (!disableInSinglePlayerMode || !event.getServer().isSinglePlayer()))
        {
            accs = Maps.newHashMap();
            load(AccSaveDir);
        }
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event)
    {
        if (accs != null)
        {
            if (autoBackup)
                backup(AccSaveDir, BackupDir);
            clear(AccSaveDir);
            save(AccSaveDir);
            accs = null;
        }
    }

    private void pack(File dir, File zip)
    {
        ZipOutputStream stream = null;
        try
        {
            stream = new ZipOutputStream(new FileOutputStream(zip));
            pack(dir, stream);
        }
        catch (IOException e)
        {
            logger.warning(String.format("Error packing dir \'%s\': %s", dir.getPath(), e.toString()));
        }
        finally
        {
            try
            {
                Closeables.close(stream, true);
            }
            catch (IOException ignored)
            {
            }
        }
    }

    private void pack(File dir, ZipOutputStream zip)
    {
        pack(dir, zip, "");
    }

    private void pack(File dir, ZipOutputStream zip, String basepath)
    {
        for (File f : dir.listFiles())
            try
            {
                if (f.isFile())
                {
                    zip.putNextEntry(new ZipEntry(String.format("%s%s", basepath, f.getName())));
                    zip.write(Files.toByteArray(f));
                }
                else if (f.isDirectory())
                {
                    zip.putNextEntry(new ZipEntry(String.format("%s%s/", basepath, f.getName())));
                    pack(f, zip, String.format("%s%s/", basepath, f.getName()));
                }
            }
            catch (IOException e)
            {
                logger.warning(String.format("Error packing file \'%s\': %s", f.getPath(), e.toString()));
            }
    }

    private void save(File dir)
    {
        for (AccInfo i : accs.values())
            save(dir, i);
    }

    private void save(File dir, AccInfo i)
    {
        File f = new File(dir, String.format("%s.accinfo", i.name));
        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream(f);
            AccInfo.write(stream, i);
        }
        catch (IOException e)
        {
            logger.severe(String.format("Error writing \'%s\': %s", f.getPath(), e.toString()));
        }
        finally
        {
            try
            {
                Closeables.close(stream, true);
            }
            catch (IOException ignored)
            {
            }
        }
    }

}
