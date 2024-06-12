package dk.mathiasS.weatherApp;


import dk.mathiasS.weatherApp.app.WeatherAppGui;

import javax.swing.*;

public class WeatherApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // display our weather app gui

                new WeatherAppGui().setVisible(true);
            }

            //add switch fra timer så man kan se fremtidige timer
        });
    }
}
