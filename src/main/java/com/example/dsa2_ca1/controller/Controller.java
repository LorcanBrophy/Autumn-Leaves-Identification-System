package com.example.dsa2_ca1.controller;

import com.example.dsa2_ca1.model.UnionFind;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    @FXML
    private Button searchButton;

    @FXML
    private HBox imageContainer;

    @FXML
    private ImageView originalImageView;

    private ImageView greyImageView;

    private Image userImage;

    @FXML
    private ComboBox<String> comboBox;

    private UnionFind unionFind;

    @FXML
    public void initialize() {
        comboBox.getItems().addAll("Original", "Greyscale");
        comboBox.setValue("Original");
    }

    @FXML
    private void onChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("File Explorer");

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) return;

        userImage = new Image(selectedFile.toURI().toString());
        originalImageView.setImage(userImage);
        greyImageView = null;

        comboBox.setValue("Original");
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

                double grey = (colour.getRed() + colour.getGreen() + colour.getBlue()) / 3;

                Color bwColour = grey < 0.65 ? Color.BLACK : Color.WHITE;

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
    ///          Then apply a size threshold to each parent to ensure the cluster is real
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
                double grey = (colour.getRed() + colour.getGreen() + colour.getBlue()) / 3;

                int currentPixel = (y * width) + x;
                binaryGrid[currentPixel] = grey < 0.55 ? 0 : 1;
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

                    // TODO can do diagonals if wanted later
                }

            }
        }
    }

    // step 3
    // can place inside clusterSearch to improve performance if i want later
    private int filterClusters(int[] grid) {
        Map<Integer, Integer> map = new HashMap<>();

        // counts the size of each cluster
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == 1) {
                int root = unionFind.find(i);
                map.put(root, map.getOrDefault(root, 0) + 1);
            }
        }

        // applies threshold
        int minThreshold = 40;
        int maxThreshold = 1000;
        int filteredClusters = 0;

        for (int clusterSize : map.values()) {
            if (clusterSize >= minThreshold && clusterSize <= maxThreshold) {
                filteredClusters++;
            }
        }

        return filteredClusters;
    }

    /////////////////////////////////////////////////////////////////////////////////

    @FXML
    private void onComboBoxChanged() {
        if (userImage == null) return;

        String option = comboBox.getValue();

        if (option.equals("Greyscale")) {
            if (greyImageView == null) {
                greyImageView = new ImageView(convertImage(userImage));
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
        if (userImage == null) return;

        int[] binaryGrid = imageToBinaryGrid(userImage);
        int width = (int) userImage.getWidth();
        int height = (int) userImage.getHeight();

        clusterSearch(binaryGrid, width, height);
        System.out.println("Total Leaves: " + filterClusters(binaryGrid));


    }
}
