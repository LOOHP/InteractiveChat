package com.loohp.interactivechat.main;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || Arrays.asList(args).contains("--nogui")) {
            CMLMain.launch(args);
        } else {
            GUIMain.launch(args);
        }
    }

}
