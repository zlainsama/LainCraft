package lain.mods.bilicraftcomments.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lain.mods.bilicraftcomments.common.CommonProxy;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy
{

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
            Comment comment = new Comment(mode, text, lifespan, ticks);
            comment.onAdd();
            comments.add(comment);
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
    }

    @ForgeSubscribe
    public void onRenderOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL)
        {
            Comment.preRender();
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
            Comment.postRender();
        }
    }

}
