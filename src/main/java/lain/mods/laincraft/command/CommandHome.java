package lain.mods.laincraft.command;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.player.ServerPlayer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandHome extends CommandBase
{

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1)
    {
        if (par1 instanceof ServerPlayer)
            return LainCraft.isLain(((ServerPlayer) par1).username) || super.canCommandSenderUseCommand(par1);
        return false;
    }

    @Override
    public String getCommandName()
    {
        return "home";
    }

    private void home(ServerPlayer par1)
    {
        if (par1._getHomePosition() != null)
            par1._teleportTo(par1._getHomePosition(), false);
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            home((ServerPlayer) par1);
    }

}
