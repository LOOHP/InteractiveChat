package com.loohp.interactivechat.main;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || Arrays.asList(args).contains("--nogui")) {
            CMLMain.launch(args);
        } else {
            GUIMain.launch(args);
        }
    }

    public static void mainInteractiveChatDiscordSrvAddon(String[] args) {
        try {
            Class<?> clazz = Class.forName("com.loohp.interactivechatdiscordsrvaddon.main.Main");
            Method method = clazz.getMethod("run", String[].class);
            method.invoke(null, new Object[] {args});
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
