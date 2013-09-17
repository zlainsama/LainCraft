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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "AccMan", name = "Account Manager", version = "1.6.x-v1")
public class AccMan
{

    private Logger logger;
    private File AccSaveDir;
    private File BackupDir;

    private boolean enabled;
    private boolean autoBackup;

    private Map<String, AccInfo> accs;
    private Limiter limiter;

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
    public void onModLoad(FMLInitializationEvent evetn)
    {
        limiter = new Limiter();
        MinecraftForge.EVENT_BUS.register(limiter);
        NetworkRegistry.instance().registerConnectionHandler(new IConnectionHandler()
        {
            @Override
            public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
            {
            }

            @Override
            public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
            {
                return null;
            }

            @Override
            public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
            {
            }

            @Override
            public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
            {
            }

            @Override
            public void connectionClosed(INetworkManager manager)
            {
            }

            @Override
            public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
            {
                limiter.setWhitelistMode(false);
                limiter.clearList();
            }
        });
        GameRegistry.registerPlayerTracker(new IPlayerTracker()
        {
            @Override
            public void onPlayerLogin(EntityPlayer player)
            {
                if (player instanceof EntityPlayerMP)
                {
                    EntityPlayerMP plr = (EntityPlayerMP) player;
                    AccInfo data = getData(plr.username);
                    if (data != null)
                    {
                        if (data.logon && !data.lastIP.equals(plr.playerNetServerHandler.netManager.getSocketAddress().toString()))
                            data.logon = false;
                        if (data.logon && Math.abs(data.lastSeen - System.currentTimeMillis()) > 180)
                            data.logon = false;
                        syncLimiterStatus(plr);
                    }
                }
            }

            @Override
            public void onPlayerLogout(EntityPlayer player)
            {
                if (player instanceof EntityPlayerMP)
                {
                    EntityPlayerMP plr = (EntityPlayerMP) player;
                    AccInfo data = getData(plr.username);
                    if (data != null)
                    {
                        data.lastIP = plr.playerNetServerHandler.netManager.getSocketAddress().toString();
                        data.lastSeen = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onPlayerChangedDimension(EntityPlayer player)
            {
            }

            @Override
            public void onPlayerRespawn(EntityPlayer player)
            {
            }
        });
        NetworkRegistry.instance().registerChannel(new IPacketHandler()
        {
            @Override
            public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
            {
                try
                {
                    limiter.setWhitelistMode("1".equals(new String(packet.data, "UTF-8")));
                }
                catch (Exception e)
                {
                    logger.severe(String.format("Error reading limiter message: %s", e.toString()));
                }
            }
        }, "ACCMAN|LI", Side.CLIENT);
        NetworkRegistry.instance().registerChannel(new IPacketHandler()
        {
            @Override
            public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
            {
                // TODO Auto-generated method stub

            }
        }, "ACCMAN|LO", Side.SERVER);
    }

    private void syncLimiterStatus(EntityPlayerMP player)
    {
        AccInfo data = getData(player.username);
        if (data != null)
        {
            if (data.logon)
            {
                limiter.removeFromList(player);
                sendLimiterMsg(false, player.playerNetServerHandler.netManager);
            }
            else
            {
                limiter.addToList(player);
                sendLimiterMsg(true, player.playerNetServerHandler.netManager);
            }
        }
    }

    private void sendLimiterMsg(boolean whitelistMode, INetworkManager manager)
    {
        try
        {
            manager.addToSendQueue(new Packet250CustomPayload("ACCMAN|LI", (whitelistMode ? "1" : "0").getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            logger.severe(String.format("Error sending limiter message: %s", e.toString()));
        }
    }

    private void sendLoginMsg(String message, INetworkManager manager)
    {
        try
        {
            manager.addToSendQueue(new Packet250CustomPayload("ACCMAN|LO", message.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            logger.severe(String.format("Error sending login message: %s", e.toString()));
        }
    }

    private AccInfo getData(String username)
    {
        username = StringUtils.stripControlCodes(username).toLowerCase();
        if (accs != null)
        {
            AccInfo data = accs.get(username);
            if (data == null)
            {
                data = new AccInfo(username, "");
                accs.put(username, data);
            }
            return data;
        }
        return null;
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        limiter.setWhitelistMode(false);
        limiter.clearList();
        if (enabled/* && !event.getServer().isSinglePlayer() */) // TODO: DEBUG
        {
            accs = Maps.newHashMap();
            load(AccSaveDir);
            limiter.setWhitelistMode(true);
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event)
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
