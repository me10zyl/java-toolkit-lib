package toolkit.utils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PatchUtil {

    public static <T> void applyPatch(IService<T> service, Patch<T> patch) {
        List<T> deleteList = patch.getDeleteList();
        if (!deleteList.isEmpty()) {
            service.removeByIds(deleteList);
        }
        List<T> insertList = patch.getInsertList();
        if (!insertList.isEmpty()) {
            service.saveBatch(insertList);
        }
        List<T> updateList = patch.getUpdateList();
        if (!updateList.isEmpty()) {
            service.updateBatchById(updateList);
        }
    }
}
