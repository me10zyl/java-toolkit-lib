package toolkit.dataflow.dto;

import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ToString
public class DataFlow implements Serializable {
    private Map<String, Object> cacheMap = new HashMap<>();

    public DataFlow put(String key, Object value){
        cacheMap.put(key, value);
        return this;
    }

    public <T> T get(String key){
        return (T)cacheMap.get(key);
    }

    public void merge(DataFlow another){
        cacheMap.putAll(another.cacheMap);
    }


    public static DataFlow getDatas(Consumer<DataFlow> dataFlowConsumer){
        DataFlow t = new DataFlow();
        dataFlowConsumer.accept(t);
        return t;
    }
}
