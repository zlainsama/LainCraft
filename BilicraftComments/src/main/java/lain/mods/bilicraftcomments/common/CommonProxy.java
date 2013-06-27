package lain.mods.bilicraftcomments.common;

import lain.mods.bilicraftcomments.BilicraftComments;
import lain.mods.bilicraftcomments.LevelComment;
import lain.mods.laincraft.utils.Translator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;

public class CommonProxy
{

    Translator msgInternalError = new Translator("BcC_InternalError");
    Translator msgNotInWhitelist = new Translator("BcC_NotInWhitelist");
    Translator msgInBlacklist = new Translator("BcC_InBlacklist");
    Translator msgTooFastToComment = new Translator("BcC_TooFastToComment");
    Translator msgInvalidArguments = new Translator("BcC_InvalidArguments");
    Translator msgOutdatedProtocol = new Translator("BcC_OutdatedProtocol");

    public void displayComment(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
    }

    public void handleCommentRequest(EntityPlayerMP plr, String[] args)
    {
        if (args.length >= 3)
        {
            ExtendedPlayerProperties prop = ExtendedPlayerProperties.getProperties(plr);
            if (prop == null)
            {
                msgInternalError.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (Settings.whitelistMode && !Whitelist.contains(plr.username))
            {
                msgNotInWhitelist.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (Blacklist.contains(plr.username))
            {
                msgInBlacklist.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (!prop.timer.checkTimeIfValid(plr.worldObj.getTotalWorldTime(), Settings.commentInterval, false))
            {
                msgTooFastToComment.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            int mode = Integer.parseInt(args[0]);
            int lifespan = Integer.parseInt(args[1]);
            StringBuilder buf = new StringBuilder();
            for (int i = 2; i < args.length; i++)
            {
                if (i > 2)
                    buf.append(" ");
                buf.append(args[i]);
            }
            String text = buf.toString().trim().replace("&", "\u00a7").replace("\u00a7\u00a7", "&");
            if (!Settings.isModeAllowed(mode) || lifespan < Settings.minLifespan || lifespan > Settings.maxLifespan || StringUtils.stripControlCodes(text).isEmpty())
            {
                msgInvalidArguments.s(plr, EnumChatFormatting.DARK_RED.toString());
                return;
            }
            if (BilicraftComments.manager != null)
            {
                if (!BilicraftComments.manager.hasPermission(plr.username, "BcC.commentMode." + mode))
                {
                    msgInvalidArguments.s(plr, EnumChatFormatting.DARK_RED.toString());
                    return;
                }
                if (!BilicraftComments.manager.hasPermission(plr.username, "BcC.colorComments"))
                    text = StringUtils.stripControlCodes(text);
            }
            BilicraftComments.logger.log(LevelComment.comment, String.format("[username:%s] [mode:%d] [lifespan:%d] %s", plr.username, mode, lifespan, text));
            prop.timer.markTime(plr.worldObj.getTotalWorldTime());
            Packet packet = BilicraftComments.createDisplayPacket(mode, lifespan, EnumChatFormatting.RESET + plr.getTranslatedEntityName() + " > " + text);
            for (Object o : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
            {
                if (o instanceof EntityPlayerMP)
                    ((EntityPlayerMP) o).playerNetServerHandler.sendPacketToPlayer(packet);
            }
        }
    }

    public void handleCommentRequest(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        if (player instanceof EntityPlayerMP)
        {
            msgOutdatedProtocol.s((EntityPlayerMP) player, EnumChatFormatting.DARK_RED.toString());
            // EntityPlayerMP plr = (EntityPlayerMP) player;
            // ExtendedPlayerProperties prop = ExtendedPlayerProperties.getProperties(plr);
            // if (prop == null)
            // {
            // msgInternalError.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // if (Settings.whitelistMode && !Whitelist.contains(plr.username))
            // {
            // msgNotInWhitelist.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // if (Blacklist.contains(plr.username))
            // {
            // msgInBlacklist.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // if (!prop.timer.checkTimeIfValid(plr.worldObj.getTotalWorldTime(), Settings.commentInterval, false))
            // {
            // msgTooFastToComment.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // DataInputStream dis = null;
            // try
            // {
            // dis = new DataInputStream(new ByteArrayInputStream(packet.data));
            // int mode = dis.readShort();
            // int lifespan = dis.readShort();
            // String text = dis.readUTF().trim().replace("&", "\u00a7").replace("\u00a7\u00a7", "&");
            // if (!Settings.isModeAllowed(mode) || lifespan < Settings.minLifespan || lifespan > Settings.maxLifespan || StringUtils.stripControlCodes(text).isEmpty())
            // {
            // msgInvalidArguments.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // if (BilicraftComments.manager != null)
            // {
            // if (!BilicraftComments.manager.hasPermission(plr.username, "BcC.commentMode." + mode))
            // {
            // msgInvalidArguments.s(plr, EnumChatFormatting.DARK_RED.toString());
            // return;
            // }
            // if (!BilicraftComments.manager.hasPermission(plr.username, "BcC.colorComments"))
            // text = StringUtils.stripControlCodes(text);
            // }
            // BilicraftComments.logger.log(LevelComment.comment, String.format("[username:%s] [mode:%d] [lifespan:%d] %s", plr.username, mode, lifespan, text));
            // prop.timer.markTime(plr.worldObj.getTotalWorldTime());
            // packet = BilicraftComments.createDisplayPacket(mode, lifespan, EnumChatFormatting.RESET + plr.getTranslatedEntityName() + " > " + text);
            // for (Object o : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
            // {
            // if (o instanceof EntityPlayerMP)
            // ((EntityPlayerMP) o).playerNetServerHandler.sendPacketToPlayer(packet);
            // }
            // }
            // catch (IOException e)
            // {
            // System.err.println("error reading incoming comment request: " + e.toString());
            // }
            // finally
            // {
            // if (dis != null)
            // try
            // {
            // dis.close();
            // }
            // catch (IOException ignored)
            // {
            // }
            // }
        }
    }

    public void load()
    {
        Whitelist.load();
        Blacklist.load();
        ExtendedPlayerProperties.load();
        msgInternalError.a("There is an internal error occurred.");
        msgNotInWhitelist.a("You need to be in comment whitelist to send comment.");
        msgInBlacklist.a("You are in comment blacklist, so you can't send comment.");
        msgTooFastToComment.a("You're fast! Aren't you? Take a break please.");
        msgInvalidArguments.a("Some of your arguments are not allowed.");
        msgOutdatedProtocol.a("Your client's protocol is outdated, please update your client mod.");
        msgInternalError.a("发生了一个内部错误。", "zh_CN");
        msgNotInWhitelist.a("你需要在评论白名单中来发送评论。", "zh_CN");
        msgInBlacklist.a("你在评论黑名单中，所以你不能发送评论。", "zh_CN");
        msgTooFastToComment.a("你真快！不是么？请休息一下。", "zh_CN");
        msgInvalidArguments.a("你的一些参数是不被允许的。", "zh_CN");
        msgOutdatedProtocol.a("你客户端的通讯协议已经过期，请更新你的客户端MOD。", "zh_CN");
    }
}
