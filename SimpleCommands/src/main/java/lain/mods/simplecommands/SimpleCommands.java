package lain.mods.simplecommands;

import lain.mods.simplecommands.command.CommandBack;
import lain.mods.simplecommands.command.CommandHome;
import lain.mods.simplecommands.command.CommandSetHome;
import lain.mods.simplecommands.command.CommandSpawn;
import lain.mods.simplecommands.command.CommandStorage;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "SimpleCommands", name = "SimpleCommands", version = "", dependencies = "required-after:LainCraftCore", useMetadata = true)
public class SimpleCommands
{

    @Mod.ServerStarting
    public void ServerStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBack());
        event.registerServerCommand(new CommandHome());
        event.registerServerCommand(new CommandSetHome());
        event.registerServerCommand(new CommandSpawn());
        event.registerServerCommand(new CommandStorage());
    }

}
