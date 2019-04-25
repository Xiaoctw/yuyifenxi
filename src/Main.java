import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class Main {
    private static Set<String> types;//类型种类,只有int,float,double这几种
    private static List<String> inputSeq;
    private static List<String> inputSeq1;
    private static List<ID> idList;
    private static int offset;
    private static List<Siyuanzu> siyuanzuResList;
    private static Map<String,String> idToType;
    public static void main(String[] args) {
        try {
            init();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        parseStateBlock(0,inputSeq1.size()-1);
        try {
            PrintStream stream=new PrintStream("四元式序列");
            for (Siyuanzu siyuanzu : siyuanzuResList) {
                stream.println(siyuanzu.ope+" "+siyuanzu.arg1+" "+siyuanzu.arg2+" "+siyuanzu.res);
            }
            stream.close();
            stream=new PrintStream("变量符号表");
            stream.println("    变量类型     变量名      宽度      偏移");
            for (ID id : idList) {
                stream.printf("%10s%10s%10d%10d\n",id.type,id.name,id.width,id.offset);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
    private static void init() throws FileNotFoundException {
        offset=0;//初始化偏置
        siyuanzuResList=new ArrayList<>();
        idToType=new HashMap<>();
        idList=new ArrayList<>();
        types=new HashSet<>();
        types.addAll(Arrays.asList("float","int","double"));
        Scanner in=new Scanner(new File("符号表"));
        String line=in.nextLine();
        String[] strs1=line.split(" ");
        Set<String> keyWords = new HashSet<>();
        keyWords.addAll(Arrays.asList(strs1).subList(1, strs1.length));
        String string=in.nextLine();
        String[] strs2=string.split(" ");
        Set<String> ids = new HashSet<>();
        ids.addAll(Arrays.asList(strs2).subList(1,strs2.length));
        String[] strs3=in.nextLine().split(" ");
        Set<String> nums = new HashSet<>();
        nums.addAll(Arrays.asList(strs3).subList(1,strs3.length));
       in.nextLine();
       String[] str4=in.nextLine().split(" ");
        Set<String> operators = new HashSet<>();
       operators.addAll(Arrays.asList(str4));
       inputSeq1=new ArrayList<>();
       inputSeq=new ArrayList<>();
       LexicalAnalysis lexicalAnalysis=new LexicalAnalysis();
       List<String> list=lexicalAnalysis.list;
        for (String s : list) {
            inputSeq.add(dealToken(s));
            inputSeq1.add(dealToken1(s));
        }
    }

    private static String dealToken(String s){
        String[] strings= s.split("\t");
        if(strings[1].charAt(1)=='标'){
            return "id";//这是一个标识符
        }else if(strings[1].charAt(1)=='常'){
            return "num";
        }else {
            return strings[0];
        }
    }

    private static String dealToken1(String s){
        String[] strings=s.split("\t");
        return strings[0];
    }

    /**
     * 处理一个语句块序列,注意这里不是一个语句,是多个语句
     * lo与hi为token字的下表
     * 这里规定hi一定是指向分号
     * 这里可以写成递归的形式
     */
    private static void parseStateBlock(int lo,int hi){
        if(lo>=hi){//说明已经没有什么可以执行的语句了
            return;
        }
        String token= inputSeq.get(lo);
        if(token.equals("if")){//这是一个判断语句
            int index=lo;
            while (index<=hi&&!inputSeq.get(index).equals("else")){
                index++;
            }
            while (!inputSeq.get(index).equals("}")){
                index++;//找到对应大括号结束的位置
            }
            dealWithPD(lo,index);
            parseStateBlock(index+1,hi);
            //添加了数组的处理过程,利用正则表达式匹配数组
        }else if(types.contains(token)||token.matches("^double\\[(\\d+)]*")||token.matches("^int\\[(\\d+)]*")||token.matches("^float\\[(\\d+)]*")){//这是一个声明语句
            int index=lo;
            while (index<=hi&&!inputSeq.get(index).equals(";")){
                index++;
            }
            dealWithSM(lo,index);//处理一个声明语句
            parseStateBlock(index+1,hi);
        }else if(token.equals("do")){//这是一个循环语句
            int index=lo;
            while (index<=hi&&!inputSeq.get(index).equals("while")){
                index++;
            }
            while (!inputSeq.get(index).equals(";")){
                index++;
            }
            dealWithXH(lo,index);
            parseStateBlock(index+1,hi);
        }else {//这里就是普通的执行语句
            int index=lo;
            while (index<=hi&&!inputSeq.get(index).equals(";")){
                index++;
            }
            dealWithZX(lo,index);
            parseStateBlock(index+1,hi);
        }
    }

    /**
     *
     * @param beg 起始为if的下标
     * @param end 终结为}的下标
     */
    private static void dealWithPD(int beg,int end){
        int index_then=beg;
        while (!inputSeq1.get(index_then).equals("then")){
            index_then++;
        }
        int index_else=index_then;
        while (!inputSeq1.get(index_else).equals("else")){
            index_else++;
        }
        int index_end=index_else;
        while (!inputSeq1.get(index_end).equals("}")){
            index_end++;
        }
        int mark=siyuanzuResList.size();//获得起始位置,之后回填的记号
        dealWithBR(beg+2,index_then-2);//去除左右括号,处理布尔表达式
        int mark1=siyuanzuResList.size();//做好一个标记
        parseStateBlock(index_then+2,index_else-2);//处理第一个块
        int mark2=siyuanzuResList.size();//做好一个标记
        backWard(mark1,mark2,mark,mark1-1);
        parseStateBlock(index_else+2,index_end-1);//处理第二个块
    }

    /**
     * 这一步是布尔表达式的回退操作
     * mark1为一个标志,mark2为另一个标志
     * 回退设置beg到end之间的所有四元组
     * @param mark1 第一个标志,表达式为真时跳转的位置
     * @param mark2 第二个标志,表达式为假时跳转的位置
     * @param beg 开始下标
     * @param end 结束下标
     */
    private static void backWard(int mark1,int mark2,int beg,int end){
        for (int i = beg; i <=end ; i++) {
            if(siyuanzuResList.get(i).res.equals("true")){
                siyuanzuResList.get(i).res=""+mark1;
            }else {
                siyuanzuResList.get(i).res=""+mark2;
            }
        }
    }
    private static void dealWithXH(int beg,int end){
        int index=beg;
        while (index<=end&&!inputSeq.get(index).equals("}")){
            index++;
        }
        int index_end1=index;
        while (index<=end&&!inputSeq.get(index).equals(")")){
            index++;
        }
        int index_end2=index;
        int mark1=siyuanzuResList.size();
        parseStateBlock(beg+2,index_end1-1);
        int mark2=siyuanzuResList.size();
        dealWithBR(index_end1+3,index_end2-1);
        int mark3=siyuanzuResList.size();
        backWard(mark1,mark3,mark2,mark3-1);
    }

    /**
     * 声明语句就这样翻译,
     * 处理偏移和字符类型,加上字符的id
     * @param beg
     * @param end
     */


    /**
     * 这个函数处理声明语句
     * 把声明的每一个标识符加入到链表中
     * @param beg 开始
     * @param end 结束
     */
    private static void dealWithSM(int beg,int end){
        if(beg+1>end){
            return;
        }
        String idName=inputSeq1.get(beg+1);
        if(contains(idName)){
            System.out.println("ERROR LINE "+(beg+1)+":标识符"+idName+"已经定义过,重复定义");
            return;
        }
            ID id = new ID(inputSeq1.get(beg), idName);
            idToType.put(idName,inputSeq1.get(beg));
            id.offset = offset;
            offset += id.width;
            idList.add(id);
    }
    private static boolean contains(String name){
        for (ID id : idList) {
            if (id.name.equals(name)){
                return true;
            }
        }
        return false;
    }
    private static void dealWithZX(int beg,int end){
        GramAna1 ana1=new GramAna1(inputSeq,beg+2,end-1);//处理真正执行的部分,不包括最后的赋值,注意去掉最后的分号
        List<Production> list=ana1.productionList;//获得所有的产生式
        int index=1;//标注t的下标
        boolean first=true;//标注第一个,只有第一个做一下特殊处理
        for (Production p : list) {//寻找整个产生式中是否存在符号,比如说+,-,*,/
            if (p.rights.contains("+")||p.rights.contains("-")||p.rights.contains("*")||p.rights.contains("/")){
                String temp="t"+index;
                String ope=getOPE(p);
                int i1=beg;
                if(first){
                    for (int i = beg; i <= end ; i++) {
                        if(inputSeq1.get(i).equals(ope)){
                            i1=i;
                            break;
                        }
                    }
                    first=false;
                    Siyuanzu siyuanzu=new Siyuanzu();
                    siyuanzu.arg1=inputSeq1.get(i1-1);
                    siyuanzu.arg2=inputSeq1.get(i1+1);
                        if (contains1(siyuanzu.arg1, i1) || contains1(siyuanzu.arg2, i1)) {
                            System.out.println("ERROR LINE " + i1 + ":数组类型不能进行加减运算");
                            return;
                        }
                        siyuanzu.ope = ope;
                        siyuanzu.res = temp;
                        siyuanzuResList.add(siyuanzu);
                }else {
                    for (int i = beg; i <= end ; i++) {
                        if(inputSeq1.get(i).equals(ope)){
                            i1=i;
                            break;
                        }
                    }
                    Siyuanzu siyuanzu=new Siyuanzu();
                    if(inputSeq1.get(i1-1).equals(")")&&inputSeq1.get(i1+1).equals("(")){//左右都是括号包围
                        siyuanzu.arg1="t"+(index-2);
                        siyuanzu.arg1="t"+(index-1);
                        siyuanzu.res=temp;
                        siyuanzu.ope=ope;
                    }else if(inputSeq1.get(i1-1).equals(")")){
                        siyuanzu.arg1="t"+(index-1);
                        siyuanzu.arg2=inputSeq1.get(i1+1);
                            if (contains1(siyuanzu.arg2, i1)) {
                                System.out.println("ERROR LINE " + i1 + ":数组类型不能进行加减运算");
                                return;
                            }
                        siyuanzu.res=temp;
                        siyuanzu.ope=ope;
                    }else if(inputSeq1.get(i1+1).equals("(")){
                        siyuanzu.arg1="t"+(index-1);
                        siyuanzu.arg2=inputSeq1.get(i1-1);

                            if (contains1(siyuanzu.arg2, i1)) {
                                System.out.println("ERROR LINE " + i1 + ":数组类型不能进行加减运算");
                                return;
                            }
                            siyuanzu.res = temp;
                            siyuanzu.ope = ope;

                    }else{
                        siyuanzu.arg1="t"+(index-1);
                        siyuanzu.arg2=inputSeq1.get(i1+1);//默认是下一个输入符号
                        if (contains1(siyuanzu.arg1, i1) || contains1(siyuanzu.arg2, i1)) {
                                System.out.println("ERROR LINE " + i1 + ":数组类型不能进行加减运算");
                                return;
                            }
                        siyuanzu.res=temp;
                        siyuanzu.ope=ope;
                    }
                    siyuanzuResList.add(siyuanzu);
                }
                index++;//继续下一个
            }
        }
        if(!first) {//说明存在运算符
            Siyuanzu siyuanzu = new Siyuanzu();
            siyuanzu.ope = "=";//最后进行一个赋值操作
            siyuanzu.arg1 = "t" + (index - 1);
            siyuanzu.arg2 = null;//如果不存在的话那就直接赋值为null,相当于跳过了
            siyuanzu.res = inputSeq1.get(beg);
            siyuanzuResList.add(siyuanzu);
        }else {//可能不存在运算符,这时需要单独处理
            Siyuanzu siyuanzu=new Siyuanzu();
            siyuanzu.arg1=inputSeq1.get(end-1);
            siyuanzu.res=inputSeq1.get(beg);
            siyuanzu.ope="=";//最后进行赋值操作
            siyuanzuResList.add(siyuanzu);
        }
    }
    private static boolean contains1(String s,int i)  {
        String s1=idToType.get(s);
        if(s1==null){
           return false;
        }
        return s1.matches("^double\\[(\\d+)]*") || s1.matches("^int\\[(\\d+)]*") || s1.matches("^float\\[(\\d+)]*");
    }
    /**
     * 得到一个产生式中存在的操作符,
     * 通过遍历实现
     * @param p
     * @return
     */
    private static String getOPE(Production p){
        for (String s : p.rights) {
            if(s.equals("+")){
                return "+";
            }
            if(s.equals("-")){
                return "-";
            }
            if(s.equals("*")){
                return "*";
            }
            if(s.equals("/")){
                return "/";
            }
        }
        return "+";
    }

    /**
     * 处理布尔表达式
     * 在这里跳转的位置首先用true或者是false代替,
     *
     * @param beg
     * @param end
     */
    private static void dealWithBR(int beg,int end){
        if(inputSeq1.get(end).equals(";")){
            end--;
        }
        List<String> brOpes=new ArrayList<>();
        //获得所有的布尔表达式符号
        boolean hasOpe=false;
        for (int i = beg; i <= end ; i++) {
            if(inputSeq1.get(i).equals("and")||inputSeq1.get(i).equals("or")){
                brOpes.add(inputSeq1.get(i));
                hasOpe=true;
            }
        }
        if(hasOpe) {
            List<BRItem> items = new ArrayList<>();
            for (int i = beg; i <= end; i++) {
                if (inputSeq1.get(i).equals("<")) {
                    BRItem item = new BRItem();
                    item.left = inputSeq1.get(i - 1);
                    item.ope = "<";
                    item.right = inputSeq1.get(i + 1);
                    items.add(item);
                } else if (inputSeq1.get(i).equals(">")) {
                    BRItem item = new BRItem();
                    item.left = inputSeq1.get(i - 1);
                    item.ope = ">";
                    item.right = inputSeq1.get(i + 1);
                    items.add(item);
                } else if (inputSeq1.get(i).equals("==")) {
                    BRItem item = new BRItem();
                    item.left = inputSeq1.get(i - 1);
                    item.ope = "==";
                    item.right = inputSeq1.get(i + 1);
                    items.add(item);
                }
            }
            /**
             * 从前向后推进,遇到可能会产生短路求值
             */
            for (int i = 0; i < items.size(); i++) {
                BRItem item = items.get(i);
                Siyuanzu fSiyuanzu = new Siyuanzu();
                fSiyuanzu.ope = "j" + item.ope;
                Siyuanzu tsiyuanzu = new Siyuanzu();
                tsiyuanzu.ope = "j" + item.ope;
                tsiyuanzu.arg1 = item.left;
                tsiyuanzu.arg2 = item.right;
                siyuanzuResList.add(tsiyuanzu);
                siyuanzuResList.add(fSiyuanzu);
                if (i < items.size() - 1) {
                    if (brOpes.get(i).equals("and")) {
                        tsiyuanzu.res = "" + siyuanzuResList.size();//如果为真,那么看下一个表达式
                        fSiyuanzu.res = "false";
                    } else {
                        tsiyuanzu.res = "true";
                        fSiyuanzu.res = "" + (siyuanzuResList.size());//如果为假,那么看下一个表达式
                    }
                } else {
                    tsiyuanzu.res = "true";
                    fSiyuanzu.res = "false";
                }
            }
        }else {
               Siyuanzu tsiyuanzu=new Siyuanzu();
               tsiyuanzu.ope="j"+inputSeq1.get(beg+1);
               tsiyuanzu.arg1=inputSeq1.get(beg);
               tsiyuanzu.arg2=inputSeq1.get(end);
               tsiyuanzu.res="true";
               Siyuanzu fsiyuanzu=new Siyuanzu();
               fsiyuanzu.ope="j"+inputSeq1.get(beg+1);
               fsiyuanzu.res="false";
               siyuanzuResList.add(tsiyuanzu);
               siyuanzuResList.add(fsiyuanzu);
        }
    }


}

class BRItem{
    String left;
    String right;
    String ope;
}

class ID{
    String type;
    String name;
    int offset;
    int width;

    ID(String type, String name) {
        this.type = type;
        this.name = name;
        if(type.equals("int")){
            width=4;
        }else if(type.equals("float")){
            width=4;
        }else if (type.equals("double")){
            width=8;
        }else if (type.matches("^float\\[(\\d+)]*")){
            width= Integer.parseInt(type.substring(6,type.length()-1))*4;
        }else if(type.matches("^int\\[(\\d+)]*")){
            width= Integer.parseInt(type.substring(4,type.length()-1))*4;
        }else {
            width= Integer.parseInt(type.substring(7,type.length()-1))*8;
        }
    }
}

class Siyuanzu{
    String ope;
    String arg1;
    String arg2;
    String res;
}

//class NotFoundIdException extends Exception{
//}


