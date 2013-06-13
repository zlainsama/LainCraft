package lain.mods.bilicraftcomments.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lain.mods.bilicraftcomments.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{

    KeyBinding keyCommentGui = new KeyBinding("Open Comment Gui", 0x17);
    List<Comment> comments = new CopyOnWriteArrayList();
    long ticks = 0L;

    @Override
    public void displayComment(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dis = null;
        try
        {
            dis = new DataInputStream(new ByteArrayInputStream(packet.data));
            int mode = dis.readShort();
            int lifespan = dis.readShort();
            String text = dis.readUTF();
            if (!StringUtils.stripControlCodes(text).isEmpty())
            {
                Comment comment = new Comment(mode, text, lifespan, ticks);
                comment.onAdd();
                comments.add(comment);
            }
        }
        catch (IOException e)
        {
            System.err.println("error reading incoming comment: " + e.toString());
        }
        finally
        {
            if (dis != null)
                try
                {
                    dis.close();
                }
                catch (IOException ignored)
                {
                }
        }
    }

    @Override
    public void load()
    {
        super.load();
        MinecraftForge.EVENT_BUS.register(this);
        TickRegistry.registerTickHandler(new ITickHandler()
        {
            @Override
            public String getLabel()
            {
                return "TickCounter";
            }

            @Override
            public void tickEnd(EnumSet<TickType> arg0, Object... arg1)
            {
                ticks = ticks + 1;
            }

            @Override
            public EnumSet<TickType> ticks()
            {
                return EnumSet.of(TickType.CLIENT);
            }

            @Override
            public void tickStart(EnumSet<TickType> arg0, Object... arg1)
            {
            }
        }, Side.CLIENT);
        NetworkRegistry.instance().registerConnectionHandler(new IConnectionHandler()
        {
            @Override
            public void clientLoggedIn(NetHandler arg0, INetworkManager arg1, Packet1Login arg2)
            {
                comments.clear();
            }

            @Override
            public void connectionClosed(INetworkManager arg0)
            {
            }

            @Override
            public void connectionOpened(NetHandler arg0, MinecraftServer arg1, INetworkManager arg2)
            {
            }

            @Override
            public void connectionOpened(NetHandler arg0, String arg1, int arg2, INetworkManager arg3)
            {
            }

            @Override
            public String connectionReceived(NetLoginHandler arg0, INetworkManager arg1)
            {
                return null;
            }

            @Override
            public void playerLoggedIn(Player arg0, NetHandler arg1, INetworkManager arg2)
            {
            }
        });
        KeyBindingRegistry.registerKeyBinding(new KeyHandler(new KeyBinding[] { keyCommentGui }, new boolean[] { false })
        {
            @Override
            public String getLabel()
            {
                return "KeyBinding";
            }

            @Override
            public void keyDown(EnumSet<TickType> paramEnumSet, KeyBinding paramKeyBinding, boolean paramBoolean1, boolean paramBoolean2)
            {
                if (paramKeyBinding == keyCommentGui && paramBoolean1)
                {
                    Minecraft client = FMLClientHandler.instance().getClient();
                    if (client != null && client.currentScreen == null)
                        client.displayGuiScreen(new GuiComment());
                }
            }

            @Override
            public void keyUp(EnumSet<TickType> paramEnumSet, KeyBinding paramKeyBinding, boolean paramBoolean)
            {
            }

            @Override
            public EnumSet<TickType> ticks()
            {
                return EnumSet.of(TickType.CLIENT);
            }
        });
    }

    @ForgeSubscribe
    public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) // PS: forge 721 doesn't send this, you need at least forge 722
        {
            if (!comments.isEmpty())
            {
                Comment.prepare();
                for (Comment comment : comments)
                {
                    if (comment.isDead(ticks))
                    {
                        comment.onRemove();
                        comments.remove(comment);
                        continue;
                    }
                    comment.update(ticks, event.partialTicks);
                    comment.draw();
                }
            }
        }
    }

}
