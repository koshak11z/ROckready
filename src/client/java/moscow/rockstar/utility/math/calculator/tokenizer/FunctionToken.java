/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math.calculator.tokenizer;

import moscow.rockstar.utility.math.calculator.function.Function;
import moscow.rockstar.utility.math.calculator.tokenizer.Token;

public class FunctionToken
extends Token {
    private final Function function;

    public FunctionToken(Function function) {
        super(3);
        this.function = function;
    }

    public Function getFunction() {
        return this.function;
    }
}

