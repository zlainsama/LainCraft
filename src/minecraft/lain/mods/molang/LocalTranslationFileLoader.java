package lain.mods.molang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.google.common.io.Closeables;

public class LocalTranslationFileLoader
{

    public static TranslationTable load(File baseDir) throws IOException
    {
        TranslationTable t = new TranslationTable();
        File dir = new File(baseDir, "langLocal");
        if (dir.exists() || dir.mkdirs())
        {
            for (File f : dir.listFiles())
            {
                if (!f.isFile())
                    continue;
                String ext = f.getName();
                if (!ext.contains("."))
                    continue;
                ext = ext.substring(ext.lastIndexOf(".") + 1);
                if (ext.equalsIgnoreCase("lang"))
                {
                    InputStream data = null;
                    try
                    {
                        data = new FileInputStream(f);
                        t.importTranslationFile(data);
                    }
                    finally
                    {
                        Closeables.close(data, true);
                    }
                }
                else if (ext.equalsIgnoreCase("zip"))
                {
                    ZipFile zip = null;
                    try
                    {
                        zip = new ZipFile(f);
                        for (ZipEntry entry : Collections.list(zip.entries()))
                        {
                            if (entry.isDirectory())
                                continue;
                            ext = entry.getName();
                            if (ext.contains("/"))
                                ext = ext.substring(ext.lastIndexOf("/") + 1);
                            if (ext.contains("\\"))
                                ext = ext.substring(ext.lastIndexOf("\\") + 1);
                            if (!ext.contains("."))
                                continue;
                            ext = ext.substring(ext.lastIndexOf(".") + 1);
                            if (ext.equalsIgnoreCase("lang"))
                                t.importTranslationFile(zip.getInputStream(entry));
                        }
                    }
                    finally
                    {
                        Closeables.close(zip, true);
                    }
                }
            }
        }
        return t;
    }

}
