package lain.mods.laincraft.event;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.Event.HasResult;

@HasResult
public class ServerCheckCommandAccessEvent extends Event
{

    public static ServerCheckCommandAccessEvent post(ICommandSender sender, ICommand command, boolean defaultAllowed)
    {
        ServerCheckCommandAccessEvent event = new ServerCheckCommandAccessEvent(sender, command, defaultAllowed);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public final ICommandSender sender;
    public final ICommand command;
    public final boolean defaultAllowed;

    public ServerCheckCommandAccessEvent(ICommandSender sender, ICommand command, boolean defaultAllowed)
    {
        super();
        this.sender = sender;
        this.command = command;
        this.defaultAllowed = defaultAllowed;
    }

}
