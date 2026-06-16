/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math.calculator.tokenizer;

class UnknownFunctionOrVariableException
extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final String message;
    private final String expression;
    private final String token;
    private final int position;

    public UnknownFunctionOrVariableException(String expression, int position, int length) {
        this.expression = expression;
        this.token = UnknownFunctionOrVariableException.token(expression, position, length);
        this.position = position;
        this.message = "Unknown function or variable '" + this.token + "' at pos " + position + " in expression '" + expression + "'";
    }

    private static String token(String expression, int position, int length) {
        int end;
        int len = expression.length();
        if (len < (end = position + length - 1)) {
            end = len;
        }
        return expression.substring(position, end);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public String getExpression() {
        return this.expression;
    }

    public String getToken() {
        return this.token;
    }

    public int getPosition() {
        return this.position;
    }
}

