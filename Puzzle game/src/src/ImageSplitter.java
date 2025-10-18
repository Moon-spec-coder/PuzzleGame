package src;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class ImageSplitter {

    public static BufferedImage[][] splitImage(BufferedImage src, int rows, int cols) {
        int pieceWidth = src.getWidth() / cols;
        int pieceHeight = src.getHeight() / rows;
        BufferedImage[][] pieces = new BufferedImage[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Shape irregularShape = createIrregularShape(pieceWidth, pieceHeight);
                BufferedImage piece = new BufferedImage(pieceWidth, pieceHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = piece.createGraphics();
                g2d.setClip(irregularShape);
                g2d.drawImage(src,
                        0, 0, pieceWidth, pieceHeight,
                        c * pieceWidth, r * pieceHeight,
                        (c + 1) * pieceWidth, (r + 1) * pieceHeight, null);
                g2d.dispose();
                pieces[r][c] = piece;
            }
        }
        return pieces;
    }

    private static Shape createIrregularShape(int w, int h) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(w, 0);
        path.lineTo(w - 10, h);
        path.lineTo(10, h);
        path.closePath();
        return path;
    }
}

