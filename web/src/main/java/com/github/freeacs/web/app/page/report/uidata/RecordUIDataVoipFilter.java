/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.web.app.page.report.UnitListData;
import java.util.Map;

/**
 * This class acts a wrapper for all the filter logic used on the syslog unit list page.
 *
 * <p>It makes sure to populate the filters to the template map.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataVoipFilter {
  public final Double totalscore_low;
  public static final Double totalscore_low_default = 1d;

  public final Double totalscore_high;
  public static final Double totalscore_high_default = null;

  public RecordUIDataVoipFilter(UnitListData inputData, Map<String, Object> root) {
    totalscore_low = inputData.getFilterTotalScoreLow().getDouble(totalscore_low_default);
    totalscore_high = inputData.getFilterTotalScoreHigh().getDouble(totalscore_high_default);
    root.put(inputData.getFilterTotalScoreHigh().getKey(), totalscore_high);
    root.put(inputData.getFilterTotalScoreLow().getKey(), totalscore_low);
  }

  public boolean isRecordRelevant(RecordUIDataVoip record) {
    return isTotalScoreRelevant(record);
  }

  private boolean isTotalScoreRelevant(RecordUIDataVoip record) {
    if (totalscore_high != null) {
      return record.getTotalScore() > totalscore_low && record.getTotalScore() <= totalscore_high;
    }
    return record.getTotalScore() > totalscore_low;
  }
}
