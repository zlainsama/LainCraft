package lain.mods.laincraft.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class ConfigUtils
{

    public static void loadFromConfig(Configuration config, Class cls, String prefix, String category)
    {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : cls.getDeclaredFields())
        {
            int mod = f.getModifiers();
            if (f.getName().startsWith(prefix) && Modifier.isStatic(mod) && !Modifier.isFinal(mod))
            {
                try
                {
                    f.setAccessible(true);
                    fields.add(f);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
        ConfigCategory cate = config.getCategory(category);
        for (Field f : fields)
        {
            try
            {
                String n = f.getName().substring(prefix.length());
                if (!cate.containsKey(n))
                {
                    Property prop = null;
                    Object o = f.get(null);
                    if (o instanceof Integer)
                    {
                        prop = new Property(n, "", Property.Type.INTEGER);
                        prop.set((Integer) o);
                    }
                    else if (o instanceof Boolean)
                    {
                        prop = new Property(n, "", Property.Type.BOOLEAN);
                        prop.set((Boolean) o);
                    }
                    else if (o instanceof Double)
                    {
                        prop = new Property(n, "", Property.Type.DOUBLE);
                        prop.set((Double) o);
                    }
                    else if (o instanceof String)
                    {
                        prop = new Property(n, "", Property.Type.STRING);
                        prop.set((String) o);
                    }
                    else if (o != null)
                    {
                        prop = new Property(n, "", Property.Type.STRING);
                        prop.set(o.toString());
                    }
                    else
                    {
                        prop = new Property(n, "", Property.Type.STRING);
                        prop.set("");
                    }
                    cate.put(n, prop);
                }
                else
                {
                    Property prop = cate.get(n);
                    switch (prop.getType())
                    {
                        case INTEGER:
                            f.set(null, prop.getInt(0));
                            break;
                        case BOOLEAN:
                            f.set(null, prop.getBoolean(false));
                            break;
                        case DOUBLE:
                            f.set(null, prop.getDouble(0D));
                            break;
                        case STRING:
                        default:
                            f.set(null, prop.getString());
                            break;
                    }
                }
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    public static void saveToConfig(Configuration config, Class cls, String prefix, String category)
    {
        List<Field> fields = new ArrayList<Field>();
        for (Field f : cls.getDeclaredFields())
        {
            int mod = f.getModifiers();
            if (f.getName().startsWith(prefix) && Modifier.isStatic(mod) && !Modifier.isFinal(mod))
            {
                try
                {
                    f.setAccessible(true);
                    fields.add(f);
                }
                catch (Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
        ConfigCategory cate = config.getCategory(category);
        for (Field f : fields)
        {
            try
            {
                String n = f.getName().substring(prefix.length());
                Property prop = null;
                Object o = f.get(null);
                if (o instanceof Integer)
                {
                    prop = new Property(n, "", Property.Type.INTEGER);
                    prop.set((Integer) o);
                }
                else if (o instanceof Boolean)
                {
                    prop = new Property(n, "", Property.Type.BOOLEAN);
                    prop.set((Boolean) o);
                }
                else if (o instanceof Double)
                {
                    prop = new Property(n, "", Property.Type.DOUBLE);
                    prop.set((Double) o);
                }
                else if (o instanceof String)
                {
                    prop = new Property(n, "", Property.Type.STRING);
                    prop.set((String) o);
                }
                else if (o != null)
                {
                    prop = new Property(n, "", Property.Type.STRING);
                    prop.set(o.toString());
                }
                else
                {
                    prop = new Property(n, "", Property.Type.STRING);
                    prop.set("");
                }
                cate.put(n, prop);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

}
