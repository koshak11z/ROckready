/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math.calculator.tokenizer;

import moscow.rockstar.utility.math.calculator.tokenizer.Token;

public class VariableToken
extends Token {
    private final String name;

    public String getName() {
        return this.name;
    }

    public VariableToken(String name) {
        super(6);
        this.name = name;
    }
}

