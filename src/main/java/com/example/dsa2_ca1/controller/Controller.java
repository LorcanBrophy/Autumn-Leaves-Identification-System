package com.example.dsa2_ca1.controller;

// TODO ASK PETER IF :

        /*for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                int index = y * width + x;
                }
        }

        for (int index = 0; index < width * height; index++) {
            int x = index % width;
            int y = index / width;
        }*/


import com.example.dsa2_ca1.model.MyArrayList;
import com.example.dsa2_ca1.model.MyList;
import com.example.dsa2_ca1.model.UnionFind;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    @FXML
    private CheckBox showRanksCheckBox;

    @FXML
    private Button searchButton;

    @FXML
    private HBox imageContainer;

    @FXML
    private ImageView originalImageView;
    private ImageView greyImageView;

    private Image userImage;
    private Image resized;

    @FXML
    private ComboBox<String> comboBox;

    private UnionFind unionFind;

    private int[] binaryGrid;
    private Map<Integer, Integer> clusterSizes;

    private boolean drawRanks = false;

    private final int minThreshold = 35;
    private final int maxThreshold = 3000;

    @FXML
    public void initialize() {
        comboBox.getItems().addAll("Original", "Greyscale");
        comboBox.setValue("Original");
    }

    private void preprocessImage() {
        if (resized == null) return;

        int width = (int) resized.getWidth();
        int height = (int) resized.getHeight();

        // 1. convert to binary grid
        binaryGrid = imageToBinaryGrid(resized);

        // 2. run unionFind / cluster search
        clusterSearch(binaryGrid, width, height);

        // 3. get clusters
        clusterSizes = getClusterSizes(binaryGrid);
    }





    @FXML
    private void onChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File Explorer");

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        userImage = new Image(selectedFile.toURI().toString());
        resized = new Image(selectedFile.toURI().toString(), 512, 0, true, true);

        originalImageView.setImage(userImage);

        imageContainer.getChildren().setAll(originalImageView);
        comboBox.setValue("Original");

        binaryGrid = null;
        clusterSizes = null;

        preprocessImage();
    }


    private Image convertImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color colour = reader.getColor(x, y);

                double luminance = 0.3 * colour.getRed() + 0.59 * colour.getGreen() + 0.11 * colour.getBlue();

                Color bwColour = luminance < 0.6 ? Color.BLACK : Color.WHITE;

                writer.setColor(x, y, bwColour);
            }
        }

        return output;
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
    private int[] imageToBinaryGrid(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        int[] binaryGrid = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color colour = reader.getColor(x, y);
                double luminance = 0.3 * colour.getRed() + 0.59 * colour.getGreen() + 0.11 * colour.getBlue();

                int currentPixel = (y * width) + x;
                binaryGrid[currentPixel] = luminance  < 0.6 ? 0 : 1;
            }
        }

        return binaryGrid;
    }

    // step 2
    private void clusterSearch(int[] grid, int width, int height) {
        unionFind = new UnionFind(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentPixel = (y * width) + x;

                // check if current pixel is a leaf, if true, then check adjacent pixels
                if (grid[currentPixel] == 1) {

                    // left
                    if (x > 0 && grid[currentPixel - 1] == 1) unionFind.union(currentPixel, currentPixel - 1);

                    // up
                    if (y > 0 && grid[currentPixel - width] == 1) unionFind.union(currentPixel, currentPixel - width);

                    // top left
                    if (y > 0 && x > 0 && grid[currentPixel - width - 1] == 1) unionFind.union(currentPixel, currentPixel - width - 1);

                    // top right
                    if (y > 0 && x < width - 1 && grid[currentPixel - width + 1] == 1) unionFind.union(currentPixel, currentPixel - width + 1);
                }

            }
        }
    }

    // step 3
    private Map<Integer, Integer> getClusterSizes(int[] grid) {
        Map<Integer, Integer> map = new HashMap<>();

        // counts the size of each cluster
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == 1) {
                int root = unionFind.find(i);
                map.put(root, map.getOrDefault(root, 0) + 1);
            }
        }

        return map;
    }


    private int countValidClusters(Map<Integer, Integer> map) {
        int count = 0;

        for (int size : map.values()) {
            if (size >= minThreshold && size <= maxThreshold) {
                count++;
            }
        }

        return count;
    }

    /////////////////////////////////////////////////////////////////////////////////

    @FXML
    private void onComboBoxChanged() {
        if (userImage == null) return;

        String option = comboBox.getValue();

        if (option.equals("Greyscale")) {
            if (greyImageView == null) {
                greyImageView = new ImageView(convertImage(resized));
                greyImageView.setPreserveRatio(true);
                greyImageView.setFitWidth(512);
                greyImageView.setFitHeight(512);
            }

            Image bwImage = greyImageView.getImage();

            imageContainer.getChildren().setAll(originalImageView, greyImageView);
        } else {
            imageContainer.getChildren().setAll(originalImageView);
        }
    }

    public void onSearchButtonClicked() {
        System.out.println("Total Leaves: " + countValidClusters(clusterSizes));
    }

    public void onChangeLeavesColour() {

        // create new recoloured image
        Image recolored = recolorClusters(binaryGrid, (int) resized.getWidth(), (int) resized.getHeight(), getClusterSizes(binaryGrid));
        ImageView recoloredImageView = new ImageView(recolored);

        // add image to HBox
        imageContainer.getChildren().setAll(originalImageView, recoloredImageView);
    }

    // 1. loop through all pixels
    // 2. compute currentPixel
    // 3. if the pixel is valid, find its root
    // 4.
    private Image recolorClusters(int[] grid, int width, int height, Map<Integer, Integer> map) {
        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        Map<Integer, Color> colourMap = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentPixel = (y * width) + x;

                if (grid[currentPixel] == 1) {
                    int root = unionFind.find(currentPixel);
                    int size = map.get(root);

                    if (size >= minThreshold && size <= maxThreshold) {
                        colourMap.putIfAbsent(root, Color.color(Math.random(), Math.random(), Math.random()));
                        writer.setColor(x, y, colourMap.get(root));
                    } else {
                        writer.setColor(x, y, Color.BLACK);
                    }
                } else {
                    writer.setColor(x, y, Color.BLACK);
                }
            }
        }

        return output;
    }








    ///////////////////////////////////////////////////////
    ///  DRAW RECTANGLES OVER LEAVES

    private Map<Integer, int[]> findBounds(int[] grid, int width, int height, Map<Integer, Integer> map) {
        // root -> [minX, maxX, minY, maxY]
        Map<Integer, int[]> boundingCoords = new HashMap<>();

        // 1. loop through all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // 2. compute the currentPixel
                int currentPixel = (y * width) + x;

                // 3. if the pixel is valid, find its root
                if (grid[currentPixel] == 1) {
                    int root = unionFind.find(currentPixel);
                    int size = map.get(root);

                    if (size >= minThreshold && size <= maxThreshold) {

                        // 4. update bounding box associated with that root
                        boundingCoords.putIfAbsent(root, new int[]{x, x, y, y});
                        int[] bounds = boundingCoords.get(root);

                        bounds[0] = Math.min(x, bounds[0]); // minX
                        bounds[1] = Math.max(x, bounds[1]); // maxX
                        bounds[2] = Math.min(y, bounds[2]); // minY
                        bounds[3] = Math.max(y, bounds[3]); // maxY
                    }
                }
            }
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


    public void onDrawBounds() {

        // compute bounding coords
        Map<Integer, int[]> boundingCoords = findBounds(binaryGrid, (int) resized.getWidth(), (int) resized.getHeight(), clusterSizes);

        // compute order of ranks
        Map<Integer, Integer> ranks = orderClusters(getClusterSizes(binaryGrid));

        // draw image with rectangles
        Canvas canvas = drawBounds(resized, boundingCoords, ranks);

        // add image to HBox
        imageContainer.getChildren().setAll(originalImageView, canvas);

    }

    ///////////////////////////////////////////////////////



    private Map<Integer, Integer> orderClusters(Map<Integer, Integer> map) {
        MyList<int[]> clusters = new MyArrayList<>();

        // fill list with [root, size]
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() >= minThreshold && entry.getValue() <= maxThreshold)
                clusters.add(new int[]{entry.getKey(), entry.getValue()});
        }

        // sort bubble
        for (int i = 0; i < clusters.size() - 1; i++) {
            for (int j = 0; j < clusters.size() - 1 - i; j++) {
                if (clusters.get(j)[1] < clusters.get(j + 1)[1]) {
                    // swap
                    int[] temp = clusters.get(j);
                    clusters.set(j, clusters.get(j + 1));
                    clusters.set(j + 1, temp);
                }
            }
        }


        // assign ranks
        Map<Integer, Integer> clusterRanks = new HashMap<>();

        for (int i = 0; i < clusters.size(); i++) {
            int root = clusters.get(i)[0];
            clusterRanks.put(root, i + 1);
        }

        return clusterRanks;
    }

    public void onToggleRanks() {
        drawRanks = showRanksCheckBox.isSelected();
    }
}
