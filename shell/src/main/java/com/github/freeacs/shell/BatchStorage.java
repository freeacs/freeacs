package com.github.freeacs.shell;

import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import java.util.ArrayList;
import java.util.List;

public class BatchStorage {
  private List<UnitParameter> addChangeUnitParameters;

  private List<UnitParameter> deleteUnitParameters;

  private UnitTempStorage addUnits = new UnitTempStorage();

  private UnitTempStorage deleteUnits = new UnitTempStorage();

  private List<UnittypeParameter> addChangeUnittypeParameters;

  private List<UnittypeParameter> deleteUnittypeParameters;

  public List<UnitParameter> getAddChangeUnitParameters() {
    if (addChangeUnitParameters == null) {
      addChangeUnitParameters = new ArrayList<>();
    }
    return addChangeUnitParameters;
  }

  public List<UnittypeParameter> getAddChangeUnittypeParameters() {
    if (addChangeUnittypeParameters == null) {
      addChangeUnittypeParameters = new ArrayList<>();
    }
    return addChangeUnittypeParameters;
  }

  public UnitTempStorage getAddUnits() {
    return addUnits;
  }

  public List<UnitParameter> getDeleteUnitParameters() {
    if (deleteUnitParameters == null) {
      deleteUnitParameters = new ArrayList<>();
    }
    return deleteUnitParameters;
  }

  public UnitTempStorage getDeleteUnits() {
    return deleteUnits;
  }

  public List<UnittypeParameter> getDeleteUnittypeParameters() {
    if (deleteUnittypeParameters == null) {
      deleteUnittypeParameters = new ArrayList<>();
    }
    return deleteUnittypeParameters;
  }

  public void setAddChangeUnitParameters(List<UnitParameter> unitParameters) {
    this.addChangeUnitParameters = unitParameters;
  }

  public void setAddChangeUnittypeParameters(List<UnittypeParameter> addChangeUnittypeParameters) {
    this.addChangeUnittypeParameters = addChangeUnittypeParameters;
  }

  public void setDeleteUnitParameters(List<UnitParameter> deleteUnitParameters) {
    this.deleteUnitParameters = deleteUnitParameters;
  }

  public void setDeleteUnittypeParameters(List<UnittypeParameter> deleteUnittypeParameters) {
    this.deleteUnittypeParameters = deleteUnittypeParameters;
  }
}
