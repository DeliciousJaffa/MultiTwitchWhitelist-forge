package cat.jaffa.multitwitchwhitelist.forge.asm;

import cat.jaffa.multitwitchwhitelist.forge.LoginListener;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;
import java.util.Arrays;

/**
 * Created by Jaffa on 15/08/2017.
 */

public class LoginTransformer implements IClassTransformer
{
    private static final String[] classesBeingTransformed =
            {
                    "net.minecraft.server.management.PlayerList"
            };

    private static final boolean isBadMethod = false;

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBeingTransformed)
    {
        boolean isObfuscated = !name.equals(transformedName);
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
    }

    private static byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated)
    {
        System.out.println("Transforming: " + classesBeingTransformed[index]);
        try
        {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            switch(index)
            {
                case 0:
                    transformLogin(classNode, isObfuscated);
                    break;
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        return classBeingTransformed;
    }

    private static void transformLogin(ClassNode PlayerListClass, boolean isObfuscated) throws TransformException {
        final String allowUserToConnect = isObfuscated ? "a" : "allowUserToConnect";
        final String allowUserToConnect_DESC = "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;";

        for (MethodNode method : PlayerListClass.methods)
        {
            if (method.name.equals(allowUserToConnect) && method.desc.equals(allowUserToConnect_DESC))
            {
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray())
                {
                    if (instruction.getOpcode() == ALOAD)
                    {
                        if (((VarInsnNode) instruction).var == 0 & instruction.getNext().getOpcode() == GETFIELD)
                        {
                            targetNode = instruction;
                            break;
                        }
                    }
                }
                if (targetNode != null)
                {
                    LabelNode skipNode = new LabelNode();
                    InsnList toInsert = new InsnList();
                    toInsert.add(new VarInsnNode(ALOAD, 2));
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(LoginListener.class), "serverLogin", "(Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;", false));
                    toInsert.add(new VarInsnNode(ASTORE,4));
                    toInsert.add(new VarInsnNode(ALOAD,4));
                    toInsert.add(new JumpInsnNode(IFNULL, skipNode));
                    toInsert.add(new VarInsnNode(ALOAD,4));
                    toInsert.add(new InsnNode(ARETURN));
                    toInsert.add(skipNode);
                    method.instructions.insertBefore(targetNode, toInsert);
                }
                else
                {
                    throw new TransformException("Something went wrong transforming PlayerList.allowUserToConnect");
                }
            }
        }
    }
}