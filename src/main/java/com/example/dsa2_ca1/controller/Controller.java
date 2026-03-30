package com.example.dsa2_ca1.controller;

// TODO ASK PETER IF :

        /*

        for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                }
        }

        for (int index = 0; index < width * height; index++) {
            int x = index % width;
            int y = index / width;
        }

        */


import com.example.dsa2_ca1.model.MyArrayList;
import com.example.dsa2_ca1.model.MyList;
import com.example.dsa2_ca1.model.UnionFind;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Controller {


    @FXML
    private CheckMenuItem showRanksMenuItem;

    @FXML
    private HBox imageContainer;
    @FXML
    private HBox colourCalibrationContainer;
    @FXML
    private HBox colourCalibrationContainer2;
    @FXML
    private HBox colourCalibrationContainer3;
    @FXML
    private VBox colourCalibrationContainerVBOX;

    @FXML
    private ImageView originalImageView;


    private Image resized;

    private UnionFind unionFind;
    private int[] binaryGrid;
    private Map<Integer, MyList<Integer>> clusterSizes;
    private Map<Integer, Integer> clusterRanks;

    private boolean imageProcessed = false;

    private boolean drawRanks = false;


    private Map<Integer, int[]> boundingCoords;
    private MyList<Node> nodes;
    private GraphicsContext graphicsContextTSP;

    private State currentState;

    // TODO ADD A LISTENER MAYBE ONLY WHEN USER CLICKS "CHOOSE COLOURS"
    // TODO MAKE IT SO INSTEAD OF AVG COLOUR, CAN CHOOSE MULTIPLE COLOURS
    @FXML
    public void initialize() {

        currentState = State.NO_IMAGE;

        showTutorial("No image selected", "Go to File → Open... to load an image");

    }

    private enum State {
        NO_IMAGE,
        IMAGE_LOADED,
        CALIBRATING,
        CALIBRATED,
        SEARCHING,
        COLOUR_ONE_LEAF,
        TSP,
        TOOLTIP
    }

    private void updateUI() {
        switch (currentState) {
            case NO_IMAGE:
                showTutorial("No image selected", "Go to File → Open... to load an image");
                break;
            case IMAGE_LOADED:
                showTutorial("Image loaded", "Setup → Select Colours", originalImageView);
                break;
            case CALIBRATING:
                showTutorial("Colour Selection", "Click the image 3 times to select the colours", colourCalibrationContainer3);
                break;
            case CALIBRATED:
                showTutorial("Colour Selection Complete", "", colourCalibrationContainer3);
                break;
            case SEARCHING:
                showTutorial("Total leaves", "Approximately " + countValidClusters() + " Leaves", originalImageView);
                break;
            case COLOUR_ONE_LEAF:
                showTutorial("Colour One Leaf", "Click the white leaves to set them to random colours", colourCalibrationContainer3);
                break;
            case TSP:
                showTutorial("TSP Animation", "Click a leaf in a rectangle to start animation", colourCalibrationContainer3);
                break;
            case TOOLTIP:
                showTutorial("Size Tooltip", "Click any leaf in a rectangle to show the tooltip", colourCalibrationContainer3);
                break;
        }
    }

    private void showTutorial(String titleText, String instructionText, javafx.scene.Node... extraNodes) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        Label title = new Label(titleText);
        title.getStyleClass().add("placeholder-title");

        Label instruction = new Label(instructionText);
        instruction.getStyleClass().add("placeholder-instruction");

        box.getChildren().addAll(title, instruction);
        if (extraNodes.length > 0) {
            box.getChildren().addAll(extraNodes);
        }

        imageContainer.getChildren().setAll(box);
    }



    public Color selectedColour;
    private final MyList<Color> selectedColours = new MyArrayList<>();

    private int numColourSelected = 0;

    int rectSize = 164;
    private final Rectangle rectColourHover = new Rectangle(rectSize, rectSize);
    private final Rectangle rectColour1 = new Rectangle(rectSize, rectSize);
    private final Rectangle rectColour2 = new Rectangle(rectSize, rectSize);
    private final Rectangle rectColour3 = new Rectangle(rectSize, rectSize);
    private final Rectangle rectColourAverage = new Rectangle(rectSize, rectSize);

    BufferedImage bufferedImage;


    @FXML
    private void calibrateColour() {
        if (bufferedImage == null) return;

        currentState = State.CALIBRATING;
        updateUI();

        numColourSelected = 0;
        selectedColours.clear();

        // colour rects
        rectColourHover.setStroke(Color.BLACK);
        rectColour1.setStroke(Color.BLACK);
        rectColour2.setStroke(Color.BLACK);
        rectColour3.setStroke(Color.BLACK);
        rectColourAverage.setStroke(Color.BLACK);

        rectColourHover.setFill(Color.SNOW);
        rectColour1.setFill(Color.SNOW);
        rectColour2.setFill(Color.SNOW);
        rectColour3.setFill(Color.SNOW);
        rectColourAverage.setFill(Color.SNOW);

        // label each rectangle
        VBox hoverBox = createLabeledRect("Hover", rectColourHover);
        VBox rect1Box = createLabeledRect("Colour 1", rectColour1);
        VBox rect2Box = createLabeledRect("Colour 2", rectColour2);
        VBox rect3Box = createLabeledRect("Colour 3", rectColour3);
        VBox averageBox = createLabeledRect("Average Colour", rectColourAverage);

        // put 3 colour rects in hbox
        colourCalibrationContainer.getChildren().setAll(rect1Box, rect2Box, rect3Box);
        colourCalibrationContainer2.getChildren().setAll(averageBox, hoverBox);

        // put hbox, average, and hover in vbox
        colourCalibrationContainerVBOX.getChildren().setAll(colourCalibrationContainer, colourCalibrationContainer2);

        // add to main container
        colourCalibrationContainer3.getChildren().setAll(originalImageView, colourCalibrationContainerVBOX);

        // shows colour hovered over
        originalImageView.setOnMouseMoved(event -> {
            int x = (int) event.getX();
            int y = (int) event.getY();

            PixelReader reader = resized.getPixelReader();
            Color hovered = reader.getColor(x, y);

            rectColourHover.setFill(hovered);
        });

        // click to select colour
        originalImageView.setOnMouseClicked(event -> {
            int x = (int) event.getX();
            int y = (int) event.getY();

            PixelReader reader = resized.getPixelReader();
            Color clicked = reader.getColor(x, y);

            selectedColours.add(clicked);
            updateAverageSelectedColour();

            rectColourAverage.setFill(selectedColour);

            switch (numColourSelected) {
                case 0 -> rectColour1.setFill(clicked);
                case 1 -> rectColour2.setFill(clicked);
                case 2 -> rectColour3.setFill(clicked);
            }

            numColourSelected++;
            System.out.println("Selected colours: " + selectedColours.size());

            if (numColourSelected >= 3) {
                originalImageView.setOnMouseMoved(null);
                originalImageView.setOnMouseClicked(null);
                System.out.println("Calibration complete.");
                imageProcessed = false;
                preprocessImage();

                currentState = State.CALIBRATED;
                updateUI();
            }
        });
    }

    private VBox createLabeledRect(String labelText, Rectangle rect) {
        Label label = new Label(labelText);
        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(label, rect);
        return vbox;
    }

    private void updateAverageSelectedColour() {
        double r = 0, g = 0, b = 0;

        for (Color c : selectedColours) {
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }

        int n = selectedColours.size();
        selectedColour = new Color(r / n, g / n, b / n, 1.0);
    }

    private void preprocessImage() {
        if (selectedColour == null) {
            System.out.println("Click a leaf to select colour first");
            return;
        }
        if (imageProcessed) return;

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // 1. convert to binary grid
        binaryGrid = buildBinaryGrid(bufferedImage);

        // 2. run unionFind / cluster search
        unionFind = buildUnionFind(binaryGrid, width, height);

        // 3. get clusters
        clusterSizes = buildValidClusters(binaryGrid, unionFind);

        // 4. get clusterRanks
        clusterRanks = orderClusters(clusterSizes);

        // compute bounding coords
        boundingCoords = findBounds(clusterSizes);

        nodes = findCentres(boundingCoords);

        imageProcessed = true;
    }



    /////////////////////////////////////////////////////////////////////////////////
    ///
    ///  STEP 1. Convert image to binary grid (0 = black, 1 = white).
    ///
    ///  STEP 2. Loop through the binary grid to find adjacent white pixels, using UnionFind to merge them into clusters.
    ///
    ///  STEP 3. Find the unique number of elements in the parent[] array made by UnionFind.
    ///
    ///  STEP 4. Apply a size threshold to each parent to ensure the cluster is real
    ///
    /////////////////////////////////////////////////////////////////////////////////

    // step 1
    public int[] buildBinaryGrid(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] grid = new int[width * height];

        double colourThreshold = 0.3;
        double thresholdSquared = colourThreshold * colourThreshold;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // returns 32 bit int
                // i.e. rgb = aaaaaaaa rrrrrrrr gggggggg bbbbbbbb
                int rgb = image.getRGB(x, y);

                // shift bits so desired colour is in final byte placement

                // shift 16 bits :  r = 00000000 00000000 aaaaaaaa rrrrrrrr
                // then bitwise AND : & 00000000 00000000 00000000 11111111
                //                  r = 00000000 00000000 00000000 rrrrrrrr
                int r = (rgb >> 16) & 255;

                // shift 16 bits :  g = 00000000 aaaaaaaa rrrrrrrr gggggggg
                // then bitwise AND : & 00000000 00000000 00000000 11111111
                //                  g = 00000000 00000000 00000000 gggggggg
                int g = (rgb >> 8) & 255;

                // shift 16 bits :  b = aaaaaaaa rrrrrrrr gggggggg bbbbbbbb
                // then bitwise AND : & 00000000 00000000 00000000 11111111
                //                  b = 00000000 00000000 00000000 bbbbbbbb
                int b = rgb & 255;

                // r, g, b are 0-255 binary, Color uses 0-1
                double r2 = r / 255.0;
                double g2 = g / 255.0;
                double b2 = b / 255.0;

                double distance = colourDistance(r2, g2, b2, selectedColour);


                int currentPixel = (y * width) + x;
                grid[currentPixel] = (distance < thresholdSquared) ? 1 : 0;
            }
        }

        return grid;
    }

    // step 2
    public UnionFind buildUnionFind(int[] grid, int width, int height) {
        UnionFind unionFind = new UnionFind(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentPixel = (y * width) + x;

                if (grid[currentPixel] == 1) {

                    // left
                    if (x > 0 && grid[currentPixel - 1] == 1)
                        unionFind.union(currentPixel, currentPixel - 1);

                    // up
                    if (y > 0 && grid[currentPixel - width] == 1)
                        unionFind.union(currentPixel, currentPixel - width);

                    // top left
                    if (y > 0 && x > 0 && grid[currentPixel - width - 1] == 1)
                        unionFind.union(currentPixel, currentPixel - width - 1);

                    // top right
                    if (y > 0 && x < width - 1 && grid[currentPixel - width + 1] == 1)
                        unionFind.union(currentPixel, currentPixel - width + 1);
                }
            }
        }

        return unionFind;
    }

    // step 3
    public Map<Integer, MyList<Integer>> buildValidClusters(int[] binaryGrid, UnionFind unionFind) {
        Map<Integer, MyList<Integer>> allClusters = new HashMap<>();

        // counts the size of each cluster
        for (int i = 0; i < binaryGrid.length; i++) {
            if (binaryGrid[i] == 1) {
                int root = unionFind.find(i);
                allClusters.computeIfAbsent(root, _ -> new MyArrayList<>()).add(i);
            }
        }

        Map<Integer, MyList<Integer>> filteredClusters = new HashMap<>();

        for (Map.Entry<Integer, MyList<Integer>> entry : allClusters.entrySet()) {
            int size = entry.getValue().size();

            int minThreshold = 35;
            int maxThreshold = 3000;
            if (size >= minThreshold && size <= maxThreshold)
                filteredClusters.put(entry.getKey(), entry.getValue());
        }

        return filteredClusters;
    }

    /////////////////////////////////////////////////////////////////////////////////




    // FILE SELECTION

    @FXML
    private void onChooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File Explorer");

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        BufferedImage original = ImageIO.read(selectedFile);
        bufferedImage = resize(original, 512, 512);

        resized = new Image(selectedFile.toURI().toString(), 512, 512, false, false);

        originalImageView.setImage(resized);
        imageContainer.getChildren().setAll(originalImageView);

        // reset vars for preprocessImage()
        selectedColours.clear();
        selectedColour = null;
        imageProcessed = false;
        binaryGrid = null;
        clusterSizes = null;

        currentState = State.IMAGE_LOADED;
        updateUI();
    }

    public BufferedImage resize(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();

        graphics.drawImage(originalImage, 0, 0, width, height, null);
        graphics.dispose();

        return resizedImage;
    }


    // DISPLAY ORIGINAL IMAGE

    public void onDisplayOriginal() {
        imageContainer.getChildren().setAll(originalImageView);
    }

    // EXIT

    @FXML
    private void onExit() {
        Platform.exit();
    }


    // BW IMAGE CONVERSION

    public void onDisplayBW() {
        preprocessImage();
        if (bufferedImage == null || selectedColour == null) return;

        Image bw = convertImageWithColour(clusterSizes);
        ImageView bwImageView = new ImageView(bw);

        imageContainer.getChildren().setAll(originalImageView, bwImageView);

    }

    private Image convertImageWithColour(Map<Integer, MyList<Integer>> map) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        // make entire image black
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }

        for (Map.Entry<Integer, MyList<Integer>> entry : map.entrySet()) {
            for (int currentPixel : entry.getValue()) {
                int x = currentPixel % width;
                int y = currentPixel / width;
                writer.setColor(x, y, Color.WHITE);
            }
        }

        return output;
    }

    private double colourDistance(double r2, double g2, double b2, Color userColour) {

        double r = r2 - userColour.getRed();
        double g = g2 - userColour.getGreen();
        double b = b2 - userColour.getBlue();

        return (r * r) + (g * g) + (b * b);
    }

    // COLOUR DISJOINT SET

    public void onDisplayRandColours() {
        preprocessImage();
        if (binaryGrid == null) return;


        // create new recoloured image
        Image recolored = recolorClusters(clusterSizes);
        ImageView recoloredImageView = new ImageView(recolored);

        // add image to HBox
        imageContainer.getChildren().setAll(originalImageView, recoloredImageView);
    }

    // 1. loop through all pixels
    // 2. compute currentPixel
    // 3. if the pixel is valid, find its root
    // 4.
    private Image recolorClusters(Map<Integer, MyList<Integer>> map) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        // make entire image black
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }

        // colour known pixels
        for (Map.Entry<Integer, MyList<Integer>> entry : map.entrySet()) {
            Color clusterColor = Color.color(Math.random(), Math.random(), Math.random());

            for (int currentPixel : entry.getValue()) {
                int x = currentPixel % width;
                int y = currentPixel / width;
                writer.setColor(x, y, clusterColor);
            }
        }

        return output;
    }

    // COLOUR ONE LEAF IN BW IMAGE

    @FXML
    private void onColourOneLeaf() {
        if (resized == null) return;
        preprocessImage();

        currentState = State.COLOUR_ONE_LEAF;
        updateUI();

        Image bw = convertImageWithColour(clusterSizes);

        Canvas canvas = new Canvas(resized.getWidth(), resized.getHeight());
        graphicsContextTSP = canvas.getGraphicsContext2D();
        graphicsContextTSP.drawImage(bw, 0, 0);

        colourCalibrationContainer3.getChildren().setAll(originalImageView, canvas);

        System.out.println("Click on a leaf in the image to recolour it.");

        canvas.setOnMouseClicked(event -> {
            Image result = colourClickedLeaf(event, clusterSizes);
            if (result == null) return;

            // graphicsContextTSP.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            graphicsContextTSP.drawImage(result, 0, 0);
        });
    }

    private Image colourClickedLeaf(MouseEvent mouseEvent, Map<Integer, MyList<Integer>> map) {
        if (bufferedImage == null) return null;

        int userX = (int) mouseEvent.getX();
        int userY = (int) mouseEvent.getY();

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int userPixel = (userY * width) + userX;

        if (binaryGrid[userPixel] == 0) return null;

        int userRoot = unionFind.find(userPixel);

        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        Color clusterColour = Color.color(Math.random(), Math.random(), Math.random());

        for (int currentPixel : map.get(userRoot)) {
            int x = currentPixel % width;
            int y = currentPixel / width;
            writer.setColor(x, y, clusterColour);
        }

        return output;
    }

    // DRAW BOUNDARY BOXES

    public void onDisplayBounds() {
        preprocessImage();

        if (binaryGrid == null) return;

        // draw image with rectangles
        Canvas canvas = drawBounds(resized, boundingCoords, clusterRanks);

        // add image to HBox
        imageContainer.getChildren().setAll(originalImageView, canvas);

    }

    private Map<Integer, int[]> findBounds(Map<Integer, MyList<Integer>> map) {
        int width = bufferedImage.getWidth();

        // root -> [minX, maxX, minY, maxY]
        Map<Integer, int[]> boundingCoords = new HashMap<>();

        for (Map.Entry<Integer, MyList<Integer>> entry : map.entrySet()) {
            int root = entry.getKey();
            MyList<Integer> pixels = entry.getValue();

            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int currentPixel : pixels) {
                int x = currentPixel % width;
                int y = currentPixel / width;

                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }

            boundingCoords.put(root, new int[]{minX, maxX, minY, maxY});
        }

        return boundingCoords;
    }

    private Canvas drawBounds(Image original, Map<Integer, int[]> boundingCoords, Map<Integer, Integer> clusterRanks) {

        // 1. get original image dimensions
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();

        Canvas canvas = new Canvas(width, height);
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        graphicsContext.drawImage(original, 0, 0, width, height);

        graphicsContext.setStroke(Color.BLUE);
        graphicsContext.setLineWidth(2);

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));

        // 4. loop through map
        for (Map.Entry<Integer, int[]> entry : boundingCoords.entrySet()) {
            int root = entry.getKey();
            int[] bounds = entry.getValue();

            int minX = bounds[0];
            int maxX = bounds[1];
            int minY = bounds[2];
            int maxY = bounds[3];

            int rectWidth = maxX - minX + 1;
            int rectHeight = maxY - minY + 1;

            graphicsContext.strokeRect(minX, minY, rectWidth, rectHeight);

            if (drawRanks && clusterRanks != null && clusterRanks.containsKey(root)) {
                int rank = clusterRanks.get(root);

                graphicsContext.fillText(String.valueOf(rank), minX + 2, minY + 14);
            }
        }

        return canvas;
    }

    // SHOW ORDER OF SETS

    public void onToggleRanks() {
        drawRanks = showRanksMenuItem.isSelected();
    }

    private Map<Integer, Integer> orderClusters(Map<Integer, MyList<Integer>> map) {

        MyList<int[]> clusters = new MyArrayList<>();

        // fill list with [root, size]
        for (Map.Entry<Integer, MyList<Integer>> entry : map.entrySet()) {
            clusters.add(new int[]{entry.getKey(), entry.getValue().size()});
        }

        insertionSort(clusters);

        // assign ranks
        Map<Integer, Integer> clusterRanks = new HashMap<>();
        for (int i = 0; i < clusters.size(); i++) {
            int root = clusters.get(i)[0];
            clusterRanks.put(root, i + 1);
        }

        return clusterRanks;
    }

    public void insertionSort(MyList<int[]> clusters) {
        for (int i = 1; i < clusters.size(); i++) {
            int[] keyArr = clusters.get(i);
            int size = keyArr[1];

            int j = i - 1;
            while (j >= 0 && clusters.get(j)[1] < size) {
                clusters.set(j + 1, clusters.get(j));
                j--;
            }
            clusters.set(j + 1, keyArr);
        }
    }

    // COUNT NUM OF LEAVES

    public void onSearchButtonClicked() {
        preprocessImage();
        if (clusterSizes == null) return;

        currentState = State.SEARCHING;
        updateUI();
    }

    private int countValidClusters() {
        return clusterSizes.size();
    }

    // TSP

    // 1. find centre of node
    // 2. use nearest neighbor to find the closest node
    // 3. draw line between them

    public void onTSP() {
        if (resized == null) return;
        preprocessImage();

        currentState = State.TSP;
        updateUI();

        // draw boundary boxes over image
        Canvas canvas = drawBounds(resized, boundingCoords, null);

        // get graphics context
        graphicsContextTSP = canvas.getGraphicsContext2D();

        colourCalibrationContainer3.getChildren().setAll(originalImageView, canvas);

        System.out.println("Click on a leaf.");

        canvas.setOnMouseClicked(event -> {
            Node start = findClickedNode(event);
            if (start == null) return;

            // compute TSP path
            MyList<Node> path = nearestNeighbour(nodes, start);

            // animate the path
            animateTSP(path, graphicsContextTSP, boundingCoords);

            //canvas.setOnMouseClicked(null);
        });

    }

    private record Node(int root, double x, double y) {}

    private Node findClickedNode(MouseEvent mouseEvent) {
        if (resized == null) return null;

        int userX = (int) mouseEvent.getX();
        int userY = (int) mouseEvent.getY();

        int width = (int) resized.getWidth();

        int userPixel = (userY * width) + userX;

        if (binaryGrid[userPixel] == 0) {
            System.out.println("whoops");
            return null;
        }

        int userRoot = unionFind.find(userPixel);
        return new Node(userRoot, userX, userY);
    }

    private MyList<Node> findCentres(Map<Integer, int[]> bounds) {
        System.out.println("works");
        MyList<Node> nodes = new MyArrayList<>();

        for (Map.Entry<Integer, int[]> entry : bounds.entrySet()) {
            int root = entry.getKey();
            int[] coords = entry.getValue();

            double centreX = (coords[0] + coords[1]) / 2.0;
            double centreY = (coords[2] + coords[3]) / 2.0;

            nodes.add(new Node(root, centreX, centreY));
        }

        return nodes;
    }

    // 1. create a list of all unvisited nodes
    // 2. choose a node and mark it unvisited
    // 3. find distance between current node and all unvisited nodes
    // 4. choose node with the shortest distance
    // 5. remove selected node from unvisited list

    private double tspDistance(Node a, Node b) {
        double distX = a.x() - b.x();
        double distY = a.y() - b.y();

        return (distX * distX) + (distY *  distY);
    }

    private MyList<Node> nearestNeighbour(MyList<Node> nodes, Node start) {
        MyList<Node> path = new MyArrayList<>();
        MyList<Node> unvisited = new MyArrayList<>();

        for (int i = 0; i < nodes.size(); i++) {
            unvisited.add(nodes.get(i));
        }

        Node current = start;
        path.add(current);
        unvisited.remove(current);

        while (!unvisited.isEmpty()) {
            Node nearest = null;
            double shortestDistance = Double.MAX_VALUE;

            for (int i = 0; i < unvisited.size(); i++) {
                Node candidate = unvisited.get(i);
                double distance = tspDistance(current, candidate);

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearest = candidate;

                }
            }

            path.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }

        return path;
    }

    private void animateTSP(MyList<Node> path, GraphicsContext graphicsContext, Map<Integer, int[]> boundingCoords) {

        Timeline timeline = new Timeline();
        double interval = 7000.0 / path.size(); // ms

        for (int i = 1; i < path.size(); i++) {

            Node previous = path.get(i - 1);
            Node current = path.get(i);

            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * interval), _ -> {

                // draw red line
                graphicsContext.setStroke(Color.RED);
                graphicsContext.setLineWidth(2.0);
                graphicsContext.strokeLine(previous.x(), previous.y(), current.x(), current.y());

                int[] prevBounds = boundingCoords.get(previous.root);
                if (prevBounds != null) {
                    int minX = prevBounds[0];
                    int maxX = prevBounds[1];
                    int minY = prevBounds[2];
                    int maxY = prevBounds[3];

                    for (int iter = 0; iter < 4; iter++) {
                        graphicsContext.setStroke(Color.BLUE);
                        graphicsContext.strokeRect(minX, minY, maxX - minX + 1, maxY - minY + 1);
                    }
                }

                int[] bounds = boundingCoords.get(current.root);

                if (bounds != null) {
                    int minX = bounds[0];
                    int maxX = bounds[1];
                    int minY = bounds[2];
                    int maxY = bounds[3];

                    for (int iter = 0; iter < 4; iter++) {
                        graphicsContext.setStroke(Color.YELLOW);
                        graphicsContext.strokeRect(minX, minY, maxX - minX + 1, maxY - minY + 1);
                    }

                }
            });

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }


    // REPORT SIZE OF LEAF IN PIXELS

    // DISPLAY CANVAS OF BOUNDING BOX
    // ADD LISTENER
    // DISPLAY TOOLTIP SHOWING RANK/SIZE

    public void onDisplayTooltip() {
        if (resized == null) return;
        preprocessImage();

        currentState = State.TOOLTIP;
        updateUI();

        // draw boundary boxes over image
        Canvas canvas = drawBounds(resized, boundingCoords, null);

        colourCalibrationContainer3.getChildren().setAll(originalImageView, canvas);

        Tooltip tooltip = new Tooltip();


        canvas.setOnMouseClicked(event -> {
            Node clickedNode = findClickedNode(event);
            if (clickedNode == null) return;

            int userX = (int) event.getX();
            int userY = (int) event.getY();

            int width = (int) resized.getWidth();

            int userPixel = (userY * width) + userX;

            int root = clickedNode.root;

            if (binaryGrid[userPixel] == 0) {
                System.out.println("here");
                return;
            }

            tooltip.setText(
                    "Leaf/Cluster Number: " + clusterRanks.get(root) +
                    "\nSize (in pixels): " + clusterSizes.get(root).size()
            );

            Point2D screenPoint = canvas.localToScreen(event.getX(), event.getY());
            tooltip.hide();
            tooltip.show(canvas, screenPoint.getX(), screenPoint.getY());
        });
    }

}
