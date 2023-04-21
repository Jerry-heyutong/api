package com.algorithm;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HeYuTong
 * @Date: 2023/4/17 10:47
 * @Description: 自定义公式计算器 1.1
 * 支持加减乘除
 */
public class FormularyCalculator {
    /**
     * 匹配公式中参数名称所用的正则表达式
     */
    public static final String DEFAULT_PARAMETER_REGEX = "[^\\d()*+-/ ]+";

    /**
     * 负号占位符，用户自定义，不要冲突。
     */
    public String negative;
    /**
     * 除法时的最大精度
     */
    public int scale;
    /**
     * 公式,变量名称可以是中文也可以是code,但是要注意里面不可以有negative的占位符。
     * 公式中的变量名称是params的key值，必须一一对应。
     * 计算过后公式会用%s来表示变量名
     */
    private String exp;

    public FormularyCalculator() {
        this.negative = "@";
        this.scale = 9;
    }

    public FormularyCalculator(String exp) {
        this.negative = "@";
        this.scale = 9;
        this.exp = exp;
    }

    public FormularyCalculator(String negative, String exp) {
        this.negative = negative;
        this.scale = 9;
        this.exp = exp;
    }

    public FormularyCalculator(int scale, String exp) {
        this.negative = "@";
        this.scale = scale;
        this.exp = exp;
    }

    public FormularyCalculator(String negative, int scale, String exp) {
        this.negative = negative;
        this.scale = scale;
        this.exp = exp;
    }

    /**
     * 通过可配置的公式和入参，采用分治的思想做到自定义的公式计算
     * 思想：递归、分治处理每一个计算块
     * 默认不使用缓存，可根据结果集预测是否需要走缓存。
     */
    public List<BigDecimal> calculate(List<List<String>> params) {
        List<BigDecimal> result = new Vector<>();
        List<String> expressionList = analyzeBatch(exp, params);
        expressionList.parallelStream().forEach(e ->
                result.add(calculate(e))
        );
        System.out.println("结果大小:" + result.size());
        return result;
    }

    /**
     * 根据公式计算单个结果
     * 通过可配置的公式和入参，采用分治的思想做到自定义的公式计算
     * 思想：递归、分治处理每一个计算块
     */

    public BigDecimal calculateOne(List<String> param) {
        String expression = analyze(param);
        return calculate(expression);
    }

    private BigDecimal calculate(String expression) {
        if (expression.contains(OperatorEnum.BRACKET_LEFT.operatorName) || expression.contains(OperatorEnum.BRACKET_RIGHT.operatorName)) {
            //分离出括号中的前式 leftExpression、中间子式 subExpression、后式 rightExpression，对中间子式进行计算，将计算结果转化为公式重新递归
            int indexOfBracketLeft = expression.lastIndexOf(OperatorEnum.BRACKET_LEFT.operatorName);
            int indexOfBracketRight = indexOfBracketLeft;
            while (!OperatorEnum.BRACKET_RIGHT.operatorName.equals(expression.substring(indexOfBracketRight, indexOfBracketRight + 1))) {
                indexOfBracketRight++;
            }
            /*for (int j = indexOfBracketLeft; j < expression.length(); j++) {
                if (OperatorEnum.BRACKET_RIGHT.operatorName.contentEquals(expression.subSequence(j, j + 1))) {
                    indexOfBracketRight = j;
                    break;
                }
            }*/
            String leftExpression = expression.substring(0, indexOfBracketLeft);
            String subExpression = expression.substring(indexOfBracketLeft + 1, indexOfBracketRight);
            String rightExpression = expression.substring(indexOfBracketRight + 1);
            BigDecimal calculate = calculate(subExpression);
            if (calculate.compareTo(BigDecimal.ZERO) < 0) {
                return calculate(leftExpression + negative + calculate.toString().replace(OperatorEnum.SUBTRACT.operatorName, "") + rightExpression);
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
            //分离出 乘除 前的子式 leftExpression 和 乘除 后的子式 rightExpression,之后对各个子式进行递归计算
            if (expression.contains(OperatorEnum.MULTIPLY.operatorName)) {
                String leftExpression = expression.substring(0, expression.indexOf(OperatorEnum.MULTIPLY.operatorName));
                String rightExpression = expression.substring(expression.indexOf(OperatorEnum.MULTIPLY.operatorName) + 1);
                return calculate(leftExpression).multiply(calculate(rightExpression));
            } else {
                String leftExpression = expression.substring(0, expression.lastIndexOf(OperatorEnum.DIVIDE.operatorName));
                String rightExpression = expression.substring(expression.lastIndexOf(OperatorEnum.DIVIDE.operatorName) + 1);
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
                String s = expression.replace(negative, "");
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
     * 解析公式和参数
     *
     * @param params 获取到的参数list{1,2,3,4,5}
     * @return 解析后的表达式 (1+2)*3/4-5
     */
    private String analyze(List<String> params) {
        for (int i = 0; i < params.size(); i++) {
            String value = params.get(i);
            //为避免参数值和公式冲突，需要处理负号
            if (value.contains(OperatorEnum.SUBTRACT.operatorName)) {
                params.set(i, value.replace(OperatorEnum.SUBTRACT.operatorName, negative));
            }
        }
        return String.format(exp, params.toArray());
    }

    /**
     * 批量解析公式和参数
     *
     * @param expression 获取到的表达式 例:   ( a + b ) * c / d - e
     * @param params     获取到的参数list{{1,2,3,4,5},{5,4,3,2,1}}
     * @return 解析后的表达集合式 {"(1+2)*3/4-5",""}
     */
    private List<String> analyzeBatch(String expression, List<List<String>> params) {
        exp = expression.replaceAll(DEFAULT_PARAMETER_REGEX, "%s").replace(" ", "");
        List<String> analyzeBatchList = new Vector<>();
        params.parallelStream().forEach(p ->
                analyzeBatchList.add(
                        analyze(p)
                )
        );
        return analyzeBatchList;
    }

    public static List<String> findParamNamesFromExpression(String expression) {
        List<String> paramNames = new ArrayList<>();
        Pattern p = Pattern.compile(DEFAULT_PARAMETER_REGEX);//把正则封装成对象
        Matcher m = p.matcher(expression);//让正则与要作用的字符串进行匹配
        while (m.find())//按规则作用于字符串，并进行查找
        {
            //System.out.println(m.group());//获取匹配后的结果
            paramNames.add(m.group());
        }
        return paramNames;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }


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
        int size = 1;
        List<List<String>> bigList = new LinkedList<>();
        // String expression = "(累计营收*0.3 + 累计回款*0.7- 往年累计结算-外包费用-单列成本+其他应补应扣-归档保证金)*协作比例-投标项目奖励";

        // String expression = "A%B-b*c";
        String expression = "(所内完成产值  +  合作支持产值  )  *  (  1  -  公司发展基金应提比例    -  公司激励基金应提比例 )  *  计提比例    -  院审项目完成产值  * 完成比例";
        //String expression = "(累计营收*0.3 + 累计回款*0.7 -往年累计结算)*0.05";
        FormularyCalculator calculator = new FormularyCalculator(expression);
        for (int i = 0; i < size; i++) {
            List<String> arr = new ArrayList<>();
            arr.add(Double.toString(1212800));
            arr.add(Double.toString(0));
            arr.add(Double.toString(0));
            arr.add(Double.toString(0));
            arr.add(Double.toString(1));
            arr.add(Double.toString(10000));
            arr.add(Double.toString(0.02));
            bigList.add(arr);
        }
        long e1 = System.currentTimeMillis();
        System.out.println(calculator.calculate(bigList));
        System.out.println(calculator.getExp());
        long e = System.currentTimeMillis();
        System.out.println("计算耗时:" + (e - e1));

        String expression1 = "(累计营收*0.3 + 累计回款*0.7 -往年累计结算)*0.05";
        calculator.setExp(expression1);
        List<List<String>> bigList2 = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            List<String> arr = new ArrayList<>();
            arr.add(Double.toString(4444800));
            arr.add(Double.toString(0));
            arr.add(Double.toString(0));
            arr.add(Double.toString(0));
            arr.add(Double.toString(1));
            arr.add(Double.toString(10000));
            arr.add(Double.toString(0.02));
            bigList2.add(arr);
        }
        long e3 = System.currentTimeMillis();
        System.out.println(calculator.calculate(bigList2));
        System.out.println(calculator.getExp());
        long e4 = System.currentTimeMillis();
        System.out.println("计算耗时:" + (e4 - e3));
    }


}
