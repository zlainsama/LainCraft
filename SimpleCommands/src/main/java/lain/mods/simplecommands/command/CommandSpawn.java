package lain.mods.simplecommands.command;

import lain.mods.laincraft.player.ServerPlayer;
import lain.mods.laincraft.utils.PositionData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.FMLCommonHandler;

public class CommandSpawn extends CommandBase
{

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1)
    {
        if (par1 instanceof ServerPlayer)
            return super.canCommandSenderUseCommand(par1);
        return false;
    }

    @Override
    public String getCommandName()
    {
        return "spawn";
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            ((ServerPlayer) par1)._teleportTo(PositionData.getSpawnPoint(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0)));
    }

}
