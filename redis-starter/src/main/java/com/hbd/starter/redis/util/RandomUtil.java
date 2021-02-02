package com.hbd.starter.redis.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 有序随机数生成
 * created by hbd
 * 2020/03/17
 */
public class RandomUtil {

    public static String SerialString = "S5Y3Z2H7UJLTNPK8XVW";
    public static String RandomChar = "ABCQDEFGRM";        //随机字母
    public static String RandomNumber = "01469";             //随机数字
    public static String RandomStr = RandomChar + RandomNumber;//随机字符串

    /**
     * 数字转自定义进制
     * @param originalNum
     * @return
     */
    public static String NumberToHex(long originalNum) {
        long devideNum = originalNum / SerialString.length();   //整除
        int remainderNum = (int)(originalNum % SerialString.length());//取模
        if(devideNum > 0) {
            return String.valueOf(SerialString.charAt(remainderNum)) + NumberToHex(devideNum);
        } else {
            return String.valueOf(SerialString.charAt(remainderNum));
        }
    }

    /**
     * 用随机数填充空白位
     * @param oldCode
     * @param length
     * @return
     */
    public static String fillBlankWithRandomStr(String oldCode, int length) {
        StringBuffer sb = new StringBuffer();
        if(oldCode.length()<length) {
            Random r = new Random();
            char c = oldCode.charAt(oldCode.length()-1);
            for(int i=0; i< length - oldCode.length(); i++) {
                //数字字母随机组合
                if(i==0) {
                    c = RandomStr.charAt(r.nextInt(RandomStr.length()));
                } else if(RandomChar.contains(String.valueOf(c))) {
                    c = RandomNumber.charAt(r.nextInt(RandomNumber.length()));
                } else if(RandomNumber.contains(String.valueOf(c))) {
                    c = RandomChar.charAt(r.nextInt(RandomChar.length()));
                }
                sb.append(c);
            }
        }
        sb.append(oldCode);
        return sb.toString();
    }

    /**
     * 将有序数字转换成随机数
     * @param serialNumber  有序数字
     * @param lenth          随机数长度
     * @return
     */
    public static String generateRandomStr(Long serialNumber, int lenth) {
        return fillBlankWithRandomStr(NumberToHex(serialNumber), lenth);
    }

    public static void main(String[] args) {
        Set<String> unionSet = new HashSet<>();
        for(long i=1; i<=99999; i++) {
            String code = generateRandomStr(i, 8);
            unionSet.add(code);
            System.out.println(i + " = " + code);
        }
        System.out.println(unionSet.size());
    }

}
