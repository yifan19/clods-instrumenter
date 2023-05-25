package ca.uoft.drsg.bminstrument;

import java.lang.instrument.Instrumentation;


public class InstrumentationAgent {
    public static Instrumentation instrumentation;
    public static void premain(String argument, 
                             Instrumentation instrumentation) {
        System.out.println("premain running...");
        InstrumentationAgent.instrumentation = instrumentation;
        Listener listener = new Listener(8089);
        listener.start();
        // Rule CHANGME = new Rule("bar", "foo", 0, "baz");
        // start registering instrumentation

    }

    public static void agentmain(String argument,
                                 Instrumentation instrumentation) {
        premain(argument, instrumentation);
    }

    // private static void transformClass(
    //     String className, Instrumentation instrumentation) {
    //     Class<?> targetCls = null;
    //     ClassLoader targetClassLoader = null;
    //     // see if we can get the class using forName
    
    //     try {
    //         targetCls = Class.forName(className);
    //         targetClassLoader = targetCls.getClassLoader();
    //         transform(targetCls, targetClassLoader, instrumentation);
    //         return;
    //     } catch (Exception ex) {
    //         LOGGER.error("Class [{}] not found with Class.forName");
    //     }
    //     // otherwise iterate all loaded classes and find what we want
    //     for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
    //         if(clazz.getName().equals(className)) {
    //             targetCls = clazz;
    //             targetClassLoader = targetCls.getClassLoader();
    //             transform(targetCls, targetClassLoader, instrumentation);
    //             return;
    //         }
    //     }
    //     throw new RuntimeException(
    //     "Failed to find class [" + className + "]");
    // }

    // private static void transform(
    //     Class<?> clazz, 
    //     ClassLoader classLoader,
    //     Instrumentation instrumentation) {
    //     AtmTransformer dt = new AtmTransformer(
    //     clazz.getName(), classLoader);
    //     instrumentation.addTransformer(dt, true);
    //     try {
    //         instrumentation.retransformClasses(clazz);
    //     } catch (Exception ex) {
    //         throw new RuntimeException(
    //         "Transform failed for: [" + clazz.getName() + "]", ex);
    //     }
    // }

}
