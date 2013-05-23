package lain.mods.laincraft.server;

import java.util.List;
import lain.mods.laincraft.event.ServerCheckCommandAccessEvent;
import lain.mods.laincraft.event.ServerCommandRenamingEvent;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;

public class CommandManager extends ServerCommandManager
{

    private class CommandWrapper implements ICommand
    {
        private final ICommand c;
        private final String n;

        private CommandWrapper(ICommand c)
        {
            this.c = c;
            String n = ServerCommandRenamingEvent.post(c).newName;
            if (n == null)
                n = c.getCommandName();
            this.n = n;
        }

        @Override
        public List addTabCompletionOptions(ICommandSender par1, String[] par2)
        {
            return c.addTabCompletionOptions(par1, par2);
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender par1)
        {
            ServerCheckCommandAccessEvent event = ServerCheckCommandAccessEvent.post(par1, this, c.canCommandSenderUseCommand(par1));
            switch (event.getResult())
            {
                case ALLOW:
                    return true;
                case DEFAULT:
                    return event.defaultAllowed;
                case DENY:
                    return false;
                default:
                    break;
            }
            return false;
        }

        @Override
        public int compareTo(Object o)
        {
            if (o instanceof CommandWrapper)
                return c.compareTo(((CommandWrapper) o).c);
            return c.compareTo(o);
        }

        @Override
        public List getCommandAliases()
        {
            return c.getCommandAliases();
        }

        @Override
        public String getCommandName()
        {
            return n;
        }

        @Override
        public String getCommandUsage(ICommandSender par1)
        {
            return c.getCommandUsage(par1);
        }

        @Override
        public boolean isUsernameIndex(String[] par1, int par2)
        {
            return c.isUsernameIndex(par1, par2);
        }

        @Override
        public void processCommand(ICommandSender par1, String[] par2)
        {
            c.processCommand(par1, par2);
        }
    }

    @Override
    public ICommand registerCommand(ICommand par1)
    {
        return super.registerCommand(new CommandWrapper(par1));
    }

}
