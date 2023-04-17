package com.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HeYuTong
 * @Date: 2023/4/17 10:47
 * @Description: 自定义公式计算器 1.0
 * 支持加减乘除
 */
public abstract class FormularyCalculator {

    /**
     * 负号
     */
    public String negative;

    public static void main(String[] args) {
        String expression = " ( a + b ) * c / d - e - e";
        HashMap<String, String> params = new HashMap<>();
        params.put("a", "1");
        params.put("b", "1");
        params.put("c", "1");
        params.put("d", "1");
        params.put("e", "-1");
        FormularyCalculator formularyCalculator = new FormularyCalculator("F") {
            @Override
            String initExpression() {
                return expression;
            }

            @Override
            Map<String, String> initParams() {
                return params;
            }
        };
        BigDecimal calculate = formularyCalculator.calculateWithParams();
        System.out.println(formularyCalculator.expression);
        System.out.println(calculate);
        params.put("e", "0");
        formularyCalculator.init();
        System.out.println(formularyCalculator.expression);
        System.out.println(formularyCalculator.calculateWithParams());
    }

    private String expression;



    /**
     * 通过可配置的公式和入参，采用分治的思想做到自定义的公式计算
     * 思想：递归、分治处理每一个计算块
     */
    public BigDecimal calculateWithParams() {
        return calculate(expression);
    }

    private BigDecimal calculate(String expression) {
        if (expression.contains(OperatorEnum.BRACKET_LEFT.operatorName) || expression.contains(OperatorEnum.BRACKET_RIGHT.operatorName)) {
            //分离出括号中的子式 subExpression、括号前式 leftExpression、括号后式 rightExpression
            String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.BRACKET_LEFT.operatorName));
            String subExpression = expression.substring(expression.indexOf(OperatorEnum.BRACKET_LEFT.operatorName) + 1, expression.indexOf(OperatorEnum.BRACKET_RIGHT.operatorName));
            String rightExpression = expression.substring(expression.indexOf(OperatorEnum.BRACKET_RIGHT.operatorName) + 1);
            expression = leftExpression + calculate(subExpression) + rightExpression;
            return calculate(expression);
        } else if (expression.contains(OperatorEnum.PLUS.operatorName) || expression.contains(OperatorEnum.SUBSTRACT.operatorName)) {
            //分离出 加号或者减号 前的子式 leftExpression 和 加号或减号 后的子式 rightExpression
            if (expression.contains(OperatorEnum.PLUS.operatorName)) {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.PLUS.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.PLUS.operatorName) + 1);
                return calculate(leftExpression).add(calculate(rightExpression));
            } else {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.SUBSTRACT.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.SUBSTRACT.operatorName) + 1);
                return calculate(leftExpression).subtract(calculate(rightExpression));
            }
        } else if (expression.contains(OperatorEnum.MULTIPLY.operatorName) || expression.contains(OperatorEnum.DIVIDE.operatorName)) {
            //分离出 加号或者减号 前的子式 leftExpression 和 加号或减号 后的子式 rightExpression
            if (expression.contains(OperatorEnum.MULTIPLY.operatorName)) {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.MULTIPLY.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.MULTIPLY.operatorName) + 1);
                return calculate(leftExpression).multiply(calculate(rightExpression));
            } else {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.DIVIDE.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.DIVIDE.operatorName) + 1);
                BigDecimal denominator = calculate(rightExpression);
                if(BigDecimal.ZERO.equals(denominator)){
                    throw new RuntimeException("分母不能为0!:"+rightExpression);
                }
                return calculate(leftExpression).divide(denominator,6, RoundingMode.HALF_UP);
            }
        }else {
            if(expression.contains(negative)){
                return new BigDecimal(expression.replaceAll(negative,"")).multiply(new BigDecimal("-1"));
            }else{
                return new BigDecimal(expression);
            }
        }
    }

    public FormularyCalculator(String negative) {
        this.negative = negative;
        init();
    }

    /**
     * 初始化计算器
     */
    public void init() {
        expression = analyze(initExpression(), initParams());
    }


    /**
     * 解析公式和参数
     *
     * @param expression 获取到的表达式 例:   ( a + b ) * c / d - e
     * @param params 获取到的参数map
     * @return 解析后的表达式
     */
    private String analyze(String expression, Map<String, String> params) {
        expression = expression.trim().replaceAll(" ", "");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            //为避免参数值和公式冲突，需要处理负号
            if(value.contains(OperatorEnum.SUBSTRACT.operatorName)){
                value = value.replaceAll(OperatorEnum.SUBSTRACT.operatorName,"F");
            }
            expression = expression.replaceAll(entry.getKey(),value);
        }
        return expression;
    }


    /**
     * 获取自定义的表达式
     *
     * @return 用户配置的公式
     */
    abstract String initExpression();

    /**
     * 获取自定义的参数列表
     *
     * @return 用户配置的参数
     */
    abstract Map<String, String> initParams();

    public enum OperatorEnum {
        /**
         * 加
         */
        PLUS("+"),
        /**
         * 减
         */
        SUBSTRACT("-"),
        /**
         * 乘
         */
        MULTIPLY("*"),

        /**
         * 除
         */
        DIVIDE("/"),

        /**
         * 左括号
         */
        BRACKET_LEFT("("),

        /**
         * 右括号
         */
        BRACKET_RIGHT(")"),

        ;

        public final String operatorName;

        OperatorEnum(String operatorName) {
            this.operatorName = operatorName;
        }
    }

}
