/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import moscow.rockstar.utility.math.calculator.ArrayStack;
import moscow.rockstar.utility.math.calculator.ValidationResult;
import moscow.rockstar.utility.math.calculator.function.Function;
import moscow.rockstar.utility.math.calculator.function.Functions;
import moscow.rockstar.utility.math.calculator.operator.Operator;
import moscow.rockstar.utility.math.calculator.tokenizer.FunctionToken;
import moscow.rockstar.utility.math.calculator.tokenizer.NumberToken;
import moscow.rockstar.utility.math.calculator.tokenizer.OperatorToken;
import moscow.rockstar.utility.math.calculator.tokenizer.Token;
import moscow.rockstar.utility.math.calculator.tokenizer.VariableToken;

public class Expression {
    private final Token[] tokens;
    private final Map<String, Double> variables;
    private final Set<String> userFunctionNames;

    private static Map<String, Double> createDefaultVariables() {
        HashMap<String, Double> vars = new HashMap<String, Double>(4);
        vars.put("pi", Math.PI);
        vars.put("\u03c0", Math.PI);
        vars.put("\u03c6", 1.61803398874);
        vars.put("e", Math.E);
        return vars;
    }

    public Expression(Expression existing) {
        this.tokens = Arrays.copyOf(existing.tokens, existing.tokens.length);
        this.variables = new HashMap<String, Double>();
        this.variables.putAll(existing.variables);
        this.userFunctionNames = new HashSet<String>(existing.userFunctionNames);
    }

    Expression(Token[] tokens) {
        this.tokens = tokens;
        this.variables = Expression.createDefaultVariables();
        this.userFunctionNames = Collections.emptySet();
    }

    Expression(Token[] tokens, Set<String> userFunctionNames) {
        this.tokens = tokens;
        this.variables = Expression.createDefaultVariables();
        this.userFunctionNames = userFunctionNames;
    }

    public Expression setVariable(String name, double value) {
        this.checkVariableName(name);
        this.variables.put(name, value);
        return this;
    }

    private void checkVariableName(String name) {
        if (this.userFunctionNames.contains(name) || Functions.getBuiltinFunction(name) != null) {
            throw new IllegalArgumentException("The variable name '" + name + "' is invalid. Since there exists a function with the same name");
        }
    }

    public Expression setVariables(Map<String, Double> variables) {
        for (Map.Entry<String, Double> v : variables.entrySet()) {
            this.setVariable(v.getKey(), v.getValue());
        }
        return this;
    }

    public Expression clearVariables() {
        this.variables.clear();
        return this;
    }

    public Set<String> getVariableNames() {
        HashSet<String> variables = new HashSet<String>();
        for (Token t : this.tokens) {
            if (t.getType() != 6) continue;
            variables.add(((VariableToken)t).getName());
        }
        return variables;
    }

    public ValidationResult validate(boolean checkVariablesSet) {
        ArrayList<String> errors = new ArrayList<String>(0);
        if (checkVariablesSet) {
            for (Token t : this.tokens) {
                String var;
                if (t.getType() != 6 || this.variables.containsKey(var = ((VariableToken)t).getName())) continue;
                errors.add("The setVariable '" + var + "' has not been set");
            }
        }
        int count = 0;
        for (Token tok : this.tokens) {
            switch (tok.getType()) {
                case 1: 
                case 6: {
                    ++count;
                    break;
                }
                case 3: {
                    Function func = ((FunctionToken)tok).getFunction();
                    int argsNum = func.getNumArguments();
                    if (argsNum > count) {
                        errors.add("Not enough arguments for '" + func.getName() + "'");
                    }
                    if (argsNum > 1) {
                        count -= argsNum - 1;
                        break;
                    }
                    if (argsNum != 0) break;
                    ++count;
                    break;
                }
                case 2: {
                    Operator op = ((OperatorToken)tok).getOperator();
                    if (op.getNumOperands() != 2) break;
                    --count;
                }
            }
            if (count >= 1) continue;
            errors.add("Too many operators");
            return new ValidationResult(false, errors);
        }
        if (count > 1) {
            errors.add("Too many operands");
        }
        return errors.size() == 0 ? ValidationResult.SUCCESS : new ValidationResult(false, errors);
    }

    public ValidationResult validate() {
        return this.validate(true);
    }

    public Future<Double> evaluateAsync(ExecutorService executor) {
        return executor.submit(this::evaluate);
    }

    public double evaluate() {
        ArrayStack output = new ArrayStack();
        for (Token t : this.tokens) {
            if (t.getType() == 1) {
                output.push(((NumberToken)t).getValue());
                continue;
            }
            if (t.getType() == 6) {
                String name = ((VariableToken)t).getName();
                Double value = this.variables.get(name);
                if (value == null) {
                    throw new IllegalArgumentException("No value has been set for the setVariable '" + name + "'.");
                }
                output.push(value);
                continue;
            }
            if (t.getType() == 2) {
                OperatorToken op = (OperatorToken)t;
                if (output.size() < op.getOperator().getNumOperands()) {
                    throw new IllegalArgumentException("Invalid number of operands available for '" + op.getOperator().getSymbol() + "' operator");
                }
                if (op.getOperator().getNumOperands() == 2) {
                    double rightArg = output.pop();
                    double leftArg = output.pop();
                    output.push(op.getOperator().apply(leftArg, rightArg));
                    continue;
                }
                if (op.getOperator().getNumOperands() != 1) continue;
                double arg = output.pop();
                output.push(op.getOperator().apply(arg));
                continue;
            }
            if (t.getType() != 3) continue;
            FunctionToken func = (FunctionToken)t;
            int numArguments = func.getFunction().getNumArguments();
            if (output.size() < numArguments) {
                throw new IllegalArgumentException("Invalid number of arguments available for '" + func.getFunction().getName() + "' function");
            }
            double[] args = new double[numArguments];
            for (int j = numArguments - 1; j >= 0; --j) {
                args[j] = output.pop();
            }
            output.push(func.getFunction().apply(args));
        }
        if (output.size() > 1) {
            throw new IllegalArgumentException("Invalid number of items on the output queue. Might be caused by an invalid number of arguments for a function.");
        }
        return output.pop();
    }
}

