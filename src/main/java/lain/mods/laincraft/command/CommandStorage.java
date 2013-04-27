package lain.mods.laincraft.command;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.player.ServerPlayer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandStorage extends CommandBase
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
        return "storage";
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            ((ServerPlayer) par1)._openPersonalStorage();
    }

}
