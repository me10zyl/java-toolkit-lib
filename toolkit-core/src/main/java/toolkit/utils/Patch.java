package toolkit.utils;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.IService;
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

    public void applyPatch(IService<T> service) {
        List<T> deleteList = this.getDeleteList();
        if (!deleteList.isEmpty()) {
            service.removeByIds(deleteList);
        }
        List<T> insertList = this.getInsertList();
        if (!insertList.isEmpty()) {
            service.saveBatch(insertList);
        }
        List<T> updateList = this.getUpdateList();
        if (!updateList.isEmpty()) {
            service.updateBatchById(updateList);
        }
    }
}
