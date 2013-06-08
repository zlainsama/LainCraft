package lain.mods.bilicraftcomments.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
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
        packet.channel = "LC|BcC|D";
        for (Object o : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
        {
            if (o instanceof EntityPlayerMP)
                ((EntityPlayerMP) o).playerNetServerHandler.sendPacketToPlayer(packet);
        }
    }
}
