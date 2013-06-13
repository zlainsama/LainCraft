package lain.mods.bilicraftcomments;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import lain.mods.bilicraftcomments.common.Blacklist;
import lain.mods.bilicraftcomments.common.CommonProxy;
import lain.mods.bilicraftcomments.common.PacketHandler;
import lain.mods.bilicraftcomments.common.Settings;
import lain.mods.bilicraftcomments.common.Whitelist;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.Translator;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = "BilicraftComments", name = "BilicraftComments", version = "", dependencies = "required-after:LainCraftCore", useMetadata = true)
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { "LC|BcC|R", "LC|BcC|D" }, packetHandler = PacketHandler.class)
public class BilicraftComments
{

    @SidedProxy(serverSide = "lain.mods.bilicraftcomments.common.CommonProxy", clientSide = "lain.mods.bilicraftcomments.client.ClientProxy")
    public static CommonProxy proxy;

    public static Config config;
    public static IPermissionManager manager;
    public static Logger logger;

    public static final int logLimit = 33554432;

    public static Packet250CustomPayload createDisplayPacket(int mode, int lifespan, String text)
    {
        return createPacket("LC|BcC|D", mode, lifespan, text);
    }

    public static Packet250CustomPayload createPacket(String channel, int mode, int lifespan, String text)
    {
        DataOutputStream dos = null;
        try
        {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            dos = new DataOutputStream(buf);
            dos.writeShort(mode);
            dos.writeShort(lifespan);
            dos.writeUTF(text);
            dos.flush();
            return new Packet250CustomPayload(channel, buf.toByteArray());
        }
        catch (Exception e)
        {
            System.err.println("error creating packet: " + e.toString());
            return null;
        }
        finally
        {
            if (dos != null)
                try
                {
                    dos.close();
                }
                catch (IOException ignored)
                {
                }
        }
    }

    public static Packet250CustomPayload createRequestPacket(int mode, int lifespan, String text)
    {
        return createPacket("LC|BcC|R", mode, lifespan, text);
    }

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        config = new Config(new File(SharedConstants.getLainCraftDirFile(), "BilicraftComments.cfg"), "BilicraftComments");
        config.register(Settings.class, null);
        config.load();
        Settings.update();
        config.save();
        logger = event.getModLog();
        try
        {
            File logPath = new File(SharedConstants.getMinecraftDirFile(), "BcC_CommentLog_%g.log");
            logger.addHandler(new FileHandler(logPath.getPath(), logLimit, 4, true)
            {
                {
                    setLevel(Level.ALL);
                    setFormatter(new Formatter()
                    {
                        String LINE_SEPARATOR = System.getProperty("line.separator");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        @Override
                        public String format(LogRecord record)
                        {
                            if (record.getLevel() == LevelComment.comment)
                            {
                                StringBuilder msg = new StringBuilder();
                                msg.append(dateFormat.format(Long.valueOf(record.getMillis())));
                                msg.append(" ");
                                msg.append(record.getMessage());
                                msg.append(LINE_SEPARATOR);
                                return msg.toString();
                            }
                            return "";
                        }
                    });
                }

                @Override
                public synchronized void close() throws SecurityException
                {
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Translator a = new Translator();
        a.k("commands.bcc_broadcast.usage");
        a.a("/bcc_broadcast <player> [mode] [lifespan] <text ... >");
        a.a("/bcc_broadcast <玩家> [模式] [寿命] <文本 ... >", "zh_CN");
        a.k("commands.bcc_whitelist_add.usage");
        a.a("/bcc_whitelist_add <username>");
        a.a("/bcc_whitelist_add <用户名>", "zh_CN");
        a.k("commands.bcc_whitelist_add.added");
        a.a("'%1$s' added to comment whitelist.");
        a.a("已添加 '%1$s' 到评论白名单.", "zh_CN");
        a.k("commands.bcc_whitelist_remove.usage");
        a.a("/bcc_whitelist_remove <username>");
        a.a("/bcc_whitelist_remove <用户名>", "zh_CN");
        a.k("commands.bcc_whitelist_remove.removed");
        a.a("'%1$s' removed from comment whitelist.");
        a.a("已将 '%1$s' 从评论白名单中移除.", "zh_CN");
        a.k("commands.bcc_blacklist_add.usage");
        a.a("/bcc_blacklist_add <username>");
        a.a("/bcc_blacklist_add <用户名>", "zh_CN");
        a.k("commands.bcc_blacklist_add.added");
        a.a("'%1$s' added to comment blacklist.");
        a.a("已添加 '%1$s' 到评论黑名单.", "zh_CN");
        a.k("commands.bcc_blacklist_remove.usage");
        a.a("/bcc_blacklist_remove <username>");
        a.a("/bcc_blacklist_remove <用户名>", "zh_CN");
        a.k("commands.bcc_blacklist_remove.removed");
        a.a("'%1$s' removed from comment blacklist.");
        a.a("已将 '%1$s' 从评论黑名单中移除.", "zh_CN");
    }

    @Mod.Init
    public void load(FMLInitializationEvent event)
    {
        proxy.load();
    }

    @Mod.ServerStarting
    public void onServerStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_broadcast";
            }

            @Override
            public String getCommandUsage(ICommandSender arg0)
            {
                return arg0.translateString("commands.bcc_broadcast.usage", new Object[0]);
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 2;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                if (arg1.length >= 4)
                {
                    EntityPlayerMP[] players = PlayerSelector.matchPlayers(arg0, arg1[0]);
                    if (players == null)
                    {
                        players = new EntityPlayerMP[] { FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(arg1[0]) };
                        if (players[0] == null)
                            throw new PlayerNotFoundException();
                    }
                    int mode = parseIntBounded(arg0, arg1[1], 0, 3);
                    int lifespan = parseIntWithMin(arg0, arg1[2], -1);
                    StringBuilder buf = new StringBuilder();
                    for (int i = 3; i < arg1.length; i++)
                    {
                        if (i > 3)
                            buf.append(" ");
                        buf.append(arg1[i]);
                    }
                    String text = buf.toString().trim().replace("&", "\u00a7").replace("\u00a7\u00a7", "&");
                    BilicraftComments.logger.log(LevelComment.comment, String.format("[CONSOLE](target:%s) [mode:%d] [lifespan:%d] %s", arg1[0], mode, lifespan, text));
                    Packet250CustomPayload packet = createDisplayPacket(mode, lifespan, text);
                    for (EntityPlayerMP player : players)
                        player.playerNetServerHandler.sendPacketToPlayer(packet);
                }
                else
                {
                    throw new WrongUsageException("commands.bcc_broadcast.usage", new Object[0]);
                }
            }
        });
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_reload";
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 3;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                config.load();
                Settings.update();
                config.save();
                Whitelist.load();
                Blacklist.load();
            }
        });
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_whitelist_add";
            }

            @Override
            public String getCommandUsage(ICommandSender arg0)
            {
                return arg0.translateString("commands.bcc_whitelist_add.usage", new Object[0]);
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 3;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                if (arg1.length > 0)
                {
                    Whitelist.load();
                    Whitelist.add(arg1[0]);
                    Whitelist.save();
                    arg0.sendChatToPlayer(arg0.translateString("commands.bcc_whitelist_add.added", arg1[0]));
                }
                else
                    throw new WrongUsageException("commands.bcc_whitelist_add.usage", new Object[0]);
            }
        });
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_whitelist_remove";
            }

            @Override
            public String getCommandUsage(ICommandSender arg0)
            {
                return arg0.translateString("commands.bcc_whitelist_remove.usage", new Object[0]);
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 3;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                if (arg1.length > 0)
                {
                    Whitelist.load();
                    Whitelist.remove(arg1[0]);
                    Whitelist.save();
                    arg0.sendChatToPlayer(arg0.translateString("commands.bcc_whitelist_remove.removed", arg1[0]));
                }
                else
                    throw new WrongUsageException("commands.bcc_whitelist_remove.usage", new Object[0]);
            }
        });
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_blacklist_add";
            }

            @Override
            public String getCommandUsage(ICommandSender arg0)
            {
                return arg0.translateString("commands.bcc_blacklist_add.usage", new Object[0]);
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 3;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                if (arg1.length > 0)
                {
                    Blacklist.load();
                    Blacklist.add(arg1[0]);
                    Blacklist.save();
                    arg0.sendChatToPlayer(arg0.translateString("commands.bcc_blacklist_add.added", arg1[0]));
                }
                else
                    throw new WrongUsageException("commands.bcc_blacklist_add.usage", new Object[0]);
            }
        });
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandName()
            {
                return "bcc_blacklist_remove";
            }

            @Override
            public String getCommandUsage(ICommandSender arg0)
            {
                return arg0.translateString("commands.bcc_blacklist_remove.usage", new Object[0]);
            }

            @Override
            public int getRequiredPermissionLevel()
            {
                return 3;
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                if (arg1.length > 0)
                {
                    Blacklist.load();
                    Blacklist.remove(arg1[0]);
                    Blacklist.save();
                    arg0.sendChatToPlayer(arg0.translateString("commands.bcc_blacklist_remove.removed", arg1[0]));
                }
                else
                    throw new WrongUsageException("commands.bcc_blacklist_remove.usage", new Object[0]);
            }
        });
    }

}
