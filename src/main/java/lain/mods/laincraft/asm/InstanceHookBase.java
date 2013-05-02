package lain.mods.laincraft.asm;

import java.util.HashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.IClassTransformer;

public class InstanceHookBase implements IClassTransformer
{

    private class a extends ClassVisitor
    {
        private a(ClassVisitor cv)
        {
            super(262144, cv);
            modified = false;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
        {
            return new b(super.visitMethod(access, name, desc, signature, exceptions));
        }
    }

    private class b extends MethodVisitor
    {
        private b(MethodVisitor mv)
        {
            super(262144, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc)
        {
            if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>"))
            {
                String transformedType = FMLDeobfuscatingRemapper.INSTANCE.map(owner).replace('.', '/');
                if (maps.containsKey(transformedType))
                {
                    owner = maps.get(transformedType);
                    modified = true;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitTypeInsn(int opcode, String type)
        {
            if (opcode == Opcodes.NEW)
            {
                String transformedType = FMLDeobfuscatingRemapper.INSTANCE.map(type).replace('.', '/');
                if (maps.containsKey(transformedType))
                {
                    type = maps.get(transformedType);
                    modified = true;
                }
            }
            super.visitTypeInsn(opcode, type);
        }
    }

    private boolean modified = false;
    private HashMap<String, String> maps = new HashMap<String, String>();

    public void addMapping(String targetClass, String hookClass)
    {
        maps.put(targetClass.replace('.', '/'), hookClass.replace('.', '/'));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if (maps.containsValue(transformedName.replace('.', '/')))
            return bytes;
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classReader.accept(new a(classWriter), ClassReader.EXPAND_FRAMES);
        return modified ? classWriter.toByteArray() : bytes;
    }

}
