import java.util.List;
public class Production{
    String left;
    List<String> rights;

    public Production(String left, List<String> rights) {
        this.left = left;
        this.rights = rights;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Production)){
            return false;
        }
        Production p=(Production)o;
        boolean res=left.equals(p.left);
        if(rights.size()!=((Production) o).rights.size()){
            return false;
        }
        for (int i = 0; i < rights.size(); i++) {
            res&=rights.get(i).equals(((Production) o).rights.get(i));
        }
        return res;
    }

    @Override
    public int hashCode() {
        return left.length()+rights.size();
    }
}