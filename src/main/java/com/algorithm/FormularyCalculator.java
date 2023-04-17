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
     * 负号占位符，用户自定义，不要冲突。
     */
    private final String negative;

    private int scale = 6;

    /**
     * 公式
     */
    private String expression;

    public FormularyCalculator(String negative) {
        this.negative = negative;
        init();
    }

    public FormularyCalculator(String negative, int scale) {
        this.negative = negative;
        this.scale = scale;
        init();
    }


    /**
     * 通过可配置的公式和入参，采用分治的思想做到自定义的公式计算
     * 思想：递归、分治处理每一个计算块
     */
    public BigDecimal calculateWithParams() {
        return calculate(expression);
    }

    private BigDecimal calculate(String expression) {
        if (expression.contains(OperatorEnum.BRACKET_LEFT.operatorName) || expression.contains(OperatorEnum.BRACKET_RIGHT.operatorName)) {
            //分离出括号中的前式 leftExpression、子式 subExpression、后式 rightExpression
            String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.BRACKET_LEFT.operatorName));
            String subExpression = expression.substring(expression.indexOf(OperatorEnum.BRACKET_LEFT.operatorName) + 1, expression.lastIndexOf(OperatorEnum.BRACKET_RIGHT.operatorName));
            String rightExpression = expression.substring(expression.lastIndexOf(OperatorEnum.BRACKET_RIGHT.operatorName) + 1);
            BigDecimal calculate = calculate(subExpression);
            if (calculate.compareTo(BigDecimal.ZERO) < 0) {
                return calculate(leftExpression + negative + calculate.toString().replaceAll("-", "") + rightExpression);
            } else {
                return calculate(leftExpression + calculate + rightExpression);
            }
        } else if (expression.contains(OperatorEnum.PLUS.operatorName) || expression.contains(OperatorEnum.SUBTRACT.operatorName)) {
            //分离出 加号或者减号 前的子式 leftExpression 和 加号或减号 后的子式 rightExpression
            if (expression.contains(OperatorEnum.PLUS.operatorName)) {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.PLUS.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.PLUS.operatorName) + 1);
                return calculate(leftExpression).add(calculate(rightExpression));
            } else {
                //负号后的子式需要变号
                String leftExpression = expression.substring(0, expression.lastIndexOf(OperatorEnum.SUBTRACT.operatorName));
                String rightExpression = expression.substring(expression.lastIndexOf(OperatorEnum.SUBTRACT.operatorName) + 1);
                return calculate(leftExpression).subtract(calculate(rightExpression));
            }
        } else if (expression.contains(OperatorEnum.MULTIPLY.operatorName) || expression.contains(OperatorEnum.DIVIDE.operatorName)) {
            //分离出 加号或者减号 前的子式 leftExpression 和 加号或减号 后的子式 rightExpression,之后对各个子式进行递归计算
            if (expression.contains(OperatorEnum.MULTIPLY.operatorName)) {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.MULTIPLY.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.MULTIPLY.operatorName) + 1);
                return calculate(leftExpression).multiply(calculate(rightExpression));
            } else {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.DIVIDE.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.DIVIDE.operatorName) + 1);
                BigDecimal denominator = calculate(rightExpression);
                if (BigDecimal.ZERO.equals(denominator)) {
                    throw new RuntimeException("分母不能为0!");
                }
                return calculate(leftExpression).divide(denominator, scale, RoundingMode.HALF_UP);
            }
        } else {
            //递归结束条件
            if (expression.contains(negative)) {
                //处理负号
                String s = expression.replaceAll(negative, "");
                return new BigDecimal("-1").multiply(calculate(s));
            }

            BigDecimal result = new BigDecimal(expression);
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                return calculate(negative + expression);
            }
            return result;
        }
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
     * @param params     获取到的参数map
     * @return 解析后的表达式
     */
    private String analyze(String expression, Map<String, String> params) {
        expression = expression.trim().replaceAll(" ", "");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            //为避免参数值和公式冲突，需要处理负号
            if (value.contains(OperatorEnum.SUBTRACT.operatorName)) {
                value = value.replaceAll(OperatorEnum.SUBTRACT.operatorName, "F");
            }
            expression = expression.replaceAll(entry.getKey(), value);
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
        SUBTRACT("-"),
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

    public static void main(String[] args) {
        Map<String, FormularyCalculator> map = new HashMap<>(1);
        long s = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            //String expression = "a-b-c";
            String expression = "(累计营收*0.3 + 累计回款*0.7- 往年累计结算-外包费用-单列成本+其他应补应扣-归档保证金)*协作比例-投标项目奖励";
            HashMap<String, String> params = new HashMap<>();
            params.put("累计营收", "10000");
            params.put("累计回款", "10000");
            params.put("往年累计结算", "10000");
            params.put("外包费用", "10000");
            params.put("单列成本", "10000");
            params.put("其他应补应扣", "10000");
            params.put("归档保证金", "10000");
            params.put("协作比例", "10000");
            params.put("投标项目奖励", "10000");
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
            map.put(expression, formularyCalculator);
        }
        long e1 = System.currentTimeMillis();
        System.out.println("1耗时:" + (e1 - s));
        for (Map.Entry<String, FormularyCalculator> calculatorEntry : map.entrySet()) {
            System.out.println(calculatorEntry.getValue().calculateWithParams());
        }
        long e = System.currentTimeMillis();
        System.out.println("2耗时:" + (e - e1));
    }
}
