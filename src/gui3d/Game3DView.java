package gui3d;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import logic.CheckDetector;
import logic.GameStateChecker;
import logic.MinimaxAI;
import logic.MoveOption;
import logic.MoveValidator;
import model.Board;
import model.PlayerColor;
import model.Position;
import model.pieces.Piece;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Game3DView {

    private static final int TILE_SIZE = 70;
    private static final int BOARD_SIZE = 8;
    private static final double TILE_HEIGHT = 7;
    private static final Duration MOVE_DURATION = Duration.millis(280);

    // Tavolina top ≈ Y=40 | Dyshemeja = Y=138
    // Karrigia seat Y=CHAIR_Y → siperfaqja e uleses ≈ CHAIR_Y - 6
    private static final double CHAIR_Y = 62.0;
    private static final double AVATAR_Z_DIST = 365.0;
    private static final double CHAIR_Z_DIST = 400.0;

    private final BorderPane root = new BorderPane();
    private final Group world = new Group();
    private final Group cameraPivot = new Group();

    private SubScene subScene;
    private PerspectiveCamera camera;
    private Rotate rotateX;
    private Rotate rotateY;

    private double mouseOldX, mouseOldY;

    private Board board;
    private PlayerColor currentTurn = PlayerColor.WHITE;

    private final PlayerColor humanColor = PlayerColor.WHITE;
    private final PlayerColor aiColor = PlayerColor.BLACK;

    private boolean gameOver = false;
    private boolean aiThinking = false;
    private boolean animationRunning = false;

    private final MinimaxAI minimaxAI = new MinimaxAI();
    private int aiDepth = 3;

    private final Label titleLabel = new Label("Chess 3D");
    private final Label statusLabel = new Label("Zgjidh nje figure");
    private final Label turnLabel = new Label();
    private final Label playerInfoLabel = new Label("Lojtari: PLAYER (White)");
    private final Label aiInfoLabel = new Label("Kundershtari: AI (Black)");
    private final Label aiStatusLabel = new Label("AI Status: Ready");
    private final Label gameStateLabel = new Label("Gjendja: Ne vazhdim");

    private final ObservableList<String> moveHistory = FXCollections.observableArrayList();
    private final ListView<String> moveHistoryList = new ListView<>(moveHistory);

    private final Map<String, Node> pieceNodes = new HashMap<>();
    private final Map<String, Box> tileNodes = new HashMap<>();
    private final Set<String> legalMoveKeys = new HashSet<>();

    private Group playerAvatar;
    private Group aiAvatar;
    private Timeline playerIdleTimeline;
    private Timeline aiIdleTimeline;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public Scene createScene() {
        board = new Board();

        titleLabel.setFont(Font.font(23));
        titleLabel.setTextFill(Color.WHITE);
        turnLabel.setTextFill(Color.LIGHTGREEN);
        statusLabel.setTextFill(Color.KHAKI);
        turnLabel.setFont(Font.font(17));
        statusLabel.setFont(Font.font(17));
        updateTurnLabel();

        Button resetButton = new Button("Restart");
        resetButton.setOnAction(e -> resetGame());

        Button easyButton = new Button("AI Depth 2");
        easyButton.setOnAction(e -> {
            aiDepth = 2;
            aiStatusLabel.setText("AI Status: Depth 2");
            root.requestFocus();
        });

        Button normalButton = new Button("AI Depth 3");
        normalButton.setOnAction(e -> {
            aiDepth = 3;
            aiStatusLabel.setText("AI Status: Depth 3");
            root.requestFocus();
        });

        Button defaultViewButton = new Button("Default View");
        defaultViewButton.setOnAction(e -> {
            setDefaultView();
            root.requestFocus();
        });

        Button topViewButton = new Button("Top View");
        topViewButton.setOnAction(e -> {
            setTopView();
            root.requestFocus();
        });

        HBox topBar = new HBox(12, titleLabel, turnLabel, resetButton, easyButton,
                normalButton, defaultViewButton, topViewButton, statusLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: linear-gradient(to right,#151515,#222222);");

        buildSubScene();

        root.setTop(topBar);
        root.setCenter(new StackPane(subScene));
        root.setRight(buildSidePanel());

        return new Scene(root, 1450, 850, true);
    }

    private VBox buildSidePanel() {
        Label sideTitle = new Label("Game Panel");
        sideTitle.setFont(Font.font(22));
        playerInfoLabel.setFont(Font.font(16));
        aiInfoLabel.setFont(Font.font(16));
        aiStatusLabel.setFont(Font.font(16));
        gameStateLabel.setFont(Font.font(16));

        Label historyTitle = new Label("Historia e levizjeve");
        historyTitle.setFont(Font.font(18));

        moveHistoryList.setPrefWidth(260);
        moveHistoryList.setPrefHeight(520);

        VBox panel = new VBox(12, sideTitle, playerInfoLabel, aiInfoLabel,
                aiStatusLabel, gameStateLabel, historyTitle, moveHistoryList);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color:#f4f6f8;-fx-border-color:#c7cdd3;-fx-border-width:0 0 0 1;");

        return panel;
    }

    public void requestFocus() {
        root.requestFocus();
    }

    private void resetGame() {
        if (animationRunning) return;

        stopAvatarAnimations();

        board.resetBoard();
        currentTurn = PlayerColor.WHITE;
        selectedRow = -1;
        selectedCol = -1;
        legalMoveKeys.clear();
        moveHistory.clear();
        gameOver = false;
        aiThinking = false;

        rebuildWorld();
        updateTurnLabel();
        lockAvatarY(playerAvatar);
        lockAvatarY(aiAvatar);

        statusLabel.setText("Loja u rifillua");
        aiStatusLabel.setText("AI Status: Ready");
        gameStateLabel.setText("Gjendja: Ne vazhdim");

        setDefaultView();
        root.requestFocus();
    }

    private void buildSubScene() {
        rebuildWorld();

        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(9000);
        camera.setFieldOfView(45);

        rotateX = new Rotate(0, Rotate.X_AXIS);
        rotateY = new Rotate(0, Rotate.Y_AXIS);

        cameraPivot.getChildren().clear();
        cameraPivot.getChildren().add(camera);
        cameraPivot.getTransforms().setAll(rotateX, rotateY);

        Group root3D = new Group(world, cameraPivot);

        subScene = new SubScene(root3D, 1200, 760, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(30, 20, 12));
        subScene.setCamera(camera);
        subScene.widthProperty().bind(root.widthProperty().subtract(280));
        subScene.heightProperty().bind(root.heightProperty().subtract(55));

        setDefaultView();
        enableMouseControl();
    }

    private void setDefaultView() {
        rotateX.setAngle(-22);
        rotateY.setAngle(0);
        camera.setTranslateX(0);
        camera.setTranslateY(-80);
        camera.setTranslateZ(-1100);
    }

    private void setTopView() {
        rotateX.setAngle(-80);
        rotateY.setAngle(0);
        camera.setTranslateX(0);
        camera.setTranslateY(-60);
        camera.setTranslateZ(-1300);
    }

    private void enableMouseControl() {
        subScene.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        subScene.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !animationRunning) {
                double dx = e.getSceneX() - mouseOldX;
                double dy = e.getSceneY() - mouseOldY;

                rotateY.setAngle(rotateY.getAngle() + dx * 0.3);
                rotateX.setAngle(rotateX.getAngle() - dy * 0.3);

                if (rotateX.getAngle() < -85) rotateX.setAngle(-85);
                if (rotateX.getAngle() > -5) rotateX.setAngle(-5);

                mouseOldX = e.getSceneX();
                mouseOldY = e.getSceneY();
            }
        });

        subScene.setOnScroll(e -> {
            if (animationRunning) return;

            double newZ = camera.getTranslateZ() + (e.getDeltaY() > 0 ? 80 : -80);
            if (newZ > -400) newZ = -400;
            if (newZ < -2200) newZ = -2200;

            camera.setTranslateZ(newZ);
        });
    }

    private void rebuildWorld() {
        stopAvatarAnimations();

        world.getChildren().clear();
        pieceNodes.clear();
        tileNodes.clear();

        buildRoomEnvironment();
        buildTable();
        buildBoardBase();
        buildBoard3D();
        buildCoordinates();
        buildPieces3D();
        buildSeatedAvatars();
        addLights();

        startIdleAnimation();
    }

    private void buildRoomEnvironment() {
        PhongMaterial wallWood = textureMaterial(Color.rgb(55, 32, 18), Color.rgb(120, 70, 34), 3, 128);
        PhongMaterial darkWood = textureMaterial(Color.rgb(45, 25, 14), Color.rgb(92, 54, 27), 3, 128);
        PhongMaterial floorWood = textureMaterial(Color.rgb(64, 38, 22), Color.rgb(135, 82, 42), 3, 160);
        PhongMaterial goldFrame = textureMaterial(Color.rgb(130, 91, 32), Color.rgb(225, 178, 76), 2, 96);
        PhongMaterial carpetMat = textureMaterial(Color.rgb(72, 26, 22), Color.rgb(150, 91, 58), 1, 128);
        addPainting(-1300, -250, 1768, 360, 250, goldFrame,
                Color.rgb(60, 45, 35),
                Color.rgb(170, 120, 70),
                "battle");

        addPainting(1300, -250, 1768, 360, 250, goldFrame,
                Color.rgb(35, 55, 75),
                Color.rgb(150, 150, 95),
                "castle");

        addPainting(-1750, -250, 1768, 300, 220, goldFrame,
                Color.rgb(80, 45, 45),
                Color.rgb(180, 100, 80),
                "battle");

        addPainting(1750, -250, 1768, 300, 220, goldFrame,
                Color.rgb(45, 70, 55),
                Color.rgb(150, 130, 80),
                "castle");

        Box floor = new Box(5000, 20, 5000);
        floor.setTranslateY(138);
        floor.setTranslateZ(40);
        floor.setMaterial(floorWood);

        Box backWall = new Box(5000, 1300, 24);
        backWall.setTranslateY(-260);
        backWall.setTranslateZ(1800);
        backWall.setMaterial(wallWood);

        Box leftWall = new Box(24, 1300, 5000);
        leftWall.setTranslateX(-2200);
        leftWall.setTranslateY(-260);
        leftWall.setTranslateZ(40);
        leftWall.setMaterial(wallWood);

        Box rightWall = new Box(24, 1300, 5000);
        rightWall.setTranslateX(2200);
        rightWall.setTranslateY(-260);
        rightWall.setTranslateZ(40);
        rightWall.setMaterial(wallWood);

        Box carpet = new Box(950, 5, 950);
        carpet.setTranslateY(126);
        carpet.setMaterial(carpetMat);

        for (int i = -2; i <= 2; i++) {
            Box vp = new Box(18, 520, 18);
            vp.setTranslateX(i * 360);
            vp.setTranslateY(-260);
            vp.setTranslateZ(1782);
            vp.setMaterial(darkWood);
            world.getChildren().add(vp);
        }

        Box topRail = new Box(5000, 22, 18);
        topRail.setTranslateY(-590);
        topRail.setTranslateZ(1780);
        topRail.setMaterial(darkWood);

        Box bottomRail = new Box(5000, 24, 18);
        bottomRail.setTranslateY(80);
        bottomRail.setTranslateZ(1780);
        bottomRail.setMaterial(darkWood);

        addPainting(-520, -260, 1768, 440, 290, goldFrame, Color.rgb(72, 55, 40), Color.rgb(180, 145, 85), "battle");
        addPainting(520, -260, 1768, 440, 290, goldFrame, Color.rgb(45, 70, 70), Color.rgb(160, 145, 95), "castle");
        addBookshelf(0, -230, 1775);
        addWallLamp(-1650, -210, 1765);
        addWallLamp(1650, -210, 1765);

        addPhotoPainting(-750, -210, 1765, 620, 380, goldFrame,
                "/paintings/chess1.jpg");

        addPhotoPainting(0, -210, 1765, 680, 420, goldFrame,
                "/paintings/castle.jpg");

        addPhotoPainting(750, -210, 1765, 620, 380, goldFrame,
                "/paintings/abstract.jpg");

        world.getChildren().addAll(floor, carpet, backWall, leftWall, rightWall, topRail, bottomRail);
    }

    private void addPainting(double x, double y, double z, double w, double h,
                             PhongMaterial frameMat, Color c1, Color c2, String type) {
        Box fb = new Box(w + 34, h + 34, 16);
        fb.setTranslateX(x);
        fb.setTranslateY(y);
        fb.setTranslateZ(z - 6);
        fb.setMaterial(frameMat);

        Box cv = new Box(w, h, 8);
        cv.setTranslateX(x);
        cv.setTranslateY(y);
        cv.setTranslateZ(z + 2);
        cv.setMaterial(textureMaterial(c1, c2, 1, 160));

        Group art = new Group();

        if ("battle".equals(type)) {

            for (int i = 0; i < 5; i++) {

                PhongMaterial mat1 = new PhongMaterial();
                mat1.setDiffuseColor(Color.rgb(45 + i * 20, 36, 28));

                Cylinder r = new Cylinder(6, 38);
                r.setTranslateX(x - 120 + i * 55);
                r.setTranslateY(y + 25);
                r.setTranslateZ(z + 8);
                r.setMaterial(mat1);

                PhongMaterial mat2 = new PhongMaterial();
                mat2.setDiffuseColor(Color.rgb(120, 38 + i * 10, 26));

                Box f = new Box(38, 20, 4);
                f.setTranslateX(x - 100 + i * 55);
                f.setTranslateY(y - 25);
                f.setTranslateZ(z + 9);
                f.setMaterial(mat2);

                art.getChildren().addAll(r, f);
            }

        } else {

            PhongMaterial castleMat = new PhongMaterial();
            castleMat.setDiffuseColor(Color.rgb(100, 100, 90));

            Box castle = new Box(90, 100, 5);
            castle.setTranslateX(x + 60);
            castle.setTranslateY(y + 10);
            castle.setTranslateZ(z + 9);
            castle.setMaterial(castleMat);

            PhongMaterial towerMat = new PhongMaterial();
            towerMat.setDiffuseColor(Color.rgb(88, 88, 82));

            Cylinder t1 = new Cylinder(18, 120);
            t1.setTranslateX(x + 10);
            t1.setTranslateY(y + 10);
            t1.setTranslateZ(z + 10);
            t1.setMaterial(towerMat);

            Cylinder t2 = new Cylinder(18, 120);
            t2.setTranslateX(x + 110);
            t2.setTranslateY(y + 10);
            t2.setTranslateZ(z + 10);
            t2.setMaterial(towerMat);

            PhongMaterial hillMat = new PhongMaterial();
            hillMat.setDiffuseColor(Color.rgb(54, 80, 55));

            Box hills = new Box(220, 45, 5);
            hills.setTranslateX(x - 10);
            hills.setTranslateY(y + 55);
            hills.setTranslateZ(z + 8);
            hills.setMaterial(hillMat);

            // ☀️ dielli
            PhongMaterial sunMat = new PhongMaterial();
            sunMat.setDiffuseColor(Color.rgb(255, 200, 120));

            Sphere sun = new Sphere(18);
            sun.setTranslateX(x + 80);
            sun.setTranslateY(y - 40);
            sun.setTranslateZ(z + 10);
            sun.setMaterial(sunMat);

            art.getChildren().addAll(hills, castle, t1, t2, sun);
        }}

    private void addBookshelf(double x, double y, double z) {
        PhongMaterial sm = textureMaterial(Color.rgb(48, 25, 12), Color.rgb(98, 58, 28), 3, 96);

        Box shelf = new Box(170, 300, 28);
        shelf.setTranslateX(x);
        shelf.setTranslateY(y);
        shelf.setTranslateZ(z + 10);
        shelf.setMaterial(sm);
        world.getChildren().add(shelf);

        for (int row = 0; row < 4; row++) {
            Box rail = new Box(155, 9, 35);
            rail.setTranslateX(x);
            rail.setTranslateY(y - 110 + row * 65);
            rail.setTranslateZ(z + 26);
            rail.setMaterial(sm);
            world.getChildren().add(rail);

            for (int i = 0; i < 8; i++) {
                Box book = new Box(12, 42 + (i % 3) * 8, 20);
                book.setTranslateX(x - 65 + i * 18);
                book.setTranslateY(y - 135 + row * 65);
                book.setTranslateZ(z + 45);
                book.setMaterial(matte(Color.rgb(45 + i * 15, 35 + row * 15, 32 + i * 7)));
                world.getChildren().add(book);
            }
        }
    }

    private void addWallLamp(double x, double y, double z) {
        Cylinder base = new Cylinder(18, 8);
        base.setTranslateX(x);
        base.setTranslateY(y);
        base.setTranslateZ(z + 12);
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setMaterial(matte(Color.rgb(160, 120, 55)));

        Box arm = new Box(10, 10, 70);
        arm.setTranslateX(x);
        arm.setTranslateY(y + 12);
        arm.setTranslateZ(z + 45);
        arm.setMaterial(matte(Color.rgb(160, 120, 55)));

        Cylinder shade = new Cylinder(34, 50);
        shade.setTranslateX(x);
        shade.setTranslateY(y + 46);
        shade.setTranslateZ(z + 85);
        shade.setMaterial(matte(Color.rgb(255, 220, 160)));

        PointLight lamp = new PointLight(Color.rgb(255, 205, 145));
        lamp.setTranslateX(x);
        lamp.setTranslateY(y + 35);
        lamp.setTranslateZ(z + 85);

        world.getChildren().addAll(base, arm, shade, lamp);
    }

    private void buildTable() {
        Box top = new Box(620, 28, 620);
        top.setTranslateY(54);
        top.setMaterial(textureMaterial(Color.rgb(105, 62, 30), Color.rgb(170, 112, 58), 3, 96));

        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                Box leg = new Box(42, 165, 42);
                leg.setTranslateX(sx * 255);
                leg.setTranslateY(145);
                leg.setTranslateZ(sz * 255);
                leg.setMaterial(textureMaterial(Color.rgb(70, 38, 22), Color.rgb(125, 76, 38), 3, 96));
                world.getChildren().add(leg);
            }
        }

        world.getChildren().add(top);
    }

    private void buildBoardBase(){

        double tableTopY = 54 - (28 / 2.0); // = 40

        double baseHeight = 8;

        Box base = new Box(
                BOARD_SIZE * TILE_SIZE + 30,
                baseHeight,
                BOARD_SIZE * TILE_SIZE + 30
        );

        // 🔥 FIX KRYESOR
        base.setTranslateY(tableTopY - baseHeight / 2.0);

        base.setMaterial(matte(Color.rgb(55,58,64)));

        world.getChildren().add(base);
    }

    private void buildBoard3D() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Box tile = new Box(TILE_SIZE, TILE_HEIGHT, TILE_SIZE);
                tile.setTranslateX(toX(col));
                tile.setTranslateZ(toZ(row));
                tile.setTranslateY(34.5);
                setTileDefaultMaterial(tile, row, col);

                final int r = row;
                final int c = col;
                tile.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) handleTileClick(r, c);
                });

                tileNodes.put(key(row, col), tile);
                world.getChildren().add(tile);
            }
        }
    }

    private void buildCoordinates() {
        for (int col = 0; col < BOARD_SIZE; col++) {
            Text t = createCoordinateText(String.valueOf((char) ('A' + col)));
            t.setTranslateX(toX(col) - 8);
            t.setTranslateY(28);
            t.setTranslateZ(toZ(8) - 8);
            world.getChildren().add(t);
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            Text t = createCoordinateText(String.valueOf(8 - row));
            t.setTranslateX(toX(8) - 8);
            t.setTranslateY(28);
            t.setTranslateZ(toZ(row) + 8);
            world.getChildren().add(t);
        }
    }

    private Text createCoordinateText(String value) {
        Text t = new Text(value);
        t.setFill(Color.rgb(28, 28, 28));
        t.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        t.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
        return t;
    }

    private void buildPieces3D() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) continue;

                Group p3d = Piece3DFactory.createPiece3D(piece);
                p3d.setScaleX(0.70);
                p3d.setScaleY(0.70);
                p3d.setScaleZ(0.70);
                p3d.setTranslateX(toX(col));
                p3d.setTranslateY(29);
                p3d.setTranslateZ(toZ(row));

                final int r = row;
                final int c = col;
                p3d.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        handleTileClick(r, c);
                        e.consume();
                    }
                });

                pieceNodes.put(key(row, col), p3d);
                world.getChildren().add(p3d);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // AVATARET + KARRIGET - VERSION I KORRIGJUAR
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildSeatedAvatars() {
        Group chair1 = createChair();
        chair1.setTranslateX(0);
        chair1.setTranslateY(CHAIR_Y);
        chair1.setTranslateZ(CHAIR_Z_DIST);
        chair1.setRotationAxis(Rotate.Y_AXIS);
        chair1.setRotate(0);

        Group chair2 = createChair();
        chair2.setTranslateX(0);
        chair2.setTranslateY(CHAIR_Y);
        chair2.setTranslateZ(-CHAIR_Z_DIST);
        chair2.setRotationAxis(Rotate.Y_AXIS);
        chair2.setRotate(180);

        world.getChildren().addAll(chair1, chair2);

        playerAvatar = loadObjAvatarSafe("src/resources/models/player1.obj", true);
        aiAvatar = loadObjAvatarSafe("src/resources/models/player2.obj", false);

        scaleAndPlaceAvatar(playerAvatar, AVATAR_Z_DIST, 180.0);
        scaleAndPlaceAvatar(aiAvatar, -AVATAR_Z_DIST, 0.0);

        world.getChildren().addAll(playerAvatar, aiAvatar);


    }

    private void scaleAndPlaceAvatar(Group avatar, double zPos, double rotY) {
        javafx.geometry.Bounds b = avatar.getBoundsInLocal();

        double modelH = b.getHeight();
        double modelD = b.getDepth();
        double modelW = b.getWidth();

        double maxDim = Math.max(modelH, Math.max(modelW, modelD));
        double targetSize = 200.0;
        double scale = maxDim > 0.001 ? targetSize / maxDim : 1.0;

        avatar.setScaleX(scale);
        avatar.setScaleY(scale);
        avatar.setScaleZ(scale);

        avatar.setTranslateX(0);

        double seatSurfaceY = CHAIR_Y - 6.0;
        double sitCorrection = 30;
        double avatarY = seatSurfaceY - sitCorrection;

        avatar.setTranslateY(avatarY);

        double forwardOffset = (zPos > 0) ? -12.0 : 12.0;
        avatar.setTranslateZ(zPos + forwardOffset);

        avatar.setRotationAxis(Rotate.Y_AXIS);
        avatar.setRotate(rotY);

        System.out.printf(
                "Avatar vendosur: Z=%.0f | scale=%.3f | Y=%.2f%n",
                zPos + forwardOffset, scale, avatarY
        );
    }

    private Group loadObjAvatarSafe(String path, boolean isPlayer) {
        try {
            Group g = loadObjAvatar(path);
            long meshCount = g.getChildren().stream().filter(n -> n instanceof MeshView).count();

            System.out.println("OBJ meshes: " + meshCount + " nga " + path);

            if (meshCount == 0) {
                System.out.println("OBJ bosh → fallback");
                return createDetailedFallback(isPlayer);
            }

            return g;
        } catch (Exception e) {
            System.out.println("OBJ deshtoi (" + e.getMessage() + ") → fallback");
            return createDetailedFallback(isPlayer);
        }
    }

    private Group loadObjAvatar(String path) throws Exception {
        Group avatar = new Group();
        InputStream is = new FileInputStream(path);

        List<float[]> vertices = new ArrayList<>();
        Map<String, List<int[]>> facesByMaterial = new LinkedHashMap<>();
        Map<String, Color> materialColors = new HashMap<>();

        String currentMaterial = "default";
        facesByMaterial.put(currentMaterial, new ArrayList<>());

        String objFolder = "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (slash >= 0) objFolder = path.substring(0, slash + 1);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("mtllib ")) {
                    materialColors.putAll(loadMtlColors(objFolder + line.substring(7).trim()));
                } else if (line.startsWith("usemtl ")) {
                    currentMaterial = line.substring(7).trim();
                    facesByMaterial.putIfAbsent(currentMaterial, new ArrayList<>());
                } else if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    if (p.length >= 4) {
                        vertices.add(new float[]{
                                Float.parseFloat(p[1]),
                                Float.parseFloat(p[2]),
                                Float.parseFloat(p[3])
                        });
                    }
                } else if (line.startsWith("f ")) {
                    String[] p = line.split("\\s+");

                    if (p.length >= 4) {
                        int v1 = parseObjVertexIndex(p[1]);

                        for (int i = 2; i < p.length - 1; i++) {
                            facesByMaterial.get(currentMaterial).add(new int[]{
                                    v1,
                                    parseObjVertexIndex(p[i]),
                                    parseObjVertexIndex(p[i + 1])
                            });
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, List<int[]>> entry : facesByMaterial.entrySet()) {
            List<int[]> faces = entry.getValue();
            if (faces.isEmpty()) continue;

            TriangleMesh mesh = new TriangleMesh();

            for (float[] v : vertices) {
                mesh.getPoints().addAll(v[0], -v[1], v[2]);
            }

            mesh.getTexCoords().addAll(0, 0);

            for (int[] f : faces) {
                mesh.getFaces().addAll(f[0], 0, f[1], 0, f[2], 0);
            }

            MeshView mv = new MeshView(mesh);
            mv.setCullFace(CullFace.NONE);

            Color color = materialColors.getOrDefault(entry.getKey(), guessAvatarColor(entry.getKey(), path));

            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(color);
            mat.setSpecularColor(Color.rgb(35, 35, 35));
            mat.setSpecularPower(6);

            mv.setMaterial(mat);
            avatar.getChildren().add(mv);
        }

        return avatar;
    }

    private Group createDetailedFallback(boolean isPlayer) {
        Group g = new Group();

        Color bodyColor = isPlayer ? Color.rgb(45, 90, 170) : Color.rgb(170, 38, 38);
        Color pantsColor = isPlayer ? Color.rgb(30, 55, 110) : Color.rgb(110, 25, 25);
        Color skinColor = Color.rgb(225, 185, 145);
        Color hairColor = isPlayer ? Color.rgb(50, 35, 20) : Color.rgb(20, 20, 20);
        Color shoeColor = Color.rgb(28, 22, 16);

        Sphere head = new Sphere(26);
        head.setTranslateY(-178);
        head.setMaterial(matte(skinColor));

        Sphere hair = new Sphere(28);
        hair.setTranslateY(-186);
        hair.setMaterial(matte(hairColor));

        Cylinder neck = new Cylinder(9, 20);
        neck.setTranslateY(-150);
        neck.setMaterial(matte(skinColor));

        Box torso = new Box(62, 80, 34);
        torso.setTranslateY(-100);
        torso.setMaterial(matte(bodyColor));

        // Krahet - te perkulur perpara mbi tavoline
        Cylinder armL = new Cylinder(9, 62);
        armL.setTranslateX(-38);
        armL.setTranslateY(-88);
        armL.setTranslateZ(-18);
        armL.setRotationAxis(Rotate.X_AXIS);
        armL.setRotate(72);
        armL.setMaterial(matte(bodyColor));

        Cylinder armR = new Cylinder(9, 62);
        armR.setTranslateX(38);
        armR.setTranslateY(-88);
        armR.setTranslateZ(-18);
        armR.setRotationAxis(Rotate.X_AXIS);
        armR.setRotate(72);
        armR.setMaterial(matte(bodyColor));

        // Parakrahet mbi tavoline
        Cylinder foreArmL = new Cylinder(8, 58);
        foreArmL.setTranslateX(-34);
        foreArmL.setTranslateY(-58);
        foreArmL.setTranslateZ(-52);
        foreArmL.setRotationAxis(Rotate.X_AXIS);
        foreArmL.setRotate(88);
        foreArmL.setMaterial(matte(bodyColor));

        Cylinder foreArmR = new Cylinder(8, 58);
        foreArmR.setTranslateX(34);
        foreArmR.setTranslateY(-58);
        foreArmR.setTranslateZ(-52);
        foreArmR.setRotationAxis(Rotate.X_AXIS);
        foreArmR.setRotate(88);
        foreArmR.setMaterial(matte(bodyColor));

        // Duart mbi tavoline
        Sphere handL = new Sphere(10);
        handL.setTranslateX(-34);
        handL.setTranslateY(-55);
        handL.setTranslateZ(-85);
        handL.setMaterial(matte(skinColor));

        Sphere handR = new Sphere(10);
        handR.setTranslateX(34);
        handR.setTranslateY(-55);
        handR.setTranslateZ(-85);
        handR.setMaterial(matte(skinColor));

        Box waist = new Box(58, 18, 32);
        waist.setTranslateY(-54);
        waist.setMaterial(matte(pantsColor));

        // Kofshe horizontale mbi sedilje
        Box thighL = new Box(24, 14, 78);
        thighL.setTranslateX(-16);
        thighL.setTranslateY(-31);
        thighL.setTranslateZ(18);
        thighL.setMaterial(matte(pantsColor));

        Box thighR = new Box(24, 14, 78);
        thighR.setTranslateX(16);
        thighR.setTranslateY(-31);
        thighR.setTranslateZ(18);
        thighR.setMaterial(matte(pantsColor));

        // Kercinj te perkulur poshte
        Cylinder shinL = new Cylinder(9, 66);
        shinL.setTranslateX(-16);
        shinL.setTranslateY(3);
        shinL.setTranslateZ(48);
        shinL.setRotationAxis(Rotate.X_AXIS);
        shinL.setRotate(12);
        shinL.setMaterial(matte(pantsColor));

        Cylinder shinR = new Cylinder(9, 66);
        shinR.setTranslateX(16);
        shinR.setTranslateY(3);
        shinR.setTranslateZ(48);
        shinR.setRotationAxis(Rotate.X_AXIS);
        shinR.setRotate(12);
        shinR.setMaterial(matte(pantsColor));

        Box shoeL = new Box(24, 12, 38);
        shoeL.setTranslateX(-16);
        shoeL.setTranslateY(33);
        shoeL.setTranslateZ(55);
        shoeL.setMaterial(matte(shoeColor));

        Box shoeR = new Box(24, 12, 38);
        shoeR.setTranslateX(16);
        shoeR.setTranslateY(33);
        shoeR.setTranslateZ(55);
        shoeR.setMaterial(matte(shoeColor));

        Sphere eyeL = new Sphere(4);
        eyeL.setTranslateX(-10);
        eyeL.setTranslateY(-182);
        eyeL.setTranslateZ(23);
        eyeL.setMaterial(matte(Color.WHITE));

        Sphere eyeR = new Sphere(4);
        eyeR.setTranslateX(10);
        eyeR.setTranslateY(-182);
        eyeR.setTranslateZ(23);
        eyeR.setMaterial(matte(Color.WHITE));

        Sphere pupL = new Sphere(2);
        pupL.setTranslateX(-10);
        pupL.setTranslateY(-182);
        pupL.setTranslateZ(26);
        pupL.setMaterial(matte(Color.rgb(30, 20, 10)));

        Sphere pupR = new Sphere(2);
        pupR.setTranslateX(10);
        pupR.setTranslateY(-182);
        pupR.setTranslateZ(26);
        pupR.setMaterial(matte(Color.rgb(30, 20, 10)));

        g.getChildren().addAll(
                shoeL, shoeR,
                shinL, shinR,
                thighL, thighR,
                waist, torso,
                armL, armR,
                foreArmL, foreArmR,
                handL, handR,
                neck, hair, head,
                eyeL, eyeR, pupL, pupR
        );

        System.out.println("Fallback avatar: " + (isPlayer ? "PLAYER (blu)" : "AI (kuq)"));
        return g;
    }

    private Map<String, Color> loadMtlColors(String mtlPath) {
        Map<String, Color> colors = new HashMap<>();
        String name = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mtlPath), StandardCharsets.UTF_8))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("newmtl ")) {
                    name = line.substring(7).trim();
                } else if (line.startsWith("Kd ") && name != null) {
                    String[] p = line.split("\\s+");

                    if (p.length >= 4) {
                        colors.put(name, new Color(
                                clamp01(Double.parseDouble(p[1])),
                                clamp01(Double.parseDouble(p[2])),
                                clamp01(Double.parseDouble(p[3])),
                                1
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("MTL nuk u lexua: " + mtlPath);
        }

        return colors;
    }

    private Color guessAvatarColor(String m, String path) {
        if (m == null) m = "";
        m = m.toLowerCase();

        if (m.contains("skin") || m.contains("face") || m.contains("head") ||
                m.contains("hand") || m.contains("arm") || m.contains("leg")) {
            return Color.rgb(220, 178, 142);
        }

        if (m.contains("hair")) return Color.rgb(55, 38, 25);

        if (m.contains("top") || m.contains("shirt") || m.contains("suit") || m.contains("cloth")) {
            return path.contains("player1") ? Color.rgb(62, 72, 82) : Color.rgb(80, 95, 130);
        }

        if (m.contains("pant") || m.contains("jean") || m.contains("bottom")) return Color.rgb(35, 45, 65);
        if (m.contains("shoe")) return Color.rgb(35, 28, 22);
        if (m.contains("eye")) return Color.rgb(230, 230, 220);

        return path.contains("player1") ? Color.rgb(190, 160, 130) : Color.rgb(95, 120, 160);
    }

    private double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    private int parseObjVertexIndex(String t) {
        return Integer.parseInt(t.split("/")[0]) - 1;
    }

    private Group createChair() {
        Group chair = new Group();

        PhongMaterial wood = matte(Color.rgb(78, 45, 24));
        PhongMaterial darkWood = matte(Color.rgb(45, 25, 14));

        // Sedilja. Qendra e sediljes eshte Y=0; siperfaqja e saj eshte Y=-6.
        Box seat = new Box(115, 12, 95);
        seat.setTranslateY(0);
        seat.setTranslateZ(0);
        seat.setMaterial(wood);

        // Mbeshtetesja mbrapa. E vendosur pak mbrapa sediljes qe avatari te mos futet brenda saj.
        Box back = new Box(115, 85, 12);
        back.setTranslateY(-42);
        back.setTranslateZ(48);
        back.setMaterial(wood);

        // Kembet perpara
        Box frontLeftLeg = new Box(10, 72, 10);
        frontLeftLeg.setTranslateX(-44);
        frontLeftLeg.setTranslateY(36);
        frontLeftLeg.setTranslateZ(-36);
        frontLeftLeg.setMaterial(darkWood);

        Box frontRightLeg = new Box(10, 72, 10);
        frontRightLeg.setTranslateX(44);
        frontRightLeg.setTranslateY(36);
        frontRightLeg.setTranslateZ(-36);
        frontRightLeg.setMaterial(darkWood);

        // Kembet mbrapa lidhen me mbeshtetesen.
        Box backLeftLeg = new Box(10, 105, 10);
        backLeftLeg.setTranslateX(-44);
        backLeftLeg.setTranslateY(18);
        backLeftLeg.setTranslateZ(42);
        backLeftLeg.setMaterial(darkWood);

        Box backRightLeg = new Box(10, 105, 10);
        backRightLeg.setTranslateX(44);
        backRightLeg.setTranslateY(18);
        backRightLeg.setTranslateZ(42);
        backRightLeg.setMaterial(darkWood);

        chair.getChildren().addAll(
                seat,
                back,
                frontLeftLeg,
                frontRightLeg,
                backLeftLeg,
                backRightLeg
        );

        return chair;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMACIONET
    // ═══════════════════════════════════════════════════════════════════════════
    private void animateAvatarSit(Group avatar) {
        if (avatar == null) return;

        double finalY = avatar.getTranslateY();

        TranslateTransition sit = new TranslateTransition(Duration.seconds(1.2), avatar);
        sit.setFromY(finalY - 80);
        sit.setToY(finalY);
        sit.setInterpolator(Interpolator.EASE_OUT);
        sit.play();
    }

    private void startBreathingAnimation(Group avatar) {
        if (avatar == null) return;

        ScaleTransition breathing = new ScaleTransition(Duration.seconds(2.4), avatar);
        breathing.setFromY(avatar.getScaleY());
        breathing.setToY(avatar.getScaleY() * 1.025);
        breathing.setAutoReverse(true);
        breathing.setCycleCount(Animation.INDEFINITE);
        breathing.setInterpolator(Interpolator.EASE_BOTH);
        breathing.play();
    }

    private void startIdleAnimation() {
        Group active = currentTurn == humanColor ? playerAvatar : aiAvatar;

        if (active == null || gameOver) return;

        double baseY = active.getTranslateY();
        double baseZ = active.getTranslateZ();
        double fwd = currentTurn == humanColor ? -12 : 12;

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(active.translateYProperty(), baseY, Interpolator.EASE_BOTH),
                        new KeyValue(active.translateZProperty(), baseZ, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(active.translateYProperty(), baseY - 4, Interpolator.EASE_BOTH),
                        new KeyValue(active.translateZProperty(), baseZ + fwd, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(active.translateYProperty(), baseY, Interpolator.EASE_BOTH),
                        new KeyValue(active.translateZProperty(), baseZ, Interpolator.EASE_BOTH))
        );

        tl.setCycleCount(Animation.INDEFINITE);

        if (currentTurn == humanColor) {
            playerIdleTimeline = tl;
        } else {
            aiIdleTimeline = tl;
        }

        tl.play();
    }

    private void playReachAnimation(Group avatar, boolean playerSide) {
        if (avatar == null || animationRunning) return;

        double baseZ = avatar.getTranslateZ();

        // shumë e vogël → nuk duket kërcim
        double forward = playerSide ? -6 : 6;

        Timeline reach = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(avatar.translateZProperty(), baseZ, Interpolator.EASE_BOTH)
                ),

                new KeyFrame(Duration.millis(160),
                        new KeyValue(avatar.translateZProperty(), baseZ + forward, Interpolator.EASE_OUT)
                ),

                new KeyFrame(Duration.millis(320),
                        new KeyValue(avatar.translateZProperty(), baseZ, Interpolator.EASE_IN)
                )
        );

        reach.play();
    }
    private void startAIThinkingAnimation() {
        if (aiAvatar == null) return;
        if (aiIdleTimeline != null) aiIdleTimeline.stop();

        double bZ = aiAvatar.getTranslateZ();

        aiIdleTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(aiAvatar.translateZProperty(), bZ)
                ),
                new KeyFrame(Duration.millis(350),
                        new KeyValue(aiAvatar.translateZProperty(), bZ + 5)
                ),
                new KeyFrame(Duration.millis(700),
                        new KeyValue(aiAvatar.translateZProperty(), bZ)
                )
        );

        aiIdleTimeline.setCycleCount(Animation.INDEFINITE);
        aiIdleTimeline.play();
    }
    private void stopAvatarAnimations() {
        if (playerIdleTimeline != null) {
            playerIdleTimeline.stop();
            playerIdleTimeline = null;
        }

        if (aiIdleTimeline != null) {
            aiIdleTimeline.stop();
            aiIdleTimeline = null;
        }
    }

    private void handleTileClick(int row, int col) {
        if (gameOver || aiThinking || animationRunning) return;

        if (currentTurn != humanColor) {
            statusLabel.setText("Po luan AI...");
            return;
        }

        Piece clicked = board.getPiece(row, col);

        if (selectedRow == -1) {
            selectPieceIfValid(row, col, clicked);
            return;
        }

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            statusLabel.setText("Zgjedhja u anulua");
            return;
        }

        if (legalMoveKeys.contains(key(row, col))) {
            animatePlayerMove(new Position(selectedRow, selectedCol), new Position(row, col));
            return;
        }

        if (clicked != null && clicked.getColor() == currentTurn) {
            selectPieceIfValid(row, col, clicked);
        } else {
            statusLabel.setText("Levizje e pavlefshme");
        }
    }

    private void selectPieceIfValid(int row, int col, Piece piece) {
        if (piece == null) {
            statusLabel.setText("Nuk ka figure");
            return;
        }

        if (piece.getColor() != currentTurn) {
            statusLabel.setText("Nuk eshte radha");
            return;
        }

        playReachAnimation(playerAvatar, true);

        List<Position> lm = MoveValidator.getLegalMoves(board, piece);

        clearSelection();

        selectedRow = row;
        selectedCol = col;

        highlightSelectedTile(row, col);
        highlightLegalMoves(lm);

        statusLabel.setText(lm.isEmpty() ? "Kjo figure nuk ka levizje" : "Figura: (" + row + "," + col + ")");
    }

    private void animatePlayerMove(Position from, Position to) {
        Piece piece = board.getPiece(from.getRow(), from.getCol());

        if (piece == null || piece.getColor() != currentTurn) return;

        boolean ok = MoveValidator.getLegalMoves(board, piece).stream().anyMatch(m -> m.equals(to));

        if (!ok) {
            statusLabel.setText("Levizje e pavlefshme");
            return;
        }

        Node mn = pieceNodes.get(key(from.getRow(), from.getCol()));

        if (mn == null) {
            finishPlayerMove(from, to);
            return;
        }

        animationRunning = true;
        clearSelection();

        Piece cap = board.getPiece(to.getRow(), to.getCol());

        if (cap != null) {
            Node cn = pieceNodes.get(key(to.getRow(), to.getCol()));
            if (cn != null) world.getChildren().remove(cn);
        }

        if (playerIdleTimeline != null) {
            playerIdleTimeline.stop();
            playerIdleTimeline = null;
        }

        Timeline sl = createMoveTimeline(mn, to);
        sl.setOnFinished(e -> finishPlayerMove(from, to));
        sl.play();
    }

    private void finishPlayerMove(Position from, Position to) {
        board.movePiece(from, to);
        addMoveToHistory("PLAYER", from, to);

        currentTurn = oppositeColor(currentTurn);
        animationRunning = false;

        rebuildWorld();
        updateTurnLabel();
        updateSidePanel();

        if (!evaluateGameState()) runAiMove();
    }

    private void runAiMove() {
        if (gameOver || currentTurn != aiColor || animationRunning) return;

        aiThinking = true;
        aiStatusLabel.setText("AI Status: Duke menduar...");
        statusLabel.setText("AI po mendon...");

        startAIThinkingAnimation();

        PauseTransition p = new PauseTransition(Duration.millis(700));
        p.setOnFinished(ev -> {
            MoveOption best = minimaxAI.chooseBestMove(board.copy(), aiColor, aiDepth);

            if (best == null) {
                aiThinking = false;
                aiStatusLabel.setText("AI: No moves");
                evaluateGameState();
                return;
            }

            animateAiMove(best);
        });

        p.play();
    }

    private void animateAiMove(MoveOption move) {
        Position from = move.getFrom();
        Position to = move.getTo();

        Node mn = pieceNodes.get(key(from.getRow(), from.getCol()));

        if (mn == null) {
            finishAiMove(from, to);
            return;
        }

        animationRunning = true;

        Piece cap = board.getPiece(to.getRow(), to.getCol());

        if (cap != null) {
            Node cn = pieceNodes.get(key(to.getRow(), to.getCol()));
            if (cn != null) world.getChildren().remove(cn);
        }

        if (aiIdleTimeline != null) {
            aiIdleTimeline.stop();
            aiIdleTimeline = null;
        }

        playReachAnimation(aiAvatar, false);

        Timeline sl = createMoveTimeline(mn, to);
        sl.setOnFinished(e -> finishAiMove(from, to));
        sl.play();
    }

    private void finishAiMove(Position from, Position to) {
        board.movePiece(from, to);
        addMoveToHistory("AI", from, to);

        currentTurn = humanColor;
        aiThinking = false;
        animationRunning = false;

        rebuildWorld();
        updateTurnLabel();
        updateSidePanel();
        evaluateGameState();
    }

    private Timeline createMoveTimeline(Node node, Position to) {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(node.translateXProperty(), node.getTranslateX()),
                        new KeyValue(node.translateYProperty(), node.getTranslateY()),
                        new KeyValue(node.translateZProperty(), node.getTranslateZ())),
                new KeyFrame(MOVE_DURATION.divide(2),
                        new KeyValue(node.translateYProperty(), -32, Interpolator.EASE_OUT)),
                new KeyFrame(MOVE_DURATION,
                        new KeyValue(node.translateXProperty(), toX(to.getCol()), Interpolator.EASE_BOTH),
                        new KeyValue(node.translateYProperty(), -6, Interpolator.EASE_IN),
                        new KeyValue(node.translateZProperty(), toZ(to.getRow()), Interpolator.EASE_BOTH))
        );
    }

    private boolean evaluateGameState() {
        PlayerColor s = currentTurn;

        if (GameStateChecker.isCheckmate(board, s)) {
            gameOver = true;
            statusLabel.setText("Checkmate! Fiton " + oppositeColor(s));
            gameStateLabel.setText("Gjendja: Checkmate");
            aiStatusLabel.setText("AI Status: Finished");
            return true;
        }

        if (GameStateChecker.isStalemate(board, s)) {
            gameOver = true;
            statusLabel.setText("Stalemate! Barazim.");
            gameStateLabel.setText("Gjendja: Stalemate");
            aiStatusLabel.setText("AI Status: Finished");
            return true;
        }

        if (CheckDetector.isKingInCheck(board, s)) {
            statusLabel.setText("Check ndaj " + s);
            gameStateLabel.setText("Gjendja: Check");
        } else {
            statusLabel.setText(s == humanColor ? "Radha jote" : "Radha e AI");
            gameStateLabel.setText("Gjendja: Ne vazhdim");
        }

        aiStatusLabel.setText(aiThinking ? "AI Status: Duke menduar..." : "AI Status: Ready");

        return false;
    }

    private void addMoveToHistory(String who, Position from, Position to) {
        moveHistory.add(who + ": " + squareName(from) + " → " + squareName(to));
        moveHistoryList.scrollTo(moveHistory.size() - 1);
    }

    private String squareName(Position p) {
        return "" + (char) ('A' + p.getCol()) + (8 - p.getRow());
    }

    private void updateSidePanel() {
        aiStatusLabel.setText(aiThinking ? "AI Status: Duke menduar..." : "AI Status: Ready");
        gameStateLabel.setText(gameOver ? "Gjendja: Perfunduar" : "Gjendja: Ne vazhdim");
    }

    private void highlightSelectedTile(int row, int col) {
        Box t = tileNodes.get(key(row, col));
        if (t != null) t.setMaterial(matte(Color.rgb(128, 148, 125)));
    }

    private void highlightLegalMoves(List<Position> moves) {
        legalMoveKeys.clear();

        for (Position m : moves) {
            String k = key(m.getRow(), m.getCol());
            legalMoveKeys.add(k);

            Box t = tileNodes.get(k);

            if (t != null) {
                t.setMaterial(matte(board.getPiece(m.getRow(), m.getCol()) != null
                        ? Color.rgb(148, 115, 115)
                        : Color.rgb(148, 158, 162)));
            }
        }
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        legalMoveKeys.clear();

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Box t = tileNodes.get(key(r, c));
                if (t != null) setTileDefaultMaterial(t, r, c);
            }
        }
    }

    private void setTileDefaultMaterial(Box tile, int row, int col) {
        tile.setMaterial(matte((row + col) % 2 == 0
                ? Color.rgb(205, 205, 198)
                : Color.rgb(120, 125, 132)));
    }

    private void addLights() {
        AmbientLight ambient = new AmbientLight(Color.color(0.32, 0.30, 0.28));

        PointLight ceil = new PointLight(Color.rgb(150, 120, 95));
        ceil.setTranslateX(0);
        ceil.setTranslateY(-340);
        ceil.setTranslateZ(80);

        PointLight tbl = new PointLight(Color.rgb(160, 140, 115));
        tbl.setTranslateX(0);
        tbl.setTranslateY(-170);
        tbl.setTranslateZ(-120);

        PointLight frt = new PointLight(Color.rgb(95, 110, 130));
        frt.setTranslateX(0);
        frt.setTranslateY(-120);
        frt.setTranslateZ(540);

        PointLight pLight = new PointLight(Color.rgb(160, 135, 110));
        pLight.setTranslateX(0);
        pLight.setTranslateY(-220);
        pLight.setTranslateZ(AVATAR_Z_DIST - 30);

        PointLight aLight = new PointLight(Color.rgb(160, 135, 110));
        aLight.setTranslateX(0);
        aLight.setTranslateY(-220);
        aLight.setTranslateZ(-AVATAR_Z_DIST + 30);

        world.getChildren().addAll(ambient, ceil, tbl, frt, pLight, aLight);
    }

    private PhongMaterial textureMaterial(Color base, Color highlight, int pattern, int size) {
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();

        Random rnd = new Random(1000L + size * 17L + pattern * 31L
                + (long) (base.getRed() * 255) * 7L
                + (long) (highlight.getGreen() * 255) * 13L);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double wave;

                if (pattern == 3) {
                    wave = 0.5 + 0.5 * Math.sin(x * 0.18 + Math.sin(y * 0.08) * 2.2);
                    wave = wave * 0.55 + rnd.nextDouble() * 0.18;
                } else if (pattern == 2) {
                    wave = ((x + y) % 18 < 9 ? 0.65 : 0.35) + rnd.nextDouble() * 0.18;
                } else {
                    wave = 0.35 + rnd.nextDouble() * 0.45;
                    if (x % 24 < 2 || y % 24 < 2) wave += 0.18;
                }

                wave = Math.max(0, Math.min(1, wave));
                pw.setColor(x, y, base.interpolate(highlight, wave));
            }
        }

        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseMap(img);
        mat.setSpecularColor(highlight.darker());
        mat.setSpecularPower(pattern == 2 ? 32 : 12);

        return mat;
    }

    private PhongMaterial matte(Color c) {
        PhongMaterial m = new PhongMaterial();
        m.setDiffuseColor(c);
        m.setSpecularColor(c.darker());
        return m;
    }

    private void updateTurnLabel() {
        turnLabel.setText("Turn: " + currentTurn);
    }

    private PlayerColor oppositeColor(PlayerColor c) {
        return c == PlayerColor.WHITE ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    private double toX(int col) {
        return (col - 3.5) * TILE_SIZE;
    }

    private double toZ(int row) {
        return (row - 3.5) * TILE_SIZE;
    }

    private String key(int r, int c) {
        return r + "_" + c;
    }

    private void lockAvatarY(Group avatar) {
        double fixedY = avatar.getTranslateY();

        avatar.translateYProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.doubleValue() - fixedY) > 0.01) {
                avatar.setTranslateY(fixedY);
            }
        });
    }


    private void addPhotoPainting(double x, double y, double z,
                                  double w, double h,
                                  PhongMaterial frameMat,
                                  String imagePath) {

        Box frame = new Box(w + 40, h + 40, 16);
        frame.setTranslateX(x);
        frame.setTranslateY(y);
        frame.setTranslateZ(z - 8);
        frame.setMaterial(frameMat);

        Box canvas = new Box(w, h, 6);
        canvas.setTranslateX(x);
        canvas.setTranslateY(y);
        canvas.setTranslateZ(z + 8);

        PhongMaterial photoMat = new PhongMaterial();

        try {
            InputStream stream = getClass().getResourceAsStream(imagePath);

            if (stream == null) {
                String filePath = "src/resources" + imagePath;
                stream = new FileInputStream(filePath);
            }

            Image img = new Image(stream, w, h, false, true);

            if (img.isError()) {
                System.out.println("Foto ka error: " + imagePath);
                photoMat.setDiffuseColor(Color.DARKRED);
            } else {
                photoMat.setDiffuseMap(img);
            }

        } catch (Exception e) {
            System.out.println("Foto nuk u lexua: " + imagePath + " | " + e.getMessage());
            photoMat.setDiffuseColor(Color.rgb(120, 70, 40));
        }

        photoMat.setSpecularColor(Color.rgb(80, 70, 55));
        photoMat.setSpecularPower(8);

        canvas.setMaterial(photoMat);

        world.getChildren().addAll(frame, canvas);
    }

}
