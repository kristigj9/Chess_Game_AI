package gui3d;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import model.PlayerColor;
import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.pieces.Queen;
import model.pieces.Rook;

public class Piece3DFactory {

    private static final double BASE_RADIUS = 20;
    private static final double BASE_HEIGHT = 8;

    public static Group createPiece3D(Piece piece) {
        Group group = new Group();

        PhongMaterial mainMaterial = createPieceMaterial(piece);
        PhongMaterial accentMaterial = createAccentMaterial(piece);

        Cylinder base = new Cylinder(BASE_RADIUS, BASE_HEIGHT);
        base.setMaterial(mainMaterial);
        base.setTranslateY(-4);

        Cylinder ring = new Cylinder(BASE_RADIUS - 3, 3);
        ring.setMaterial(accentMaterial);
        ring.setTranslateY(-9);

        group.getChildren().addAll(base, ring);

        if (piece instanceof Pawn) {
            buildPawn(group, mainMaterial, accentMaterial);
        } else if (piece instanceof Rook) {
            buildRook(group, mainMaterial, accentMaterial);
        } else if (piece instanceof Knight) {
            buildKnight(group, mainMaterial, accentMaterial);
        } else if (piece instanceof Bishop) {
            buildBishop(group, mainMaterial, accentMaterial);
        } else if (piece instanceof Queen) {
            buildQueen(group, mainMaterial, accentMaterial);
        } else if (piece instanceof King) {
            buildKing(group, mainMaterial, accentMaterial);
        }

        return group;
    }

    private static void buildPawn(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder lowerBody = new Cylinder(9, 12);
        lowerBody.setMaterial(mainMaterial);
        lowerBody.setTranslateY(-14);

        Cylinder neck = new Cylinder(6, 10);
        neck.setMaterial(accentMaterial);
        neck.setTranslateY(-24);

        Sphere head = new Sphere(11);
        head.setMaterial(mainMaterial);
        head.setTranslateY(-38);

        group.getChildren().addAll(lowerBody, neck, head);
    }

    private static void buildRook(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder body = new Cylinder(12, 24);
        body.setMaterial(mainMaterial);
        body.setTranslateY(-18);

        Box crownBase = new Box(22, 6, 22);
        crownBase.setMaterial(accentMaterial);
        crownBase.setTranslateY(-32);

        Box tower1 = new Box(5, 7, 5);
        tower1.setMaterial(mainMaterial);
        tower1.setTranslateX(-7);
        tower1.setTranslateY(-38);
        tower1.setTranslateZ(-7);

        Box tower2 = new Box(5, 7, 5);
        tower2.setMaterial(mainMaterial);
        tower2.setTranslateX(7);
        tower2.setTranslateY(-38);
        tower2.setTranslateZ(-7);

        Box tower3 = new Box(5, 7, 5);
        tower3.setMaterial(mainMaterial);
        tower3.setTranslateX(-7);
        tower3.setTranslateY(-38);
        tower3.setTranslateZ(7);

        Box tower4 = new Box(5, 7, 5);
        tower4.setMaterial(mainMaterial);
        tower4.setTranslateX(7);
        tower4.setTranslateY(-38);
        tower4.setTranslateZ(7);

        group.getChildren().addAll(body, crownBase, tower1, tower2, tower3, tower4);
    }

    private static void buildKnight(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder body = new Cylinder(11, 22);
        body.setMaterial(mainMaterial);
        body.setTranslateY(-16);

        Box neck = new Box(10, 16, 10);
        neck.setMaterial(accentMaterial);
        neck.setTranslateY(-28);
        neck.setTranslateX(2);

        Box head = new Box(18, 18, 10);
        head.setMaterial(mainMaterial);
        head.setTranslateY(-38);
        head.setTranslateX(6);

        Box ear = new Box(5, 8, 5);
        ear.setMaterial(accentMaterial);
        ear.setTranslateY(-50);
        ear.setTranslateX(10);

        group.getChildren().addAll(body, neck, head, ear);
    }

    private static void buildBishop(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder body = new Cylinder(10, 24);
        body.setMaterial(mainMaterial);
        body.setTranslateY(-18);

        Sphere top = new Sphere(10);
        top.setMaterial(accentMaterial);
        top.setTranslateY(-37);

        Sphere tip = new Sphere(4);
        tip.setMaterial(mainMaterial);
        tip.setTranslateY(-49);

        group.getChildren().addAll(body, top, tip);
    }

    private static void buildQueen(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder body = new Cylinder(11, 28);
        body.setMaterial(mainMaterial);
        body.setTranslateY(-20);

        Sphere crownCenter = new Sphere(10);
        crownCenter.setMaterial(accentMaterial);
        crownCenter.setTranslateY(-39);

        Sphere top = new Sphere(4);
        top.setMaterial(mainMaterial);
        top.setTranslateY(-52);

        Sphere left = new Sphere(3);
        left.setMaterial(mainMaterial);
        left.setTranslateX(-8);
        left.setTranslateY(-46);

        Sphere right = new Sphere(3);
        right.setMaterial(mainMaterial);
        right.setTranslateX(8);
        right.setTranslateY(-46);

        group.getChildren().addAll(body, crownCenter, top, left, right);
    }

    private static void buildKing(Group group, PhongMaterial mainMaterial, PhongMaterial accentMaterial) {
        Cylinder body = new Cylinder(11, 30);
        body.setMaterial(mainMaterial);
        body.setTranslateY(-20);

        Box crown = new Box(10, 10, 10);
        crown.setMaterial(accentMaterial);
        crown.setTranslateY(-40);

        Box crossVertical = new Box(4, 16, 4);
        crossVertical.setMaterial(mainMaterial);
        crossVertical.setTranslateY(-53);

        Box crossHorizontal = new Box(14, 4, 4);
        crossHorizontal.setMaterial(mainMaterial);
        crossHorizontal.setTranslateY(-53);

        group.getChildren().addAll(body, crown, crossVertical, crossHorizontal);
    }

    private static PhongMaterial createPieceMaterial(Piece piece) {
        PhongMaterial material = new PhongMaterial();

        if (piece.getColor() == PlayerColor.WHITE) {
            material.setDiffuseColor(Color.rgb(228, 220, 196));
            material.setSpecularColor(Color.rgb(228, 220, 196));
        } else {
            material.setDiffuseColor(Color.rgb(42, 42, 46));
            material.setSpecularColor(Color.rgb(42, 42, 46));
        }

        return material;
    }

    private static PhongMaterial createAccentMaterial(Piece piece) {
        PhongMaterial material = new PhongMaterial();

        if (piece.getColor() == PlayerColor.WHITE) {
            material.setDiffuseColor(Color.rgb(205, 196, 176));
            material.setSpecularColor(Color.rgb(205, 196, 176));
        } else {
            material.setDiffuseColor(Color.rgb(60, 60, 66));
            material.setSpecularColor(Color.rgb(60, 60, 66));
        }

        return material;
    }
}