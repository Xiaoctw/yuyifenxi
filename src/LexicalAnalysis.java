import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class LexicalAnalysis{
    private  int row=1;
    private  int index=0;
    private  boolean flag=true;
    private  char[] tokens;
    private HashSet<String> keyWords=new HashSet<>();
    private Map<Integer,HashSet<String>> table1 =new HashMap<>();//关系表
    private Set<String> table2=new HashSet<>();//界符,运算符
    private PrintStream stream;
    private Map<Integer,Integer> tokenToLine;//把token对应的行数和line对应的行数进行映射
    List<String> list=new ArrayList<>();
    LexicalAnalysis(Map<Integer,Integer> map) {
        tokenToLine=map;
        File file = new File("input");
        Scanner in = null;
        try {
            in = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        keyWords.addAll(Arrays.asList("main","if","else","do","while","for","switch",
                "case","int","double","float","long","void","and","or","not","then"));
        try {
            stream=new PrintStream("LexicalAnalysisRes");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert in != null;
        while (in.hasNext()){
            String line= in.nextLine();
            getWord(line);
            row++;
        }
        assert stream != null;
        list.add("#\t<终结符,_>");
        stream.println("#\t<终结符,_>");
        try {
            stream=new PrintStream(new File("符号表"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Integer i : table1.keySet()) {
            if(i==1){
                stream.print("关键字:");
                for (String s : table1.get(1)) {
                    stream.print(" "+s);
                }
                stream.println();
            }else if(i==2){
                stream.print("标识符:");
                for (String s : table1.get(2)) {
                    stream.print(" "+s);
                }
                stream.println();
            }else {
                stream.print("常数:");
                for (String s : table1.get(3)) {
                    stream.print(" "+s);
                }
                stream.println();
            }
        }
        stream.println("其他运算符,界符:");
        for (String s : table2) {
            stream.print(s+"  ");
        }
    }
    private  void getWord(String line){
        char[] chars=line.toCharArray();
        index=0;
        while (index<chars.length){
            int res=judge_token(chars);
            if(res==-2&&flag){
                System.out.println("第"+row+"行出现错误,"+"浮点数错误,出现多个'.'");
            }else if(res==-1&&flag){
                System.out.println("第"+row+"行出现错误,出现不该出现的字符");
            }else {
                int i=0;
                StringBuilder sb=new StringBuilder();
                while (tokens[i]!='\0'){
                    sb.append(tokens[i]);
                    i++;
                }
                if(res==14){
                    flag=false;
                }
                if(flag) {
                    String s=sb.toString();
                    if(res==1){
                        if (!table1.containsKey(res)){
                            table1.put(1,new HashSet<>());
                        }
                        table1.get(1).add(s.toUpperCase());
                        list.add(s+"\t<"+s.toUpperCase()+",_>");
                        stream.println(s+"\t<"+s.toUpperCase()+",_>");
                        tokenToLine.put(list.size()-1,row);
                    }else if(res==2) {
                        if (!table1.containsKey(res)){
                            table1.put(res,new HashSet<>());
                        }
                        table1.get(res).add(s);
                        list.add(s+"\t<标识符,"+s+">");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(s+"\t<标识符,"+s+">");
                    }else if(res==3){
                        if (!table1.containsKey(res)){
                            table1.put(res,new HashSet<>());
                        }
                        table1.get(res).add(s);
                        list.add(s+"\t<常数,"+s+">");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(s+"\t<常数,"+s+">");
                    }else if(res==4){
                        list.add("(\t<左括号,_>");
                        stream.println("(\t<左括号,_>");
                        table2.add("(");
                        tokenToLine.put(list.size()-1,row);
                    }else if(res==5){
                        list.add(")\t<右括号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(")\t<右括号,_>");
                        table2.add(")");
                    }else if(res==6){
                        table2.add("{");
                        list.add("{\t<左大括号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("{\t<左大括号,_>");
                    }else if(res==7){
                        table2.add("}");
                        list.add("}\t<右大括号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("}\t<右大括号,_>");
                    }else if (res==8){
                        table2.add("++");
                        list.add("++\t<自增,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("++\t<自增,_>");
                    }else if(res==9){
                        table2.add("+");
                        list.add("+\t<加号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("+\t<加号,_>");
                    }else if(res==10){
                        table2.add("--");
                        list.add("--\t<自减,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("--\t<自减,_>");
                    }else if(res==11){
                        table2.add("-");
                        list.add("-\t<减号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("-\t<减号,_>");
                    }else if(res==12){
                        table2.add("*");
                        list.add("*\t<乘号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("*\t<乘号,_>");
                    }else if(res==13){
                        list.add("*/\t<注释结尾,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("*/\t<注释结尾,_>");
                    }else if(res==15){
                        table2.add("/");
                        list.add("/\t<斜杠,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("/\t<斜杠,_>");
                    }else if(res==16){
                        table2.add("==");
                        list.add("==\t<判断相等,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("==\t<判断相等,_>");
                    }else if(res==17){
                        table2.add("=");
                        list.add("=\t<等号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("=\t<等号,_>");
                    }else if(res==18){
                        table2.add(">=");
                        list.add(">=\t<大于等于,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(">=\t<大于等于,_>");
                    }else if(res==19){
                        table2.add(">");
                        list.add(">\t<大于,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(">\t<大于,_>");
                    }else if(res==20){
                        table2.add("<=");
                        list.add("<=\t<小于等于,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("<=\t<小于等于,_>");
                    }else if(res==21){
                        table2.add("<");
                        list.add("<\t<小于,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("<\t<小于,_>");
                    }else if(res==22){
                        table2.add(";");
                        list.add(";\t<分号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(";\t<分号,_>");
                    }else if(res==23){
                        table2.add("\"");
                        list.add("\"\t<引号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("\"\t<引号,_>");
                    }else if(res==24){
                        table2.add("!=");
                        list.add("!=\t<不等号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("!=\t<不等号,_>");
                    }else if(res==25){
                        table2.add("!");
                        list.add("!\t<取反,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("!\t<取反,_>");
                    }else if(res==26){
                        table2.add("#");
                        list.add("#\t<井号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("#\t<井号,_>");
                    }else if(res==27){
                        table2.add(",");
                        list.add(",\t<逗号,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println(",\t<逗号,_>");
                    }else if(res==28) {
                        table2.add("&");
                        list.add("&\t<按位与,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("&\t<按位与,_>");
                    }else if(res==29){
                        table2.add("&&");
                        list.add("&&\t<与,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("&&\t<与,_>");
                    }else if(res==30){
                        table2.add("|");
                        list.add("|\t<按位或,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("|\t<按位或,_>");
                    }else{
                        table2.add("||");
                        list.add("||\t<或,_>");
                        tokenToLine.put(list.size()-1,row);
                        stream.println("||\t<或,_>");
                    }
                }
                if(!flag&&res==13){
                    flag=true;
                }
            }
            // row++;
        }
    }
    private  int judge_token(char[] chars){
        int len=chars.length;
        tokens=new char[20];
        char ch=chars[index];
        while (ch==' '){//处理掉多余的空格
            index++;
            ch=chars[index];
        }
        int token_num=0;//数组从0开始
        if((ch>='a' && ch <= 'z') || (ch >= 'A' &&ch <= 'Z')|| ch=='_'){
            while ((ch>='a' && ch <= 'z') || (ch >= 'A' &&ch <= 'Z') || (ch >= '0' && ch <= '9')||(ch=='[')||(ch==']')){
                tokens[token_num]=ch;
                token_num++;
                index++;
                if(index==len){
                    break;
                }
                ch=chars[index];
            }
            String key=new String(tokens).substring(0,token_num);
            if(key.matches("^double\\[(\\d+)]*")||key.matches("^int\\[(\\d+)]*")||key.matches("^float\\[(\\d+)]*")){
                return 1;
            }
            if (keyWords.contains(key)){
                return 1;//关键字
            }
            return 2;//标识符
        }else if(ch >= '0' && ch <= '9'){
            boolean flag=false;
            while ((ch >= '0' && ch <= '9') || ch == '.') {
                if(flag&&ch=='.'){
                    return -2;
                }
                if(ch=='.'){
                    flag=true;
                }
                tokens[token_num++] = ch;
                index++;
                if(index==len){
                    break;
                }
                ch=chars[index];
            }
            return 3;
        }else {
            tokens[token_num]=ch;
            token_num++;
            switch (ch){
                case '(':
                    index++;
                    return 4;
                case  ')':
                    index++;
                    return 5;
                case '{':
                    index++;
                    return 6;
                case '}':
                    index++;
                    return 7;
                case '+':
                    index++;
                    //  ch=chars[index];
                    if(len == index){
                        return 9;
                    }
                    ch=chars[index];
                    if(ch=='+'){
                        tokens[token_num]=ch;
                        //  token_num++;
                        index++;
                        return 8;
                    }else{
                        return 9;
                    }
                case '-':
                    index++;
                    if(index==len){
                        return 11;
                    }
                    ch=chars[index];
                    if(ch=='-'){
                        tokens[token_num]=ch;
                        index++;
                        return 10;
                    }else {
                        return 11;
                    }
                case  '*':
                    index++;
                    if(index==len){
                        return 12;
                    }
                    ch=chars[index];
                    if(ch=='/'){
                        tokens[token_num]=ch;
                        index++;
                        return 13;
                    }else {
                        return 12;
                    }
                case '/':
                    index++;
                    if(index==len){
                        return 15;
                    }
                    ch=chars[index];
                    if(ch=='*'){
                        tokens[token_num]=ch;
                        index++;
                        return 14;
                    }else {
                        return 15;
                    }
                case '=':
                    index++;
                    if(len==index){
                        return 17;
                    }
                    ch=chars[index];
                    if(ch=='='){
                        tokens[token_num]=ch;
                        index++;
                        return 16;
                    }else {
                        return 17;
                    }
                case '>':
                    index++;
                    if(index==len){
                        return 19;
                    }
                    ch=chars[index];
                    if(ch=='='){
                        tokens[token_num]=ch;
                        index++;
                        return 18;
                    }else {
                        return 19;
                    }
                case '<':
                    index++;
                    if(index==len){
                        return 21;
                    }
                    ch=chars[index];
                    if(ch=='='){
                        tokens[token_num]=ch;
                        index++;
                        return 20;
                    }else {
                        return 21;
                    }
                case ';':
                    index++;
                    return 22;
                case '"':
                    index++;
                    return 23;
                case '!':
                    index++;
                    if(index==len){
                        return 25;
                    }
                    ch=chars[index];
                    if(ch=='='){
                        tokens[token_num]=ch;
                        index++;
                        return 24;
                    }else {
                        return 25;
                    }
                case '#':
                    index++;
                    return 26;
                case ',':
                    index++;
                    return 27;
                case '&':
                    index++;
                    if(len==index){
                        return 28;
                    }
                    ch=chars[index];
                    if(ch=='&'){
                        tokens[token_num]=ch;
                        index++;
                        return 29;
                    }else {
                        return 28;
                    }
                case '|':
                    index++;
                    if(len==index){
                        return 30;
                    }
                    ch=chars[index];
                    if(ch=='='){
                        tokens[token_num]=ch;
                        index++;
                        return 31;
                    }else {
                        return 30;
                    }
                default:
                    index++;
                    return -1;
            }
        }
    }
}