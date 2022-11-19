package com.hypdncy.BurpLoader;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainForm {
    private static Keygen keygen;
    private static JFrame frame;
    private static JButton btn_run;
    private static JTextField text_cmd;
    private static JTextField text_license_text;
    private static JTextArea text_license;
    private static JTextArea request;
    private static JTextArea response;
    private static JLabel label0_1;
    private static JPanel panel1;
    private static JPanel panel2;
    private static JPanel panel3;
    private static JCheckBox check_autorun;
    private static JCheckBox check_ignore;
    private static String LatestVersion;
    private static final String DownloadURL = "https://portswigger.net/burp/releases/download?product=pro&type=Jar&version=";

    private static Properties configureProperties;

    private static List<String> getCommand() {
        Path AgentPath = new File(MainForm.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath();
        Path AgentDir = AgentPath.getParent();
        String LastFile = "burpsuite_jar_not_found.jar";

        if (Files.isDirectory(AgentDir)) {
            try {
                Optional<Path> opPath = Files.list(AgentDir)
                        .filter(p -> !Files.isDirectory(p) && p.getFileName().toString().startsWith("burpsuite_") && p.getFileName().toString().endsWith(".jar"))
                        .min((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()));

                if (opPath.isPresent()) {
                    LastFile = opPath.get().getFileName().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> runCommand = new ArrayList<>();
        runCommand.add("java");
        runCommand.add("-javaagent:" + AgentPath.getFileName().toString());

        if (Runtime.version().feature() >= 17) {
            runCommand.add("--add-opens=java.desktop/javax.swing=ALL-UNNAMED");
            runCommand.add("--add-opens=java.base/java.lang=ALL-UNNAMED");

        }

        runCommand.add("-jar");
        runCommand.add(LastFile);


        return runCommand;
    }

    private static void loadProperties() {
        Properties prop = new Properties();
        try {
            Reader configureFileReader = new FileReader("config.cfg", StandardCharsets.UTF_8);
            prop.load(configureFileReader);
        } catch (IOException e) {
            prop.setProperty("auto_run", "0");
            prop.setProperty("ignore", "0");
        }
        configureProperties = prop;
    }


    private static void saveProperties() {
        try (OutputStream output = new FileOutputStream("config.cfg")) {
            configureProperties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    private static String getLatestVersion() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://portswigger.net/burp/releases/data?pageSize=1"))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36")
                .build();

        CompletableFuture<HttpResponse<String>> asyncResponse = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        try {
            String asyncResultBody = asyncResponse.thenApply(HttpResponse::body).get(3, TimeUnit.SECONDS);

            int targetIndex = asyncResultBody.indexOf("\"ProductId\":\"pro\",\"ProductPlatform\":\"Jar\",\"ProductPlatformLabel\":\"JAR\"");
            if (targetIndex == -1) {
                return "";
            }
            String result2 = asyncResultBody.substring(targetIndex + 166);
            return result2.substring(0, result2.indexOf("\""));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "";
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // init
        loadProperties();
        String commandString = String.join(" ", getCommand());
        List<String> commandList = getCommand();


        if (configureProperties.getProperty("auto_run").equals("1")) {
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(commandList).start();
                if (configureProperties.getProperty("ignore").equals("1") || commandString.contains(getLatestVersion())) {
                    System.exit(0);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (configureProperties.getProperty("ignore").equals("0")) {
            LatestVersion = getLatestVersion();
        }

        keygen = new Keygen();
        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();
        frame = new JFrame("Burp Suite Pro Loader & Keygen");
        btn_run = new JButton("Run");
        label0_1 = new JLabel("Checking the latest version of BurpSuite...");
        JLabel label1 = new JLabel("Loader Command:", SwingConstants.RIGHT);
        JLabel label2 = new JLabel("License Text:", SwingConstants.RIGHT);
        text_cmd = new JTextField(commandString);
        text_license_text = new JTextField("dev0");
        text_license = new JTextArea(keygen.generateLicense(text_license_text.getText()));
        request = new JTextArea();
        response = new JTextArea();
        check_autorun = new JCheckBox("Auto Run");
        check_ignore = new JCheckBox("Ignore Update");
        check_autorun.setBounds(200, 25, 120, 20);
        check_autorun.setSelected(configureProperties.getProperty("auto_run").equals("1"));
        check_autorun.addChangeListener(changeEvent -> {
            if (check_autorun.isSelected()) {
                configureProperties.setProperty("auto_run", "1");
            } else {
                configureProperties.setProperty("auto_run", "0");
            }
            saveProperties();
        });
        check_ignore.setBounds(320, 25, 160, 20);
        check_ignore.setSelected(configureProperties.getProperty("ignore").equals("1"));
        check_ignore.addChangeListener(changeEvent2 -> {
            if (check_ignore.isSelected()) {
                configureProperties.setProperty("ignore", "1");
            } else {
                configureProperties.setProperty("ignore", "0");
            }
            saveProperties();
        });
        label0_1.setLocation(150, 5);
        label1.setBounds(5, 70, 140, 22);
        text_cmd.setLocation(150, 70);
        btn_run.setSize(60, 22);
        label2.setBounds(5, 97, 140, 22);
        text_license_text.setLocation(150, 97);
        panel1.setBorder(BorderFactory.createTitledBorder("License"));
        panel2.setBorder(BorderFactory.createTitledBorder("Activation Request"));
        panel3.setBorder(BorderFactory.createTitledBorder("Activation Response"));
        text_license.setLocation(10, 15);
        request.setLocation(10, 15);
        response.setLocation(10, 15);
        panel1.setLocation(5, 124);
        panel1.setLayout(null);
        panel2.setLayout(null);
        panel3.setLayout(null);
        frame.setLayout(null);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e2) {
                int H = MainForm.frame.getHeight() - 170;
                int W = MainForm.frame.getWidth();
                MainForm.text_cmd.setSize(W - 235, 22);
                MainForm.btn_run.setLocation(W - 80, 70);
                MainForm.text_license_text.setSize(W - 170, 22);
                MainForm.label0_1.setSize(W - 170, 20);
                MainForm.text_license.setSize(((W - 15) / 2) - 25, (H / 2) - 25);
                MainForm.request.setSize(((W - 15) / 2) - 25, (H / 2) - 25);
                MainForm.response.setSize(W - 43, (H / 2) - 25);
                MainForm.panel1.setSize(((W - 15) / 2) - 5, H / 2);
                MainForm.panel2.setBounds(3 + ((W - 15) / 2), 124, ((W - 15) / 2) - 5, H / 2);
                MainForm.panel3.setBounds(5, 129 + (H / 2), W - 23, H / 2);
            }
        });
        btn_run.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e2) {
                super.mouseClicked(e2);
                try {
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.command(commandList).start();
                } catch (IOException e12) {
                    e12.printStackTrace();
                }
            }
        });
        text_license.setLineWrap(true);
        text_license.setEditable(false);
        text_license_text.setHorizontalAlignment(0);
        text_license.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        text_license_text.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e2) {
                MainForm.text_license.setText(MainForm.keygen.generateLicense(MainForm.text_license_text.getText()));
            }

            public void removeUpdate(DocumentEvent e2) {
                MainForm.text_license.setText(MainForm.keygen.generateLicense(MainForm.text_license_text.getText()));
            }

            public void changedUpdate(DocumentEvent e2) {
                MainForm.text_license.setText(MainForm.keygen.generateLicense(MainForm.text_license_text.getText()));
            }
        });
        request.setLineWrap(true);
        request.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        request.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e2) {
                MainForm.response.setText(MainForm.keygen.generateActivation(MainForm.request.getText()));
            }

            public void removeUpdate(DocumentEvent e2) {
                MainForm.response.setText(MainForm.keygen.generateActivation(MainForm.request.getText()));
            }

            public void changedUpdate(DocumentEvent e2) {
                MainForm.response.setText(MainForm.keygen.generateActivation(MainForm.request.getText()));
            }
        });
        response.setLineWrap(true);
        response.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        frame.add(check_autorun);
        frame.add(check_ignore);
        frame.add(btn_run);
        frame.add(label0_1);
        frame.add(label1);
        frame.add(label2);
        frame.add(panel1);
        frame.add(panel2);
        frame.add(panel3);
        frame.add(text_cmd);
        frame.add(text_license_text);
        panel1.add(text_license);
        panel2.add(request);
        panel3.add(response);
        if (text_cmd.getText().contains("burpsuite_jar_not_found.jar")) {
            btn_run.setEnabled(false);
            check_autorun.setSelected(false);
            check_autorun.setEnabled(false);
        }
        frame.setVisible(true);
        btn_run.setFocusable(false);
        if (LatestVersion.equals("")) {
            label0_1.setText("Failed to check the latest version of BurpSuite");
        } else if (!commandString.contains(LatestVersion + ".jar")) {
            label0_1.setText("Latest version:" + LatestVersion + ". Click to download.");
            label0_1.setForeground(Color.BLUE);
            label0_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label0_1.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e2) {
                    super.mouseClicked(e2);
                    try {
                        Desktop.getDesktop().browse(new URI(MainForm.DownloadURL + MainForm.LatestVersion));
                    } catch (Exception ignored) {
                    }
                }
            });
        } else {
            label0_1.setText("Your BurpSuite is already the latest version(" + LatestVersion + ")");
            label0_1.setForeground(new Color(0, 100, 0));
        }
    }
}