package lain.mods.bilicraftcomments;

import java.util.logging.Level;

public class LevelComment extends Level
{

    public static final Level comment = new LevelComment("COMMENT", FINEST.intValue(), FINEST.getResourceBundleName());

    protected LevelComment(String name, int value, String resourceBundleName)
    {
        super(name, value, resourceBundleName);
    }

}
