package toolkit.utils;

import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class DumpUtil {

    public static void startWatchOOM(){
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long maxMemory = Runtime.getRuntime().maxMemory();
                long usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
                double usagePercentage = (double) usedMemory / maxMemory * 100;

                int percent = 80;
                if (usagePercentage > percent) { // 设置内存占用阈值
                    try {
                        log.error("userPercentage超过{}%,准备heapdump并退出程序", percent);
                        generateHeapDump("./logs/dumpoom.hprof");
                    } catch (Exception e) {
                        log.error("generate heapdump ERROR", e);
                    }
                    try{
                        Runtime.getRuntime().exec("jstat -gcutil 1 > ./logs/jstat_result.txt");
                    }catch (Exception e){

                    }
                    System.exit(0); // 触发一次后退出监控
                }
            }
        }, 5 * 60 * 1000, 10000); // 每 10 秒检查一次
    }

    private static void generateHeapDump(String filePath) throws Exception {
        log.info("生成heapdump {}", filePath);
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        mxBean.dumpHeap(filePath, true);
    }
}
