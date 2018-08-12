package tech.jsqlparser.demo;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.SimpleNode;

public class ValueExpression implements Expression {

    private String value;

    public ValueExpression(String value){
        this.value = value;
    }


    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
    }

    @Override
    public SimpleNode getASTNode() {
        return null;
    }

    @Override
    public void setASTNode(SimpleNode node) {

    }

    @Override
    public String toString() {
        return value;
    }
}
