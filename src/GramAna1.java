import java.util.*;

class GramAna1{
    private List<Production> productions;//产生式
    private Map<String,Integer> indexes=new HashMap<>();
    private String[][] analyticalTable={{"","+","*","(",")","id","num","#","E","T","F"},
            {"0","","","S4","","S5","S12","","1","2","3"},
            {"1","S7","","","","","","acc","","",""},
            {"2","r2","S6","","r2","","","r2","","",""},
            {"3","r4","r4","","r4","","","r4","","",""},
            {"4","","","S4","","S5","S12","","10","2","3"},
            {"5","r6","r6","","r6","","","r6","","",""},
            {"6","","","S4","","S5","S12","","","","8"},
            {"7","","","S4","","S5","S12","","","9","3"},
            {"8","r3","r3","","r3","","","r3","","",""},
            {"9","r1","S6","","r1","","","r1","","",""},
            {"10","S7","","","S11","","","","","",""},
            {"11","r5","r5","","r5","","","r5","","",""},
            {"12","r7","r7","","r7","","","r7","","",""}
    };
    List<Production> productionList;
    private List<String> inputSeq;
    private int beg;
    private int end;
    // private String[][] analyticalTable;
    ;//分析表
    public GramAna1(List<String> inputSeq_ ,int beg_,int end_) {
        //String[][] table;
        init();
        inputSeq=inputSeq_;
        beg=beg_;
        end=end_;
        productionList=parse();
    }

    /**
     * 真正的从关系表中获得产生式序列的过程.
     * @return
     */
    private List<Production> parse(){
        List<Production> productionList=new ArrayList<>();//保存最后的结果
        Stack<Integer> stateStack=new Stack<>();//状态栈
        Stack<String> tokenStack=new Stack<>();//符号栈
        stateStack.push(0);
        tokenStack.push("#");
        List<String> list=new ArrayList<>();
        for (int i = beg; i <=end ; i++) {
            list.add(inputSeq.get(i));
        }
        list.add("#");
        int j=0;
        while (true) {
            String ch=list.get(j);
          //  String ch = dealToken(string);
            int index_i,index_j;
            if(analyticalTable[stateStack.peek()+1][indexes.get(ch)+1].equals("acc")){
                break;
            } else{
                index_i=stateStack.peek()+1;
                index_j=indexes.get(ch)+1;
            }
            if (analyticalTable[index_i][index_j].charAt(0) == 'S') {//这是一个移进项目
                int state = Integer.parseInt(analyticalTable[index_i][index_j].substring(1));
                stateStack.push(state);
                tokenStack.push(ch);
                j++;
            } else if (analyticalTable[index_i][index_j].charAt(0) == 'r') {//规约项目
                int num = Integer.parseInt(analyticalTable[index_i][index_j].substring(1));//产生式的序号,注意产生式可能有很多位
                Production production = productions.get(num);
                int len = production.rights.size();
                for (int i = 0; i < len; i++) {
                    if(stateStack.empty()||tokenStack.empty()){
                        break;
                    }
                    stateStack.pop();
                    tokenStack.pop();
                }
                String left=production.left;
                productionList.add(production);//将这个产生式加入进去
                tokenStack.push(left);
                if(stateStack.empty()){
                    stateStack.add(0);
                }
                stateStack.push(Integer.valueOf((analyticalTable[stateStack.peek()+1][indexes.get(left)+1])));//将栈压入
            }
        }
        return productionList;
    }

    private String dealToken(String s){
        String[] strings= s.split("\t");
        if(strings[1].charAt(1)=='标'){
            return "id";//这是一个标识符
        }else if(strings[1].charAt(1)=='常'){
            return "num";
        }else {
            return strings[0];
        }
    }

    private void init(){
        productions=new ArrayList<>();
        //非终结符
        Set<String> nonTerminals = new HashSet<>();
        //FIRST符号集
        Map<String, HashSet<String>> FIRST = new HashMap<>();
        //FOLLOW符号集
        Map<String, HashSet<String>> FOLLOW = new HashMap<>();
        //符号表
        Set<String> table = new HashSet<>();
        productions.add(new Production("E1", Collections.singletonList("E")));
        productions.add(new Production("E",Arrays.asList("E","+","T")));
        productions.add(new Production("E", Collections.singletonList("T")));
        productions.add(new Production("T",Arrays.asList("T","*","F")));
        productions.add(new Production("T", Collections.singletonList("F")));
        productions.add(new Production("F",Arrays.asList("(","E",")")));
        productions.add(new Production("F", Collections.singletonList("id")));
        productions.add(new Production("F", Collections.singletonList("num")));
        indexes=new HashMap<>();
        indexes.put("+",0);
        indexes.put("*",1);
        indexes.put("(",2);
        indexes.put(")",3);
        indexes.put("id",4);
        indexes.put("num",5);
        indexes.put("#",6);
        indexes.put("E",7);
        indexes.put("T",8);
        indexes.put("F",9);
        table =new HashSet<>();
        table.addAll(Arrays.asList("+","*","(",")","id","num"));
        nonTerminals.addAll(Arrays.asList("E","T","F"));
    }




}



