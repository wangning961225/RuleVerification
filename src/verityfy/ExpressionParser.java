//package verityfy;
//
//import java.util.Stack;
//
//public class ExpressionParser {
//    private static final boolean DEBUG = true;
//
//    private static ExpressionParser parser = new ExpressionParser();
//
//    private ExpressionParser() {
//
//    }
//
//    public static ExpressionParser getInstance() {
//        return parser;
//    }
//
//    public boolean fireRule(String expression, double fact) {
//        traceCalculate("\nexpression:" + expression);
//        expression = expression.replace("\n|\r", "").trim();
//        char[] chars = expression.toCharArray();
//        return parseExpression(fact, chars);
//    }
//
//
//    private boolean parseExpression(double fact, char[] chars) {
//        boolean result = true;
//        String operand = "";
//        String operator = "";
//        Stack operatorsStack = new Stack();
//        Stack operandsStack = new Stack();
//        for (int i = 0; i < chars.length; i++) {
//            char token = chars[i];
//            traceCalculate("token:" + token);
//            if (Character.isDigit(token) || token == '.') {
//                if (!operator.equals("")) {
//                    traceCalculate("push operator:" + operator);
//                    //    将操作符放入操作符号栈
//                    operatorsStack.push(operator);
//                    operator = "";
//
//                }
//                operand += token;
//                result = checkTail(fact, operatorsStack, operandsStack,
//                        chars.length, operand, result, i);
//                continue;
//            } else if (Character.isLetter(token)) {
//                if (!operator.equals("")) {
//                    traceCalculate("push operator:" + operator);
//                    //    将操作符放入操作符号栈
//                    operatorsStack.push(operator);
//                    operator = "";
//                }
//                operand = String.valueOf(token);
//                result = checkTail(fact, operatorsStack, operandsStack,
//                        chars.length, operand, result, i);
//                //将操作数放入操作数栈
//                operandsStack.push(operand);
//                traceCalculate("push operand:" + token);
//                operand = "";
//                continue;
//            } else {
//                if (!operatorsStack.isEmpty() && !operandsStack.isEmpty()) {
//                    //当前操作数是字母(变量)，已存入栈，因此需要取出
//                    if (operand.equals("")) {
//                        operand = (String) operandsStack.pop();
//                        result = result
//                                && calculatePerfomance(operatorsStack,
//                                operandsStack, operand, fact);
//                        //当前操作数是数字
//                    } else {
//                        result = result
//                                && calculatePerfomance(operatorsStack,
//                                operandsStack, operand, fact);
//
//                    }
//                }
//
//                if (!operand.equals("")) {
//                    result = checkTail(fact, operatorsStack, operandsStack,
//                            chars.length, operand, result, i);
//                    //将操作数放入操作数栈
//                    operandsStack.push(operand);
//                    traceCalculate("push2 operand:" + operand);
//                    operand = "";
//                }
//
//                operator += token;
//                continue;
//            }
//
//        }
//        return result;
//    }
//
//    /**
//     * 判断是否已经到表达式尾端，如果是，计算
//     */
//    private boolean checkTail(double fact, Stack operatorsStack,
//                              Stack operandsStack, int chars_length, String operand,
//                              boolean result, int i) {
//        if (i == chars_length - 1) {
//            result = result
//                    && calculatePerfomance(operatorsStack, operandsStack,
//                    operand, fact);
//        }
//        return result;
//    }
//
//    private void displayStack(String name,Stack stack) {
//        if (DEBUG) {
//            for (int i = 0; i < stack.pool.size(); i++)
//                System.out.println(name+stack.pool.get(i));
//        }
//    }
//
//    private boolean calculatePerfomance(Stack operatorsStack,
//                                        Stack operandsStack, String currentOperand, double fact) {
//        traceCalculate("开始计算");
//        displayStack("operators stack:",operatorsStack);
//        displayStack("operands stack:",operandsStack);
//        traceCalculate("currentOperand=" + currentOperand);
//        String operator = (String) operatorsStack.pop();
//        double lastOperand = coverOperandToDouble((String) operandsStack.pop(),
//                fact);
//        double nextOperand = coverOperandToDouble(currentOperand, fact);
//        boolean result = true;
//        if (operator.equals("=="))
//            return lastOperand == nextOperand;
//        if (operator.indexOf("=") >= 0)
//            hasEqual = true;
//        char[] operators = operator.toCharArray();
//        for (int i = 0; i < operators.length; i++) {
//            switch (operators[i]) {
//                case '<':
//                    result = result && (lastOperand < nextOperand);
//                    break;
//                case '=':
//                    //result为false，也就是小于，大于符号不满足的时候，判断等号是否成立
//                    if (!result)
//                        result = (lastOperand == nextOperand);
//                    break;
//                case '>':
//                    result = result && (lastOperand > nextOperand);
//                    break;
//            }
//        }
//        if ((!result) && hasEqual)
//            result = lastOperand == nextOperand;
//        return result;
//
//    }
//
//    /**
//     * 用于debug
//     */
//    private void traceCalculate(String info) {
//        if (DEBUG)
//            System.out.println(info);
//    }
//
//    private double coverOperandToDouble(String operand, double fact) {
//        //如果是字母，也就是变量，返回fact变量
//        if (Character.isLetter(operand.toCharArray()[0]))
//            return fact;
//        else
//            return Double.parseDouble(operand);
//    }
//}
