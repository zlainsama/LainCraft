package lain.mods.laincraft.utils.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import lain.mods.laincraft.utils.ReflectionUtils;
import lain.mods.laincraft.utils.io.UnicodeInputStreamReader;

public class Config
{

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public static @interface Property
    {
        public String defaultValue() default "";

        public String name() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    public static @interface SingleComment
    {
        public String value();
    }

    public static final Pattern lineSplitter = Pattern.compile("\r?\n");
    public static final Pattern lineMarker = Pattern.compile("\\\\n");
    public static final String newLine = System.getProperty("line.separator");

    public static final String signature = "SimpleConfiguration v1 [%s]";

    private final File file;
    private final String name;
    private final Map<Field, List<Object>> linkedFields;
    private final Map<String, ConfigProperty> props;

    public String comment;

    public Config(File par1, String par2)
    {
        file = par1;
        name = par2;
        linkedFields = new HashMap<Field, List<Object>>();
        props = new HashMap<String, ConfigProperty>();
    }

    public void clear()
    {
        props.clear();
    }

    public boolean containsKey(String key)
    {
        return props.containsKey(key);
    }

    public boolean containsValue(ConfigProperty property)
    {
        return props.containsValue(property);
    }

    public ConfigProperty get(String key)
    {
        return props.get(key);
    }

    public String getProperty(String key)
    {
        if (props.containsKey(key))
            return props.get(key).getString();
        return null;
    }

    public String getProperty(String key, String defaultValue)
    {
        if (!props.containsKey(key))
            props.put(key, new ConfigProperty(key, defaultValue));
        return props.get(key).getString();
    }

    public Set<String> keySet()
    {
        return props.keySet();
    }

    public void load()
    {
        load0();
        loadFields();
    }

    private void load0()
    {
        BufferedReader buf = null;
        try
        {
            buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(file), "UTF-8"));
            boolean flag = false;
            String line = null;
            while ((line = buf.readLine()) != null)
            {
                line = line.trim();
                if (!flag)
                {
                    if (!line.equals("# " + String.format(signature, name)))
                        break;
                    flag = true;
                }
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                int i = line.indexOf("=");
                if (i == -1)
                    continue;
                String k = line.substring(0, i).trim();
                String v = line.substring(i + 1).trim();
                setProperty(k, lineMarker.matcher(v).replaceAll("\n"));
            }
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                }
        }
    }

    private void loadFields()
    {
        for (Field f : linkedFields.keySet())
        {
            Property info = f.getAnnotation(Property.class);
            String key = info.name();
            if (key.isEmpty())
                key = f.getName();
            String defaultValue = info.defaultValue();
            if (Modifier.isStatic(f.getModifiers()))
                ReflectionUtils.parseField(f, getProperty(key, defaultValue));
            else
                for (Object obj : linkedFields.get(f))
                    ReflectionUtils.parseField(f, obj, getProperty(key, defaultValue));
            if (f.isAnnotationPresent(SingleComment.class) && props.containsKey(key))
                props.get(key).comment = f.getAnnotation(SingleComment.class).value();
        }
    }

    public ConfigProperty put(String key, ConfigProperty property)
    {
        return props.put(key, property);
    }

    public void register(Class cls, Object obj)
    {
        for (Field f : cls.getDeclaredFields())
        {
            if (!Modifier.isFinal(f.getModifiers()) && f.isAnnotationPresent(Property.class))
            {
                try
                {
                    f.setAccessible(true);
                    if (!linkedFields.containsKey(f))
                        linkedFields.put(f, new ArrayList<Object>());
                    if (cls.isInstance(obj))
                        linkedFields.get(f).add(obj);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }

    public ConfigProperty remove(String key)
    {
        return props.remove(key);
    }

    public void save()
    {
        saveFields();
        save0();
    }

    private void save0()
    {
        BufferedWriter buf = null;
        try
        {
            buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            buf.write("# " + String.format(signature, name) + newLine);
            buf.write("# WARNING: this file was saved in UTF-8" + newLine + newLine);
            if (comment != null)
            {
                for (String line : lineSplitter.split(comment))
                    buf.write("# " + line + newLine);
                buf.write(newLine);
            }
            for (String key : new TreeSet<String>(keySet()))
            {
                ConfigProperty prop = get(key);
                if (prop.comment != null)
                {
                    buf.write(newLine);
                    for (String line : lineSplitter.split(prop.comment))
                        buf.write("# " + line + newLine);
                }
                buf.write(key + " = " + lineSplitter.matcher(prop.getString()).replaceAll("\\\\n") + newLine);
            }
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (buf != null)
                try
                {
                    buf.close();
                }
                catch (IOException e)
                {
                }
        }
    }

    public void saveFields()
    {
        for (Field f : linkedFields.keySet())
        {
            Property info = f.getAnnotation(Property.class);
            String key = info.name();
            if (key.isEmpty())
                key = f.getName();
            String defaultValue = info.defaultValue();
            if (Modifier.isStatic(f.getModifiers()))
                setProperty(key, ReflectionUtils.toString(f));
            else if (!linkedFields.get(f).isEmpty())
                setProperty(key, ReflectionUtils.toString(f, linkedFields.get(f).get(0)));
            if (f.isAnnotationPresent(SingleComment.class) && props.containsKey(key))
                props.get(key).comment = f.getAnnotation(SingleComment.class).value();
        }
    }

    public void setProperty(String key, String value)
    {
        if (!props.containsKey(key))
            props.put(key, new ConfigProperty(key));
        props.get(key).set(value);
    }

    public Collection<ConfigProperty> values()
    {
        return props.values();
    }

}
