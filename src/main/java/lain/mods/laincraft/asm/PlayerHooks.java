package lain.mods.laincraft.asm;

import java.util.HashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.IClassTransformer;

public class PlayerHooks implements IClassTransformer
{

    class a extends ClassVisitor
    {
        public a(ClassVisitor cv)
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

    class b extends MethodVisitor
    {
        public b(MethodVisitor mv)
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

    private boolean modified;

    HashMap<String, String> maps = new HashMap<String, String>();

    public PlayerHooks()
    {
        maps.put("net/minecraft/client/entity/EntityClientPlayerMP", "lain/mods/laincraft/player/ClientPlayer");
        maps.put("net/minecraft/client/entity/EntityOtherPlayerMP", "lain/mods/laincraft/player/ClientPlayerOther");
        maps.put("net/minecraft/entity/player/EntityPlayerMP", "lain/mods/laincraft/player/ServerPlayer");
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
