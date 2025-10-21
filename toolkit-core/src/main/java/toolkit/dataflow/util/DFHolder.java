package toolkit.dataflow.util;


import toolkit.dataflow.dto.DataFlow;
import toolkit.dataflow.dto.inter.IContextKey;

public class DFHolder {
    private final static ThreadLocal<DataFlow> tl = new ThreadLocal<>();

    public static void init(){
        tl.set(new DataFlow());
    }

    public static  DataFlow get() {
        return tl.get();
    }

    public static  <T> T get(IContextKey key) {
        DataFlow dataFlow = tl.get();
        if(dataFlow == null){
            return null;
        }
        return dataFlow.get(key.getKeyName());
    }
    public static  DataFlow put(IContextKey key, Object value) {
        tl.get().put(key.getKeyName(), value);
        return tl.get();
    }

    public static void release(){
        tl.remove();
    }
}
