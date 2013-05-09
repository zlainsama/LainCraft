package lain.mods.laincraft.player;

import lain.mods.laincraft.event.ClientPlayerSendMessageEvent;
import lain.mods.laincraft.event.ClientPlayerUpdateSkinEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.util.Session;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class ClientPlayer extends EntityClientPlayerMP
{

    protected String cloak;

    public ClientPlayer(Minecraft par1Minecraft, World par2World, Session par3Session, NetClientHandler par4NetClientHandler)
    {
        super(par1Minecraft, par2World, par3Session, par4NetClientHandler);
        UpdateSkins();
    }

    @Override
    public void sendChatMessage(String par1Str)
    {
        ClientPlayerSendMessageEvent event = ClientPlayerSendMessageEvent.post(this, par1Str);
        if (!event.isCanceled() && event.message != null)
            super.sendChatMessage(event.message);
    }

    @Override
    public void updateCloak()
    {
        cloakUrl = cloak;
    }

    public void UpdateSkins()
    {
        skinUrl = "http://skins.minecraft.net/MinecraftSkins/" + StringUtils.stripControlCodes(username) + ".png";
        cloak = "http://skins.minecraft.net/MinecraftCloaks/" + StringUtils.stripControlCodes(username) + ".png";
        ClientPlayerUpdateSkinEvent event = ClientPlayerUpdateSkinEvent.post(this, texture, skinUrl, cloak);
        if (!event.isCanceled())
        {
            if (event.texture != null)
                texture = event.texture;
            skinUrl = event.skinUrl;
            cloak = event.cloakUrl;
        }
    }

}
