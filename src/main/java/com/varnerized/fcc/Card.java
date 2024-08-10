package com.varnerized.fcc;

import java.io.File;

public class Card {
    String title;
    File image;
    String text;

    public Card(String title, File image, String text) {
        this.title = title;
        this.image = image;
        this.text = text;
    }
}
