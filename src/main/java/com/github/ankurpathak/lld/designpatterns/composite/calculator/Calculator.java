package com.github.ankurpathak.lld.designpatterns.composite.calculator;

interface IExpression {
    int evaluate();
}


class Operand implements IExpression {
    private final int value;

    public Operand(int value) {
        this.value = value;
    }

    @Override
    public int evaluate() {
        return value;
    }
}

enum Operator {
    ADD('+'),
    SUBTRACT('-'),
    MULTIPLY('*'),
    DIVIDE('/');

    private final char symbol;

    Operator(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}

class Expression implements IExpression {
    private final Operator operator;
    private final IExpression left;
    private final IExpression right;

    public Expression(Operator operator, IExpression left, IExpression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public int evaluate() {
        return switch (operator) {
            case ADD -> left.evaluate() + right.evaluate();
            case SUBTRACT -> left.evaluate() - right.evaluate();
            case MULTIPLY -> left.evaluate() * right.evaluate();
            case DIVIDE -> left.evaluate() / right.evaluate();
        };
    }
}

class Main {
    public static void main(String[] args) {
        IExpression operand1 = new Operand(5);
        IExpression operand2 = new Operand(3);
        IExpression operand3 = new Operand(2);

        IExpression addition = new Expression(Operator.ADD, operand1, operand2);
        IExpression multiplication = new Expression(Operator.MULTIPLY, addition, operand3);

        System.out.println("Result: " + multiplication.evaluate());
    }
}