package lain.mods.bilicraftcomments.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import lain.mods.bilicraftcomments.BilicraftComments;
import lain.mods.laincraft.utils.Translator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;

public class CommonProxy
{

    Translator msgInternalError = new Translator("BcC_InternalError");
    Translator msgNotInWhitelist = new Translator("BcC_NotInWhitelist");
    Translator msgTooFastToComment = new Translator("BcC_TooFastToComment");
    Translator msgInvalidArguments = new Translator("BcC_InvalidArguments");

    public void displayComment(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
    }

    public void handleCommentRequest(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        if (player instanceof EntityPlayerMP)
        {
            EntityPlayerMP plr = (EntityPlayerMP) player;
            ExtendedPlayerProperties prop = ExtendedPlayerProperties.getProperties(plr);
            if (prop == null)
            {
                msgInternalError.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (!Whitelist.contains(plr.username))
            {
                msgNotInWhitelist.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (!prop.timer.checkTimeIfValid(plr.worldObj.getTotalWorldTime(), Settings.commentInterval, false))
            {
                msgTooFastToComment.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            DataInputStream dis = null;
            try
            {
                dis = new DataInputStream(new ByteArrayInputStream(packet.data));
                int mode = dis.readShort();
                int lifespan = dis.readShort();
                String text = dis.readUTF();
                if (!Settings.isModeAllowed(mode) || lifespan < Settings.minLifespan || lifespan > Settings.maxLifespan || StringUtils.stripControlCodes(text.trim().replace("&", "\u00a7").replace("\u00a7\u00a7", "&")).isEmpty())
                {
                    msgInvalidArguments.s(plr, EnumChatFormatting.DARK_RED.toString());
                    return;
                }
                prop.timer.markTime(plr.worldObj.getTotalWorldTime());
                packet = BilicraftComments.createDisplayPacket(mode, lifespan, EnumChatFormatting.RESET + plr.getTranslatedEntityName() + " > " + text);
                for (Object o : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
                {
                    if (o instanceof EntityPlayerMP)
                        ((EntityPlayerMP) o).playerNetServerHandler.sendPacketToPlayer(packet);
                }
            }
            catch (IOException e)
            {
                System.err.println("error reading incoming comment request: " + e.toString());
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

    public void load()
    {
        Whitelist.load();
        ExtendedPlayerProperties.load();
        msgInternalError.a("There is an internal error occurred.");
        msgNotInWhitelist.a("You need to be in comment whitelist to send comment.");
        msgTooFastToComment.a("You're fast! Aren't you? Take a break please.");
        msgInvalidArguments.a("Some of your arguments are not allowed.");
        msgInternalError.a("发生了一个内部错误。", "zh_CN");
        msgNotInWhitelist.a("你需要在评论白名单中来发送评论。", "zh_CN");
        msgTooFastToComment.a("你真快！不是么？请休息一下。", "zh_CN");
        msgInvalidArguments.a("你的一些参数是不被允许的。", "zh_CN");
    }

}
