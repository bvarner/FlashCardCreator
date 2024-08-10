package com.varnerized.fcc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Page {
    static final float WIDTH = 8.5f;
    static final float HEIGHT = 11f;

    static final float CARD_WIDTH = 3.5f;
    static final float CARD_HEIGHT = 2.5f;

    static final float LEFT_MARGIN = (WIDTH - (CARD_WIDTH * 2)) / 2;
    ;
    static final float RIGHT_MARGIN = WIDTH - (LEFT_MARGIN * 2);
    static final float TOP_MARGIN = (HEIGHT - (CARD_HEIGHT * 3)) / 2;
    ;
    static final float BOTTOM_MARGIN = HEIGHT - (TOP_MARGIN * 2);

    List<Card> cards = new ArrayList<>();

    public Page() {
    }

    public int getPageCapacity() {
        return 6;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    public void render(Side s, PageFormat pageFormat, Graphics2D g2d) throws IOException {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        Point[] cardOrigins = new Point[]{
                new Point(0, 0),
                new Point((int)Math.floor(pageFormat.getImageableWidth() / 2), 0),

                new Point(0,(int)Math.floor(pageFormat.getImageableHeight() * ((double) 1 / 3))),
                new Point((int)Math.floor(pageFormat.getImageableWidth() / 2), (int)Math.floor(pageFormat.getImageableHeight() * ((double) 1 / 3))),

                new Point(0, (int)Math.floor(pageFormat.getImageableHeight() * ((double) 2 / 3))),
                new Point((int)Math.floor(pageFormat.getImageableWidth() / 2), (int)Math.floor(pageFormat.getImageableHeight() * ((double) 2 / 3)))
        };

        Rectangle[] cardBoundaries = new Rectangle[]{
                new Rectangle(cardOrigins[0].x, cardOrigins[0].y, cardOrigins[1].x, cardOrigins[2].y),
                new Rectangle(cardOrigins[1].x, cardOrigins[1].y, cardOrigins[1].x, cardOrigins[2].y),

                new Rectangle(cardOrigins[2].x, cardOrigins[2].y, cardOrigins[1].x, cardOrigins[2].y),
                new Rectangle(cardOrigins[3].x, cardOrigins[3].y, cardOrigins[1].x, cardOrigins[2].y),

                new Rectangle(cardOrigins[4].x, cardOrigins[4].y, cardOrigins[1].x, cardOrigins[2].y),
                new Rectangle(cardOrigins[5].x, cardOrigins[5].y, cardOrigins[1].x, cardOrigins[2].y)
        };

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            if (s == Side.FRONT) {
                BufferedImage ogImage = ImageIO.read(card.image);
                BufferedImage image = new BufferedImage(ogImage.getWidth(), ogImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D imageG2d = (Graphics2D) image.getGraphics();
                imageG2d.drawImage(ogImage, 0, 0, null);

                double imageAspectRatio = (double) image.getWidth() / (double) image.getHeight();
                double targetAspectRation = (double) cardBoundaries[i].width / (double) cardBoundaries[i].height;

                // resized target height for image.
                int iWidth, iHeight;
                int xOffset, yOffset;

                if (imageAspectRatio > targetAspectRation) {
                    iWidth = cardBoundaries[i].width;
                    iHeight = (int) (cardBoundaries[i].width / imageAspectRatio);
                    xOffset = (int) cardBoundaries[i].x;
                    yOffset = (int) cardBoundaries[i].y + (cardBoundaries[i].height - iHeight) / 2;
                } else {
                    iWidth = (int) (cardBoundaries[i].height * imageAspectRatio);
                    iHeight = cardBoundaries[i].height;
                    xOffset = cardBoundaries[i].x + (cardBoundaries[i].width - iWidth) / 2;
                    yOffset = cardBoundaries[i].y;
                }

                // Calculate and set a clipping region.
                int inset = g2d.getFontMetrics().getMaxAscent() / 4;
                Rectangle clippedRect = new Rectangle(
                        cardBoundaries[i].x + inset,
                        cardBoundaries[i].y + inset,
                        cardBoundaries[i].width - 2 * inset,
                        cardBoundaries[i].height - 2 * inset
                );
                // Set the clipping region
                g2d.setClip(clippedRect);

                Rectangle2D titleRect = g2d.getFontMetrics().getStringBounds(card.title, g2d);

                // Composite the Alpha Blend to make the title legible into the image.
                imageG2d.scale(g2d.getTransform().getScaleX(), g2d.getTransform().getScaleY());
                imageG2d.setColor(new Color(255, 255, 255, 156));
                imageG2d.fillRect(0, cardBoundaries[i].height - titleRect.getBounds().height * 2, cardBoundaries[i].width, titleRect.getBounds().height * 2);
                imageG2d.dispose();

                g2d.drawImage(image, xOffset, yOffset, iWidth, iHeight, null);

                g2d.setColor(new Color(255, 255, 255, 156));
                g2d.setColor(Color.BLACK);

                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
                g2d.drawString(card.title, cardBoundaries[i].x + (cardBoundaries[i].width / 2) - ((int)titleRect.getWidth() / 2),
                        cardBoundaries[i].y + cardBoundaries[i].height - ((int)titleRect.getHeight() / 2));
                g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN));
            } else {
                renderText(g2d, cardBoundaries[i], card.text);
            }
        }
    }


    /**
     * Renders text within the specified bounding box, inset by half the max ascent of the font.
     *
     * @param g2d        The Graphics2D context to render the text on.
     * @param boundingBox The bounding box within which the text should be rendered.
     * @param text       The string of text to render.
     */
    private void renderText(Graphics2D g2d, Rectangle boundingBox, String text) {
        // Set up FontMetrics to measure text
        Font font = g2d.getFont();
        FontMetrics fontMetrics = g2d.getFontMetrics(font);

        // Calculate ascent and insetting
        int ascent = fontMetrics.getAscent();
        int inset = ascent / 2;

        // Adjust the bounding box by insetting
        Rectangle adjustedBox = new Rectangle(
                boundingBox.x + inset,
                boundingBox.y + inset,
                boundingBox.width - 2 * inset,
                boundingBox.height - 2 * inset
        );

        // Split the text by line feeds
        String[] lines = text.split("\n");

        // Calculate the vertical position to start rendering
        int y = adjustedBox.y + ascent;

        // Define the starting x position for left justification
        int x = adjustedBox.x;

        // Render each line of text
        for (String line : lines) {
            // Calculate the width of the current line
            int lineWidth = fontMetrics.stringWidth(line);

            // Ensure the text fits within the adjusted bounding box width
            if (lineWidth > adjustedBox.width) {
                // Text wrapping: Break the line into smaller chunks
                String[] words = line.split(" ");
                StringBuilder lineBuilder = new StringBuilder();
                for (String word : words) {
                    // Check if adding the next word exceeds the adjusted bounding box width
                    if (fontMetrics.stringWidth(lineBuilder + word) > adjustedBox.width) {
                        // Draw the current line
                        g2d.drawString(lineBuilder.toString(), x, y);
                        // Move to the next line
                        y += fontMetrics.getHeight();
                        // Start a new line with the current word
                        lineBuilder = new StringBuilder(word).append(" ");
                    } else {
                        // Append the word to the current line
                        lineBuilder.append(word).append(" ");
                    }
                }
                // Draw the remaining part of the line
                g2d.drawString(lineBuilder.toString(), x, y);
                y += fontMetrics.getHeight();
            } else {
                // Draw the text if it fits within the adjusted bounding box
                g2d.drawString(line, x, y);
                y += fontMetrics.getHeight();
            }

            // Stop rendering if text exceeds the adjusted bounding box height
            if (y > adjustedBox.y + adjustedBox.height) {
                break;
            }
        }
    }
}
