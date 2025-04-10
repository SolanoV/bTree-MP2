package com.example.btreemp2;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class twoThreeTreeController extends Application implements Initializable {
    @FXML private Button menuButton;
    @FXML private AnchorPane menu;
    @FXML private TextField insertTextField;
    @FXML private TextField deleteTextField;
    @FXML private TextField searchTextField;
    @FXML private Button insertButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;
    @FXML private Button undoButton;
    @FXML private Button redoButton;
    @FXML private Button clearButton;
    @FXML private AnchorPane root;
    @FXML private Label messageLabel;

    private mpAlgo tree;
    private Stack<TreeState> undoStack = new Stack<>();
    private Stack<TreeState> redoStack = new Stack<>();
    private static final double NODE_WIDTH = 60;
    private static final double NODE_HEIGHT = 40;
    private static final double HORIZONTAL_SPACING = 100;
    private static final double VERTICAL_SPACING = 120;
    private static final double ROOT_X = 500;
    private static final double ROOT_Y = 150;

    private Map<Node, Rectangle> nodeToRectangleMap = new HashMap<>();
    private Map<Node, List<Line>> nodeToLinesMap = new HashMap<>();
    private Map<Integer, Label> keyToLabelMap = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("twoThreeTree.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void initialize(URL location, ResourceBundle resources) {
        tree = new mpAlgo();
        if (menu != null) {
            menu.setTranslateX(500);
        }

        if (messageLabel != null) {
            messageLabel.setVisible(false);
        }

        if (menuButton != null) {
            menuButton.setOnAction(event -> toggleMenu());
        }
        if (insertButton != null) {
            insertButton.setOnAction(event -> {
                System.out.println("Insert button clicked");
                handleInsert();
            });
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(event -> {
                System.out.println("Delete button clicked");
                handleDelete();
            });
        }
        if (searchButton != null) {
            searchButton.setOnAction(event -> {
                System.out.println("Search button clicked");
                handleSearch();
            });
        }
        if (undoButton != null) {
            undoButton.setOnAction(event -> {
                System.out.println("Undo button clicked");
                handleUndo();
            });
        }
        if (redoButton != null) {
            redoButton.setOnAction(event -> {
                System.out.println("Redo button clicked");
                handleRedo();
            });
        }
        if (clearButton != null) {
            clearButton.setOnAction(event -> {
                System.out.println("Clear button clicked");
                handleClear();
            });
        }
        if (insertTextField != null) {
            insertTextField.setOnMouseClicked(event -> System.out.println("Insert text field clicked"));
        }
        if (deleteTextField != null) {
            deleteTextField.setOnMouseClicked(event -> System.out.println("Delete text field clicked"));
        }
        if (searchTextField != null) {
            searchTextField.setOnMouseClicked(event -> System.out.println("Search text field clicked"));
        }

        drawTree();
    }

    private void toggleMenu() {
        if (menu == null) return;

        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.4));
        slide.setNode(menu);
        double buttonValue = menu.getTranslateX();

        if (buttonValue == 500) {
            slide.setToX(0);
            slide.setOnFinished(e -> menu.toFront());
            slide.play();
        } else if (buttonValue == 0) {
            slide.setToX(500);
            slide.play();
        }
    }

    private void handleInsert() {
        if (insertTextField == null) return;
        try {
            int value = Integer.parseInt(insertTextField.getText().trim());
            drawTree();
            animateInsertPath(value, () -> {
                saveState();
                tree.insert(value);
                redoStack.clear();
                drawTree();
                insertTextField.clear();
            });
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid number");
        }
    }

    private void handleDelete() {
        if (deleteTextField == null) return;
        try {
            int value = Integer.parseInt(deleteTextField.getText().trim());
            boolean exists = tree.search(value);
            drawTree();
            animateSearchPath(value, exists, "Element " + value + " is found and deleted", () -> {
                if (exists) {
                    saveState();
                    tree.delete(value);
                    redoStack.clear();
                    drawTree();
                }
                deleteTextField.clear();
            });
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid number");
        }
    }

    private void handleSearch() {
        if (searchTextField == null) return;
        try {
            int value = Integer.parseInt(searchTextField.getText().trim());
            boolean found = tree.search(value);
            drawTree();
            animateSearchPath(value, found, null, () -> searchTextField.clear());
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid number");
        }
    }

    private void handleUndo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new TreeState(tree.root));
            tree.root = undoStack.pop().root;
            drawTree();
        }
    }

    private void handleRedo() {
        if (!redoStack.isEmpty()) {
            saveState();
            tree.root = redoStack.pop().root;
            drawTree();
        }
    }

    private void handleClear() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.setVisible(false);
        }
        saveState();
        tree = new mpAlgo();
        redoStack.clear();
        drawTree();
    }

    private void saveState() {
        undoStack.push(new TreeState(tree.root));
        while (undoStack.size() > 20) {
            undoStack.remove(0);
        }
    }

    private void drawTree() {
        if (root == null) return;
        root.getChildren().removeIf(node -> node instanceof Rectangle || node instanceof Line || node instanceof Label);
        nodeToRectangleMap.clear();
        nodeToLinesMap.clear();
        keyToLabelMap.clear();
        if (tree.root.keys.isEmpty()) return;

        double treeWidth = calculateTreeWidth(tree.root);
        double startX = ROOT_X - treeWidth / 2;
        double startY = ROOT_Y;
        drawNode(tree.root, startX, startY, treeWidth);

        if (menu != null) {
            menu.toFront();
        }
    }

    private void drawNode(Node node, double x, double y, double width) {
        double nodeWidth = node.keys.size() * NODE_WIDTH;
        double nodeX = x + (width - nodeWidth) / 2;

        Rectangle rect = new Rectangle(nodeX, y, nodeWidth, NODE_HEIGHT);
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.getStyleClass().add("tree-node");
        rect.setStrokeWidth(1);
        nodeToRectangleMap.put(node, rect);
        root.getChildren().add(rect);

        for (int i = 0; i < node.keys.size(); i++) {
            Label label = new Label(String.valueOf(node.keys.get(i)));
            label.getStyleClass().add("node-label");
            label.setTextFill(Color.WHITE);

            double textWidth = label.getText().length() * 8;
            double labelX = nodeX + i * NODE_WIDTH + (NODE_WIDTH - textWidth) / 2;
            double labelY = y + (NODE_HEIGHT - 20) / 2;

            label.setLayoutX(labelX);
            label.setLayoutY(labelY);
            label.setUserData(rect);
            keyToLabelMap.put(node.keys.get(i), label);
            root.getChildren().add(label);
            label.toFront();
        }

        nodeToLinesMap.put(node, new ArrayList<>());

        if (!node.isLeaf) {
            double childY = y + VERTICAL_SPACING;
            double childWidth = width / node.children.size();
            double childX = x;

            for (int j = 0; j < node.children.size(); j++) {
                Node child = node.children.get(j);
                double childNodeWidth = child.keys.size() * NODE_WIDTH;
                double childCenterX = childX + (childWidth - childNodeWidth) / 2 + childNodeWidth / 2;

                if (node.children.size() == 3) {
                    double parentCenterX = nodeX + (nodeWidth / 2);
                    if (j == 1) {
                        Line line = new Line(parentCenterX, y + NODE_HEIGHT, childCenterX, childY);
                        line.setStroke(Color.WHITE);
                        line.setStrokeWidth(3);
                        line.getStrokeDashArray().addAll(10.0, 5.0);
                        nodeToLinesMap.get(node).add(line);
                        root.getChildren().add(line);
                    } else {
                        double parentX = (j == 0) ? nodeX + NODE_WIDTH / 2 : nodeX + nodeWidth - NODE_WIDTH / 2;
                        Line line = new Line(parentX, y + NODE_HEIGHT, childCenterX, childY);
                        line.setStroke(Color.WHITE);
                        line.setStrokeWidth(3);
                        line.getStrokeDashArray().addAll(10.0, 5.0);
                        nodeToLinesMap.get(node).add(line);
                        root.getChildren().add(line);
                    }
                } else {
                    for (int k = 0; k < node.keys.size(); k++) {
                        if ((j == k && j < node.children.size() - 1) || (j == node.children.size() - 1 && k == node.keys.size() - 1)) {
                            double parentX = nodeX + k * NODE_WIDTH + NODE_WIDTH / 2;
                            Line line = new Line(parentX, y + NODE_HEIGHT, childCenterX, childY);
                            line.setStroke(Color.WHITE);
                            line.setStrokeWidth(3);
                            line.getStrokeDashArray().addAll(10.0, 5.0);
                            nodeToLinesMap.get(node).add(line);
                            root.getChildren().add(line);
                        }
                    }
                }

                drawNode(child, childX, childY, childWidth);
                childX += childWidth;
            }
        }
    }

    private void animateSearchPath(int value, boolean found, String foundMessage, Runnable onFinish) {
        // Disable buttons at the start of the animation
        if (insertButton != null) insertButton.setDisable(true);
        if (deleteButton != null) deleteButton.setDisable(true);
        if (searchButton != null) searchButton.setDisable(true);

        List<Node> searchPath = tree.getSearchPath();
        SequentialTransition sequentialTransition = new SequentialTransition();

        for (int i = 0; i < searchPath.size(); i++) {
            Node currentNode = searchPath.get(i);
            Rectangle rect = nodeToRectangleMap.get(currentNode);
            if (rect == null) continue;

            PauseTransition highlightNode = new PauseTransition(Duration.seconds(0.35));
            highlightNode.setOnFinished(e -> {
                rect.setStroke(Color.RED);
                rect.setStrokeWidth(3);
            });
            sequentialTransition.getChildren().add(highlightNode);

            if (i < searchPath.size() - 1) {
                Node nextNode = searchPath.get(i + 1);
                List<Line> lines = nodeToLinesMap.get(currentNode);
                if (lines == null || lines.isEmpty()) continue;
                Line lineToHighlight = null;
                for (Line line : lines) {
                    double lineEndX = line.getEndX();
                    Rectangle nextRect = nodeToRectangleMap.get(nextNode);
                    if (nextRect == null) continue;
                    double nextRectCenterX = nextRect.getX() + nextRect.getWidth() / 2;
                    if (Math.abs(lineEndX - nextRectCenterX) < 1) {
                        lineToHighlight = line;
                        break;
                    }
                }

                if (lineToHighlight != null) {
                    Line finalLine = lineToHighlight;
                    PauseTransition highlightLine = new PauseTransition(Duration.seconds(0.35));
                    highlightLine.setOnFinished(e -> {
                        finalLine.setStroke(Color.RED);
                        finalLine.setStrokeWidth(5);
                    });
                    sequentialTransition.getChildren().add(highlightLine);
                }
            }
        }

        PauseTransition highlightValue = new PauseTransition(Duration.seconds(0.35));
        highlightValue.setOnFinished(e -> {
            if (found) {
                highlightNode(value);
            }
        });
        sequentialTransition.getChildren().add(highlightValue);

        sequentialTransition.setOnFinished(e -> {
            String message = found ? (foundMessage != null ? foundMessage : "Element " + value + " value is found") : "Element " + value + " not found";
            showMessage(message);
            for (int i = 0; i < searchPath.size() - (found ? 1 : 0); i++) {
                Node node = searchPath.get(i);
                Rectangle rect = nodeToRectangleMap.get(node);
                if (rect != null) {
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(3);
                }
                List<Line> lines = nodeToLinesMap.get(node);
                if (lines != null) {
                    for (Line line : lines) {
                        line.setStroke(Color.WHITE);
                        line.setStrokeWidth(3);
                    }
                }
            }
            // Re-enable buttons after the animation completes
            if (insertButton != null) insertButton.setDisable(false);
            if (deleteButton != null) deleteButton.setDisable(false);
            if (searchButton != null) searchButton.setDisable(false);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        sequentialTransition.play();
    }

    private void animateInsertPath(int value, Runnable onFinish) {
        // Disable buttons at the start of the animation
        if (insertButton != null) insertButton.setDisable(true);
        if (deleteButton != null) deleteButton.setDisable(true);
        if (searchButton != null) searchButton.setDisable(true);

        tree.search(value);
        List<Node> insertPath = tree.getSearchPath();
        SequentialTransition sequentialTransition = new SequentialTransition();

        for (int i = 0; i < insertPath.size(); i++) {
            Node currentNode = insertPath.get(i);
            Rectangle rect = nodeToRectangleMap.get(currentNode);
            if (rect == null) continue;

            PauseTransition highlightNode = new PauseTransition(Duration.seconds(0.35));
            highlightNode.setOnFinished(e -> {
                rect.setStroke(Color.GREEN);
                rect.setStrokeWidth(3);
            });
            sequentialTransition.getChildren().add(highlightNode);

            if (i < insertPath.size() - 1) {
                Node nextNode = insertPath.get(i + 1);
                List<Line> lines = nodeToLinesMap.get(currentNode);
                if (lines == null || lines.isEmpty()) continue;
                Line lineToHighlight = null;
                for (Line line : lines) {
                    double lineEndX = line.getEndX();
                    Rectangle nextRect = nodeToRectangleMap.get(nextNode);
                    if (nextRect == null) continue;
                    double nextRectCenterX = nextRect.getX() + nextRect.getWidth() / 2;
                    if (Math.abs(lineEndX - nextRectCenterX) < 1) {
                        lineToHighlight = line;
                        break;
                    }
                }

                if (lineToHighlight != null) {
                    Line finalLine = lineToHighlight;
                    PauseTransition highlightLine = new PauseTransition(Duration.seconds(0.35));
                    highlightLine.setOnFinished(e -> {
                        finalLine.setStroke(Color.GREEN);
                        finalLine.setStrokeWidth(5);
                    });
                    sequentialTransition.getChildren().add(highlightLine);
                }
            } else {
                PauseTransition highlightFinalNode = new PauseTransition(Duration.seconds(0.35));
                highlightFinalNode.setOnFinished(e -> {
                    rect.setStroke(Color.GREEN);
                    rect.setStrokeWidth(5);
                });
                sequentialTransition.getChildren().add(highlightFinalNode);
            }
        }

        sequentialTransition.setOnFinished(e -> {
            showMessage("Element " + value + " is inserted");
            for (Node node : insertPath) {
                Rectangle rect = nodeToRectangleMap.get(node);
                if (rect != null) {
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(3);
                }
                List<Line> lines = nodeToLinesMap.get(node);
                if (lines != null) {
                    for (Line line : lines) {
                        line.setStroke(Color.WHITE);
                        line.setStrokeWidth(3);
                    }
                }
            }
            // Re-enable buttons after the animation completes
            if (insertButton != null) insertButton.setDisable(false);
            if (deleteButton != null) deleteButton.setDisable(false);
            if (searchButton != null) searchButton.setDisable(false);
            if (onFinish != null) {
                onFinish.run();
            }
        });

        sequentialTransition.play();
    }

    private void highlightNode(int value) {
        Label label = keyToLabelMap.get(value);
        if (label != null) {
            Rectangle rect = (Rectangle) label.getUserData();
            if (rect != null) {
                rect.setStroke(Color.RED);
                rect.setStrokeWidth(3);
            }
            label.setTextFill(Color.RED);
            label.setStyle("-fx-font-weight: bold;");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> {
                if (rect != null) {
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(3);
                }
                label.setTextFill(Color.WHITE);
                label.setStyle("-fx-font-weight: normal;");
            });
            pause.play();
        }
    }

    private double calculateTreeWidth(Node node) {
        if (node.isLeaf) {
            return node.keys.size() * HORIZONTAL_SPACING;
        }
        double width = 0;
        for (Node child : node.children) {
            width += calculateTreeWidth(child);
        }
        return Math.max(width, node.keys.size() * HORIZONTAL_SPACING);
    }

    private void showMessage(String message) {
        if (messageLabel == null) return;
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            messageLabel.setText("");
            messageLabel.setVisible(false);
        });
        pause.play();
    }

    private static class TreeState {
        Node root;

        TreeState(Node root) {
            this.root = deepCopy(root);
        }

        private Node deepCopy(Node node) {
            if (node == null) return null;
            Node copy = new Node(node.isLeaf);
            copy.keys.addAll(node.keys);
            for (Node child : node.children) {
                copy.children.add(deepCopy(child));
            }
            return copy;
        }
    }
}