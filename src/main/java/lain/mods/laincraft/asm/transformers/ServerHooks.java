package lain.mods.laincraft.asm.transformers;

public class ServerHooks extends InstanceHook
{

    public ServerHooks()
    {
        addMapping("net.minecraft.command.ServerCommandManager", "lain.mods.laincraft.server.CommandManager");
    }

}
