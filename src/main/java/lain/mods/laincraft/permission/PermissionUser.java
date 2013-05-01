package lain.mods.laincraft.permission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lain.mods.laincraft.LainCraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.ChunkCoordinates;
import cpw.mods.fml.common.FMLCommonHandler;

public class PermissionUser implements ICommandSender
{

    public enum Type
    {
        ServerPlayer, ClientPlayer, FakePlayer, CommandBlock, ServerConsole, RemoteConsole, Group, Others;

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
            if (par1 instanceof PermissionGroup)
                return Group;
            return Others;
        }

        private final String name = name().toLowerCase();

        @Override
        public String toString()
        {
            return name;
        }
    }

    private String name;
    private Type type;
    private ICommandSender source;
    private final PermissionManager manager;
    private final Set<Permission> permissions;
    private final Set<Permission> permissions_viewport;
    private final Set<Permission> matchers;

    protected PermissionUser(ICommandSender par1, PermissionManager par2)
    {
        name = par1.getCommandSenderName();
        type = Type.getType(par1);
        source = par1;
        manager = par2;
        permissions = new HashSet<Permission>();
        permissions_viewport = Collections.unmodifiableSet(permissions);
        matchers = new HashSet<Permission>();
    }

    public boolean attachPermission(Permission par1)
    {
        if (!manager.checkAttachPermission(this, par1))
            return false;
        if (permissions.contains(par1))
            return false;
        permissions.add(par1);
        if (par1.isMatcher())
            matchers.add(par1);
        par1.onAttach(this);
        return true;
    }

    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2)
    {
        return source.canCommandSenderUseCommand(par1, par2);
    }

    public boolean detachPermission(Permission par1)
    {
        if (!manager.checkDetachPermission(this, par1))
            return false;
        if (!permissions.contains(par1))
            return false;
        permissions.remove(par1);
        matchers.remove(par1);
        par1.onDetach(this);
        return true;
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

    @Override
    public String getCommandSenderName()
    {
        return source.getCommandSenderName();
    }

    public PermissionManager getManager()
    {
        return manager;
    }

    public Set<Permission> getPermissions()
    {
        return permissions_viewport;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates()
    {
        return source.getPlayerCoordinates();
    }

    public ICommandSender getSource()
    {
        return source;
    }

    public String getUserName()
    {
        return name;
    }

    public Type getUserType()
    {
        return type;
    }

    public boolean hasEntity()
    {
        switch (type)
        {
            case ServerPlayer:
            case ClientPlayer:
            case FakePlayer:
            case CommandBlock:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() ^ type.hashCode() ^ source.hashCode() ^ manager.hashCode();
    }

    public boolean hasPermission(Permission par1)
    {
        if (!permissions.contains(par1))
        {
            if (par1.getMatcherType() != Permission.MatcherType.ALL)
            {
                if (par1.isMatcher())
                {
                    for (Permission permission : permissions)
                        if (par1.matches(permission))
                            return true;
                }
                else
                {
                    for (Permission matcher : matchers)
                        if (matcher.matches(par1))
                            return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean isOperator()
    {
        try
        {
            switch (type)
            {
                case ServerPlayer:
                case FakePlayer:
                case CommandBlock:
                    if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer())
                        if (LainCraft.isLain(name))
                            return true;
                    return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getOps().contains(name.toLowerCase());
                case ServerConsole:
                case RemoteConsole:
                    return true;
                default:
                    break;
            }
            return false;
        }
        catch (NullPointerException ignored)
        {
            return false;
        }
    }

    @Override
    public void sendChatToPlayer(String par1)
    {
        source.sendChatToPlayer(par1);
    }

    @Override
    public String toString()
    {
        return type + "." + name;
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
        }
    }

}
