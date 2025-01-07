package vga;


import hat.backend.c99codebuilders.C99HatBuildContext;
import hat.optools.ConstantOpWrapper;
import hat.optools.ForOpWrapper;
import hat.optools.FuncOpWrapper;
import hat.optools.IfOpWrapper;
import hat.optools.InvokeOpWrapper;
import hat.optools.OpWrapper;
import hat.optools.StructuralOpWrapper;
import hat.optools.VarDeclarationOpWrapper;
import hat.optools.VarFuncDeclarationOpWrapper;
import hat.optools.WhileOpWrapper;
import hat.text.CodeBuilder;
import hat.util.StreamCounter;
import jdk.incubator.code.Block;
import jdk.incubator.code.Op;
import jdk.incubator.code.Value;
import jdk.incubator.code.op.CoreOp;
import jdk.incubator.code.op.ExtendedOp;
import jdk.incubator.code.type.ClassType;
import jdk.incubator.code.type.JavaType;
import jdk.incubator.code.type.PrimitiveType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VerilogBuilder extends CodeBuilder<VerilogBuilder> {

    public class BuildContext {


        static class Scope<OW extends OpWrapper<?>> {
            final BuildContext.Scope<?> parent;
            final OW opWrapper;

            public Scope(BuildContext.Scope<?> parent, OW opWrapper) {
                this.parent = parent;
                this.opWrapper = opWrapper;
            }

            public CoreOp.VarOp resolve(Value value) {
                if (value instanceof Op.Result result && result.op() instanceof CoreOp.VarOp varOp) {
                    return varOp;
                }
                if (parent != null) {
                    return parent.resolve(value);
                }
                throw new IllegalStateException("failed to resolve VarOp for value " + value);
            }
        }

        static class FuncScope extends BuildContext.Scope<FuncOpWrapper> {
            FuncScope(BuildContext.Scope<?> parent, FuncOpWrapper funcOpWrapper) {
                super(parent, funcOpWrapper);
            }

            @Override
            public CoreOp.VarOp resolve(Value value) {
                if (value instanceof Block.Parameter blockParameter) {
                    if (opWrapper.parameterVarOpMap.containsKey(blockParameter)) {
                        return opWrapper.parameterVarOpMap.get(blockParameter);
                    } else {
                        throw new IllegalStateException("what ?");
                    }
                } else {
                    return super.resolve(value);
                }
            }
        }

        static abstract class LoopScope<T extends OpWrapper<?>> extends BuildContext.Scope<T> {

            public LoopScope(BuildContext.Scope<?> parent, T opWrapper) {
                super(parent, opWrapper);
            }
        }


        static class ForScope extends BuildContext.LoopScope<ForOpWrapper> {
            Map<Block.Parameter, CoreOp.VarOp> blockParamToVarOpMap = new HashMap<>();

            ForOpWrapper forOpWrapper() {
                return opWrapper;
            }

            ForScope(BuildContext.Scope<?> parent, ForOpWrapper forOpWrapper) {
                super(parent, forOpWrapper);
                var loopParams = forOpWrapper().op().loopBody().entryBlock().parameters().toArray(new Block.Parameter[0]);
                var updateParams = forOpWrapper().op().update().entryBlock().parameters().toArray(new Block.Parameter[0]);
                var condParams = forOpWrapper().op().cond().entryBlock().parameters().toArray(new Block.Parameter[0]);
                var lastInitOp = forOpWrapper().op().init().entryBlock().ops().getLast();
                var lastInitOpOperand0Result = (Op.Result) lastInitOp.operands().getFirst();
                var lastInitOpOperand0ResultOp = lastInitOpOperand0Result.op();
                CoreOp.VarOp varOps[];
                if (lastInitOpOperand0ResultOp instanceof CoreOp.TupleOp tupleOp) {
                 /*
                 for (int j = 1, i=2, k=3; j < size; k+=1,i+=2,j+=3) {
                    float sum = k+i+j;
                 }
                 java.for
                 ()Tuple<Var<int>, Var<int>, Var<int>> -> {
                     %0 : int = constant @"1";
                     %1 : Var<int> = var %0 @"j";
                     %2 : int = constant @"2";
                     %3 : Var<int> = var %2 @"i";
                     %4 : int = constant @"3";
                     %5 : Var<int> = var %4 @"k";
                     %6 : Tuple<Var<int>, Var<int>, Var<int>> = tuple %1 %3 %5;
                     yield %6;
                 }
                 (%7 : Var<int>, %8 : Var<int>, %9 : Var<int>)boolean -> {
                     %10 : int = var.load %7;
                     %11 : int = var.load %12;
                     %13 : boolean = lt %10 %11;
                     yield %13;
                 }
                 (%14 : Var<int>, %15 : Var<int>, %16 : Var<int>)void -> {
                     %17 : int = var.load %16;
                     %18 : int = constant @"1";
                     %19 : int = add %17 %18;
                     var.store %16 %19;
                     %20 : int = var.load %15;
                     %21 : int = constant @"2";
                     %22 : int = add %20 %21;
                     var.store %15 %22;
                     %23 : int = var.load %14;
                     %24 : int = constant @"3";
                     %25 : int = add %23 %24;
                     var.store %14 %25;
                     yield;
                 }
                 (%26 : Var<int>, %27 : Var<int>, %28 : Var<int>)void -> {
                     %29 : int = var.load %28;
                     %30 : int = var.load %27;
                     %31 : int = add %29 %30;
                     %32 : int = var.load %26;
                     %33 : int = add %31 %32;
                     %34 : float = conv %33;
                     %35 : Var<float> = var %34 @"sum";
                     java.continue;
                 };
                 */
                    varOps = tupleOp.operands().stream().map(operand -> (CoreOp.VarOp) (((Op.Result) operand).op())).toList().toArray(new CoreOp.VarOp[0]);
                } else {
                 /*
                 for (int j = 0; j < size; j+=1) {
                    float sum = j;
                 }
                 java.for
                    ()Var<int> -> {
                        %0 : int = constant @"0";
                        %1 : Var<int> = var %0 @"j";
                        yield %1;
                    }
                    (%2 : Var<int>)boolean -> {
                        %3 : int = var.load %2;
                        %4 : int = var.load %5;
                        %6 : boolean = lt %3 %4;
                        yield %6;
                    }
                    (%7 : Var<int>)void -> {
                        %8 : int = var.load %7;
                        %9 : int = constant @"1";
                        %10 : int = add %8 %9;
                        var.store %7 %10;
                        yield;
                    }
                    (%11 : Var<int>)void -> {
                        %12 : int = var.load %11;
                        %13 : float = conv %12;
                        %14 : Var<float> = var %13 @"sum";
                        java.continue;
                    };

                 */
                    varOps = new CoreOp.VarOp[]{(CoreOp.VarOp) lastInitOpOperand0ResultOp};
                }
                for (int i = 0; i < varOps.length; i++) {
                    blockParamToVarOpMap.put(condParams[i], varOps[i]);
                    blockParamToVarOpMap.put(updateParams[i], varOps[i]);
                    blockParamToVarOpMap.put(loopParams[i], varOps[i]);
                }
            }


            @Override
            public CoreOp.VarOp resolve(Value value) {
                if (value instanceof Block.Parameter blockParameter) {
                    CoreOp.VarOp varOp = this.blockParamToVarOpMap.get(blockParameter);
                    if (varOp != null) {
                        return varOp;
                    }
                }
                return super.resolve(value);
            }
        }

        static class IfScope extends BuildContext.Scope<IfOpWrapper> {
            IfScope(BuildContext.Scope<?> parent, IfOpWrapper opWrapper) {
                super(parent, opWrapper);
            }
        }

        static class WhileScope extends BuildContext.LoopScope<WhileOpWrapper> {
            WhileScope(BuildContext.Scope<?> parent, WhileOpWrapper opWrapper) {
                super(parent, opWrapper);
            }

        }

        BuildContext.Scope<?> scope = null;

        private void popScope() {
            scope = scope.parent;
        }

        private void pushScope(OpWrapper<?> opWrapper) {
            scope = switch (opWrapper) {
                case FuncOpWrapper $ -> new BuildContext.FuncScope(scope, $);
                case ForOpWrapper $ -> new BuildContext.ForScope(scope, $);
                case IfOpWrapper $ -> new BuildContext.IfScope(scope, $);
                case WhileOpWrapper $ -> new BuildContext.WhileScope(scope, $);
                default -> new BuildContext.Scope<>(scope, opWrapper);
            };
        }

        public void scope(OpWrapper<?> opWrapper, Runnable r) {
            pushScope(opWrapper);
            r.run();
            popScope();
        }

        FuncOpWrapper funcOpWrapper;

        BuildContext(FuncOpWrapper funcOpWrapper) {
            this.funcOpWrapper = funcOpWrapper;
        }

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

    public VerilogBuilder inputWire() {
        return input().space().wire();
    }
    public VerilogBuilder inputWire(CoreOp.VarOp varOp) {
        return inputWire().space().varName(varOp);
    }

    public VerilogBuilder range(int hi, int lo) {
        return append(Integer.toString(hi)).colon().append(Integer.toString(lo));
    }

    public VerilogBuilder outputReg(int hi, int low) {
        return output().space().reg().sbrace(_ -> range(hi, low));
    }

    public VerilogBuilder varName(CoreOp.VarOp varOp) {
        return this.identifier(varOp.varName());
    }
    public VerilogBuilder varName(CoreOp.VarOp varOp, String suffix) {
        return this.identifier(varOp.varName()+suffix);
    }
    public VerilogBuilder outputReg(int hi, int lo,CoreOp.VarOp varOp) {
        return outputReg(hi, lo).space().varName(varOp);
    }
    public VerilogBuilder outputReg(int hi, int lo,CoreOp.VarOp varOp, String suffix) {
        return outputReg(hi, lo).space().varName(varOp,suffix);
    }
    // Should now be in hat base class.
    public <I> VerilogBuilder commaNlSeparated(Iterable<I> iterable, Consumer<I> c) {
        StreamCounter.of(iterable, (counter, t) -> {
            if (counter.isNotFirst()) {
                comma().nl();
            }
            c.accept(t);
        });
        return self();
    }
    public VerilogBuilder type(JavaType type) {
        return switch (type.toString()) {
                case "vga.Verilog$Wire" -> wire();
            default -> append(type.toString());
        };
    }

    public VerilogBuilder alwaysAtPosEdge(String edge, Consumer<VerilogBuilder> consumer) {
        return always().space().at().paren(_ -> posedge().space().append(edge)).space().beginEnd(consumer);
    }
    public VerilogBuilder varFuncDeclaration(BuildContext context,VarFuncDeclarationOpWrapper opWrapper) {

        return self();
    }
    /*
 0 =  ()[ ] . -> ++ --
 1 = ++ --+ -! ~ (type) *(deref) &(addressof) sizeof
 2 = * / %
 3 = + -
 4 = << >>
 5 = < <= > >=
 6 = == !=
 7 = &
 8 = ^
 9 = |
 10 = &&
 11 = ||
 12 = ()?:
 13 = += -= *= /= %= &= ^= |= <<= >>=
 14 = ,
   */
    public int precedenceOf(Op op) {
        return switch (op) {
            case CoreOp.YieldOp o -> 0;
            case CoreOp.InvokeOp o -> 0;
            case CoreOp.FuncCallOp o -> 0;
            case CoreOp.VarOp o -> 13;
            case CoreOp.VarAccessOp.VarStoreOp o -> 13;
            case CoreOp.FieldAccessOp o -> 0;
            case CoreOp.VarAccessOp.VarLoadOp o -> 0;
            case CoreOp.ConstantOp o -> 0;
            case CoreOp.LambdaOp o -> 0;
            case CoreOp.TupleOp o -> 0;
            case ExtendedOp.JavaWhileOp o -> 0;
            case CoreOp.ConvOp o -> 1;
            case CoreOp.NegOp  o-> 1;
            case CoreOp.ModOp o -> 2;
            case CoreOp.MulOp o -> 2;
            case CoreOp.DivOp o -> 2;
            case CoreOp.NotOp o -> 2;
            case CoreOp.AddOp o -> 3;
            case CoreOp.SubOp o -> 3;
            case CoreOp.AshrOp o -> 4;
            case CoreOp.LshlOp o -> 4;
            case CoreOp.LshrOp o -> 4;
            case CoreOp.LtOp o -> 5;
            case CoreOp.GtOp o -> 5;
            case CoreOp.LeOp o -> 5;
            case CoreOp.GeOp o -> 5;
            case CoreOp.EqOp o -> 6;
            case CoreOp.NeqOp o -> 6;

            case CoreOp.AndOp o -> 11;
            case CoreOp.XorOp o -> 12;
            case CoreOp.OrOp o -> 13;
            case ExtendedOp.JavaConditionalAndOp o -> 14;
            case ExtendedOp.JavaConditionalOrOp o -> 15;
            case ExtendedOp.JavaConditionalExpressionOp o -> 18;
            case CoreOp.ReturnOp o -> 19;

            default -> throw new IllegalStateException("precedence ");
        };
    }


    public VerilogBuilder parencedence(BuildContext buildContext, Op parent, OpWrapper<?> child) {
        return parenWhen(precedenceOf(parent) < precedenceOf(child.op()), _ -> recurse(buildContext, child));
    }

    public VerilogBuilder parencedence(BuildContext buildContext, OpWrapper<?> parent, OpWrapper<?> child) {
        return parenWhen(precedenceOf(parent.op()) < precedenceOf(child.op()), _ -> recurse(buildContext, child));
    }

    public VerilogBuilder parencedence(BuildContext buildContext, Op parent, Op child) {
        return parenWhen(precedenceOf(parent) < precedenceOf(child), _ -> recurse(buildContext, OpWrapper.wrap(child)));
    }

    public VerilogBuilder parencedence(BuildContext buildContext, OpWrapper<?> parent, Op child) {
        return parenWhen(precedenceOf(parent.op()) < precedenceOf(child), _ -> recurse(buildContext, OpWrapper.wrap(child)));
    }

    public VerilogBuilder varDeclaration(BuildContext context,VarDeclarationOpWrapper opWrapper) {
        type(opWrapper.javaType()).space().varName(opWrapper.op());
        equals().space();
        parencedence(context, opWrapper, opWrapper.operandNAsResult(0).op());
        return self();
    }
    public VerilogBuilder constant(BuildContext context, ConstantOpWrapper opWrapper) {
        append(((CoreOp.ConstantOp)opWrapper.op()).value().toString());
        return self();
    }
    public VerilogBuilder methodCall(BuildContext  buildContext, InvokeOpWrapper invokeOpWrapper) {

            var name = invokeOpWrapper.name();
                var operandCount = invokeOpWrapper.operandCount();
                var returnType = invokeOpWrapper.javaReturnType();
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

     VerilogBuilder recurse(BuildContext buildContext, OpWrapper<?> wrappedOp) {
        switch (wrappedOp) {
           /* case VarLoadOpWrapper $ -> varLoad(buildContext, $);
            case VarStoreOpWrapper $ -> varStore(buildContext, $);
            case FieldLoadOpWrapper $ -> fieldLoad(buildContext, $);
            case FieldStoreOpWrapper $ -> fieldStore(buildContext, $);
            case BinaryArithmeticOrLogicOperation $ -> binaryOperation(buildContext, $);
            case UnaryArithmeticOrLogicOpWrapper $ -> unaryOperation(buildContext, $);
            case BinaryTestOpWrapper $ -> binaryTest(buildContext, $);
            case ConvOpWrapper $ -> conv(buildContext, $);*/
            case ConstantOpWrapper $ -> constant(buildContext, $);/*
            case YieldOpWrapper $ -> javaYield(buildContext, $);
            case FuncCallOpWrapper $ -> funcCall(buildContext, $);
            case LogicalOpWrapper $ -> logical(buildContext, $);*/
            case InvokeOpWrapper $ -> methodCall(buildContext, $);
            /*
            case TernaryOpWrapper $ -> ternary(buildContext, $);
            */
            case VarDeclarationOpWrapper $ -> varDeclaration(buildContext, $);
            case VarFuncDeclarationOpWrapper $ -> varFuncDeclaration(buildContext, $);
            /*case LambdaOpWrapper $ -> lambda(buildContext, $);
            case TupleOpWrapper $ -> tuple(buildContext, $);
            case WhileOpWrapper $ -> javaWhile(buildContext, $);
            case IfOpWrapper $ -> javaIf(buildContext, $);
            case ForOpWrapper $ -> javaFor(buildContext, $);

            case ReturnOpWrapper $ -> ret(buildContext, $);
            case JavaLabeledOpWrapper $ -> javaLabeled(buildContext, $);
            case JavaBreakOpWrapper $ -> javaBreak(buildContext, $);
            case JavaContinueOpWrapper $ -> javaContinue(buildContext, $); */
            default -> throw new IllegalStateException("handle nesting of op " + wrappedOp.op());
        }
        return self();
    }

    public VerilogBuilder module(CoreOp.FuncOp javaFunc) {
        nl();
        BuildContext buildContext = new BuildContext(OpWrapper.wrap(javaFunc));
        buildContext.scope(buildContext.funcOpWrapper, () -> {
            moduleDeclaration(buildContext.funcOpWrapper.functionName(), _ -> {
                var list = buildContext.funcOpWrapper.paramTable.list();
                paren(_ ->
                        indent(_ -> commaNlSeparated(list.stream().toList(), info -> {
                                    switch (info.javaType.toString()) {
                                        case "vga.Verilog$Clk" -> inputWire(info.varOp);
                                        case "vga.Verilog$Btn" -> inputWire(info.varOp);
                                        case "vga.Verilog$Vga" ->
                                                outputReg(3, 0,info.varOp,"_r").comma().nl()
                                                        .outputReg(3, 0,info.varOp,"_g").comma().nl()
                                                        .outputReg(3, 0,info.varOp,"_b");
                                        default -> append(info.javaType.toString()).space().varName(info.varOp);
                                    }
                                }
                        ).nl())
                ).semicolon();
                nl();
                indent(_-> {
                    StreamCounter.of(buildContext.funcOpWrapper.wrappedRootOpStream(), (c, root) ->
                            nlIf(c.isNotFirst()).recurse(buildContext, root).semicolonIf(!(root instanceof StructuralOpWrapper<?>))
                    );
                    alwaysAtPosEdge("clk",_->{
                        nl();
                    });
                    nl();
                });
            });

           /* beginEndIndented(_ -> {
                scope();
                StreamCounter.of(buildContext.funcOpWrapper.wrappedRootOpStream(), (c, root) ->
                        nlIf(c.isNotFirst()).recurse(buildContext, root).semicolonIf(!(root instanceof StructuralOpWrapper<?>))
                );
            });*/
        });
        return self();
    }


}
