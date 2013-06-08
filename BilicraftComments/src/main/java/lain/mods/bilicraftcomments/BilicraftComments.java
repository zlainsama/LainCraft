package lain.mods.bilicraftcomments;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import lain.mods.bilicraftcomments.common.CommonProxy;
import lain.mods.bilicraftcomments.common.PacketHandler;
import lain.mods.bilicraftcomments.common.Settings;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.packet.Packet250CustomPayload;
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

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        config = new Config(new File(SharedConstants.getLainCraftDirFile(), "BilicraftComments.cfg"), "BilicraftComments");
        config.register(Settings.class, null);
        config.load();
        config.save();
    }

    @Mod.Init
    public void load(FMLInitializationEvent event)
    {
        proxy.load();
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
        catch (IOException e)
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

    @Mod.ServerStarting
    public void onServerStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBase()
        {

            @Override
            public String getCommandName()
            {
                return "bcc_test";
            }

            @Override
            public void processCommand(ICommandSender arg0, String[] arg1)
            {
                getCommandSenderAsPlayer(arg0).playerNetServerHandler.sendPacketToPlayer(createPacket("LC|BcC|R", 0, 200, "Test Comment Text mode 0"));
                getCommandSenderAsPlayer(arg0).playerNetServerHandler.sendPacketToPlayer(createPacket("LC|BcC|R", 1, 200, "Test Comment Text mode 1"));
                getCommandSenderAsPlayer(arg0).playerNetServerHandler.sendPacketToPlayer(createPacket("LC|BcC|R", 2, 200, "Test Comment Text mode 2"));
                getCommandSenderAsPlayer(arg0).playerNetServerHandler.sendPacketToPlayer(createPacket("LC|BcC|R", 3, 200, "Test Comment Text mode 3"));
            }

        });
    }
}
