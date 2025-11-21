package toolkit.scanner.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import toolkit.scanner.entity.JobScannerDO;

import java.util.List;

@Mapper
public interface JobScannerMapper extends BaseMapper<JobScannerDO> {
    List<JobScannerDO> selectWaitToScan();
}
