package toolkit.scanner.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import toolkit.scanner.entity.JobScannerDO;

import java.util.List;

@Mapper
public interface JobScannerMapper extends BaseMapper<JobScannerDO> {
    @Select("select * from job_scanner where status = 0")
    List<JobScannerDO> selectWaitToScan();
}
