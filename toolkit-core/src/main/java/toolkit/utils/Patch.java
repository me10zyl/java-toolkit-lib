package toolkit.utils;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Patch<T>{
    private List<T> insertList = new ArrayList<>();
    private List<T> deleteList = new ArrayList<>();
    private List<T> updateList = new ArrayList<>();

    public <E> Patch<E> toPatch(Class<E> clazz) {
        Patch<E> list = new Patch<>();
        list.setInsertList(BeanUtil.copyToList(insertList, clazz));
        list.setDeleteList(BeanUtil.copyToList(deleteList,clazz));
        list.setUpdateList(BeanUtil.copyToList(updateList, clazz));
        return list;
    }
}
