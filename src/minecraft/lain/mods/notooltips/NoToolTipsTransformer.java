package lain.mods.notooltips;

import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import com.google.common.collect.Sets;

public class NoToolTipsTransformer implements IClassTransformer
{

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if ("net.minecraft.item.ItemStack".equals(transformedName))
            return transform001(bytes);
        return bytes;
    }

    private byte[] transform001(byte[] bytes)
    {
        Set<String> methodNames = Sets.newHashSet("func_82840_a", "getTooltip", "a");
        Set<String> methodDescs = Sets.newHashSet("(Lnet/minecraft/entity/player/EntityPlayer;Z)Ljava/util/List;", "(Lnet/minecraft/src/EntityPlayer;Z)Ljava/util/List;", "(Lue;Z)Ljava/util/List;");

        Set<String> targetInsnOwners = Sets.newHashSet("net/minecraft/item/ItemStack", "net/minecraft/src/ItemStack", "yd");
        Set<String> targetInsnNames = Sets.newHashSet("func_111283_C", "D");
        Set<String> targetInsnDescs = Sets.newHashSet("()Lcom/google/common/collect/Multimap;");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods)
        {
            if (methodNames.contains(methodNode.name) && methodDescs.contains(methodNode.desc))
            {
                AbstractInsnNode insn = methodNode.instructions.getFirst();
                boolean foundMethod = false;
                while (insn != null)
                {
                    if (!foundMethod && insn.getType() == AbstractInsnNode.METHOD_INSN)
                    {
                        MethodInsnNode mInsn = (MethodInsnNode) insn;
                        if (targetInsnOwners.contains(mInsn.owner) && targetInsnNames.contains(mInsn.name) && targetInsnDescs.contains(mInsn.desc))
                            foundMethod = true;
                    }
                    if (foundMethod && insn.getType() == AbstractInsnNode.VAR_INSN)
                    {
                        VarInsnNode vInsn = (VarInsnNode) insn;
                        if (vInsn.getOpcode() == Opcodes.ASTORE)
                        {
                            VarInsnNode nVInsn = new VarInsnNode(Opcodes.ALOAD, vInsn.var);
                            MethodInsnNode nMInsn = new MethodInsnNode(Opcodes.INVOKESTATIC, "lain/mods/notooltips/NoToolTipsHandler", "handleAttributesForToolTip", "(Lcom/google/common/collect/Multimap;)V");
                            methodNode.instructions.insert(insn, nMInsn);
                            methodNode.instructions.insert(insn, nVInsn);
                            foundMethod = false;
                        }
                    }
                    insn = insn.getNext();
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

}
