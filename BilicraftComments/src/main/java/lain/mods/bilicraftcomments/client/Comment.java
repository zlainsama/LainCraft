package lain.mods.bilicraftcomments.client;

import lain.mods.bilicraftcomments.BilicraftComments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Comment
{

    public static final ClientProxy proxy = (ClientProxy) BilicraftComments.proxy;

    public static Minecraft client = null;
    public static FontRenderer renderer = null;
    public static ScaledResolution res = null;
    public static int width = 320;
    public static int height = 240;
    public static int numSlots = 18;

    public static boolean isSlotOccupied(int slot, int mode, Comment exclusion, int stress, long ticks)
    {
        for (Comment c : proxy.comments)
        {
            if (c.slot != slot || c.mode != mode || c == exclusion)
                continue;
            switch (mode)
            {
                case 0:
                    if (c.x + (c.textWidth() >> stress) + 2 > width)
                        return true;
                    break;
                case 1:
                    if (c.lifespan <= 0 || (ticks < c.ticksCreated + ((c.lifespan + c.expandedLife) >> stress)))
                        return true;
                    break;
                case 2:
                    if (c.lifespan <= 0 || (ticks < c.ticksCreated + ((c.lifespan + c.expandedLife) >> stress)))
                        return true;
                    break;
                case 3:
                    int w = c.textWidth();
                    if (c.x + (w - w >> stress) < 2)
                        return true;
                    break;
            }
        }
        return false;
    }

    public static void prepare()
    {
        client = FMLClientHandler.instance().getClient();
        renderer = client.fontRenderer;
        res = new ScaledResolution(client.gameSettings, client.displayWidth, client.displayHeight);
        width = res.getScaledWidth();
        height = res.getScaledHeight();
        numSlots = (int) ((float) (height - 60) / (float) (renderer.FONT_HEIGHT + 1));
    }

    public final int mode;
    public final String text;
    public final int lifespan;
    public final long ticksCreated;

    public int slot = -1;
    public int expandedLife = 0;

    public int x;
    public int y;
    public int color; // unused
    public boolean shadow; // unused

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
            int s = 0;
            switch (mode)
            {
                case 0: // Normal
                    do
                    {
                        for (int i = 0; i < numSlots; i++)
                        {
                            int j = (int) Math.round(Math.random() * (numSlots - 1));
                            if (!isSlotOccupied(j, mode, this, s, ticks))
                            {
                                slot = j;
                                break;
                            }
                        }
                        // for (int i = 0; i < numSlots; i++)
                        // {
                        // if (!isSlotOccupied(i, mode, this, s, ticks))
                        // {
                        // slot = i;
                        // break;
                        // }
                        // }
                        if (slot != -1)
                            break;
                    }
                    while (s++ < 3);
                    break;
                case 1: // Top
                    do
                    {
                        for (int i = 0; i < numSlots; i++)
                        {
                            if (!isSlotOccupied(i, mode, this, s, ticks))
                            {
                                slot = i;
                                break;
                            }
                        }
                        if (slot != -1)
                            break;
                    }
                    while (s++ < 3);
                    break;
                case 2: // Bottom
                    do
                    {
                        for (int i = numSlots - 1; i >= 0; i--)
                        {
                            if (!isSlotOccupied(i, mode, this, s, ticks))
                            {
                                slot = i;
                                break;
                            }
                        }
                        if (slot != -1)
                            break;
                    }
                    while (s++ < 3);
                    break;
                case 3: // Backward
                    do
                    {
                        for (int i = 0; i < numSlots; i++)
                        {
                            int j = (int) Math.round(Math.random() * (numSlots - 1));
                            if (!isSlotOccupied(j, mode, this, s, ticks))
                            {
                                slot = j;
                                break;
                            }
                        }
                        // for (int i = 0; i < numSlots; i++)
                        // {
                        // if (!isSlotOccupied(i, mode, this, s, ticks))
                        // {
                        // slot = i;
                        // break;
                        // }
                        // }
                        if (slot != -1)
                            break;
                    }
                    while (s++ < 3);
                    break;
            }
        }
        if (slot == -1)
            expandedLife = Math.max(0, (int) (ticks - ticksCreated));
    }

    public void draw()
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        renderer.drawString(text, 0, 0, 0xFFFFFF, true);
        GL11.glPopMatrix();
    }

    public boolean isDead(long ticks)
    {
        if (slot == -1)
            return false;
        return lifespan > 0 && (ticks >= ticksCreated + lifespan + expandedLife);
    }

    public void onAdd()
    {
        prepare();
        update(ticksCreated, 0F);
    }

    public void onRemove()
    {
        slot = -1;
        try
        {
            client.ingameGUI.getChatGUI().addTranslatedMessage(text, new Object[0]);
        }
        catch (Throwable ignored)
        {
        }
    }

    public int textWidth()
    {
        return renderer.getStringWidth(text);
    }

    public void update(long ticks, float partialTicks)
    {
        if (slot == -1)
            assignSlot(ticks);
        if (slot != -1)
        {
            float f1 = Math.min(((float) (ticks - ticksCreated) + partialTicks) / (float) (lifespan + expandedLife), 1.0F);
            float f2;
            int w = textWidth();
            switch (mode)
            {
                case 0:
                    f2 = 1F - f1;
                    x = (int) (f2 * (width + w)) - w;
                    break;
                case 1:
                case 2:
                    x = (width - w) >> 1;
                    break;
                case 3:
                    f2 = f1;
                    x = (int) (f2 * (width + w)) - w;
                    break;
            }
            y = 2 + slot * (renderer.FONT_HEIGHT + 1);
        }
    }

}
