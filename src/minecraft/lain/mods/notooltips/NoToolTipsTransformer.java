package lain.mods.notooltips;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class NoToolTipsTransformer implements IClassTransformer
{

    class a extends ClassVisitor
    {

        Set<String> names;
        String cl;

        public a(ClassVisitor cv, String classname)
        {
            super(Opcodes.ASM4, cv);
            cl = FMLDeobfuscatingRemapper.INSTANCE.unmap(classname.replace('.', '/'));
            names = new HashSet<String>();
            names.add(NoToolTips.RUNTIME_DEOBF ? "func_111205_h" : "func_111205_h");
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
        {
            if (names.contains(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(cl, name, desc)) && "()Lcom/google/common/collect/Multimap;".equals(desc))
            {
                return new b();
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    class b extends MethodVisitor
    {

        public b()
        {
            super(Opcodes.ASM4, null);
        }

    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if ("net.minecraft.item.ItemSword".equals(transformedName))
            return transform001(bytes);
        if ("net.minecraft.item.ItemTool".equals(transformedName))
            return transform002(bytes);
        return bytes;
    }

    private byte[] transform001(byte[] bytes)
    {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classReader.accept(new a(classWriter, "net.minecraft.item.ItemSword"), ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private byte[] transform002(byte[] bytes)
    {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classReader.accept(new a(classWriter, "net.minecraft.item.ItemTool"), ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

}
