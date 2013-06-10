package lain.mods.bilicraftcomments.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import lain.mods.bilicraftcomments.BilicraftComments;
import lain.mods.bilicraftcomments.client.Comment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;

public class CommonProxy
{

    public void load()
    {
    }

    public void displayComment(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
    }

    public void handleCommentRequest(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        if (player instanceof EntityPlayerMP && Whitelist.contains(((EntityPlayerMP)player).username))
        {
            DataInputStream dis = null;
            try
            {
                dis = new DataInputStream(new ByteArrayInputStream(packet.data));
                int mode = dis.readShort();
                int lifespan = dis.readShort();
                String text = dis.readUTF();
                if (Settings.isModeAllowed(mode) && Settings.minLifespan >= lifespan && Settings.maxLifespan <= lifespan)
                {
                    packet = BilicraftComments.createDisplayPacket(mode, lifespan, text);
                    for (Object o : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
                    {
                        if (o instanceof EntityPlayerMP)
                            ((EntityPlayerMP) o).playerNetServerHandler.sendPacketToPlayer(packet);
                    }
                }
                else
                {
                    
                } 
            }
            catch (IOException e)
            {
                System.err.println("error reading incoming comment: " + e.toString());
            }
            finally
            {
                if (dis != null)
                    try
                    {
                        dis.close();
                    }
                    catch (IOException ignored)
                    {
                    }
            }
        }
    }
    
}
