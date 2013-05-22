package lain.mods.laincraft.event;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;

public class ServerCommandRenamingEvent extends Event
{

    public static ServerCommandRenamingEvent post(ICommand command)
    {
        ServerCommandRenamingEvent event = new ServerCommandRenamingEvent(command);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public final ICommand command;
    public String newName;

    public ServerCommandRenamingEvent(ICommand command)
    {
        super();
        this.command = command;
    }

}
