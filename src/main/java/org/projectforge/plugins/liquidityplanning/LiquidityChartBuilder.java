/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.liquidityplanning;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.projectforge.calendar.DayHolder;
import org.projectforge.charting.XYChartBuilder;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityChartBuilder
{
  /**
   * @param forecast
   * @param nextDays
   * @return
   */
  public JFreeChart create(final LiquidityForecast forecast, final LiquidityForecastSettings settings)
  {
    Validate.isTrue(settings.getNextDays() > 0 && settings.getNextDays() < 500);
    final DayHolder dh = new DayHolder();

    final TimeSeries accumulatedSeries = new TimeSeries("accumulated");
    final TimeSeries worstCaseSeries = new TimeSeries(PFUserContext.getLocalizedString("plugins.liquidityplanning.forecast.worstCase"));
    final TimeSeries creditSeries = new TimeSeries("credits");
    final TimeSeries debitSeries = new TimeSeries("debits");
    final Iterator<LiquidityEntry> it = forecast.getEntries().iterator();
    LiquidityEntry current = it.hasNext() == true ? it.next() : null;
    double accumulated = settings.getStartAmount().doubleValue();
    double worstCase = accumulated;
    for (int i = 0; i < settings.getNextDays(); i++) {
      double debits = 0;
      double credits = 0;
      if (current != null) {
        while (current.getDateOfPayment() == null
            || dh.before(current.getDateOfPayment()) == false
            || dh.isSameDay(current.getDateOfPayment()) == true) {
          final BigDecimal amount = current.getAmount();
          if (amount != null) {
            final double val = amount.doubleValue();
            if (val < 0) {
              credits += val;
              worstCase += val;
            } else {
              debits += val;
            }
          }
          current = it.hasNext() == true ? it.next() : null;
        }
      }
      final Day day = new Day(dh.getDayOfMonth(), dh.getMonth() + 1, dh.getYear());
      accumulated += debits + credits;
      accumulatedSeries.add(day, accumulated);
      worstCaseSeries.add(day, worstCase);
      creditSeries.add(day, -credits);
      debitSeries.add(day, debits);
      dh.add(Calendar.DATE, 1);
    }

    // final XYChartBuilder cb = new XYChartBuilder(ChartFactory.createXYBarChart(null, null, false, null, null, PlotOrientation.VERTICAL,
    // false, false, false));
    final XYChartBuilder cb = new XYChartBuilder(null, null, null, null, true);

    int counter = 0;

    // final TimeSeriesCollection cashflowSet = new TimeSeriesCollection();
    // cashflowSet.addSeries(debitSeries);
    // cashflowSet.addSeries(creditSeries);
    // final XYBarRenderer barRenderer = new XYBarRenderer(.5);
    // barRenderer.setSeriesPaint(0, cb.getGreenFill());
    // barRenderer.setSeriesPaint(1, cb.getRedFill());
    // barRenderer.setShadowVisible(false);
    // cb.setRenderer(counter, barRenderer).setDataset(counter++, cashflowSet);

    final TimeSeriesCollection accumulatedSet = new TimeSeriesCollection();
    accumulatedSet.addSeries(accumulatedSeries);
    final XYDifferenceRenderer diffRenderer = new XYDifferenceRenderer(cb.getGreenFill(), cb.getRedFill(), true);
    diffRenderer.setSeriesPaint(0, cb.getRedMarker());
    cb.setRenderer(counter, diffRenderer).setDataset(counter++, accumulatedSet).setStrongStyle(diffRenderer, false, accumulatedSeries);

    final TimeSeriesCollection worstCaseSet = new TimeSeriesCollection();
    worstCaseSet.addSeries(worstCaseSeries);
    final XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
    lineRenderer.setSeriesPaint(0, cb.getGrayMarker());
    lineRenderer.setSeriesStroke(0, cb.getDashedStroke());
    lineRenderer.setSeriesVisibleInLegend(0, true);
    cb.setRenderer(counter, lineRenderer).setDataset(counter++, worstCaseSet);

    cb.setDateXAxis(true).setYAxis(true, null);
    return cb.getChart();
  }
}