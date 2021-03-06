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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Time;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;


public class GraphStreamViewer {

    public static class StyleSheets {

        public static String BLUE_FOREST =
            "graph { fill-color: #141F2E; }" +
                "node {" +
                "	size:157px, 15px;" +
                "   fill-color: #A0FFA0;" +
                "	text-alignment: at-right;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #999;" +
                "	stroke-width: 1.0;" +
                "	text-font: couriernew;" +
                " 	text-offset: 2,-5;" +
                "	text-size: 16;" +
                "}" +
                "node.pickup {" +
                " 	fill-color: #6CC644;" +
                "}" +
                "node.delivery {" +
                " 	fill-color: #f93;" +
                "}" +
                "node.pickupInRoute {" +
                "	fill-color: #6CC644;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.deliveryInRoute {" +
                " 	fill-color: #f93;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.depot {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	shape: box;" +
                "}" +
                "node.removed {" +
                " 	fill-color: #FF8080;" +
                "	size: 10px, 10px;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #CCF;" +
                "   stroke-width: 2.0;" +
                "   shadow-mode: gradient-radial;" +
                "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
                "}" +

                "edge {" +
                "	fill-color: #D3D3D3;" +
                "	arrow-size: 6px,3px;" +
                "}" +
//                    "edge.inserted {" +
//                    "	fill-color: #A0FFA0;" +
//                    "	arrow-size: 6px,3px;" +
//                    "   shadow-mode: gradient-radial;" +
//                    "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
//                    "}" +
//                    "edge.removed {" +
//                    "	fill-color: #FF0000;" +
//                    "	arrow-size: 6px,3px;" +
//                    "   shadow-mode: gradient-radial;" +
//                    "   shadow-width: 10px; shadow-color: #EEF, #000; shadow-offset: 0px;" +
//                    "}" +
                "edge.shipment {" +
                "	fill-color: #999;" +
                "	arrow-size: 6px,3px;" +
                "}";


        @SuppressWarnings("UnusedDeclaration")
        public static String SIMPLE_WHITE =
            "node {" +
                "	size: 40px, 40px;" +
                "   fill-color: #6CC644;" +
                "	text-alignment: at-right;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #999;" +
                "	stroke-width: 1.0;" +
                "	text-font: couriernew;" +
                " 	text-offset: 2,-5;" +
                "	text-size: 8;" +
                "}" +
                "node.pickup {" +
                " 	fill-color: #6CC644;" +
                "}" +
                "node.delivery {" +
                " 	fill-color: #f93;" +
                "}" +
                "node.pickupInRoute {" +
                "	fill-color: #6CC644;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.deliveryInRoute {" +
                " 	fill-color: #f93;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +
                "node.depot {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	shape: box;" +
                "}" +
                "node.removed {" +
                " 	fill-color: #BD2C00;" +
                "	size: 10px, 10px;" +
                " 	stroke-mode: plain;" +
                "	stroke-color: #333;" +
                "   stroke-width: 2.0;" +
                "}" +

                "edge {" +
                "	fill-color: #333;" +
                "	arrow-size: 6px,3px;" +
                "}" +
                "edge.shipment {" +
                "	fill-color: #999;" +
                "	arrow-size: 6px,3px;" +
                "}";

    }

    public static Graph createMultiGraph(String name, String style) {
        Graph g = new MultiGraph(name);
        g.addAttribute("ui.quality");
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.stylesheet", style);
        return g;
    }

    public static ViewPanel createEmbeddedView(Graph graph, double scaling) {
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        ViewPanel view = viewer.addDefaultView(false);
        view.setBackground(Color.BLACK);
        viewer.enableXYZfeedback(true);

        view.setPreferredSize(new Dimension((int) (698 * scaling), (int) (440 * scaling)));
        return view;
    }

    public static String STYLESHEET =
            "graph { canvas-color: #000000; } " +
            "node {" +
            "	size: 50px, 50px;" +
            "   fill-color: #6CC644;" +
            "	text-alignment: at-right;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #999;" +
            "	stroke-width: 1.0;" +
            "	text-font: couriernew;" +
            " 	text-offset: 2,-5;" +
            "	text-size: 16;" +
            "}" +
            "node.pickup {" +
            " 	fill-color: #6CC644;" +
            "}" +
            "node.delivery {" +
            " 	fill-color: #f93;" +
            "}" +
            "node.pickupInRoute {" +
            "	fill-color: #6CC644;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +
            "node.deliveryInRoute {" +
            " 	fill-color: #f93;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +
            "node.depot {" +
            " 	fill-color: #BD2C00;" +
            "	size: 10px, 10px;" +
            " 	shape: box;" +
            "}" +
            "node.removed {" +
            " 	fill-color: #BD2C00;" +
            "	size: 10px, 10px;" +
            " 	stroke-mode: plain;" +
            "	stroke-color: #333;" +
            "   stroke-width: 2.0;" +
            "}" +

            "edge {" +
            "	fill-color: #333;" +
            "	arrow-size: 6px,3px;" +
            "}" +
            "edge.shipment {" +
            "	fill-color: #999;" +
            "	arrow-size: 6px,3px;" +
            "}";

    public enum Label {
        NO_LABEL, ID, JOB_NAME, ARRIVAL_TIME, DEPARTURE_TIME, ACTIVITY
    }

    private static class Center {
        final double x;
        final double y;

        public Center(double x, double y) {
            this.x = x;
            this.y = y;
        }

    }

    private Label label = Label.NO_LABEL;

    private long renderDelay_in_ms;

    private boolean renderShipments;

    private Center center;

    private final VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    private double zoomFactor;

    private double scaling = 1.0;


    public GraphStreamViewer(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
    }

    public GraphStreamViewer(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution) {
        this.vrp = vrp;
        this.solution = solution;
    }

    public GraphStreamViewer labelWith(Label label) {
        this.label = label;
        return this;
    }

    public GraphStreamViewer setRenderDelay(long ms) {
        this.renderDelay_in_ms = ms;
        return this;
    }

    public GraphStreamViewer setRenderShipments(boolean renderShipments) {
        this.renderShipments = renderShipments;
        return this;
    }

    public GraphStreamViewer setGraphStreamFrameScalingFactor(double factor) {
        this.scaling = factor;
        return this;
    }

    /**
     * Sets the camera-view. Center describes the center-focus of the camera and zoomFactor its
     * zoomFactor.
     * <p>
     * <p>a zoomFactor < 1 zooms in and > 1 out.
     *
     * @param centerX    x coordinate of center
     * @param centerY    y coordinate of center
     * @param zoomFactor zoom factor
     * @return the viewer
     */
    public GraphStreamViewer setCameraView(double centerX, double centerY, double zoomFactor) {
        center = new Center(centerX, centerY);
        this.zoomFactor = zoomFactor;
        return this;
    }

    public void display() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Graph g = createMultiGraph("g");

        ViewPanel view = createEmbeddedView(g, scaling);

        createJFrame(view, scaling);

        render(g, view);
    }

    private JFrame createJFrame(ViewPanel view, double scaling) {
        JFrame jframe = new JFrame();
        jframe.setIgnoreRepaint(true);
        jframe.setBackground(Color.BLACK);
        jframe.getRootPane().setBackground(Color.BLACK);

        JPanel basicPanel = new JPanel();
        basicPanel.setOpaque(false);
        basicPanel.setLayout(new BoxLayout(basicPanel, BoxLayout.Y_AXIS));

        //result-panel
        JPanel resultPanel = createResultPanel();
        //graphstream-panel


        JPanel graphStreamPanel = new JPanel();
        graphStreamPanel.setOpaque(false);
        graphStreamPanel.setPreferredSize(new Dimension((int) (800 * scaling), (int) (460 * scaling)));
        graphStreamPanel.setBackground(Color.BLACK);

        JPanel graphStreamBackPanel = new JPanel();
        graphStreamBackPanel.setOpaque(false);
        graphStreamBackPanel.setPreferredSize(new Dimension((int) (700 * scaling), (int) (450 * scaling)));
        graphStreamBackPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        graphStreamBackPanel.setBackground(Color.BLACK);

        graphStreamBackPanel.add(view);
        graphStreamPanel.add(graphStreamBackPanel);

        //setup basicPanel
        basicPanel.add(resultPanel);
        basicPanel.add(graphStreamPanel);
//		basicPanel.add(legendPanel);

        //put it together
        jframe.add(basicPanel);

        //conf jframe
        jframe.setSize((int) (800 * scaling), (int) (580 * scaling));
        jframe.setLocationRelativeTo(null);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        jframe.pack();
        jframe.setTitle("jsprit - GraphStream");
        return jframe;
    }

    private Graph createMultiGraph(String name) {
        return GraphStreamViewer.createMultiGraph(name, STYLESHEET);
    }

    private void render(Graph g, ViewPanel view) {
        view.setOpaque(false);
        view.setBackground(Color.BLACK);
        view.getCamera().setAutoFitView(true);

        if (center != null) {
            view.resizeFrame(view.getWidth(), view.getHeight());
            alignCamera(view);
        }

        for (Vehicle vehicle : vrp.vehicles()) {
            renderVehicle(g, vehicle, label);
            sleep(renderDelay_in_ms);
        }

        for (Job j : vrp.jobs().values()) {
            if (j instanceof Service) {
                renderService(g, (Service) j, label);
            } else if (j instanceof Shipment) {
                renderShipment(g, (Shipment) j, label, renderShipments);
            }
            sleep(renderDelay_in_ms);
        }

        if (solution != null) {
            int routeId = 1;
            for (VehicleRoute route : solution.routes) {
                renderRoute(g, route, routeId, renderDelay_in_ms, label);
                sleep(renderDelay_in_ms);
                routeId++;
            }
        }

    }

    private void alignCamera(View view) {
        view.getCamera().setViewCenter(center.x, center.y, 0);
        view.getCamera().setViewPercent(zoomFactor);
    }

    private JLabel createEmptyLabel() {
        JLabel emptyLabel1 = new JLabel();
        emptyLabel1.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));
        return emptyLabel1;
    }

    private JPanel createResultPanel() {
        int width = 800;
        int height = 50;

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension((int) (width * scaling), (int) (height * scaling)));
        panel.setBackground(Color.BLACK);

        JPanel subpanel = new JPanel();
        subpanel.setLayout(new FlowLayout());
        subpanel.setPreferredSize(new Dimension((int) (700 * scaling), (int) (40 * scaling)));
        subpanel.setBackground(Color.BLACK);
        subpanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        Font font = Font.decode("couriernew");

        JLabel jobs = new JLabel("jobs");
        jobs.setFont(font);
        jobs.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));

        int noJobs = 0;
        if (this.vrp != null) noJobs = this.vrp.jobs().values().size();

        JFormattedTextField nJobs = new JFormattedTextField(noJobs);
        nJobs.setFont(font);
        nJobs.setEditable(false);
        nJobs.setBorder(BorderFactory.createEmptyBorder());
        nJobs.setBackground(new Color(230, 230, 230));

        JLabel costs = new JLabel("costs");
        costs.setFont(font);
        costs.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));

        JFormattedTextField costsVal = new JFormattedTextField(getSolutionCosts());
        costsVal.setFont(font);
        costsVal.setEditable(false);
        costsVal.setBorder(BorderFactory.createEmptyBorder());
        costsVal.setBackground(new Color(230, 230, 230));

        JLabel vehicles = new JLabel("routes");
        vehicles.setFont(font);
        vehicles.setPreferredSize(new Dimension((int) (40 * scaling), (int) (25 * scaling)));
//        vehicles.setForeground(Color.DARK_GRAY);

        JFormattedTextField vehVal = new JFormattedTextField(getNoRoutes());
        vehVal.setFont(font);
        vehVal.setEditable(false);
        vehVal.setBorder(BorderFactory.createEmptyBorder());
//        vehVal.setForeground(Color.DARK_GRAY);
        vehVal.setBackground(new Color(230, 230, 230));

        //platzhalter
        JLabel placeholder1 = new JLabel();
        placeholder1.setPreferredSize(new Dimension((int) (60 * scaling), (int) (25 * scaling)));

        JLabel emptyLabel1 = createEmptyLabel();

        subpanel.add(jobs);
        subpanel.add(nJobs);

        subpanel.add(emptyLabel1);

        subpanel.add(costs);
        subpanel.add(costsVal);

        JLabel emptyLabel2 = createEmptyLabel();
        subpanel.add(emptyLabel2);

        subpanel.add(vehicles);
        subpanel.add(vehVal);

        panel.add(subpanel);

        return panel;
    }

    private Integer getNoRoutes() {
        if (solution != null) return solution.routes.size();
        return 0;
    }

    private Double getSolutionCosts() {
        if (solution != null) return solution.cost();
        return 0.0;
    }

    private void renderShipment(Graph g, Shipment shipment, Label label, boolean renderShipments) {

        Node n1 = g.addNode(makeId(shipment.id(), shipment.getPickupLocation().id));
        if (label.equals(Label.ID)) n1.addAttribute("ui.label", shipment.id());
        n1.addAttribute("x", shipment.getPickupLocation().coord.x);
        n1.addAttribute("y", shipment.getPickupLocation().coord.y);
        n1.setAttribute("ui.class", "pickup");

        Node n2 = g.addNode(makeId(shipment.id(), shipment.getDeliveryLocation().id));
        if (label.equals(Label.ID)) n2.addAttribute("ui.label", shipment.id());
        n2.addAttribute("x", shipment.getDeliveryLocation().coord.x);
        n2.addAttribute("y", shipment.getDeliveryLocation().coord.y);
        n2.setAttribute("ui.class", "delivery");

        if (renderShipments) {
            Edge s = g.addEdge(shipment.id(), makeId(shipment.id(), shipment.getPickupLocation().id),
                makeId(shipment.id(), shipment.getDeliveryLocation().id), true);
            s.addAttribute("ui.class", "shipment");
        }

    }

    private void sleep(long renderDelay_in_ms2) {
        try {
            Thread.sleep(renderDelay_in_ms2);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void renderService(Graph g, Service service, Label label) {
        Node n = g.addNode(makeId(service.id, service.location.id));
        if (label.equals(Label.ID)) n.addAttribute("ui.label", service.id);
        n.addAttribute("x", service.location.coord.x);
        n.addAttribute("y", service.location.coord.y);
        if (service.type.equals("pickup")) n.setAttribute("ui.class", "pickup");
        if (service.type.equals("delivery")) n.setAttribute("ui.class", "delivery");
    }

    private String makeId(String id, String locationId) {
        return id + "_" + locationId;
    }

    private void renderVehicle(Graph g, Vehicle vehicle, Label label) {
        String nodeId = makeId(vehicle.id(), vehicle.start().id);
        Node vehicleStart = g.addNode(nodeId);
        if (label.equals(Label.ID)) vehicleStart.addAttribute("ui.label", "depot");
//		if(label.equals(Label.ACTIVITY)) n.addAttribute("ui.label", "start");
        vehicleStart.addAttribute("x", vehicle.start().coord.x);
        vehicleStart.addAttribute("y", vehicle.start().coord.y);
        vehicleStart.setAttribute("ui.class", "depot");

        if (!vehicle.start().id.equals(vehicle.end().id)) {
            Node vehicleEnd = g.addNode(makeId(vehicle.id(), vehicle.end().id));
            if (label.equals(Label.ID)) vehicleEnd.addAttribute("ui.label", "depot");
            vehicleEnd.addAttribute("x", vehicle.end().coord.x);
            vehicleEnd.addAttribute("y", vehicle.end().coord.y);
            vehicleEnd.setAttribute("ui.class", "depot");

        }
    }

    private void renderRoute(Graph g, VehicleRoute route, int routeId, long renderDelay_in_ms, Label label) {
        int vehicle_edgeId = 1;
        String prevIdentifier = makeId(route.vehicle().id(), route.vehicle().start().id);
        if (label.equals(Label.ACTIVITY) || label.equals(Label.JOB_NAME)) {
            Node n = g.getNode(prevIdentifier);
            n.addAttribute("ui.label", "start");
        }
        for (AbstractActivity act : route.activities()) {
            Job job = ((JobActivity) act).job();
            String currIdentifier = makeId(job.id(), act.location().id);
            if (label.equals(Label.ACTIVITY)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", act.name());
            } else if (label.equals(Label.JOB_NAME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", job.name());
            } else if (label.equals(Label.ARRIVAL_TIME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", Time.parseSecondsToTime(act.arrTime()));
            } else if (label.equals(Label.DEPARTURE_TIME)) {
                Node actNode = g.getNode(currIdentifier);
                actNode.addAttribute("ui.label", Time.parseSecondsToTime(act.end()));
            }
            g.addEdge(makeEdgeId(routeId, vehicle_edgeId), prevIdentifier, currIdentifier, true);
            if (act instanceof PickupActivity) g.getNode(currIdentifier).addAttribute("ui.class", "pickupInRoute");
            else if (act instanceof DeliveryActivity)
                g.getNode(currIdentifier).addAttribute("ui.class", "deliveryInRoute");
            prevIdentifier = currIdentifier;
            vehicle_edgeId++;
            sleep(renderDelay_in_ms);
        }
        if (route.vehicle().isReturnToDepot()) {
            String lastIdentifier = makeId(route.vehicle().id(), route.vehicle().end().id);
            g.addEdge(makeEdgeId(routeId, vehicle_edgeId), prevIdentifier, lastIdentifier, true);
        }
    }

    static String makeEdgeId(int routeId, int vehicle_edgeId) {
        return routeId + "." + vehicle_edgeId;
    }

    //	public void saveAsPNG(String filename){
    //
    //	}
}
