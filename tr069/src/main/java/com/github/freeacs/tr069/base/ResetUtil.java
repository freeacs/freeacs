package com.github.freeacs.tr069.base;

import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.SystemParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ResetUtil {
  public static void resetReboot(SessionDataI sessionData) {
    log.debug("The reboot parameter is reset to 0 and the reboot will be executed");
    Unittype unittype = sessionData.getUnittype();
    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.RESTART);
    List<UnitParameter> unitParameters = new ArrayList<>();
    UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), "0", sessionData.getProfile());
    unitParameters.add(up);
    unitParameters.forEach(sessionData.getUnit()::toWriteQueue);
  }

  public static void resetReset(SessionDataI sessionData) {
    log.debug("The reset parameter is reset to 0 and the factory reset will be executed");
    Unittype unittype = sessionData.getUnittype();
    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(SystemParameters.RESET);
    List<UnitParameter> unitParameters = new ArrayList<>();
    UnitParameter up = new UnitParameter(utp, sessionData.getUnitId(), "0", sessionData.getProfile());
    unitParameters.add(up);
    unitParameters.forEach(sessionData.getUnit()::toWriteQueue);
  }
}
