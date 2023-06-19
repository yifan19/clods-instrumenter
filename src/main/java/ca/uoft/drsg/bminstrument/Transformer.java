package ca.uoft.drsg.bminstrument;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.compiler.Javac;
import javassist.Modifier;
public class Transformer implements ClassFileTransformer {
    private static final Logger LOG = LogManager.getLogger(Transformer.class);

	private Rule rule;

	public Transformer(Rule rule) {
		this.rule = rule;

	}

	public CtMethod findMethod(CtClass clazz) throws NotFoundException {
		CtMethod[] methods = clazz.getDeclaredMethods();
		CtMethod ret = null;
		for (CtMethod method : methods) {
			LOG.debug("method seen {}", method.getName());
			if (rule.getMethodName().equals(method.getName())) {
				LOG.info("found potential method for transformation {}", rule.getMethodName());
                if (areParamsEqual(method)) {
                    return method;
                }
            }
        }
        return null;
	}

    public boolean areParamsEqual(CtMethod method) throws NotFoundException{
        String [] ruleParams = rule.getParameters();
        CtClass[] targetMethodParams = method.getParameterTypes();

        if (ruleParams == null) {
            return true;
        }
        LOG.info("target={}, rulebook={}", targetMethodParams.length, ruleParams.length);

        if (targetMethodParams.length != ruleParams.length) {

            return false;
        }
        for (int i = 0; i < targetMethodParams.length; i++) {
            LOG.info("ruleParam[{}]={}, targetParam[{}]={}", i, ruleParams[i], i, targetMethodParams[i].getName());

            if (!ruleParams[i].equals(targetMethodParams[i].getName())) {
                return false;
            }
        }
        // if code gets here
        return true;
    }

    
    @Override
	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;
		String classNameSlash = rule.getClassName().replace(".", "/");

		LOG.info("loader: " + loader + "; " + "className: " + className);
		// byte[] toAdd = new byte[] {(byte) 0xb2, 0x00, 0x05, 0x04, (byte) 0x60, (byte) 0xb3, 0x00,0x05};

		//Add instrumentation to Sample class alone
		if (className.equals(classNameSlash)) {
		LOG.info("found class: start transforming {}", className);

			try {
				ClassPool classPool = ClassPool.getDefault();
				CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod instrumentedMethod = findMethod(ctClass);
                if (findMethod(ctClass) == null) {
                    LOG.error("Could not find the appropriate method");
                    return null;
                }
                LOG.info("Transforming method {}", instrumentedMethod.getLongName());
                // CtMethod instrumentedMethod = ctClass.getDeclaredMethod(rule.getMethodName());
                String insertedLine =
                    "ca.uoft.drsg.bminstrument.InstrumentationAgent.buffer.put( (long)" +
                    rule.getId() + ", (long)" +
                    rule.getVariableName() + ");";
                LOG.info(insertedLine);
                // log_bci(instrumentedMethod, ctClass);
                if (rule.getlineNumber() == -1) {
                    instrumentedMethod.insertBefore(insertedLine); 
                } else {
                //     instrumentedMethod.insertAt(rule.getlineNumber(), 
                //         insertedLine);
                    BytecodeManip bcm = new BytecodeManip(instrumentedMethod, ctClass, rule);
                    bcm.logVar();
                }
                // log_bci(instrumentedMethod, ctClass);

                // instrumentedMethod.insertAt(rule.getlineNumber(), 
				// 	rule.getVariableName() + "++;");
				byteCode = ctClass.toBytecode();
                
                try (FileOutputStream fos = new FileOutputStream("/data/new" + rule.getId() + ".class")) {
                    fos.write(byteCode);
                }
				ctClass.detach();
			} catch (Throwable ex) {
				LOG.info("Exception caught: " + ex);
				ex.printStackTrace();
			}
		}
		return byteCode;
	}
}
