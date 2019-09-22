package verityfy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetName {
    public static String getStr(String s, Map<String, String> map) {
        if(s==null)
            return "";
        s = s.replaceAll(" ", "");

        List<String> operators = new ArrayList<>();

        int length = s.length();

        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");
        operators.add("!");

        operators.add("~");
        operators.add("|");
        operators.add("&");

        operators.add("(");
        operators.add(")");

        operators.add("==");
        operators.add("~=");

        operators.add(">");
        operators.add(">=");

        operators.add("<");
        operators.add("<=");

        operators.add("=");
        operators.add(".");

        StringBuilder newStr = new StringBuilder();
        StringBuilder name = new StringBuilder();
        int i = 0;
        for (; i < length; i++) {
            if (i == 0) {
                if (!operators.contains(String.valueOf(s.charAt(i))) && !(s.charAt(i) >= '0' && s.charAt(i) <= '9')) {
                    name.append(s.charAt(i));
                } else {
                    newStr.append(s.charAt(i));
                }
            } else {
                if (operators.contains(String.valueOf(s.charAt(i - 1))) && !operators.contains(String.valueOf(s.charAt(i)))
                        && !(s.charAt(i) >= '0' && s.charAt(i) <= '9')) {
                    name.append(s.charAt(i));
                }

                if (operators.contains(String.valueOf(s.charAt(i - 1))) && ((s.charAt(i) >= '0') && (s.charAt(i) <= '9'))) {
                    newStr.append(s.charAt(i));
                }

                if (((s.charAt(i-1) >= '0') && (s.charAt(i-1) <= '9')) && ((s.charAt(i) >= '0') && (s.charAt(i) <= '9'))) {
                    newStr.append(s.charAt(i));
                }
//                if(operators.contains(String.valueOf(s.charAt(i-1))) && operators.contains(String.valueOf(s.charAt(i)))){
//                    newStr.append(s.charAt(i));
//                }
                if (!operators.contains(String.valueOf(s.charAt(i - 1))) && !operators.contains(String.valueOf(s.charAt(i)))
                        && !((s.charAt(i) >= '0') && (s.charAt(i) <= '9')) ) {
                    name.append(s.charAt(i));
                }



                if (operators.contains(String.valueOf(s.charAt(i)))) {
                    if (name.length() != 0) {
                        if (s.charAt(i) == '=' && s.charAt(i + 1) != '=') {
                            newStr.append(name);
                            name = new StringBuilder();
                        } else {
                            String value = map.get(name.toString());
                            name = new StringBuilder();
                            newStr.append(value);
                        }
                    }
                    newStr.append(s.charAt(i));
                }
            }

        }

        if (i == length && name.length() != 0) {
            String value = map.get(name.toString());
            name = new StringBuilder();
            newStr.append(value);
        }

        return newStr.toString();
    }
    public static Map<String,String> getAssignedData(String expresstion){
        Map<String,String> map=new HashMap<>();
        int index=expresstion.indexOf("=");
        String dataName=expresstion.substring(0,index);
        String operation=expresstion.substring(index+1);
        map.put(dataName,operation);
        return map;
    }
    public static List<String> operators() {
        List<String> operators = new ArrayList<>();
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");

        operators.add("!");
        operators.add("~");
        operators.add("|");
        operators.add("&");

        operators.add("(");
        operators.add(")");

        operators.add("==");
        operators.add("~=");

        operators.add(">");
        operators.add(">=");

        operators.add("<");
        operators.add("<=");

        operators.add("=");
        operators.add(".");
        return operators;
    }

    public static List<String> getDataNameList(String expression) {
        if (expression == null)
            return null;
        List<String> dataNameList=new ArrayList<>();
        expression = expression.replaceAll(" ", "");
        expression = expression.replaceAll("&amp;", "&");

        List<String> operators=operators();

        int length = expression.length();

        StringBuilder newStr = new StringBuilder();
        StringBuilder name = new StringBuilder();
        int i = 0;

        for (; i < length; i++) {
            String iStr = String.valueOf(expression.charAt(i));
            boolean isDigit = expression.charAt(i) >= '0' && expression.charAt(i) <= '9';
            if (i == 0) {
                if (!operators.contains(iStr) && !isDigit) {
                    name.append(expression.charAt(i));
                } else {
                    newStr.append(expression.charAt(i));
                }
            } else {
                String i_1Str = String.valueOf(expression.charAt(i - 1));
                if (operators.contains(i_1Str) && !operators.contains(iStr) && !isDigit) {
                    name.append(expression.charAt(i));
                }

                if (operators.contains(i_1Str) && isDigit) {
                    newStr.append(expression.charAt(i));
                }
                if (((expression.charAt(i - 1) >= '0') && (expression.charAt(i - 1) <= '9')) && isDigit) {
                    newStr.append(expression.charAt(i));
                }

                if (!operators.contains(i_1Str) && !operators.contains(iStr) && !isDigit) {
                    name.append(expression.charAt(i));
                }

                if (operators.contains(iStr)) {
                    if (name.length() != 0) {
                        if (expression.charAt(i) == '=' && expression.charAt(i + 1) != '=') {
                            name = new StringBuilder();
                        } else {
                            String nameString = name.toString();
                            if (nameString.equals("null")) {
                                name = new StringBuilder();
                            } else {
                                dataNameList.add(nameString);
                                name = new StringBuilder();
                            }

                        }
                    }
                }
            }

        }

        if (name.length() != 0) {
            String nameString = name.toString();
            if (nameString.equals("null")) {
                name = new StringBuilder();
            } else {
                dataNameList.add(nameString);
                name = new StringBuilder();
            }
        }

        return dataNameList;
    }
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("aaa", "0");
        map.put("sss", "3");
        map.put("bbb", "3.33");
        map.put("operator_feedback","null");
        String s = "!(operator_feedback ~= null)";
        String s1=null;
        System.out.println(getStr(s, map));
        System.out.println(getDataNameList(s));
        String ss = "s=s334==4";
        String[] str = ss.split("=");
        for (int i = 0; i < str.length; i++) {
            System.out.println(str[i]);
        }

        String sw="qqq,eee";

//        System.out.println(getAssignedData(s));
    }
}
