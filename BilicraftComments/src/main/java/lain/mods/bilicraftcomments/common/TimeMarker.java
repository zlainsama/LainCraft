package lain.mods.bilicraftcomments.common;

public class TimeMarker
{

    private long lastMark = 0L;

    public boolean checkTimeIfValid(long time, long delay)
    {
        return checkTimeIfValid(time, delay, true);
    }

    public boolean checkTimeIfValid(long time, long delay, boolean auto)
    {
        if (time < lastMark)
        {
            markTime(time);
            return false;
        }
        else if (lastMark + delay <= time)
        {
            if (auto)
                markTime(time);
            return true;
        }
        return false;
    }

    public void clear()
    {
        lastMark = 0L;
    }

    public long getLastMark()
    {
        return lastMark;
    }

    public void markTime(long time)
    {
        lastMark = time;
    }

}
