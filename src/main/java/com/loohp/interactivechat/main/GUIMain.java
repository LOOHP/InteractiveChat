package com.loohp.interactivechat.main;

import com.loohp.interactivechat.updater.Version;
import com.loohp.interactivechat.utils.FileUtils;
import com.loohp.interactivechat.utils.HTTPRequestUtils;
import com.loohp.yamlconfiguration.ConfigurationSection;
import com.loohp.yamlconfiguration.YamlConfiguration;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GUIMain {

    public static void launch(String[] args) {
        String title = "InteractiveChat Tools";
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            YamlConfiguration pluginYaml = new YamlConfiguration(GUIMain.class.getClassLoader().getResourceAsStream("plugin.yml"));

            String pluginName = pluginYaml.getString("name");
            String version = pluginYaml.getString("version");

            BufferedImage image = ImageIO.read(GUIMain.class.getClassLoader().getResourceAsStream("icon.png"));
            Icon icon = new ImageIcon(image);

            title = pluginName + " v" + version + " Tools";

            String message = "<html><center><b>You are running " + pluginName + " v" + version + "</b><br>" +
                "Select one of the tools below<html/>";

            JLabel messageLabel = createLabel(message, 15);
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            main: while (true) {
                int input = JOptionPane.showOptionDialog(null, messageLabel, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, new Object[] {"Check for Updates", "Validate Plugin Configs", "Generate Default Configs", "Custom Placeholder Creator", "Visit Links"}, null);
                switch (input) {
                    case 0:
                        checkForUpdates(title, icon, version);
                        break;
                    case 1:
                        validConfigs(title, icon);
                        break;
                    case 2:
                        generateDefaultConfigs(title, icon);
                        break;
                    case 3:
                        BufferedImage resizedIcon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = resizedIcon.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g.drawImage(image, 0, 0, 32, 32, null);
                        g.dispose();
                        customPlaceholderCreator(title, resizedIcon, icon);
                        break main;
                    case 4:
                        visitLinks(title, icon);
                        break;
                    default:
                        break main;
                }
            }
        } catch (Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, createLabel("An error occurred!\n" + sw, 13, Color.RED), title, JOptionPane.ERROR_MESSAGE);
        }
    }

    protected static void visitLinks(String title, Icon icon) throws URISyntaxException, IOException {
        int input = JOptionPane.showOptionDialog(null, createLabel("Visit links through buttons below!", 15), title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, new Object[] {"SpigotMC", "GitHub", "Discord Server", "Build Server"}, null);
        if (Desktop.isDesktopSupported()) {
            Desktop dt = Desktop.getDesktop();
            switch (input) {
                case -1:
                    break;
                case 0:
                    dt.browse(new URI("https://www.spigotmc.org/resources/75870/"));
                    break;
                case 1:
                    dt.browse(new URI("https://github.com/LOOHP/InteractiveChat"));
                    break;
                case 2:
                    dt.browse(new URI("http://dev.discord.loohpjames.com"));
                    break;
                case 3:
                    dt.browse(new URI("https://ci.loohpjames.com"));
                    break;
            }
        }
    }

    protected static void checkForUpdates(String title, Icon icon, String localPluginVersion) throws URISyntaxException, IOException {
        JSONObject response = (JSONObject) HTTPRequestUtils.getJSONResponse("https://api.loohpjames.com/spigot/data").get("InteractiveChat");
        String spigotPluginVersion = (String) ((JSONObject) response.get("latestversion")).get("release");
        String devBuildVersion = (String) ((JSONObject) response.get("latestversion")).get("devbuild");
        int spigotPluginId = (int) (long) ((JSONObject) response.get("spigotmc")).get("pluginid");
        int posOfThirdDot = localPluginVersion.indexOf(".", localPluginVersion.indexOf(".", localPluginVersion.indexOf(".") + 1) + 1);
        Version currentDevBuild = new Version(localPluginVersion);
        Version currentRelease = new Version(localPluginVersion.substring(0, posOfThirdDot >= 0 ? posOfThirdDot : localPluginVersion.length()));
        Version spigotmc = new Version(spigotPluginVersion);
        Version devBuild = new Version(devBuildVersion);
        int input;
        if (currentRelease.compareTo(spigotmc) < 0) { //update
            input = JOptionPane.showOptionDialog(null, createLabel("There is a new version available! (" + currentDevBuild + ")\nLocal version: " + localPluginVersion, 15), title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, new Object[] {"OK", "Download Link"}, null);
        } else if (currentDevBuild.compareTo(devBuild) < 0) { //dev build update
            input = JOptionPane.showOptionDialog(null, createLabel("There is a new DEV build available! (" + currentDevBuild + ")\nLocal version: " + localPluginVersion, 15), title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, new Object[] {"OK", "Download Link"}, null);
        } else { //latest
            JOptionPane.showMessageDialog(null, createLabel("You are already running the latest version! (" + localPluginVersion + ")", 15), title, JOptionPane.INFORMATION_MESSAGE, icon);
            input = 0;
        }
        if (input == 1) {
            if (Desktop.isDesktopSupported()) {
                Desktop dt = Desktop.getDesktop();
                dt.browse(new URI("https://ci.loohpjames.com/job/InteractiveChat/"));
            }
        }
    }

    protected static void validConfigs(String title, Icon icon) throws IOException {
        File folder = new File("InteractiveChat");
        if (!folder.exists() || !folder.isDirectory()) {
            folder = new File("InteractiveChatBungee");
            if (!folder.exists() || !folder.isDirectory()) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, createLabel("Error: Plugin folder not found", 15, Color.RED), title, JOptionPane.ERROR_MESSAGE, icon);
                return;
            }
        }
        Map<File, List<String>> results = new LinkedHashMap<>();
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                YamlConfiguration yaml = new YamlConfiguration(new FileInputStream(file));
                results.put(file, validateConfigurationSection("", yaml));
            }
        }
        StringBuilder message = new StringBuilder("Validation Results: (Plugin Folder: " + folder.getAbsolutePath() + ")\n");
        for (Entry<File, List<String>> entry : results.entrySet()) {
            String fileName = entry.getKey().getName();
            List<String> errors = entry.getValue();
            message.append("\n").append(fileName).append(": ");
            if (errors.isEmpty()) {
                message.append("Valid!\n");
            } else {
                message.append("\n");
                for (String error : errors) {
                    message.append(error).append("\n");
                }
            }
        }
        message.append("\nNote that a valid config doesn't mean REGEX are valid.");
        JOptionPane.showMessageDialog(null, createLabel(message.toString(), 13), title, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    protected static List<String> validateConfigurationSection(String currentPath, ConfigurationSection section) {
        List<String> errors = new LinkedList<>();
        try {
            for (String key : section.getKeys(false)) {
                String path = currentPath.isEmpty() ? key : currentPath + "." + key;
                try {
                    Object value = section.get(key);
                    if (value instanceof ConfigurationSection) {
                        errors.addAll(validateConfigurationSection(path, (ConfigurationSection) value));
                    }
                } catch (Throwable e) {
                    errors.add("Failed to parse option around: " + path);
                }
            }
        } catch (Throwable e) {
            errors.add("Failed to parse option around: " + currentPath);
        }
        return errors;
    }

    protected static void generateDefaultConfigs(String title, Icon icon) throws IOException {
        int input = JOptionPane.showOptionDialog(null, createLabel("Generate configs for...", 15), title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, new Object[] {"Spigot (Backend)", "Bungeecord (Proxy)"}, null);
        File folder = null;
        if (input == 0) {
            folder = new File("InteractiveChat", "generated");
            FileUtils.removeFolderRecursively(folder);
            folder.mkdirs();
            FileUtils.copy(GUIMain.class.getClassLoader().getResourceAsStream("config_default.yml"), new File(folder, "config.yml"));
            FileUtils.copy(GUIMain.class.getClassLoader().getResourceAsStream("storage.yml"), new File(folder, "storage.yml"));
        } else if (input == 1) {
            folder = new File("InteractiveChatBungee", "generated");
            FileUtils.removeFolderRecursively(folder);
            folder.mkdirs();
            FileUtils.copy(GUIMain.class.getClassLoader().getResourceAsStream("config_proxy.yml"), new File(folder, "bungeeconfig.yml"));
        }
        if (folder != null) {
            JOptionPane.showMessageDialog(null, createLabel("Files saved at: " + folder.getAbsolutePath(), 15), title, JOptionPane.INFORMATION_MESSAGE, icon);
        }
    }

    protected static void customPlaceholderCreator(String title, BufferedImage image, Icon icon) {
        new CustomPlaceholderCreator(title + " - Custom Placeholder Creator", image, icon);
    }

    protected static JLabel createLabel(String message, float fontSize) {
        return createLabel(message, fontSize, Color.BLACK);
    }

    protected static JLabel createLabel(String message, float fontSize, Color color) {
        JLabel label = new JLabel("<html>" + message.replace("\n", "<br>") + "<html/>");
        label.setFont(label.getFont().deriveFont(Font.PLAIN).deriveFont(fontSize));
        label.setForeground(color);
        return label;
    }

}
