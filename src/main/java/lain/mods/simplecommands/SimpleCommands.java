package lain.mods.simplecommands;

import lain.mods.laincraft.Plugin;
import lain.mods.laincraft.util.configuration.Config;
import lain.mods.simplecommands.command.CommandBack;
import lain.mods.simplecommands.command.CommandHome;
import lain.mods.simplecommands.command.CommandSetHome;
import lain.mods.simplecommands.command.CommandSpawn;
import lain.mods.simplecommands.command.CommandStorage;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class SimpleCommands extends Plugin
{

    @Config.SingleComment("this will add /back, /home, /sethome, /spawn, /storage")
    @Config.Property(defaultValue = "false")
    public static boolean enableExtraCommands;

    @Override
    public String getName()
    {
        return "SimpleCommands";
    }

    @Override
    public void onDisable()
    {
    }

    @Override
    public void onEnable()
    {
        Config config = getConfig();
        config.register(SimpleCommands.class, null);
        config.load();
        config.save();
        if (!enableExtraCommands)
            setEnabled(false);
    }

    @Subscribe
    public void ServerStarting(FMLServerStartingEvent event)
    {
        if (enableExtraCommands)
        {
            event.registerServerCommand(new CommandBack());
            event.registerServerCommand(new CommandHome());
            event.registerServerCommand(new CommandSetHome());
            event.registerServerCommand(new CommandSpawn());
            event.registerServerCommand(new CommandStorage());
        }
    }

}
