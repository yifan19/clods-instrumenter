package ca.uoft.drsg.bminstrument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.CtClass;
import javassist.CtMethod;
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

public class BytecodeManip {

    private static final Logger LOG = LogManager.getLogger(BytecodeManip.class);

    private CtMethod method;
    private CtClass clazz;
    private Rule rule;

    public BytecodeManip(CtMethod method, CtClass clazz, Rule rule) {
        this.method = method;
        this.clazz = clazz;
        this.rule = rule;
    }

    public void log_var() {

        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);
        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        LineNumberAttribute.Pc pc_start;
        LineNumberAttribute.Pc pc_end;

        /*
            CE: before a local variable is read  if(X)
            print before the next corresponding LOAD
            CE: or a function is called (INVOKE) if(a())
            print the top of the stack on invoke
            ? what to do with stack value, the stack might not be empty
        */
        /*
            LE: before the return statement
        */
        pc_start = lineNumberAttribute.toNearPc(rule.getlineNumber());
        pc_end = lineNumberAttribute.toNearPc(pc_start.line + 1);

        CodeIterator it = codeAttribute.iterator();

        Javac jv = new Javac(clazz);

        String insertedLine =
            "ca.uoft.drsg.bminstrument.InstrumentationAgent.buffer.put( (long)" +
            rule.getId() + ", (long)" +
            rule.getVariableName() + ");";
        try {
            jv.recordLocalVariables(codeAttribute, pc_end.index);
            jv.recordParams(method.getParameterTypes(),Modifier.isStatic(method.getModifiers()));
            jv.setMaxLocals(codeAttribute.getMaxLocals());
            jv.compileStmnt(insertedLine);
            Bytecode b = jv.getBytecode();

            int locals = b.getMaxLocals();
            int stack = b.getMaxStack();

            LOG.info("locals={}, stack={}", locals, stack);


            codeAttribute.setMaxLocals(locals);

            if (stack > codeAttribute.getMaxStack()) {
                codeAttribute.setMaxStack(stack);
            }

            int index = it.insertAt(pc_start.index, b.get());
            LOG.info("inserted at bc {}", pc_start.index);
            // iterator.insert(b.getExceptionTable(), index);
            method.getMethodInfo().rebuildStackMapIf6(clazz.getClassPool(), clazz.getClassFile2());

            CodeIterator ci = b.toCodeAttribute().iterator();
            while (ci.hasNext()) {
                int index2 = 0;
                index2 = ci.next();
                int op = ci.byteAt(index2);
                LOG.info(Mnemonic.OPCODE[op]);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }
    private void log_stack(CtMethod method, CtClass clazz) {

        String instAgentClazz = "ca/uoft/drsg/bminstrument/InstrumentationAgent";
        String bufferVar = "buffer";
        String bufferType = "Lca/uoft/drsg/bminstrument/buffer/LogEventBuffer;";

        String bufferClazz = "ca/uoft/drsg/bminstrument/buffer/LogEventBuffer";
        String putMethod = "put";
        String putMethodType = "(JJ)V";

        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);


        LineNumberAttribute.Pc pc_start;
        LineNumberAttribute.Pc pc_end;

        pc_start = lineNumberAttribute.toNearPc(rule.getlineNumber());
        pc_end = lineNumberAttribute.toNearPc(pc_start.line + 1);

        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        int n = localVariableAttribute.tableLength();
        int localVariableIndex = -1;
        int i;
        for (i = 0; i < n; ++i) {
            int index = localVariableAttribute.index(i);
            LOG.info("{} {} {}",localVariableAttribute.descriptor(i), localVariableAttribute.variableName(i),
                      index);
            String varName = localVariableAttribute.variableName(i);
            if (varName.equals(rule.getVariableName())) {
                localVariableIndex = localVariableAttribute.index(i);
                break;
            }
        }
        // LOG.info("line num =");
        // lineNumberAttribute.toStartPc(rule.getlineNumber() + 1);
        // LOG.info("bci = {}",bci_initial2);
        ConstPool cPool = clazz.getClassFile().getConstPool();
        // cPool.addClassInfo(instAgentClazz);
        // cPool.addNameAndTypeInfo(bufferVar, bufferType);
        Bytecode code = new Bytecode(cPool);
        // clazz.get
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        code.addIconst(rule.getId());
        code.add(Bytecode.I2L);
        // code.add(Bytecode.DUP2_X2);
        code.addIload(localVariableIndex);
        code.add(Bytecode.I2L);
        code.addInvokevirtual(bufferClazz, putMethod, putMethodType);
        // code.addReturn(null);
        // code.addInvokevirtual("ca/uoft/drsg/bminstrument/buffer/LogEventBuffer", "put" , "(JJ)V");

        // code.setMaxLocals(1);

        CodeIterator ci = codeAttribute.iterator();
        ci.move(pc_start.index);
        // while (ci.hasNext()) {
        //     int index = 0;
        //     try {
        //         index = ci.next();
        //     } catch (BadBytecode e) {
        //         e.printStackTrace();
        //     }
        //     if (index == bci_initial2) {
        //         return;
        //     }
        //     int op = ci.byteAt(index);
        //     if (Mnemonic.OPCODE[op].equals("")) {
        //         break;
        //     }
        //     LOG.info(Mnemonic.OPCODE[op]);
        // }

        try {
            ci.insert(code.get());
        } catch (BadBytecode e) {
            e.printStackTrace();
        }


    }

}
