/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.analysis.toolbox;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.v2;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Visualizes problem and solution.
 * <p>Note that every item to be rendered need to have coordinates.
 *
 * @author schroeder
 */
public class Plotter {

    private final static Color START_COLOR = Color.RED;
    private final static Color END_COLOR = Color.RED;
    private final static Color PICKUP_COLOR = Color.GREEN;
    private final static Color DELIVERY_COLOR = Color.BLUE;
    private final static Color SERVICE_COLOR = Color.BLUE;

    private final static Shape ELLIPSE = new Ellipse2D.Double(-3, -3, 6, 6);

    private static class MyActivityRenderer extends XYLineAndShapeRenderer {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private final XYSeriesCollection seriesCollection;

        private final Map<XYDataItem, Activity> activities;

        private final Set<XYDataItem> firstActivities;

        public MyActivityRenderer(XYSeriesCollection seriesCollection, Map<XYDataItem, Activity> activities, Set<XYDataItem> firstActivities) {
            super(false, true);
            this.seriesCollection = seriesCollection;
            this.activities = activities;
            this.firstActivities = firstActivities;
            setSeriesOutlinePaint(0, Color.DARK_GRAY);
            setUseOutlinePaint(true);
        }

        @Override
        public Shape getItemShape(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            if (firstActivities.contains(dataItem)) {
                return ShapeUtilities.createUpTriangle(4.0f);
            }
            return ELLIPSE;
        }

        @Override
        public Paint getItemOutlinePaint(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            if (firstActivities.contains(dataItem)) {
                return Color.BLACK;
            }
            return super.getItemOutlinePaint(seriesIndex, itemIndex);
        }

        @Override
        public Paint getItemPaint(int seriesIndex, int itemIndex) {
            XYDataItem dataItem = seriesCollection.getSeries(seriesIndex).getDataItem(itemIndex);
            Activity activity = activities.get(dataItem);
            if (activity.equals(Activity.PICKUP)) return PICKUP_COLOR;
            if (activity.equals(Activity.DELIVERY)) return DELIVERY_COLOR;
            if (activity.equals(Activity.SERVICE)) return SERVICE_COLOR;
            if (activity.equals(Activity.START)) return START_COLOR;
            if (activity.equals(Activity.END)) return END_COLOR;
            throw new IllegalStateException("activity at " + dataItem + " cannot be assigned to a color");
        }

    }

    private static class BoundingBox {
        double minX;
        double minY;
        double maxX;
        double maxY;

        public BoundingBox(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

    }

    private enum Activity {
        START, END, PICKUP, DELIVERY, SERVICE
    }


    private static final Logger log = LoggerFactory.getLogger(Plotter.class);

    /**
     * Label to label ID (=jobId), SIZE (=jobSize=jobCapacityDimensions)
     *
     * @author schroeder
     */
    public enum Label {
        ID, SIZE, @SuppressWarnings("UnusedDeclaration")NO_LABEL
    }

    private Label label = Label.SIZE;

    private final VehicleRoutingProblem vrp;

    private boolean plotSolutionAsWell;

    private boolean plotShipments = true;

    private Collection<VehicleRoute> routes;

    private BoundingBox boundingBox;

    private final Map<XYDataItem, Activity> activitiesByDataItem = new HashMap<XYDataItem, Plotter.Activity>();

    private final Map<XYDataItem, String> labelsByDataItem = new HashMap<XYDataItem, String>();

    private XYSeries activities;

    private final Set<XYDataItem> firstActivities = new HashSet<XYDataItem>();

    private boolean containsPickupAct;

    private boolean containsDeliveryAct;

    private boolean containsServiceAct;

    private double scalingFactor = 1.;

    private boolean invert;

    /**
     * Constructs Plotter with problem. Thus only the problem can be rendered.
     *
     * @param vrp the routing problem
     */
    public Plotter(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    /**
     * Constructs Plotter with problem and solution to render them both.
     *
     * @param vrp      the routing problem
     * @param solution the solution
     */
    public Plotter(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
        this.vrp = vrp;
        this.routes = solution.routes;
        plotSolutionAsWell = true;
    }

    /**
     * Constructs Plotter with problem and routes to render individual routes.
     *
     * @param vrp    the routing problem
     * @param routes routes
     */
    public Plotter(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) {
        this.vrp = vrp;
        this.routes = routes;
        plotSolutionAsWell = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Plotter setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
        return this;
    }

    /**
     * Sets a label.
     *
     * @param label of jobs
     * @return plotter
     */
    public Plotter setLabel(Label label) {
        this.label = label;
        return this;
    }

    public Plotter invertCoordinates(boolean invert) {
        this.invert = invert;
        return this;
    }

    /**
     * Sets a bounding box to zoom in to certain areas.
     *
     * @param minX lower left x
     * @param minY lower left y
     * @param maxX upper right x
     * @param maxY upper right y
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public Plotter setBoundingBox(double minX, double minY, double maxX, double maxY) {
        boundingBox = new BoundingBox(minX, minY, maxX, maxY);
        return this;
    }

    /**
     * Flag that indicates whether shipments should be rendered as well.
     *
     * @param plotShipments flag to plot shipment
     * @return the plotter
     */
    public Plotter plotShipments(boolean plotShipments) {
        this.plotShipments = plotShipments;
        return this;
    }

    /**
     * Plots problem and/or solution/routes.
     *
     * @param pngFileName - path and filename
     * @param plotTitle   - title that appears on top of image
     * @return BufferedImage image to write
     */
    public BufferedImage plot(String pngFileName, String plotTitle) {
        String filename = pngFileName;
        if (!pngFileName.endsWith(".png")) filename += ".png";
        if (plotSolutionAsWell) {
            return plot(vrp, routes, filename, plotTitle);
        } else if (!(vrp.initialVehicleRoutes().isEmpty())) {
            return plot(vrp, vrp.initialVehicleRoutes(), filename, plotTitle);
        } else {
            return plot(vrp, null, filename, plotTitle);
        }
    }

    private BufferedImage plot(VehicleRoutingProblem vrp, final Collection<VehicleRoute> routes, String pngFile, String title) {
        log.info("plot to {}", pngFile);
        XYSeriesCollection problem;
        XYSeriesCollection solution = null;
        final XYSeriesCollection shipments;
        try {
            retrieveActivities(vrp);
            problem = new XYSeriesCollection(activities);
            shipments = makeShipmentSeries(vrp.jobs().values());
            if (routes != null) solution = makeSolutionSeries(vrp, routes);
        } catch (NoLocationFoundException e) {
            log.warn("cannot plot vrp, since coord is missing");
            return null;
        }
        final XYPlot plot = createPlot(problem, shipments, solution);
        JFreeChart chart = new JFreeChart(title, plot);

        LegendTitle legend = createLegend(routes, shipments, plot);
        chart.removeLegend();
        chart.addLegend(legend);

        save(chart, pngFile);
        return chart.createBufferedImage(1024, 1024);

    }

    private LegendTitle createLegend(final Collection<VehicleRoute> routes, final XYSeriesCollection shipments, final XYPlot plot) {
        LegendItemSource lis = new LegendItemSource() {

            @Override
            public LegendItemCollection getLegendItems() {
                LegendItemCollection lic = new LegendItemCollection();
                LegendItem vehLoc = new LegendItem("vehLoc", Color.RED);
                vehLoc.setShape(ELLIPSE);
                vehLoc.setShapeVisible(true);
                lic.add(vehLoc);
                if (containsServiceAct) {
                    LegendItem item = new LegendItem("service", Color.BLUE);
                    item.setShape(ELLIPSE);
                    item.setShapeVisible(true);
                    lic.add(item);
                }
                if (containsPickupAct) {
                    LegendItem item = new LegendItem("pickup", Color.GREEN);
                    item.setShape(ELLIPSE);
                    item.setShapeVisible(true);
                    lic.add(item);
                }
                if (containsDeliveryAct) {
                    LegendItem item = new LegendItem("delivery", Color.BLUE);
                    item.setShape(ELLIPSE);
                    item.setShapeVisible(true);
                    lic.add(item);
                }
                if (routes != null) {
                    LegendItem item = new LegendItem("firstActivity", Color.BLACK);
                    Shape upTriangle = ShapeUtilities.createUpTriangle(3.0f);
                    item.setShape(upTriangle);
                    item.setOutlinePaint(Color.BLACK);

                    item.setLine(upTriangle);
                    item.setLinePaint(Color.BLACK);
                    item.setShapeVisible(true);

                    lic.add(item);
                }
                if (!shipments.getSeries().isEmpty()) {
                    lic.add(plot.getRenderer(1).getLegendItem(1, 0));
                }
                if (routes != null) {
                    lic.addAll(plot.getRenderer(2).getLegendItems());
                }
                return lic;
            }
        };

        LegendTitle legend = new LegendTitle(lis);
        legend.setPosition(RectangleEdge.BOTTOM);
        return legend;
    }

    private XYItemRenderer getShipmentRenderer(XYSeriesCollection shipments) {
        XYItemRenderer shipmentsRenderer = new XYLineAndShapeRenderer(true, false);   // Shapes only
        for (int i = 0; i < shipments.getSeriesCount(); i++) {
            shipmentsRenderer.setSeriesPaint(i, Color.DARK_GRAY);
            shipmentsRenderer.setSeriesStroke(i, new BasicStroke(
                    1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.f, new float[]{4.0f, 4.0f}, 0.0f
            ));
        }
        return shipmentsRenderer;
    }

    private MyActivityRenderer getProblemRenderer(final XYSeriesCollection problem) {
        MyActivityRenderer problemRenderer = new MyActivityRenderer(problem, activitiesByDataItem, firstActivities);
        problemRenderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {

            @Override
            public String generateLabel(XYDataset arg0, int arg1, int arg2) {
                XYDataItem item = problem.getSeries(arg1).getDataItem(arg2);
                return labelsByDataItem.get(item);
            }

        });
        problemRenderer.setBaseItemLabelsVisible(true);
        problemRenderer.setBaseItemLabelPaint(Color.BLACK);

        return problemRenderer;
    }

    private Range getRange(final XYSeriesCollection seriesCol) {
        if (this.boundingBox == null) return seriesCol.getRangeBounds(false);
        else return new Range(boundingBox.minY, boundingBox.maxY);
    }

    private Range getDomainRange(final XYSeriesCollection seriesCol) {
        if (this.boundingBox == null) return seriesCol.getDomainBounds(true);
        else return new Range(boundingBox.minX, boundingBox.maxX);
    }

    private XYPlot createPlot(final XYSeriesCollection problem, XYSeriesCollection shipments, XYSeriesCollection solution) {
        XYPlot plot = new XYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer problemRenderer = getProblemRenderer(problem);
        plot.setDataset(0, problem);
        plot.setRenderer(0, problemRenderer);

        XYItemRenderer shipmentsRenderer = getShipmentRenderer(shipments);
        plot.setDataset(1, shipments);
        plot.setRenderer(1, shipmentsRenderer);

        if (solution != null) {
            XYItemRenderer solutionRenderer = getRouteRenderer(solution);
            plot.setDataset(2, solution);
            plot.setRenderer(2, solutionRenderer);
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        if (boundingBox == null) {
            xAxis.setRangeWithMargins(getDomainRange(problem));
            yAxis.setRangeWithMargins(getRange(problem));
        } else {
            xAxis.setRangeWithMargins(new Range(boundingBox.minX, boundingBox.maxX));
            yAxis.setRangeWithMargins(new Range(boundingBox.minY, boundingBox.maxY));
        }

        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        return plot;
    }

    private XYItemRenderer getRouteRenderer(XYSeriesCollection solutionColl) {
        XYItemRenderer solutionRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
        for (int i = 0; i < solutionColl.getSeriesCount(); i++) {
            XYSeries s = solutionColl.getSeries(i);
            XYDataItem firstCustomer = s.getDataItem(1);
            firstActivities.add(firstCustomer);
        }
        return solutionRenderer;
    }

    private void save(JFreeChart chart, String pngFile) {
        try {
            ChartUtilities.saveChartAsPNG(new File(pngFile), chart, 1000, 600);
        } catch (IOException e) {
            log.error("cannot plot");
            log.error(e.toString());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private XYSeriesCollection makeSolutionSeries(VehicleRoutingProblem vrp, Collection<VehicleRoute> routes) {
        Map<String, v2> coords = makeMap(vrp.locations());
        XYSeriesCollection coll = new XYSeriesCollection();
        int counter = 1;
        for (VehicleRoute route : routes) {
            if (route.isEmpty()) continue;
            XYSeries series = new XYSeries(counter, false, true);

            v2 startCoord = getCoordinate(coords.get(route.start.location().id));
            series.add(startCoord.x * scalingFactor, startCoord.y * scalingFactor);

            for (AbstractActivity act : route.tourActivities().activities()) {
                v2 coord = getCoordinate(coords.get(act.location().id));
                series.add(coord.x * scalingFactor, coord.y * scalingFactor);
            }

            v2 endCoord = getCoordinate(coords.get(route.end.location().id));
            series.add(endCoord.x * scalingFactor, endCoord.y * scalingFactor);

            coll.addSeries(series);
            counter++;
        }
        return coll;
    }

    private Map<String, v2> makeMap(Collection<Location> allLocations) {
        Map<String, v2> coords = new HashMap<String, v2>();
        for (Location l : allLocations) coords.put(l.id, l.coord);
        return coords;
    }

    private XYSeriesCollection makeShipmentSeries(Collection<Job> jobs) {
        XYSeriesCollection coll = new XYSeriesCollection();
        if (!plotShipments) return coll;
        int sCounter = 1;
        String ship = "shipment";
        boolean first = true;
        for (Job job : jobs) {
            if (!(job instanceof Shipment)) {
                continue;
            }
            Shipment shipment = (Shipment) job;
            XYSeries shipmentSeries;
            if (first) {
                first = false;
                shipmentSeries = new XYSeries(ship, false, true);
            } else {
                shipmentSeries = new XYSeries(sCounter, false, true);
                sCounter++;
            }
            v2 pickupCoordinate = getCoordinate(shipment.getPickupLocation().coord);
            v2 delCoordinate = getCoordinate(shipment.getDeliveryLocation().coord);
            shipmentSeries.add(pickupCoordinate.x * scalingFactor, pickupCoordinate.y * scalingFactor);
            shipmentSeries.add(delCoordinate.x * scalingFactor, delCoordinate.y * scalingFactor);
            coll.addSeries(shipmentSeries);
        }
        return coll;
    }

    private void addJob(XYSeries activities, Job job) {
        if (job instanceof Shipment) {
            Shipment s = (Shipment) job;
            v2 pickupCoordinate = getCoordinate(s.getPickupLocation().coord);
            XYDataItem dataItem = new XYDataItem(pickupCoordinate.x * scalingFactor, pickupCoordinate.y * scalingFactor);
            activities.add(dataItem);
            addLabel(s, dataItem);
            markItem(dataItem, Activity.PICKUP);
            containsPickupAct = true;

            v2 deliveryCoordinate = getCoordinate(s.getDeliveryLocation().coord);
            XYDataItem dataItem2 = new XYDataItem(deliveryCoordinate.x * scalingFactor, deliveryCoordinate.y * scalingFactor);
            activities.add(dataItem2);
            addLabel(s, dataItem2);
            markItem(dataItem2, Activity.DELIVERY);
            containsDeliveryAct = true;
        } else if (job instanceof Pickup) {
            Pickup service = (Pickup) job;
            v2 coord = getCoordinate(service.location.coord);
            XYDataItem dataItem = new XYDataItem(coord.x * scalingFactor, coord.y * scalingFactor);
            activities.add(dataItem);
            addLabel(service, dataItem);
            markItem(dataItem, Activity.PICKUP);
            containsPickupAct = true;
        } else if (job instanceof Delivery) {
            Delivery service = (Delivery) job;
            v2 coord = getCoordinate(service.location.coord);
            XYDataItem dataItem = new XYDataItem(coord.x * scalingFactor, coord.y * scalingFactor);
            activities.add(dataItem);
            addLabel(service, dataItem);
            markItem(dataItem, Activity.DELIVERY);
            containsDeliveryAct = true;
        } else if (job instanceof Service) {
            Service service = (Service) job;
            v2 coord = getCoordinate(service.location.coord);
            XYDataItem dataItem = new XYDataItem(coord.x * scalingFactor, coord.y * scalingFactor);
            activities.add(dataItem);
            addLabel(service, dataItem);
            markItem(dataItem, Activity.SERVICE);
            containsServiceAct = true;
        } else {
            throw new IllegalStateException("job instanceof " + job.getClass() + ". this is not supported.");
        }
    }

    private void addLabel(Job job, XYDataItem dataItem) {
        if (this.label.equals(Label.SIZE)) {
            labelsByDataItem.put(dataItem, getSizeString(job));
        } else if (this.label.equals(Label.ID)) {
            labelsByDataItem.put(dataItem, String.valueOf(job.id()));
        }
    }

    private String getSizeString(Job job) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean firstDim = true;
        for (int i = 0; i < job.size().dim(); i++) {
            if (firstDim) {
                builder.append(String.valueOf(job.size().get(i)));
                firstDim = false;
            } else {
                builder.append(",");
                builder.append(String.valueOf(job.size().get(i)));
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private v2 getCoordinate(v2 coordinate) {
        if (invert) {
            return v2.the(coordinate.y, coordinate.x);
        }
        return coordinate;
    }

    private void retrieveActivities(VehicleRoutingProblem vrp) throws NoLocationFoundException {
        activities = new XYSeries("activities", false, true);
        for (Vehicle v : vrp.vehicles()) {
            v2 start_coordinate = getCoordinate(v.start().coord);
            if (start_coordinate == null) throw new NoLocationFoundException();
            XYDataItem item = new XYDataItem(start_coordinate.x * scalingFactor, start_coordinate.y * scalingFactor);
            markItem(item, Activity.START);
            activities.add(item);

            if (!v.start().id.equals(v.end().id)) {
                v2 end_coordinate = getCoordinate(v.end().coord);
                if (end_coordinate == null) throw new NoLocationFoundException();
                XYDataItem end_item = new XYDataItem(end_coordinate.x * scalingFactor, end_coordinate.y * scalingFactor);
                markItem(end_item, Activity.END);
                activities.add(end_item);
            }
        }
        for (Job job : vrp.jobs().values()) {
            addJob(activities, job);
        }
        for (VehicleRoute r : vrp.initialVehicleRoutes()) {
            for (Job job : r.tourActivities().jobs()) {
                addJob(activities, job);
            }
        }
    }

    private void markItem(XYDataItem item, Activity activity) {
        activitiesByDataItem.put(item, activity);
    }


}
