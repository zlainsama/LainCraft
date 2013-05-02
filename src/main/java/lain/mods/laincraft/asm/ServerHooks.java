package lain.mods.laincraft.asm;

public class ServerHooks extends InstanceHookBase
{

    public ServerHooks()
    {
        addMapping("net.minecraft.command.ServerCommandManager", "lain.mods.laincraft.server.CommandManager");
    }

}
