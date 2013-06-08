package lain.mods.bilicraftcomments.client;

import lain.mods.bilicraftcomments.common.TimeMarker;

public class CommentSlot
{

    private final TimeMarker marker = new TimeMarker();

    public final int slotNumber;

    public CommentSlot(int par1)
    {
        this.slotNumber = par1;
    }

    public boolean isOccupied(long ticks, int mode, boolean flag)
    {
        switch (mode)
        {
            case 0:
                return !marker.checkTimeIfValid(ticks, flag ? 40L : 100L, false);
            case 1:
                return !marker.checkTimeIfValid(ticks, flag ? 40L : 100L, false);
            case 2:
                return !marker.checkTimeIfValid(ticks, flag ? 40L : 100L, false);
            case 3:
                return !marker.checkTimeIfValid(ticks, flag ? 10L : 40L, false);
        }
        return true;
    }

    public boolean set(long ticks, int mode, boolean flag)
    {
        return set(ticks, mode, flag, false);
    }

    public boolean set(long ticks, int mode, boolean flag, boolean force)
    {
        if (force || !isOccupied(ticks, mode, flag))
        {
            marker.markTime(ticks);
            return true;
        }
        return false;
    }

}
