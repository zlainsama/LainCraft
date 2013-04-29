package lain.mods.laincraft;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import lain.mods.laincraft.command.CommandBack;
import lain.mods.laincraft.command.CommandHome;
import lain.mods.laincraft.command.CommandSetHome;
import lain.mods.laincraft.command.CommandSpawn;
import lain.mods.laincraft.command.CommandStorage;
import lain.mods.laincraft.event.ServerPlayerCanUseCommandEvent;
import lain.mods.laincraft.util.configuration.Config;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class LainCraft extends DummyModContainer
{

    @Config.SingleComment("this will add /back, /home, /sethome, /spawn, /storage")
    @Config.Property(defaultValue = "true")
    public static boolean enableExtraCommands;

    public static boolean isLain(String username)
    {
        return "zlainsama".equalsIgnoreCase(StringUtils.stripControlCodes(username));
    }

    private Set<Plugin> plugins = new HashSet<Plugin>();

    public LainCraft()
    {
        super(new ModMetadata());
        getMetadata();
        loadPlugins();
    }

    @Override
    public ModMetadata getMetadata()
    {
        ModMetadata md = super.getMetadata();
        md.modId = "LainCraft";
        md.name = "LainCraft";
        md.version = "1.0";
        md.authorList = Arrays.asList("Lain");
        md.description = "";
        md.autogenerated = false;
        StringBuilder var1 = new StringBuilder("\nplugins:").append(EnumChatFormatting.GREEN);
        if (plugins.isEmpty())
            var1.append(EnumChatFormatting.RED + " no plugins installed.");
        else
            for (Plugin plugin : new TreeSet<Plugin>(plugins))
                var1.append(" ").append(plugin.getName());
        md.description = md.description.concat(var1.toString());
        return md;
    }

    @Subscribe
    public void init(FMLPreInitializationEvent event)
    {
        Config config = new Config(new File(event.getModConfigurationDirectory(), "LainCraft.cfg"));
        config.register(LainCraft.class, null);
        config.load();
        config.save();
    }

    @Subscribe
    public void load(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void loadPlugin(String classname)
    {
        try
        {
            Plugin plugin = (Plugin) Class.forName(classname).newInstance();
            if (plugin != null)
                plugins.add(plugin);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void loadPlugins()
    {
        loadPlugin("lain.mods.molanguage.MoLanguage");
        loadPlugin("lain.mods.skinmanager.SkinManager");
    }

    @ForgeSubscribe
    public void onCheckCommandAccess(ServerPlayerCanUseCommandEvent event)
    {
        if (isLain(event.player.username) && FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer())
            event.allow = true;
    }

    @Override
    public boolean registerBus(EventBus eventbus, LoadController controllor)
    {
        eventbus.register(this);
        for (Plugin plugin : new TreeSet<Plugin>(plugins))
            eventbus.register(plugin);
        return true;
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
