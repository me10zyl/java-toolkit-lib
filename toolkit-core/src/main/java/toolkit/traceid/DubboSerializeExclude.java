package toolkit.traceid;

import lombok.Data;

@Data
public class DubboSerializeExclude {
    private final Class clazz;
    private final String[] propertyNames;

    public DubboSerializeExclude(Class clazz, String... propertyNames) {
        this.clazz = clazz;
        this.propertyNames = propertyNames;
    }
}
