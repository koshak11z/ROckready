/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math.calculator.tokenizer;

import moscow.rockstar.utility.math.calculator.operator.Operator;
import moscow.rockstar.utility.math.calculator.tokenizer.Token;

public class OperatorToken
extends Token {
    private final Operator operator;

    public OperatorToken(Operator op) {
        super(2);
        if (op == null) {
            throw new IllegalArgumentException("Operator is unknown for token.");
        }
        this.operator = op;
    }

    public Operator getOperator() {
        return this.operator;
    }
}

