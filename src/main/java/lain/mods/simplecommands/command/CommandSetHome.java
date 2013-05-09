package lain.mods.simplecommands.command;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.player.ServerPlayer;
import lain.mods.laincraft.util.PositionData;
import lain.mods.laincraft.util.Translator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandSetHome extends CommandBase
{

    public static Translator homeset = new Translator("laincraft.command.sethome.homeset");

    static
    {
        homeset.a("Home set.", "en_US");
        homeset.a("已设置家", "zh_CN");
    }

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
        return "sethome";
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            setHome((ServerPlayer) par1);
    }

    private void setHome(ServerPlayer par1)
    {
        par1._setHomePosition(new PositionData(par1));
        homeset.s(par1);
    }

}
