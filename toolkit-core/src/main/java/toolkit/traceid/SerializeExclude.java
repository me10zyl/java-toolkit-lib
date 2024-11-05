package toolkit.traceid;

import lombok.Data;

@Data
public class SerializeExclude {
    private final Class clazz;
    private final String[] propertyNames;

    public SerializeExclude(Class clazz, String... propertyNames) {
        this.clazz = clazz;
        this.propertyNames = propertyNames;
    }
}
