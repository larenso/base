/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.safehaus.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.GradientColor;
import com.vaadin.addon.charts.model.style.SolidColor;
import org.elasticsearch.action.search.SearchResponse;
import org.safehaus.core.StatisticResponse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class StatisticChart extends Chart {
    private DataSeries dataSeries;
    private Configuration configuration;
    StatisticResponse statisticResponse;
    String yAxisTitle= "";
    String yAxisTitleWithUnit = "";
    private ReferenceComponent referenceComponent;

    public StatisticChart(ReferenceComponent referenceComponent, SearchResponse response)
    {
        this.referenceComponent = referenceComponent;
        dataSeries = new DataSeries();
        configuration = this.getConfiguration();
        MonitorTab monitorTab;
        if(referenceComponent.getApplication() != null && ((Monitor)referenceComponent.getApplication()).getMain().getMonitorTab() != null)
        {
            monitorTab = ((Monitor) referenceComponent.getApplication()).getMain().getMonitorTab();
            statisticResponse =  new StatisticResponse(response, monitorTab.getMetricList().getMetricValue());
            yAxisTitle = monitorTab.getMetricList().getMetricType();
            yAxisTitleWithUnit = yAxisTitle + " (" +  statisticResponse.getUnits() +")";
        }
        else
            statisticResponse = null;
        if(statisticResponse != null)
            addData(statisticResponse);
        configuration.setSeries(dataSeries);
    }
    public StatisticChart getDefaultChart()
    {
        setHeight("400px");
        setWidth("100%");

        configuration.getChart().setZoomType(ZoomType.X);
        configuration.getChart().setSpacingRight(20);

        configuration.getTitle().setText(yAxisTitle);

        String title = "Click and drag in the plot area to zoom in";
        configuration.getSubTitle().setText(title);

        configuration.getxAxis().setType(AxisType.DATETIME);
        configuration.getxAxis().setTitle(new Title("Time"));

        configuration.getLegend().setEnabled(false);

        Axis yAxis = configuration.getyAxis();
        yAxis.setTitle(new Title(yAxisTitleWithUnit));
        yAxis.setStartOnTick(false);
        yAxis.setShowFirstLabel(false);

        configuration.getTooltip().setShared(true);

        PlotOptionsArea plotOptions = new PlotOptionsArea();

        GradientColor fillColor = GradientColor.createLinear(0, 0, 0, 1);
        fillColor.addColorStop(0, new SolidColor("#4572A7"));
        fillColor.addColorStop(1, new SolidColor(2, 0, 0, 0));
        plotOptions.setFillColor(fillColor);

        plotOptions.setLineWidth(1);
        plotOptions.setShadow(false);

        Marker marker = new Marker();
        marker.setEnabled(false);
        State hoverState = new State(true);
        hoverState.setRadius(5);
        MarkerStates states = new MarkerStates(hoverState);
        marker.setStates(states);

        State hoverStateForArea = new State(true);
        hoverState.setLineWidth(1);

        plotOptions.setStates(new States(hoverStateForArea));
        plotOptions.setMarker(marker);
        plotOptions.setShadow(false);
        configuration.setPlotOptions(plotOptions);


        PlotOptionsArea options = new PlotOptionsArea();
        dataSeries.setPlotOptions(options);
        dataSeries.setName(yAxisTitle);

        simpleReduce(dataSeries, 1000);

        this.drawChart(configuration);

        return this;
    }
    public static Date d(String dateString) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("EET"));
        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void addData(StatisticResponse memoryResponse)
    {
        for (int i = 0; i < memoryResponse.getValues().length; i++) {
            DataSeriesItem item = new DataSeriesItem(d(memoryResponse.getTimestamps()[i].toString().replace("T"," ")),
                    memoryResponse.getValues()[i]);
            dataSeries.add(item);
        }

    }
    public static void simpleReduce(DataSeries series, int pixels) {
        if(series.size() < 500)
            return;
        DataSeriesItem first = series.get(0);
        DataSeriesItem last = series.get(series.size() - 1);
        ArrayList reducedDataSet = new ArrayList();
        if (first.getX() != null) {
            // xy pairs
            double startX = first.getX().doubleValue();
            double endX = last.getX().doubleValue();
            double minDistance = (endX - startX) / pixels;
            reducedDataSet.add(first);
            double lastPoint = first.getX().doubleValue();
            for (int i = 0; i < series.size(); i++) {
                DataSeriesItem item = series.get(i);
                if (item.getX().doubleValue() - lastPoint > minDistance) {
                    reducedDataSet.add(item);
                    lastPoint = item.getX().doubleValue();
                }
            }
            series.setData(reducedDataSet);
        } else {
            // interval data
            int k = series.size() / pixels;
            if (k > 1) {
                for (int i = 0; i < series.size(); i++) {
                    if (i % k == 0) {
                        DataSeriesItem item = series.get(i);
                        reducedDataSet.add(item);
                    }
                }
                series.setData(reducedDataSet);
            }
        }
    }
    public void clearData()
    {
        dataSeries.clear();
        System.out.println("Cleared Data:" + dataSeries.getData());
    }

}
