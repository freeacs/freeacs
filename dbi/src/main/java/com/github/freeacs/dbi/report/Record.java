package com.github.freeacs.dbi.report;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes"})
public abstract class Record<R extends Record> {

  public abstract Key getKey();

  public abstract Date getTms();

  public abstract PeriodType getPeriodType();

  public abstract R clone();

  public abstract void add(R record);

  public abstract KeyFactory getKeyFactory();

  public static String[] getCounterAndAveragesMethods(Class recordClass) {
    Method[] methods = recordClass.getMethods();
    List<String> methodList = new ArrayList<>();
    for (Method m : methods) {
      if (m.getReturnType().equals(Counter.class) || m.getReturnType().equals(Average.class)) {
        methodList.add(m.getName());
      }
    }
    Collections.sort(methodList);
    return methodList.toArray(new String[methodList.size()]);
  }

  private static Map<String, String> denomMap = new HashMap<>();

  static {
    denomMap.put(RecordVoip.class.getName() + "jitterAvg".toLowerCase(), "ms");
    denomMap.put(RecordVoip.class.getName() + "jitterMax".toLowerCase(), "ms");
    denomMap.put(RecordVoip.class.getName() + "percentLossAvg".toLowerCase(), "%");
    denomMap.put(RecordVoip.class.getName() + "callLengthAvg".toLowerCase(), "hour");
    denomMap.put(RecordVoip.class.getName() + "callLengthTotal".toLowerCase(), "hour");
    denomMap.put(RecordHardware.class.getName() + "memoryHeapDdrPoolAvg".toLowerCase(), "KB");
    denomMap.put(
        RecordHardware.class.getName() + "memoryHeapDdrCurrentAvg".toLowerCase(), "KB free");
    denomMap.put(RecordHardware.class.getName() + "memoryHeapDdrLowAvg".toLowerCase(), "KB free");
    denomMap.put(RecordHardware.class.getName() + "memoryHeapOcmPoolAvg".toLowerCase(), "KB");
    denomMap.put(
        RecordHardware.class.getName() + "memoryHeapOcmCurrentAvg".toLowerCase(), "KB free");
    denomMap.put(RecordHardware.class.getName() + "memoryHeapOcmLowAvg".toLowerCase(), "KB free");
    denomMap.put(RecordHardware.class.getName() + "memoryNpDdrPoolAvg".toLowerCase(), "Pages free");
    denomMap.put(RecordHardware.class.getName() + "memoryNpDdrCurrentAvg".toLowerCase(), "Pages ");
    denomMap.put(RecordHardware.class.getName() + "memoryNpDdrLowAvg".toLowerCase(), "Pages free");
    denomMap.put(RecordHardware.class.getName() + "memoryNpOcmPoolAvg".toLowerCase(), "Pages");
    denomMap.put(
        RecordHardware.class.getName() + "memoryNpOcmCurrentAvg".toLowerCase(), "Pages free");
    denomMap.put(RecordHardware.class.getName() + "memoryNpOcmLowAvg".toLowerCase(), "Pages free");
    denomMap.put(RecordHardware.class.getName() + "cpeUptimeAvg".toLowerCase(), "minute");
    denomMap.put(RecordVoip.class.getName() + "noSipServiceTime".toLowerCase(), "minute");
    denomMap.put(RecordVoip.class.getName() + "voipQuality".toLowerCase(), "0-100");
    denomMap.put(RecordVoip.class.getName() + "mosAvg".toLowerCase(), "1-5");
    denomMap.put(RecordProvisioning.class.getName() + "sessionLengthAvg".toLowerCase(), "seconds");
    denomMap.put(RecordProvisioning.class.getName() + "provisioningQuality".toLowerCase(), "0-100");
  }

  public static String getDenominator(Class c, String method) {
    return denomMap.get(c.getName() + method.toLowerCase());
  }
}
