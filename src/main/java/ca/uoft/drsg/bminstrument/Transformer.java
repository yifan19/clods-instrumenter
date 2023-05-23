package ca.uoft.drsg.bminstrument;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;

public class Transformer implements ClassFileTransformer {

	private Rule rule;

	Transformer(Rule rule) {
		this.rule = rule;

	}
    @Override
	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;
		String classNameSlash = rule.getClassName().replace(".", "/");

		System.out.println("loader: " + loader + "; " + "className: " + className);
		// byte[] toAdd = new byte[] {(byte) 0xb2, 0x00, 0x05, 0x04, (byte) 0x60, (byte) 0xb3, 0x00,0x05};

		//Add instrumentation to Sample class alone
		if (className.equals(classNameSlash)) {
			try {
				ClassPool classPool = ClassPool.getDefault();
				CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
				
				// CtMethod[] methods = ctClass.getDeclaredMethods();
				// for (CtMethod method : methods) {
				//     System.out.println(method.getName());
				//     System.out.println(method.getLongName());
				// }

				CtMethod instrumentedMethod = ctClass.getDeclaredMethod(rule.getMethodName());
				instrumentedMethod.insertAt(rule.getlineNumber(), 
					"System.out.println(" + rule.getVariableName() + ");");
                			
				byteCode = ctClass.toBytecode();
				ctClass.detach();
			} catch (Throwable ex) {
				System.out.println("Exception: " + ex);
				ex.printStackTrace();
			}
		}
		return byteCode;
	}
}
