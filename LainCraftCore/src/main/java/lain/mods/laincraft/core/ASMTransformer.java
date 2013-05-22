package lain.mods.laincraft.core;

import java.util.HashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.IClassTransformer;

public class ASMTransformer implements IClassTransformer
{

    private abstract class InstanceHook implements IClassTransformer
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
        private boolean disabled = false;
        private HashMap<String, String> maps = new HashMap<String, String>();

        public InstanceHook()
        {
            try
            {
                setup();
            }
            catch (Throwable t)
            {
                disabled = true;
                System.err.println(t.toString());
            }
        }

        public void addMapping(String targetClass, String hookClass)
        {
            maps.put(targetClass.replace('.', '/'), hookClass.replace('.', '/'));
        }

        public abstract void setup() throws Throwable;

        @Override
        public byte[] transform(String paramString1, String paramString2, byte[] paramArrayOfByte)
        {
            if (disabled)
                return paramArrayOfByte;
            if (paramArrayOfByte == null)
                return null;
            if (maps.containsValue(paramString2.replace('.', '/')))
                return paramArrayOfByte;
            ClassReader classReader = new ClassReader(paramArrayOfByte);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classReader.accept(new a(classWriter), ClassReader.EXPAND_FRAMES);
            return modified ? classWriter.toByteArray() : paramArrayOfByte;
        }
    }

    private IClassTransformer[] transformers = new IClassTransformer[] { new InstanceHook()
    {
        @Override
        public void setup() throws Throwable
        {
            addMapping("net.minecraft.client.entity.EntityClientPlayerMP", "lain.mods.laincraft.player.ClientPlayer");
            addMapping("net.minecraft.client.entity.EntityOtherPlayerMP", "lain.mods.laincraft.player.ClientPlayerOther");
            addMapping("net.minecraft.entity.player.EntityPlayerMP", "lain.mods.laincraft.player.ServerPlayer");
            addMapping("net.minecraft.command.ServerCommandManager", "lain.mods.laincraft.server.CommandManager");
        }
    } };

    @Override
    public byte[] transform(String paramString1, String paramString2, byte[] paramArrayOfByte)
    {
        for (IClassTransformer transformer : transformers)
            paramArrayOfByte = transformer.transform(paramString1, paramString2, paramArrayOfByte);
        return paramArrayOfByte;
    }

}
