package ca.uoft.drsg.bminstrument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.Mnemonic;
import javassist.bytecode.Opcode;
import javassist.compiler.Javac;
import javassist.Modifier;

public class BytecodeManip {

    private static final Logger LOG = LogManager.getLogger(BytecodeManip.class);
    private String instAgentClazz = "ca/uoft/drsg/bminstrument/InstrumentationAgent";
    private String bufferVar = "buffer";
    private String bufferType = "Lca/uoft/drsg/bminstrument/buffer/LogEventBuffer;";

    private String bufferClazz = "ca/uoft/drsg/bminstrument/buffer/LogEventBuffer";
    private String putMethod = "put";
    private String putMethodType = "(JJ)V";
    
    private String putLoopMethod = "putLoop";
    private String putLoopMethodType = "(JJ)V";

    private String putObjectMethod = "putObject";
    private String putObjectMethodType = "(Ljava/lang/Object;J)V";

    private CtBehavior method;
    private CtClass clazz;
    private Rule rule;
    private String insertedLine;

   

    public BytecodeManip(CtBehavior method, CtClass clazz,
                         Rule rule, String insertedLine) {
        this.method = method;
        this.clazz = clazz;
        this.rule = rule;
        this.insertedLine = insertedLine;
    }
    
    private int decodeLoadParam(int op, CodeIterator ci, int index) {
        int variableIndex;

        switch (op) {
            case Opcode.ALOAD:
            case Opcode.DLOAD:
            case Opcode.FLOAD:
            case Opcode.ILOAD:
            case Opcode.LLOAD:
            variableIndex = ci.signedByteAt(index + 1);
            break;
            case Opcode.ALOAD_0:
            case Opcode.DLOAD_0:
            case Opcode.FLOAD_0:
            case Opcode.ILOAD_0:
            case Opcode.LLOAD_0:
            variableIndex = 0;
            break;
            case Opcode.ALOAD_1:
            case Opcode.DLOAD_1:
            case Opcode.FLOAD_1:
            case Opcode.ILOAD_1:
            case Opcode.LLOAD_1:
            variableIndex = 1;
            break;
            case Opcode.ALOAD_2:
            case Opcode.DLOAD_2:
            case Opcode.FLOAD_2:
            case Opcode.ILOAD_2:
            case Opcode.LLOAD_2:
            variableIndex = 2;
            break;
            case Opcode.ALOAD_3:
            case Opcode.DLOAD_3:
            case Opcode.FLOAD_3:
            case Opcode.ILOAD_3:
            case Opcode.LLOAD_3:
            variableIndex = 3;
            break;
            default:
            variableIndex = -1;
        }
        
        return variableIndex;
    }

    private int decodeStoreParam(int op, CodeIterator ci, int index) {
        int variableIndex;

        switch (op) {
            case Opcode.ASTORE:
            case Opcode.DSTORE:
            case Opcode.FSTORE:
            case Opcode.ISTORE:
            case Opcode.LSTORE:
            case Opcode.IINC:
            variableIndex = ci.signedByteAt(index + 1);
            break;
            case Opcode.ASTORE_0:
            case Opcode.DSTORE_0:
            case Opcode.FSTORE_0:
            case Opcode.ISTORE_0:
            case Opcode.LSTORE_0:
            variableIndex = 0;
            break;
            case Opcode.ASTORE_1:
            case Opcode.DSTORE_1:
            case Opcode.FSTORE_1:
            case Opcode.ISTORE_1:
            case Opcode.LSTORE_1:
            variableIndex = 1;
            break;
            case Opcode.ASTORE_2:
            case Opcode.DSTORE_2:
            case Opcode.FSTORE_2:
            case Opcode.ISTORE_2:
            case Opcode.LSTORE_2:
            variableIndex = 2;
            break;
            case Opcode.ASTORE_3:
            case Opcode.DSTORE_3:
            case Opcode.FSTORE_3:
            case Opcode.ISTORE_3:
            case Opcode.LSTORE_3:
            variableIndex = 3;
                break;           
            default:
            variableIndex = -1;
        }

        return variableIndex;
    }
    private String decodeFunctionReturnType(CodeIterator ci, int index) {
        int globalIndex = ci.s16bitAt(index + 1);
        ConstPool constPool = clazz.getClassFile2().getConstPool();
        String signature = constPool.getMethodrefType(globalIndex);
        LOG.info("signature = " + signature);
        return signature;
    }


    private String decodeFieldType(int op, CodeIterator ci, int index) {
        int globalIndex = ci.s16bitAt(index + 1);
        ConstPool constPool = clazz.getClassFile2().getConstPool();
        String fieldType = constPool.getFieldrefType(globalIndex);
        LOG.info("field index = " + globalIndex + ", type is = " + fieldType);
        return fieldType;
    }

    private void injectInstrumentation(int op, CodeIterator ci, int index, Bytecode code) {
        /* hack to test out instrumentating after a branch */
        if (rule.getStrategy().equals("conditional")) {
            grabValueLoop(code);
            callPutEntry(code);
            return;
        }

        switch(op) {
            case Opcode.IINC:
            int variableIndex = ci.signedByteAt(index + 1);
            moveToAfterByteCode(ci);
            grabValueLocalVariableInteger(code, variableIndex);
            callPut(code);
            break;


            case Opcode.INVOKEINTERFACE:
            case Opcode.INVOKESTATIC:
            case Opcode.INVOKESPECIAL:
            case Opcode.INVOKEDYNAMIC:
            case Opcode.INVOKEVIRTUAL:

            String returnSignature = decodeFunctionReturnType(ci, index);
            if (rule.getStrategy().equals("beforeSelfCall")) {
                grabValueObject(code);
                callPut(code);
            } else {
                moveToAfterByteCode(ci);
                switch(returnSignature.charAt(returnSignature.length() - 1)) {
                // Z //for boolean:
                // B //for byte:
                // S //for short:
                // C //for char:
                case 'L':
                case ';':
                grabValueObject(code);
                callPutObject(code);
                break;
                case 'J': // for long:
                grabValue64(code, false);
                callPut(code);
                break;
                case 'D': //for double
                grabValue64(code, true);
                callPut(code);
                break;
                case 'I': // for int:
                case 'F':// for float:
                default: // other
                grabValueDefault(code);
                callPut(code);
                break;
            }
            }
            break;

            case Opcode.PUTFIELD:
            case Opcode.GETFIELD:
            String fullType = decodeFieldType(op, ci, index);
            char letterType = fullType.charAt(0);
            
            /* a putfield can store anything, depending on the field */
            if (op == Opcode.GETFIELD) {
                int new_index = moveToAfterByteCode(ci);
                int new_op = ci.byteAt(new_index);
                LOG.info("bci={}, {}",new_index, Mnemonic.OPCODE[new_op]);
            }
            
            switch(letterType) {
                // Z //for boolean:
                // B //for byte:
                // S //for short:
                // C //for char:
                case '[':
                case 'L':
                grabValueObject(code);
                callPutObject(code);
                break;
                case 'J': // for long:
                grabValue64(code, false);
                callPut(code);
                break;
                case 'D': //for double
                grabValue64(code, true);
                callPut(code);
                break;
                case 'I': // for int:
                case 'F':// for float:
                grabValueDefault(code);
                callPut(code);
                break;
            }
            break;
            // 2 stack values:
            case Opcode.IF_ACMPEQ:
            case Opcode.IF_ACMPNE:
            case Opcode.IF_ICMPEQ:
            case Opcode.IF_ICMPGE:
            case Opcode.IF_ICMPGT:
            case Opcode.IF_ICMPLE:
            case Opcode.IF_ICMPLT:
            case Opcode.IF_ICMPNE:
            // 1 stack values:
            case Opcode.IFEQ:
            case Opcode.IFGE:
            case Opcode.IFGT:
            case Opcode.IFLE:
            case Opcode.IFLT:
            case Opcode.IFNE:
            case Opcode.IFNONNULL:
            case Opcode.IFNULL:
            grabValueLoop(code);
            callPutEntry(code);
            break;

            // case Opcode.ISTORE:
            case Opcode.LSTORE:
            case Opcode.LSTORE_0:
            case Opcode.LSTORE_1:
            case Opcode.LSTORE_2:
            case Opcode.LSTORE_3:
            case Opcode.LASTORE:
            case Opcode.LRETURN:
            grabValue64(code, false);
            callPut(code);
            break;
            case Opcode.DSTORE:
            case Opcode.DSTORE_0:
            case Opcode.DSTORE_1:
            case Opcode.DSTORE_2:
            case Opcode.DSTORE_3:
            // reading a long or a double from array
            case Opcode.DASTORE:
            case Opcode.DRETURN:
            grabValue64(code, true);
            callPut(code);
            break;

            case Opcode.ARETURN:
            case Opcode.ASTORE:
            case Opcode.ASTORE_0:
            case Opcode.ASTORE_1:
            case Opcode.ASTORE_2:
            case Opcode.ASTORE_3:
            grabValueObject(code);
            callPutObject(code);
            break;
            
            case Opcode.ALOAD:
            case Opcode.ALOAD_0:
            case Opcode.ALOAD_1:
            case Opcode.ALOAD_2:
            case Opcode.ALOAD_3:
            case Opcode.ACONST_NULL:

            moveToAfterByteCode(ci);
            grabValueObject(code);
            callPutObject(code);
            break;
            case Opcode.LLOAD:
            case Opcode.LLOAD_0:
            case Opcode.LLOAD_1:
            case Opcode.LLOAD_2:
            case Opcode.LLOAD_3:
            case Opcode.LCONST_0:
            case Opcode.LCONST_1:
            moveToAfterByteCode(ci);
            grabValue64(code, false);
            callPut(code);
            case Opcode.DLOAD:
            case Opcode.DLOAD_0:
            case Opcode.DLOAD_1:            
            case Opcode.DLOAD_2:
            case Opcode.DLOAD_3:
            case Opcode.DCONST_0:
            case Opcode.DCONST_1:
            moveToAfterByteCode(ci);
            grabValue64(code, true);
            callPut(code);
            break;
            case Opcode.ILOAD:
            case Opcode.ILOAD_0:
            case Opcode.ILOAD_1:
            case Opcode.ILOAD_2:
            case Opcode.ILOAD_3:
            case Opcode.ICONST_M1:
            case Opcode.ICONST_0:
            case Opcode.ICONST_1:
            case Opcode.ICONST_2:
            case Opcode.ICONST_3:
            case Opcode.ICONST_4:
            case Opcode.ICONST_5:
            moveToAfterByteCode(ci);
            default:
            grabValueDefault(code);
            callPut(code);
            break;

        }
    }
    private int moveToAfterByteCode(CodeIterator ci) {
        int index = -1;
        try {
            index = ci.next();
        } catch (Exception e) {
            LOG.info(e.toString());
        } finally {
            return index;
        }
    }

    private void grabValueDefault(Bytecode code) {
        code.add(Bytecode.DUP);
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        // assume data is a java word (4 Bytes)
        code.add(Bytecode.SWAP);
        
        code.add(Bytecode.I2L);
    }
    private void grabValueObject(Bytecode code) {
        code.add(Bytecode.DUP);
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        // assume data is a java word (4 Bytes)
        code.add(Bytecode.SWAP);
        // no converting the integer into long
    }

    private void grabValueLoop(Bytecode code) {
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        code.addLconst(rule.getLoopId());
    }

    private void grabValueLocalVariableInteger(Bytecode code, int localVariableIndex) {
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        // assume data is a java word (4 Bytes)        
        code.addIload(localVariableIndex);
        code.add(Bytecode.I2L);
    }
    private void grabValueLocalVariable(Bytecode code, int localVariableIndex, char type) {
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        // assume data is a java word (4 Bytes) 
        switch(type) {
            case '[':
            case 'L': // for object
            code.addAload(localVariableIndex);
            break;
            case 'J': // for long:
            code.addLload(localVariableIndex);
            break;
            case 'D': //for double
            code.addDload(localVariableIndex);
            break;
            case 'F':// for float:
            code.addFload(localVariableIndex);
            code.add(Bytecode.I2L);
            break;
            case 'I': // for int:
            default:
            code.addIload(localVariableIndex);
            code.add(Bytecode.I2L);
        }
    }

    private void grabValue64(Bytecode code, boolean isDouble) {
        code.add(Bytecode.DUP2);
        if (isDouble) {
            code.add(Bytecode.D2L);
        }
        code.addGetstatic(instAgentClazz, bufferVar, bufferType);
        // {DATA DATA} STATIC
        code.add(Bytecode.DUP_X2);
        // STATIC {DATA DATA} STATIC
        code.add(Bytecode.POP);
    }

    private void callPutObject(Bytecode code) {
        code.addIconst(rule.getId());
        code.add(Bytecode.I2L);
        code.addInvokevirtual(bufferClazz, putObjectMethod, putObjectMethodType);
    }
    private void callPut(Bytecode code) {
        code.addIconst(rule.getId());
        code.add(Bytecode.I2L);
        code.addInvokevirtual(bufferClazz, putMethod, putMethodType);
    }
    private void callPutEntry(Bytecode code) {
        code.addIconst(rule.getId());
        code.add(Bytecode.I2L);
        code.addInvokevirtual(bufferClazz, putLoopMethod, putLoopMethodType);
    }



    private int decodeInvokeParam(int op, CodeIterator ci, int index) {
        int globalIndex;
        switch (op) {
            case Opcode.INVOKEINTERFACE:
            case Opcode.INVOKESTATIC:
            case Opcode.INVOKESPECIAL:
            case Opcode.INVOKEDYNAMIC:
            case Opcode.INVOKEVIRTUAL:
                globalIndex = ci.s16bitAt(index + 1);
                break;           
            default:
                globalIndex = -1;
        }
        return globalIndex; 
    }

    public String findVariableType() {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        int offset = findVariableOffset(localVariableAttribute);
        if (offset == -1) {
            return "";
        }
        return localVariableAttribute.descriptor(offset);
    }

    public int findVariableIndex() {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        int offset = findVariableOffset(localVariableAttribute);
        if (offset == -1) {
            return -1;
        }
        return localVariableAttribute.index(offset);
    }

    private int findVariableOffset(LocalVariableAttribute localVariableAttribute) {
        int n = localVariableAttribute.tableLength();
        int i;
        for (i = 0; i < n; ++i) {
            int index = localVariableAttribute.index(i);
            LOG.info("{} {} {}",localVariableAttribute.descriptor(i), localVariableAttribute.variableName(i),
                      index);
            String varName = localVariableAttribute.variableName(i);
            if (varName.equals(rule.getVariableName())) {
                return i;
            }
        }
        return -1;
    }

    private int findInjectionLocation(LineNumberAttribute.Pc pc_start, LineNumberAttribute.Pc pc_end) {
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        // LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) 
        //     codeAttribute.getAttribute(LocalVariableAttribute.tag);
        ConstPool constPool = clazz.getClassFile2().getConstPool();
        int varIndex = -1;

        if (!rule.getStrategy().equals("afterCall")) {
            varIndex = findVariableIndex();
        }
         
        LOG.info("variable Index = {}",varIndex);

        CodeIterator ci = codeAttribute.iterator();
        
        ci.move(pc_start.index);
        int index = 0;
        while (ci.hasNext()) {
            try {
                index = ci.next();
            } catch (Exception e) {
                LOG.info(e.toString());
            }
            if (index >= pc_end.index) {
                LOG.info("arrived at pc_end and did not find");
                break;
            }
            int op = ci.byteAt(index);
            LOG.info("bci={}, {}",index, Mnemonic.OPCODE[op]);
            
            // if (rule.getStrategy().equals("after")) {


            // }

            if (rule.getStrategy().equals("before")) {
                int res = decodeLoadParam(op, ci, index);
                LOG.info("decodeLoadParam = {}", res);
                if (res == varIndex) {
                    break;
                }
            } else if (rule.getStrategy().equals("afterCall")) {
                
                int res = decodeInvokeParam(op, ci, index);
                LOG.info("decodeInvokeParam = {}", res);
                if (res > 0) {
                    String name = constPool.getMethodrefClassName(res) + "." +
                                  constPool.getMethodrefName(res);
                    LOG.info("name = {}", name);

                    if (name.equals(rule.getVariableName())) {
                        try {
                            index = ci.next();
                        } catch (Exception e) {
                            LOG.info(e.toString());
                        }
                        break;
                    }
                }

            } else {
                int res = decodeStoreParam(op, ci, index);
                LOG.info("decodeStoreParam = {}", res);

                if (res == varIndex) {
                    try {
                        index = ci.next();
                    } catch (Exception e) {
                        LOG.info(e.toString());
                    }
                    break;
                }
            }
        }
        return index;
    }
    public void logVar() {

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

        int start_index = findInjectionLocation(pc_start, pc_end);

        CodeIterator it = codeAttribute.iterator();

        Javac jv = new Javac(clazz);

        
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

            int index = it.insertAt(start_index, b.get());
            LOG.info("inserted at bc {}, return {}", start_index, index);
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

    public void logStack() {
        LOG.info("logging at bytecode index {}", rule.getByteCodeIndex());;
     

        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        // LineNumberAttribute lineNumberAttribute = (LineNumberAttribute) codeAttribute.getAttribute(LineNumberAttribute.tag);


        // LineNumberAttribute.Pc pc_start;
        // LineNumberAttribute.Pc pc_end;

        // pc_start = lineNumberAttribute.toNearPc(rule.getlineNumber());
        // pc_end = lineNumberAttribute.toNearPc(pc_start.line + 1);

        // int start_index = findInjectionLocation(pc_start, pc_end);
        int start_index = rule.getByteCodeIndex();
        // LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);

        // int n = localVariableAttribute.tableLength();
        // int localVariableIndex = -1;
        // int i;
        // for (i = 0; i < n; ++i) {
        //     int index = localVariableAttribute.index(i);
        //     LOG.info("{} {} {}",localVariableAttribute.descriptor(i), localVariableAttribute.variableName(i),
        //               index);
        //     String varName = localVariableAttribute.variableName(i);
        //     if (varName.equals(rule.getVariableName())) {
        //         localVariableIndex = localVariableAttribute.index(i);
        //         break;
        //     }
        // }
        // LOG.info("line num =");
        // lineNumberAttribute.toStartPc(rule.getlineNumber() + 1);
        // LOG.info("bci = {}",bci_initial2);
        ConstPool cPool = clazz.getClassFile().getConstPool();
        // cPool.addClassInfo(instAgentClazz);
        // cPool.addNameAndTypeInfo(bufferVar, bufferType);
        
        Bytecode code = new Bytecode(cPool);
        // clazz.get
        // assume data is int
                // code.add(Bytecode.SWAP);
        CodeIterator ci = codeAttribute.iterator();
        ci.move(start_index);
        int op = ci.byteAt(start_index);
        LOG.info(Mnemonic.OPCODE[op]);

        injectInstrumentation(op, ci, start_index, code);
        LOG.info(code.getSize());
        // code.setMaxLocals(1);


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
            int old_stack = codeAttribute.getMaxStack();
            int new_stack = codeAttribute.computeMaxStack();
            if (old_stack < new_stack) {
                LOG.info("stackSizeChange: {} -> {}", old_stack, new_stack);
            }
        } catch (BadBytecode e) {
            e.printStackTrace();
        }

    }

    void logParameter() {
        LOG.info("logging parameter: {}", rule.getVariableName());
        ConstPool cPool = clazz.getClassFile().getConstPool();
        CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
        CodeIterator ci = codeAttribute.iterator();
        ci.move(0);
        Bytecode code = new Bytecode(cPool);
        
        int variableIndex = findVariableIndex();
        LOG.info("variable_index: {}", variableIndex);
        String variableType = findVariableType();
        char letterType = variableType.charAt(0);

        grabValueLocalVariable(code, variableIndex, letterType);
        switch(letterType) {
            // Z //for boolean:
            // B //for byte:
            // S //for short:
            // C //for char:
            case 'L':
            case '[':
            callPutObject(code);
            break;
            case 'J': // for long:
            case 'D': //for double
            case 'I': // for int:
            case 'F':// for float:
            default:
            callPut(code);    
        }
        try {
            ci.insert(code.get());
            int old_stack = codeAttribute.getMaxStack();
            int new_stack = codeAttribute.computeMaxStack();
            if (old_stack < new_stack) {
                LOG.info("stackSizeChange: {} -> {}", old_stack, new_stack);
            }
        } catch (BadBytecode e) {
            e.printStackTrace();
        }
    }

}
