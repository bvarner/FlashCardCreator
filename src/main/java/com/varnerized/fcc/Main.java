package com.varnerized.fcc;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar robustified.jar <directory>");
            System.exit(1);
        }

        Path readFrom = Path.of(args[0]);

        if (!Files.isDirectory(readFrom)) {
            System.out.println("Not a directory: " + readFrom);
            System.exit(1);
        }

        // Otherwise let's build the Collection.
        Collection c = new Collection(readFrom);

        // Define the print attributes
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
//        attributes.add(new PageRanges(1, 2));
        attributes.add(new Destination(new File("trading_cards.ps").toURI()));
        attributes.add(MediaSizeName.NA_LETTER);
        attributes.add(PrintQuality.HIGH);
        attributes.add(Sides.DUPLEX);
        attributes.add(OrientationRequested.PORTRAIT);
        attributes.add(new MediaPrintableArea(Page.LEFT_MARGIN, Page.TOP_MARGIN, Page.RIGHT_MARGIN, Page.BOTTOM_MARGIN, MediaPrintableArea.INCH));

        // Create a print job
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(c);
        try {
            job.print(attributes);
        } catch (PrinterException e) {
            throw new RuntimeException(e);
        }
    }
}