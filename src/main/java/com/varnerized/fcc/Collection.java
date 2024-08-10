package com.varnerized.fcc;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Collection implements Printable {
    private Set<Card> cards = new HashSet<>();
    private ArrayList<Page> pages = new ArrayList<>();

    private Map<String, File> images = new HashMap<>();
    private Map<String, File> descriptions = new HashMap<>();

    public Collection(Path readFrom) {
        File dir = readFrom.toFile();

        // Get the images in the directory
        String[] imageNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.toLowerCase().endsWith(".txt");
            }
        });

        String[] textNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });

        if (null == imageNames || imageNames.length == 0) {
            throw new IllegalArgumentException("No image names found");
        }
        if (null == textNames || textNames.length == 0) {
            throw new IllegalArgumentException("No text names found");
        }


        for (String imageName : imageNames) {
            images.put(imageName.toLowerCase().substring(0, imageName.indexOf(".")), new File(dir, imageName));
        }
        for (String description : textNames) {
            descriptions.put(description.toLowerCase().substring(0, description.indexOf(".")), new File(dir, description));
        }

        if (!images.keySet().containsAll(descriptions.keySet()) && !descriptions.keySet().containsAll(images.keySet())) {
            HashSet<String> imageKeys = new HashSet<>(images.keySet());
            HashSet<String> descriptionKeys = new HashSet<>(descriptions.keySet());
            imageKeys.removeAll(descriptions.keySet());
            descriptionKeys.removeAll(images.keySet());

            System.err.println("Images Missing Descriptions: " + imageKeys);
            System.err.println("Descriptions Missing Images: " + descriptionKeys);
            throw new IllegalArgumentException("Missing Companion Data: " + imageKeys + " " + descriptionKeys);
        }

        for (Map.Entry<String, File> entry : images.entrySet()) {
            try {
                cards.add(new Card(entry.getKey(), entry.getValue(), Files.readString(descriptions.get(entry.getKey()).toPath())));
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read text file: " + descriptions.get(entry.getKey()), e);
            }
        }

        // Iterate the cards and populate Pages.
        Card[] cards = this.cards.stream().sorted(Comparator.comparing(o -> o.title)).toArray(Card[]::new);

        Page p = new Page();
        for (Card card : cards) {
            p.addCard(card);
            if (p.getCards().size() == p.getPageCapacity()) {
                pages.add(p);
                p = new Page();
            }
        }
        if (!p.getCards().isEmpty()) {
            pages.add(p);
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if ((pageIndex / 2) >= pages.size()) {
            return NO_SUCH_PAGE;
        }

        Side s = pageIndex % 2 == 0 ? Side.FRONT : Side.BACK;

        // Get the index of the actual page in our collection.
        int printPage = pageIndex / 2;

        try {
            pages.get(printPage).render(s, pageFormat, (Graphics2D) graphics);
        } catch (Exception ex) {
            throw new PrinterException(ex.getMessage());
        }

        return PAGE_EXISTS;
    }
}
