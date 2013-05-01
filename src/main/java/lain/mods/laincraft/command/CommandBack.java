package lain.mods.laincraft.command;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.player.ServerPlayer;
import lain.mods.laincraft.util.Translator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

public class CommandBack extends CommandBase
{

    public static Translator notfound = new Translator("laincraft.command.back.notfound");

    static
    {
        notfound.a("Previous location does not exists.", "en_US");
        notfound.a("上一个位置不存在", "zh_CN");
    }

    private void back(ServerPlayer par1)
    {
        if (par1._getLastPosition() != null)
            par1._teleportTo(par1._getLastPosition(), false);
        else
            notfound.s(par1, EnumChatFormatting.RED.toString());
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
        return "back";
    }

    @Override
    public void processCommand(ICommandSender par1, String[] par2)
    {
        if (par1 instanceof ServerPlayer)
            back((ServerPlayer) par1);
    }

}
