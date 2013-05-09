package lain.mods.simplecommands.command;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.player.ServerPlayer;
import lain.mods.laincraft.util.Translator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class CommandHome extends CommandBase
{

    public static Translator notfound = new Translator("laincraft.command.home.notfound");

    static
    {
        notfound.a("No home set yet.", "en_US");
        notfound.a("未设置家", "zh_CN");
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
        return "home";
    }

    private void home(ServerPlayer par1)
    {
        if (par1._getHomePosition() != null)
            par1._teleportTo(par1._getHomePosition(), false);
        else
            notfound.s(par1, EnumChatFormatting.RED.toString());
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            home((ServerPlayer) par1);
    }

}
