package com.hypdncy.BurpLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;


public class Agent {
    public static void premain(String agentOps, Instrumentation instrumentation) {
        instrument(instrumentation);
    }


    public static void agentmain(String agentOps, Instrumentation instrumentation) {
        instrument(instrumentation);
    }

    private static void instrument(Instrumentation instrumentation) {
        instrumentation.addTransformer(new PatcherTransformer());
    }


    private static class PatcherTransformer implements ClassFileTransformer {
        private boolean flag = false;
        private int count = 0;

        private void writeClassFileBuffer(byte[] classFileBuffer) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream("./old" + count + ".class");
                fos.write(classFileBuffer);//写入数据到E:/b.txt内
                System.out.println("数据写入完毕./old" + count + ".class");
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
            return transform1(classLoader, className, classBeingRedefined, protectionDomain, classFileBuffer);
        }


        public byte[] transform1(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
            if (!this.flag && classLoader != null && classLoader.toString().startsWith("burp.") && classFileBuffer.length > 110000) {
                System.out.println("Find className:" + className + (++count));
                ClassReader cr = new ClassReader(classFileBuffer);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);

                //writeClassFileBuffer(classFileBuffer);
                for (MethodNode method : cn.methods) {
                    if (method.desc.equals("([Ljava/lang/Object;Ljava/lang/Object;)V") && method.instructions.size() > 20000) {
                        InsnList insnList = method.instructions;
                        insnList.clear();
                        insnList.add(new VarInsnNode(25, 0));
                        insnList.add(new MethodInsnNode(184, "com/hypdncy/BurpLoader/KeyFilter", "test", "([Ljava/lang/Object;)V", false));
                        insnList.add(new InsnNode(177));
                        method.exceptions.clear();
                        method.tryCatchBlocks.clear();
                        this.flag = true;
                    }
                }

                ClassWriter writer = new ClassWriter(3);
                cn.accept(writer);
                //writeClassFileBuffer(classFileBuffer);
                return writer.toByteArray();
            } else {
                return classFileBuffer;
            }
        }
    }
}