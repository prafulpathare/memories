package com.cigatee.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CommonUtility {

    private static List<String> greetings = Arrays.asList("Hello there!",
            "Hi! How are you today?",
            "Good morning",
            "Good afternoon",
            "Good evening",
            "Hey, nice to see you!",
            "Welcome back!",
            "How’s your day going?",
            "Long time no see!",
            "Glad to have you here");
    private static Random random = new Random();

    public static String getDestinationFolder(String path) {

        if (path == null || path.trim().isEmpty()) return "";

        String[] paths = path.split("/");

        if (paths.length == 1) return paths[0];
        else if (paths.length == 2) return paths[1];
        else return paths[paths.length - 2];
    }

    public static String getGreetings() {
        return greetings.get(random.nextInt(greetings.size() - 1));
    }

}
