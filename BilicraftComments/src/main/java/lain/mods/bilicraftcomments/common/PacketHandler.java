package lain.mods.bilicraftcomments.common;

import lain.mods.bilicraftcomments.BilicraftComments;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager paramINetworkManager, Packet250CustomPayload paramPacket250CustomPayload, Player paramPlayer)
    {
        if ("LC|BcC|R".equals(paramPacket250CustomPayload.channel))
            BilicraftComments.proxy.handleCommentRequest(paramINetworkManager, paramPacket250CustomPayload, paramPlayer);
        else if ("LC|BcC|D".equals(paramPacket250CustomPayload.channel))
            BilicraftComments.proxy.displayComment(paramINetworkManager, paramPacket250CustomPayload, paramPlayer);
    }

}
