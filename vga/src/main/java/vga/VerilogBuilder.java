package vga;


import hat.backend.codebuilders.HATCodeBuilder;
import hat.backend.codebuilders.HATCodeBuilderWithContext;
import hat.optools.BinaryArithmeticOrLogicOperation;
import hat.optools.BinaryTestOpWrapper;
import hat.optools.ConstantOpWrapper;
import hat.optools.ConvOpWrapper;
import hat.optools.FieldLoadOpWrapper;
import hat.optools.FieldStoreOpWrapper;
import hat.optools.ForOpWrapper;
import hat.optools.FuncCallOpWrapper;
import hat.optools.FuncOpWrapper;
import hat.optools.IfOpWrapper;
import hat.optools.InvokeOpWrapper;
import hat.optools.JavaBreakOpWrapper;
import hat.optools.JavaContinueOpWrapper;
import hat.optools.JavaLabeledOpWrapper;
import hat.optools.LambdaOpWrapper;
import hat.optools.LogicalOpWrapper;
import hat.optools.OpWrapper;
import hat.optools.ReturnOpWrapper;
import hat.optools.StructuralOpWrapper;
import hat.optools.TernaryOpWrapper;
import hat.optools.TupleOpWrapper;
import hat.optools.UnaryArithmeticOrLogicOpWrapper;
import hat.optools.VarDeclarationOpWrapper;
import hat.optools.VarFuncDeclarationOpWrapper;
import hat.optools.VarLoadOpWrapper;
import hat.optools.VarStoreOpWrapper;
import hat.optools.WhileOpWrapper;
import hat.optools.YieldOpWrapper;
import hat.util.StreamCounter;
import jdk.incubator.code.Op;
import jdk.incubator.code.op.CoreOp;
import jdk.incubator.code.type.ClassType;
import jdk.incubator.code.type.JavaType;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Consumer;

import static jdk.incubator.code.interpreter.Interpreter.resolveToClass;


public class VerilogBuilder extends HATCodeBuilderWithContext<VerilogBuilder> implements HATCodeBuilder.CodeBuilderInterface<VerilogBuilder> {

    @Override
    public VerilogBuilder varLoad(CodeBuilderContext codeBuilderContext, VarLoadOpWrapper varLoadOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder varStore(CodeBuilderContext codeBuilderContext, VarStoreOpWrapper varStoreOpWrapper) {
        return null;
    }


    @Override
    public VerilogBuilder varFuncDeclaration(CodeBuilderContext codeBuilderContext, VarFuncDeclarationOpWrapper varFuncDeclarationOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder fieldLoad(CodeBuilderContext codeBuilderContext, FieldLoadOpWrapper fieldLoadOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder fieldStore(CodeBuilderContext codeBuilderContext, FieldStoreOpWrapper fieldStoreOpWrapper) {

        // The only store we should see is a fake assignment to '.v in a Bus.

      //  if (fieldStoreOpWrapper.isKernelContextAccess()) {
            identifier(fieldStoreOpWrapper.toText()).dot().identifier(fieldStoreOpWrapper.fieldName());
     //   } else if (fieldStoreOpWrapper.isStaticFinalPrimitive()) {
       //     Object value = fieldStoreOpWrapper.getStaticFinalPrimitiveValue();
         //   literal(value.toString());
       // } else {
         //   throw new IllegalStateException("What is this field load ?" + fieldStoreOpWrapper.fieldRef());
       // }

        lineComment("In field store operation");
        return null;
    }

    @Override
    public VerilogBuilder unaryOperation(CodeBuilderContext codeBuilderContext, UnaryArithmeticOrLogicOpWrapper unaryArithmeticOrLogicOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder binaryOperation(CodeBuilderContext codeBuilderContext, BinaryArithmeticOrLogicOperation binaryArithmeticOrLogicOperation) {
        return null;
    }

    @Override
    public VerilogBuilder logical(CodeBuilderContext codeBuilderContext, LogicalOpWrapper logicalOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder binaryTest(CodeBuilderContext codeBuilderContext, BinaryTestOpWrapper binaryTestOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder conv(CodeBuilderContext codeBuilderContext, ConvOpWrapper convOpWrapper) {
        return null;
    }


    @Override
    public VerilogBuilder javaYield(CodeBuilderContext codeBuilderContext, YieldOpWrapper yieldOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder lambda(CodeBuilderContext codeBuilderContext, LambdaOpWrapper lambdaOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder tuple(CodeBuilderContext codeBuilderContext, TupleOpWrapper tupleOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder funcCall(CodeBuilderContext codeBuilderContext, FuncCallOpWrapper funcCallOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaIf(CodeBuilderContext codeBuilderContext, IfOpWrapper ifOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaWhile(CodeBuilderContext codeBuilderContext, WhileOpWrapper whileOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaLabeled(CodeBuilderContext codeBuilderContext, JavaLabeledOpWrapper javaLabeledOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaContinue(CodeBuilderContext codeBuilderContext, JavaContinueOpWrapper javaContinueOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaBreak(CodeBuilderContext codeBuilderContext, JavaBreakOpWrapper javaBreakOpWrapper) {
        return null;
    }

    @Override
    public VerilogBuilder javaFor(CodeBuilderContext codeBuilderContext, ForOpWrapper forOpWrapper) {
        return null;
    }


    @Override
    public VerilogBuilder ternary(CodeBuilderContext codeBuilderContext, TernaryOpWrapper ternaryOpWrapper) {
        return null;
    }


    @Override
    public VerilogBuilder ret(CodeBuilderContext codeBuilderContext, ReturnOpWrapper returnOpWrapper) {
        return null;
    }


    VerilogBuilder moduleDeclaration(String moduleName, Consumer<VerilogBuilder> consumer) {
        beginModule().space().append(moduleName);
        consumer.accept(this);
        endModule();
        return self();
    }

    VerilogBuilder beginEnd(Consumer<VerilogBuilder> consumer) {
        begin();
        consumer.accept(this);
        end();
        return self();
    }

    public VerilogBuilder begin() {
        return keyword("begin");
    }

    public VerilogBuilder end() {
        return keyword("end");
    }

    public VerilogBuilder module() {
        return keyword("module");
    }

    public VerilogBuilder beginModule() {
        return module();
    }

    public VerilogBuilder endModule() {
        return keyword("endmodule");
    }

    public VerilogBuilder beginEndIndented(Consumer<VerilogBuilder> consumer) {
        return this.begin().nl().indent(consumer).nl().end();
    }

    public VerilogBuilder input() {
        return keyword("input");
    }

    public VerilogBuilder output() {
        return keyword("output");
    }

    public VerilogBuilder wire() {
        return keyword("wire");
    }

    public VerilogBuilder always() {
        return keyword("always");
    }

    public VerilogBuilder posedge() {
        return keyword("posedge");
    }

    public VerilogBuilder reg() {
        return keyword("reg");
    }


    public VerilogBuilder range(int hi, int lo) {
        return append(Integer.toString(hi)).colon().append(Integer.toString(lo));
    }

    public VerilogBuilder varName(CoreOp.VarOp varOp) {
        return this.identifier(varOp.varName());
    }

    public VerilogBuilder varName(CoreOp.VarOp varOp, String suffix) {
        return this.identifier(varOp.varName() + suffix);
    }


    public VerilogBuilder ioRegOrWire(IORegWireInfo ioRegWireInfo, CoreOp.VarOp varOp) {

        return either(ioRegWireInfo.isIn(),
                _ -> input(),
                _ -> output()
        ).space().either(ioRegWireInfo.isWire(),
                _ -> wire(),
                _ -> reg()
        ).when((ioRegWireInfo.max() != ioRegWireInfo.min()), _ ->
                sbrace(_ -> range(ioRegWireInfo.max(), ioRegWireInfo.min()))
        ).space().varName(varOp);
    }

    public VerilogBuilder type(JavaType javaType) {
        var ioRegWireInfo = IORegWireInfo.from(javaType);
        either(ioRegWireInfo.isWire(), _ -> wire(), _ -> reg());
        if (ioRegWireInfo.min() != ioRegWireInfo.max()) {
            sbrace(_ -> range(ioRegWireInfo.max(), ioRegWireInfo.min()));
        }

        return self();
    }

    public VerilogBuilder alwaysAtPosEdge(String edge, Consumer<VerilogBuilder> consumer) {
        return always().space().at().paren(_ -> posedge().space().append(edge)).space().beginEnd(consumer);
    }

    public VerilogBuilder varDeclaration(CodeBuilderContext context, VarDeclarationOpWrapper opWrapper) {


        type(opWrapper.javaType()).space().varName(opWrapper.op());
        // We intercept when rhs is a factory call.
        // So var w = wire_9(); -> wire [8:0] w;
        if (Verilog.isConnection(resolveToClass(opWrapper.javaType()))){
            var zeroth = opWrapper.operandNAsResult(0).op();
            var wrapped = OpWrapper.wrap(zeroth);
            // is the rhs a factory call?

            if (wrapped.hasNoOperands()){
               // blockComment(" factory culled ");
            }else {
                equals().space();
                parencedence(context, opWrapper, opWrapper.operandNAsResult(0).op());
            }
        }else {
            equals().space();
            parencedence(context, opWrapper, opWrapper.operandNAsResult(0).op());
        }
        return self();
    }

    public VerilogBuilder constant(CodeBuilderContext context, ConstantOpWrapper opWrapper) {
        append((opWrapper.op()).value().toString());
        return self();
    }

    public VerilogBuilder methodCall(CodeBuilderContext buildContext, InvokeOpWrapper invokeOpWrapper) {

        var name = invokeOpWrapper.name();
        // var operandCount = invokeOpWrapper.operandCount();
        // var returnType = invokeOpWrapper.javaReturnType();
        identifier(name);
        paren(_ ->
                commaSeparated(invokeOpWrapper.operands(), (op) -> {
                    if (op instanceof Op.Result result) {
                        recurse(buildContext, OpWrapper.wrap(result.op()));
                    } else {
                        throw new IllegalStateException("wtf?");
                    }
                })
        );

        return self();

    }


    static public Class<?> resolveToClass(JavaType javaType) {
        try {
            Type type = javaType.resolve(MethodHandles.lookup());
            if (type instanceof ParameterizedType parameterizedType) {
                return (Class<?>) parameterizedType.getRawType();
            } else if (type instanceof Class<?>) {
                return (Class<?>) type;
            } else {
                throw new RuntimeException("Failed to resolve type: " + type);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    record IORegWireInfo(Class<?> inOut, Class<?> wireReg, int max, int min) {

        static IORegWireInfo from(JavaType javaType) {
            // JavaType might be an input with type args
            // or a real reg or wire
            Class<?> inOutClass = resolveToClass(javaType);
            Class<?> finalActualClass =Verilog.isInOrOut(inOutClass)?
                resolveToClass(((ClassType) javaType).typeArguments().getFirst()): inOutClass;

            final Class<?> finalInOutClass = inOutClass;

            var result = new IORegWireInfo[]{null};
            if (Verilog.range.class.isAssignableFrom(finalActualClass)) {
                Arrays.stream(finalActualClass.getInterfaces())
                        .filter(iface -> !Verilog.isConnection(iface))
                        .forEach(iface -> {
                            try {
                                var maxField = iface.getDeclaredField("max");
                                var minField = iface.getDeclaredField("min");
                                result[0] = new IORegWireInfo(finalInOutClass, finalActualClass, maxField.getInt(null),
                                        minField.getInt(null));

                            } catch (NoSuchFieldException e) {
                                System.err.println(iface.toString() + " " + e.getMessage());
                                throw new RuntimeException(e);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            return result[0];
        }

        public boolean isWire() {
            return (Verilog.WireMarker.class.isAssignableFrom(wireReg));
        }

        public boolean isIn() {
            return (Verilog.Input.class.isAssignableFrom(inOut));
        }
    }

    public VerilogBuilder moduleIOArg(FuncOpWrapper.ParamTable.Info info) {
        var ioRegWireInfo = IORegWireInfo.from(info.javaType);
        ioRegOrWire(ioRegWireInfo, info.varOp);


        return self();
    }

    public VerilogBuilder module(CoreOp.FuncOp javaFunc) {
        nl();
        CodeBuilderContext buildContext = new CodeBuilderContext(OpWrapper.wrap(javaFunc));
        buildContext.scope(buildContext.funcOpWrapper, () -> {
            moduleDeclaration(buildContext.funcOpWrapper.functionName(), _ -> {
                // Here we deal with the i/o pins/connections
                paren(_ ->
                        nl().indent(_ -> commaNlSeparated(buildContext.funcOpWrapper.paramTable.list(), info ->
                                moduleIOArg(info)
                        ).nl())
                ).semicolon();
                // Here is the body of the module.
                nl();
                indent(_ -> {
                    StreamCounter.of(buildContext.funcOpWrapper.wrappedRootOpStream(), (c, root) ->
                            nlIf(c.isNotFirst()).recurse(buildContext, root).semicolonIf(!(root instanceof StructuralOpWrapper<?>))
                    );
                    alwaysAtPosEdge("clk", _ -> {
                        nl();
                    });
                    nl();
                });
            });
        });
        return self();
    }
}
