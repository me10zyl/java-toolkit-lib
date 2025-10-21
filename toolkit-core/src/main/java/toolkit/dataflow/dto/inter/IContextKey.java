package toolkit.dataflow.dto.inter;

/**
 * 上下文键接口，所有用于 DataFlow 的键都必须实现此接口。
 */
public interface IContextKey {
    /**
     * @return 键的唯一标识符（通常是枚举名或自定义字符串）。
     */
    String getKeyName();
}