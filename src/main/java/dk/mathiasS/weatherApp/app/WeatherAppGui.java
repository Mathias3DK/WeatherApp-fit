package dk.mathiasS.weatherApp.app;

import dk.mathiasS.weatherApp.app.WeatherApp;
import dk.mathiasS.weatherApp.app.font.FontLoader;
import dk.mathiasS.weatherApp.model.ClothingPredictor;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    private final Font klavika = FontLoader.loadFont("/assets/font.otf");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask;


    public WeatherAppGui(){
        // setup our gui and add a title
        super("TheMathWeatherApp");

        // configure gui to end the program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of our gui (in pixels)
        setSize(400,  650);

        // load our gui at the center of the screen
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within the gui
        setLayout(null);

        // prevent any resize of our gui
        setResizable(false);

        Image icon = Toolkit.getDefaultToolkit().getImage(WeatherApp.class.getResource("/icon.png"));
        setIconImage(icon);

        addGuiComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                scheduler.shutdown();
            }
        });
    }

    private void addGuiComponents(){

        JPanel background = new JPanel();
        background.setBackground(new Color(35, 48, 79));
        background.setBounds(0, 0, 400, 70);

        // search field
        JTextField searchTextField = new JTextField(" Søg her");
        searchTextField.setBorder(BorderFactory.createLineBorder(new Color(0,0,0),2));
        searchTextField.setBackground(new Color(35, 48, 79));
        searchTextField.setForeground(new Color(255, 255, 255));

        JSeparator searchSeperator = new JSeparator();

        searchSeperator.setOrientation(SwingConstants.HORIZONTAL);
        searchSeperator.setBackground(new Color(159, 163, 178));
        searchSeperator.setBounds(0, 70, 400, 10);

        add(searchSeperator);

        // set the location and size of our component
        searchTextField.setBounds(15, 15, 301, 45);

        // change the font style and size
        searchTextField.setFont(new Font("Klavika", Font.PLAIN, 24));

        add(searchTextField);

        JLabel CityText = new JLabel("BY, LANDKODE");
        CityText.setBounds(27,-10, 400, 217);
        CityText.setFont(new Font("Klavika", Font.BOLD, 24));

        JLabel CityText_sub = new JLabel("DAY DATE MONTH");
        CityText_sub.setBounds(28,10, 400, 217);
        CityText_sub.setFont(new Font("Klavika", Font.PLAIN, 16));

        add(CityText_sub);
        add(CityText);

        // Size of the icon
        int iconWidth = 75;
        int iconHeight = 75;

        // Size of the rounded panel (slightly larger than the icon)
        int panelWidth = 115;
        int panelHeight = 115;

        // Calculate the position to center the panel and icon
        int panelX = (167 - panelWidth) / 2;
        int panelY = 140;

        // Add the rounded panel behind the weather image
        RoundedPanel roundedPanel = new RoundedPanel();
        roundedPanel.setBounds(panelX, panelY, panelWidth, panelHeight);

        // weather image
        JLabel weatherConditionImage = new JLabel(loadImage("/assets/night_partly_clear.png", true));
        int iconX = panelX + (panelWidth - iconWidth) / 2;
        int iconY = panelY + (panelHeight - iconHeight) / 2;
        weatherConditionImage.setBounds(iconX, iconY, iconWidth, iconHeight);
        add(weatherConditionImage);
        add(roundedPanel);

        // temperature text
        JLabel temperatureText = new JLabel("10°");
        temperatureText.setBounds(59, 155, 400, 70);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 78));

        // center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // weather condition description
        JLabel weatherConditionDesc = new JLabel("Skyet");
        weatherConditionDesc.setBounds(59, 210, 400, 70);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 24));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JSeparator s = new JSeparator();

        s.setOrientation(SwingConstants.HORIZONTAL);
        s.setBackground(Color.BLACK);
        s.setBounds(20, 300, 350, 30);

        add(s);

        JLabel predicted_title = new JLabel("AI VURDERING");
        predicted_title.setBounds(27,380, 400, 217);
        predicted_title.setFont(new Font("Dialog", Font.BOLD, 24));
        add(predicted_title);

        JLabel predicted_description = new JLabel("Ikke bestemt ");
        predicted_description.setBounds(27,400, 400, 217);
        predicted_description.setFont(new Font("Dialog", Font.PLAIN, 18));
        add(predicted_description);

        JButton b = new JButton("Visualisér Data");
        b.setBackground(new Color(35, 48, 79));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Tahoma", Font.BOLD, 15));
        b.setBounds(27, 550, 330, 35);

        b.addActionListener(e -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                     InstantiationException ex) {
                throw new RuntimeException(ex);
            }

            try {
                URL imageUrl = Objects.requireNonNull(WeatherApp.class.getResource("/graphviz.png"));
                BufferedImage originalImage = ImageIO.read(imageUrl);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int maxWidth = screenSize.width - 100;
                int maxHeight = screenSize.height - 100;

                int imgWidth = originalImage.getWidth();
                int imgHeight = originalImage.getHeight();

                int newWidth = imgWidth;
                int newHeight = imgHeight;

                if (imgWidth > maxWidth || imgHeight > maxHeight) {
                    double widthRatio = (double) maxWidth / imgWidth;
                    double heightRatio = (double) maxHeight / imgHeight;
                    double scalingFactor = Math.min(widthRatio, heightRatio);

                    newWidth = (int) (imgWidth * scalingFactor);
                    newHeight = (int) (imgHeight * scalingFactor) - 30;
                }

                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);

                JOptionPane.showMessageDialog(null,
                        "1: T-shirt og shorts\n2: T-shirt og bukser\n3: Langærmet trøje og bukser\n4: Sweatshirt og bukser\n5: Trøje, jakke og bukser\n6: Jakke og jeans\n7: Regnjakke- eller paraply og jeans\n8: Vinterjakke- tilhørende regnjakke og bukser\n9: Vinterjakke og bukser\n10: Vinterjakke, hue og handsker",
                        "Data", JOptionPane.INFORMATION_MESSAGE, icon);
            } catch (IOException en) {
                en.printStackTrace();
            }

        });

        add(b);

        //HØJEST START

        JLabel title_below = new JLabel("Højest");
        title_below.setBounds(65, 345, 60, 30);
        title_below.setForeground(new Color(215, 215, 215));
        title_below.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(title_below);

        JLabel title_high = new JLabel("25.0°");
        title_high.setBounds(65, 325, 60, 30);
        title_high.setForeground(new Color(255, 255, 255));
        title_high.setFont(new Font("Dialog", Font.BOLD, 18));
        add(title_high);

        //HØJEST END
        //LAVEST START

        JLabel title_low = new JLabel("Lavest");
        title_low.setBounds(65, 405, 60, 30);
        title_low.setForeground(new Color(215, 215, 215));
        title_low.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(title_low);

        JLabel low_below = new JLabel("22.0°");
        low_below.setBounds(65, 385, 60, 30);
        low_below.setForeground(new Color(255, 255, 255));
        low_below.setFont(new Font("Dialog", Font.BOLD, 18));
        add(low_below);

        //LAVEST END
        //RAIN START

        JLabel rain_title = new JLabel("Nedbør");
        rain_title.setBounds(175, 405, 60, 30);
        rain_title.setForeground(new Color(215, 215, 215));
        rain_title.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(rain_title);

        JLabel rain_below = new JLabel("22%");
        rain_below.setBounds(175, 385, 100, 30);
        rain_below.setForeground(new Color(255, 255, 255));
        rain_below.setFont(new Font("Dialog", Font.BOLD, 16));
        add(rain_below);

        //RAIN END

        JLabel sunset_title = new JLabel("Sunset");
        sunset_title.setBounds(285, 405, 60, 30);
        sunset_title.setForeground(new Color(215, 215, 215));
        sunset_title.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(sunset_title);

        JLabel sunset_below = new JLabel("20:56");
        sunset_below.setBounds(285, 385, 60, 30);
        sunset_below.setForeground(new Color(255, 255, 255));
        sunset_below.setFont(new Font("Dialog", Font.BOLD, 16));
        add(sunset_below);

        //WIND

        JLabel wind_title = new JLabel("Vind");
        wind_title.setBounds(175, 345, 60, 30);
        wind_title.setForeground(new Color(215, 215, 215));
        wind_title.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(wind_title);

        JLabel wind_below = new JLabel("7m/s");
        wind_below.setBounds(175, 325, 60, 30);
        wind_below.setForeground(new Color(255, 255, 255));
        wind_below.setFont(new Font("Dialog", Font.BOLD, 16));
        add(wind_below);

        JLabel sunrise_title = new JLabel("Sunrise");
        sunrise_title.setBounds(285, 345, 60, 30);
        sunrise_title.setForeground(new Color(215, 215, 215));
        sunrise_title.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(sunrise_title);

        JLabel sunrise_below = new JLabel("05:54");
        sunrise_below.setBounds(285, 325, 60, 30);
        sunrise_below.setForeground(new Color(255, 255, 255));
        sunrise_below.setFont(new Font("Dialog", Font.BOLD, 16));
        add(sunrise_below);

        RoundedPanel DataBackground = new RoundedPanel();
        DataBackground.setBounds(20, 320, 350, 120);

        add(DataBackground);

        JSeparator s_2 = new JSeparator();

        s_2.setOrientation(SwingConstants.HORIZONTAL);
        s_2.setBackground(Color.BLACK);
        s_2.setBounds(20, 460, 350, 30);

        add(s_2);

        // search button
        JButton searchButton = new JButton(loadImage("/assets/search.png",false));
        searchButton.setFocusable(false);

        // change the cursor to a hand cursor when hovering over this button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        searchButton.setBounds(325, 15, 47, 45);
        searchButton.setBackground(new Color(35, 48, 79));
        searchButton.setForeground(new Color(35, 48, 79));

        searchButton.setBorder(BorderFactory.createLineBorder(new Color(0,0,0),2));

        searchButton.addActionListener(e -> {
            // get location from user
            String userInput = searchTextField.getText();

            // validate input - remove whitespace to ensure non-empty text
            if (userInput.replaceAll("\\s", "").length() <= 0) {
                return;
            }

            if (scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel(true);
            }

            scheduledTask = scheduler.scheduleAtFixedRate(() -> {

                // retrieve weather data
                weatherData = WeatherApp.getWeatherData(userInput);

                // update gui

                if(weatherData == null){

                    searchTextField.setText(" By findes ikke.");
                    return;

                }

                // update weather image
                String weatherCondition = (String) weatherData.get("weather_condition");

                // depending on the condition, we will update the weather image that corresponds with the condition
                if(weatherCondition!=null){ // bare få night time og day time fra API
                    weatherConditionImage.setIcon(loadImage("/assets/" + (!(boolean)weatherData.get("is_day") ? "night_" : "") + weatherCondition + ".png", true));
                }

                searchTextField.setText(" Søg her");

                CityText.setText("" + weatherData.get("city_name"));

                String[] months = new String[]{"Januar","Februar","Marts","April","Maj","Juni","Juli","August","September","Oktober","November","December"};
                String[] days = new String[]{"Søndag", "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag"};

                Date date = new Date();

                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(date);

                CityText_sub.setText(days[calendar.get(Calendar.DAY_OF_WEEK) - 1] + " " + date.getDate() + ". " + months[date.getMonth()]);

                // update temperature text
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + "°");

                title_high.setText(weatherData.get("highestDegree") + "°");
                low_below.setText(weatherData.get("lowestDegree") + "°");

                sunset_below.setText("" + weatherData.get("sunset"));
                sunrise_below.setText("" + weatherData.get("sunrise"));

                // update weather condition text
                if(weatherCondition == null) return;

                switch (weatherCondition){
                    case "night_clear":
                    case "clear":
                        weatherConditionDesc.setText("Klar Himmel");
                        break;
                    case "night_cloudy":
                    case "cloudy":
                        weatherConditionDesc.setText("Skyet");
                        break;
                    case "night_partly_clear":
                    case "partly_clear":
                        weatherConditionDesc.setText("Lidt Skyet");
                        break;
                    case "night_mainly_clear":
                    case "mainly_clear":
                        weatherConditionDesc.setText("Næsten Klar Himmel");
                        break;
                    case "rain":
                    case "night_rain":
                        weatherConditionDesc.setText("Nedbør");
                        break;
                }

                rain_below.setText(weatherData.get("rain") + "mm");

                // update windspeed text
                double windspeed = (double) weatherData.get("windspeed");
                wind_below.setText(windspeed + "m/s");

                predicted_description.setText("Finder vurdering ud fra datasæt..");

                scheduler.schedule(() -> {
                    try{

                        String outlook = "";
                        boolean windy = false;
                        long humidity = 0;

                        String output = "Ikke bestemt";

                        if (weatherData != null) {

                            outlook = (weatherCondition.contains("snow") ? "snowy" : (weatherCondition.contains("rain") ? "rainy" : (weatherCondition.contains("cloudy") || weatherCondition.contains("partly_clear")) ? "overcast" : "sunny"));
                            windy = windspeed >= 6;
                            humidity = (long)weatherData.get("humidity");

                        }

                        String result_class;
                        try {
                            ClothingPredictor predictor = new ClothingPredictor("/model/weather_data.arff");

                            predictor.buildClassifier();

                            result_class = predictor.predict(outlook, (temperature * 9/5) + 32, (long)weatherData.get("rain_possibility"), humidity, windy);

                            if(result_class == null){
                                return;
                            }

                            System.out.println("Prediction: " + " (Outlook: " + outlook + ", Temperature: " + temperature + " °C, rain_probability: " + weatherData.get("rain_possibility") + ", " + "Humidity: " + humidity + " %, Windy: " + windy + ")");
                        } catch (Exception ex) {
                            throw new RuntimeException("Fejl ved forudsigelse", ex);
                        }

                        //define hvad resultatet betyder (1,2,3,4,5) resultatet bestemmes af dataene fra den valgte time
                        int prediction = Integer.parseInt(result_class);

                        switch (prediction) {
                            case 1:
                                output = "T-shirt og shorts";
                                break;
                            case 2:
                                output = "T-shirt og bukser";
                                break;
                            case 3:
                                output = "Langærmet trøje og bukser";
                                break;
                            case 4:
                                output = "Sweatshirt og bukser";
                                break;
                            case 5:
                                output = "Trøje, jakke og bukser";
                                break;
                            case 6:
                                output = "Jakke og jeans";
                                break;
                            case 7:
                                output = "Regnjakke- eller paraply og bukser";
                                break;
                            case 8:
                                output = "Vinterjakke- tilhørende regnjakke og bukser";
                                break;
                            case 9:
                                output = "Vinterjakke og bukser";
                                break;
                            case 10:
                                output = "Vinterjakke, hue og handsker";
                                break;

                        } //

                        final String output_final = output;
                        SwingUtilities.invokeLater(() -> predicted_description.setText(output_final));

                    } catch (Exception e_2) {
                        System.err.println("Fejl: " + e_2.getMessage());
                        e_2.printStackTrace();
                    }
                }, 3 * 1000, TimeUnit.MILLISECONDS);

            }, 0, 2, TimeUnit.MINUTES);
        });
        add(searchButton);
        add(background);

        repaint();

    }

    // used to create images in our gui components
    public ImageIcon loadImage(String resourcePath, boolean bool) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.out.println("Could not find resource: " + resourcePath);
                return null;
            }

            BufferedImage image = ImageIO.read(is);
            Image newImage = image.getScaledInstance(75, 75, Image.SCALE_SMOOTH);

            return new ImageIcon(bool ? newImage : image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
    private static class RoundedPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(90, 111, 159));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        }
    }

}









