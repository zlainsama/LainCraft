package lain.mods.laincraft.permission;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChunkCoordinates;

public class PermissionUser implements ICommandSender
{

    enum Type
    {
        ServerPlayer, ClientPlayer, FakePlayer, CommandBlock, ServerConsole, RemoteConsole, Others;

        public static Type getType(ICommandSender par1)
        {
            if (par1 instanceof EntityPlayer)
            {
                if (par1 instanceof EntityPlayerMP)
                    return ServerPlayer;
                if (par1 instanceof EntityPlayerSP)
                    return ClientPlayer;
                return FakePlayer;
            }
            if (par1 instanceof TileEntityCommandBlock)
                return CommandBlock;
            if (par1 instanceof MinecraftServer)
                return ServerConsole;
            if (par1 instanceof RConConsoleSource)
                return RemoteConsole;
            return Others;
        }
    }

    private String name;
    private Type type;
    private ICommandSender source;
    private final PermissionManager manager;

    protected PermissionUser(ICommandSender par1, PermissionManager par2)
    {
        name = par1.getCommandSenderName();
        type = Type.getType(par1);
        source = par1;
        manager = par2;
    }

    public boolean attachPermission(Permission par1)
    {
        return manager.attachPermission(this, par1);
    }

    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2)
    {
        return source.canCommandSenderUseCommand(par1, par2);
    }

    public boolean detachPermission(Permission par1)
    {
        return manager.detachPermission(this, par1);
    }

    @Override
    public boolean equals(Object par1)
    {
        if (this == par1)
            return true;
        if (par1 instanceof PermissionUser)
        {
            PermissionUser var1 = (PermissionUser) par1;
            return name.equals(var1.name) && type.equals(var1.type) && source.equals(var1.source) && manager.equals(var1.manager);
        }
        return false;
    }

    public ICommandSender getCommandSender()
    {
        return source;
    }

    @Override
    public String getCommandSenderName()
    {
        return source.getCommandSenderName();
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates()
    {
        return source.getPlayerCoordinates();
    }

    public String getUserName()
    {
        return name;
    }

    public Type getUserType()
    {
        return type;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() ^ type.hashCode() ^ source.hashCode() ^ manager.hashCode();
    }

    @Override
    public void sendChatToPlayer(String par1)
    {
        source.sendChatToPlayer(par1);
    }

    @Override
    public String translateString(String par1, Object... par2)
    {
        return source.translateString(par1, par2);
    }

    public void update(ICommandSender par1)
    {
        if (manager.checkUpdate(this, source, par1))
        {
            name = par1.getCommandSenderName();
            type = Type.getType(par1);
            source = par1;
            manager.onUpdate(this);
        }
    }

}
