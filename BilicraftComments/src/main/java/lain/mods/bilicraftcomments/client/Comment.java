package lain.mods.bilicraftcomments.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import cpw.mods.fml.client.FMLClientHandler;

public class Comment
{

    public static final List<CommentSlot> slots;
    public static final int numSlots = 20;

    static
    {
        slots = new ArrayList(numSlots);
        for (int i = 0; i < numSlots; i++)
            slots.add(new CommentSlot(i));
    }

    public static void postRender()
    {
    }

    public static void preRender()
    {
    }

    public final int mode;
    public final String text;
    public final int lifespan;

    public final long ticksCreated;

    public int slot = -1;
    public int expandedLife = 0;

    public Minecraft client = FMLClientHandler.instance().getClient();
    public FontRenderer renderer = client.fontRenderer;

    public int x;
    public int y;
    public int color;
    public boolean shadow;

    public Comment(int mode, String text, int lifespan, long ticks)
    {
        this.mode = mode;
        this.text = text;
        this.lifespan = lifespan;
        this.ticksCreated = ticks;
    }

    public void assignSlot(long ticks)
    {
        if (slot == -1)
        {
            CommentSlot s = null;
            boolean f = false;
            switch (mode)
            {
                case 0: // Normal
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    f = true;
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    break;
                case 1: // Top
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    f = true;
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    break;
                case 2: // Bottom
                    for (int i = numSlots - 1; i >= 0; i--)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    f = true;
                    for (int i = numSlots - 1; i >= 0; i--)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    break;
                case 3: // Backward
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    f = true;
                    for (int i = 0; i < numSlots; i++)
                    {
                        s = slots.get(i);
                        if (s != null && !s.isOccupied(ticks, mode, f))
                            break;
                    }
                    break;
            }
            if (s != null && s.set(ticks, mode, f))
                slot = s.slotNumber;
        }
        if (slot == -1)
            expandedLife = Math.max(0, (int) (ticks - ticksCreated));
    }

    public void draw()
    {
        renderer.drawString(text, x, y, color, shadow);
    }

    public boolean isDead(long ticks)
    {
        if (slot == -1)
            return false;
        return lifespan > 0 && (ticks >= ticksCreated + lifespan + expandedLife);
    }

    public void onAdd()
    {
        assignSlot(ticksCreated);
    }

    public void onRemove()
    {
        slot = -1;
    }

    public int width()
    {
        return renderer.getStringWidth(text);
    }

    public void update(long ticks, float partialTicks)
    {
        if (slot == -1)
            assignSlot(ticks);
        if (slot != -1)
        {
            float f1 = (ticks - ticksCreated) / (lifespan + expandedLife);
            switch (mode)
            {
                case 0:
                    x = (int) (f1 * (client.displayWidth + width()));
                    break;
                case 1:
                case 2:
                case 3:
            }
            y = slot * 9;
            color = 0xFFFFFF;
            shadow = true;
        }
    }

}
