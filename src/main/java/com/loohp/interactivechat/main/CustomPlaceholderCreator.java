package com.loohp.interactivechat.main;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.yamlconfiguration.ConfigurationSection;
import com.loohp.yamlconfiguration.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomPlaceholderCreator extends JFrame {

    private static final Pattern IMPOSSIBLE_PATTERN = Pattern.compile("^\\b$");

    private String title;
    private BufferedImage image;
    private Icon icon;

    private JTextField textFieldName;
    private JTextArea textAreaConfigOutput;
    private JTextField textFieldDescription;
    private JTextField textFieldKeyword;
    private JCheckBox checkBoxParseKeyword;
    private JComboBox<ParsePlayer> boxParsePlayer;
    private JTextField textFieldCooldown;
    private JTextArea textAreaHover;
    private JCheckBox checkBoxClick;
    private JComboBox<ClickEventAction> boxClickAction;
    private JTextField textFieldClickValue;
    private JCheckBox checkBoxReplace;
    private JTextArea textAreaReplaceText;
    private JCheckBox checkBoxHover;
    private JTextField textFieldTestChat;
    private JTextArea textAreaTestOutput;
    private JPanel panel;
    private JScrollPane scrollPaneConfigOutput;
    private JButton buttonImport;
    private JButton buttonDefaultExamples;
    private JSpinner spinnerIndex;
    private JScrollPane scrollReplaceText;
    private JScrollPane scrollHoverText;
    private JScrollPane scrollTestOutput;

    public CustomPlaceholderCreator(String title, BufferedImage image, Icon icon) {
        this.title = title;
        this.image = image;
        this.icon = icon;

        $$$setupUI$$$();
        setTitle(title);
        setSize(1200, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setIconImage(image);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(panel);

        for (ParsePlayer parsePlayer : ParsePlayer.values()) {
            boxParsePlayer.addItem(parsePlayer);
        }
        for (ClickEventAction action : ClickEventAction.values()) {
            boxClickAction.addItem(action);
        }

        PlainDocument doc = (PlainDocument) textFieldCooldown.getDocument();
        doc.setDocumentFilter(new IntFilter());

        ConfigOutputListener configOutputListener = new ConfigOutputListener();
        textFieldName.addKeyListener(configOutputListener);
        textFieldDescription.addKeyListener(configOutputListener);
        textFieldKeyword.addKeyListener(configOutputListener);
        checkBoxParseKeyword.addActionListener(configOutputListener);
        boxParsePlayer.addActionListener(configOutputListener);
        textFieldCooldown.addKeyListener(configOutputListener);
        textAreaHover.addKeyListener(configOutputListener);
        checkBoxClick.addActionListener(configOutputListener);
        boxClickAction.addActionListener(configOutputListener);
        textFieldClickValue.addKeyListener(configOutputListener);
        checkBoxReplace.addActionListener(configOutputListener);
        textAreaReplaceText.addKeyListener(configOutputListener);
        checkBoxHover.addActionListener(configOutputListener);
        textAreaReplaceText.addKeyListener(configOutputListener);
        checkBoxHover.addActionListener(configOutputListener);
        spinnerIndex.addChangeListener(configOutputListener);

        textFieldTestChat.addActionListener(e -> {
            String input = e.getActionCommand();
            Component component = PlainTextComponentSerializer.plainText().deserialize(input);
            component = processCustomPlaceholder(toCustomPlaceholder(), component);
            textFieldTestChat.setText("");
            textAreaTestOutput.append(input + " -> " + PlainTextComponentSerializer.plainText().serialize(component) + "\n");

            SwingUtilities.invokeLater(() -> {
                scrollTestOutput.getHorizontalScrollBar().setValue(0);
            });
        });

        buttonImport.addActionListener(e -> {
            File folder = new File("InteractiveChat");
            File file = new File(folder, "config.yml");
            try {
                YamlConfiguration yaml = new YamlConfiguration(new FileInputStream(file));
                loadFromYaml(yaml);
            } catch (IOException ex) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, GUIMain.createLabel("There is an error while loading from config:\n" + ex.getMessage(), 13, Color.RED), title, JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonDefaultExamples.addActionListener(e -> {
            try {
                YamlConfiguration yaml = new YamlConfiguration(getClass().getClassLoader().getResourceAsStream("config_default.yml"));
                loadFromYaml(yaml);
            } catch (IOException ex) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, GUIMain.createLabel("There is an error while loading from config:\n" + ex.getMessage(), 13, Color.RED), title, JOptionPane.ERROR_MESSAGE);
            }
        });

        updateConfigOutput(toCustomPlaceholder());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUIComponents() {
        spinnerIndex = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        JComponent editor = spinnerIndex.getEditor();
        if (editor instanceof DefaultEditor) {
            DefaultEditor spinnerEditor = (DefaultEditor) editor;
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
    }

    public void loadFromYaml(ConfigurationSection yaml) {
        JPanel panel = new JPanel();
        JLabel label = GUIMain.createLabel("Select Placeholder: ", 13);
        panel.add(label);
        JComboBox<String> options = new JComboBox<>();
        for (String key : yaml.getConfigurationSection("CustomPlaceholders").getKeys(false)) {
            options.addItem(key + ". " + yaml.getString("CustomPlaceholders." + key + ".Name"));
        }
        panel.add(options);
        int result = JOptionPane.showOptionDialog(null, panel, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, null, null);
        if (result < 0) {
            return;
        }
        ConfigurationSection section = yaml.getConfigurationSection("CustomPlaceholders." + (options.getSelectedIndex() + 1));
        spinnerIndex.setValue(options.getSelectedIndex() + 1);
        textFieldName.setText(section.getString("Name"));
        textFieldDescription.setText(section.getString("Description"));
        boxParsePlayer.setSelectedItem(ParsePlayer.fromString(section.getString("ParsePlayer")));
        textFieldKeyword.setText(section.getString("Keyword"));
        checkBoxParseKeyword.setSelected(section.getBoolean("ParseKeyword"));
        textFieldCooldown.setText(section.getLong("Cooldown") + "");
        checkBoxHover.setSelected(section.getBoolean("Hover.Enable"));
        textAreaHover.setText(String.join("\n", section.getStringList("Hover.Text")));
        checkBoxClick.setSelected(section.getBoolean("Click.Enable"));
        try {
            boxClickAction.setSelectedItem(ClickEventAction.valueOf(section.getString("Click.Action")));
        } catch (Throwable e) {
            boxClickAction.setSelectedIndex(0);
        }
        textFieldClickValue.setText(section.getString("Click.Value"));
        checkBoxReplace.setSelected(section.getBoolean("Replace.Enable"));
        textAreaReplaceText.setText(section.getString("Replace.ReplaceText"));

        SwingUtilities.invokeLater(() -> {
            scrollHoverText.getHorizontalScrollBar().setValue(0);
            scrollHoverText.getVerticalScrollBar().setValue(0);
            scrollReplaceText.getHorizontalScrollBar().setValue(0);
            scrollReplaceText.getVerticalScrollBar().setValue(0);
        });

        updateConfigOutput(toCustomPlaceholder());
    }

    public CustomPlaceholder toCustomPlaceholder() {
        String name = textFieldName.getText();
        String description = textFieldDescription.getText();
        ParsePlayer parsePlayer = (ParsePlayer) boxParsePlayer.getSelectedItem();
        Pattern keyword = validRegex(textFieldKeyword.getText()) == null ? Pattern.compile(textFieldKeyword.getText()) : IMPOSSIBLE_PATTERN;
        boolean parseKeyword = checkBoxParseKeyword.isSelected();
        long cooldown = validLong(textFieldCooldown.getText()) == null ? Long.parseLong(textFieldCooldown.getText()) : 0;
        CustomPlaceholderHoverEvent hoverEvent = new CustomPlaceholderHoverEvent(checkBoxHover.isSelected(), textAreaHover.getText());
        CustomPlaceholderClickEvent clickEvent = new CustomPlaceholderClickEvent(checkBoxClick.isSelected(), (ClickEventAction) boxClickAction.getSelectedItem(), textFieldClickValue.getText());
        CustomPlaceholderReplaceText replaceText = new CustomPlaceholderReplaceText(checkBoxReplace.isSelected(), textAreaReplaceText.getText());
        return new CustomPlaceholder((int) spinnerIndex.getValue(), parsePlayer, keyword, parseKeyword, cooldown, hoverEvent, clickEvent, replaceText, name, description);
    }

    private PatternSyntaxException validRegex(String regex) {
        try {
            Pattern.compile(regex);
            return null;
        } catch (PatternSyntaxException e) {
            return e;
        }
    }

    private NumberFormatException validLong(String longInteger) {
        try {
            Long.parseLong(longInteger);
            return null;
        } catch (NumberFormatException e) {
            return e;
        }
    }

    private int getLinesCount(String lines) {
        return lines.split("\\R").length;
    }

    public void updateConfigOutput(CustomPlaceholder customPlaceholder) {
        PatternSyntaxException error = validRegex(textFieldKeyword.getText());
        if (error != null) {
            textAreaConfigOutput.setForeground(Color.RED);
            textAreaConfigOutput.setText("Invalid Keyword Regex!!\nDetails:\n\n" + error.getClass().getName() + ":\n" + error.getLocalizedMessage());
            return;
        }
        textAreaConfigOutput.setForeground(Color.BLACK);
        String pos = String.valueOf(customPlaceholder.getPosition());
        ConfigurationSection config = ConfigurationSection.newConfigurationSection();
        config.set(pos + ".Name", customPlaceholder.getName());
        config.set(pos + ".Description", customPlaceholder.getDescription());
        config.set(pos + ".ParsePlayer", customPlaceholder.getParsePlayer().toString());
        config.set(pos + ".Keyword", customPlaceholder.getKeyword().pattern());
        config.set(pos + ".ParseKeyword", customPlaceholder.getParseKeyword());
        config.set(pos + ".Cooldown", customPlaceholder.getCooldown());
        config.set(pos + ".Hover.Enable", customPlaceholder.getHover().isEnabled());
        config.set(pos + ".Hover.Text", Arrays.asList(customPlaceholder.getHover().getText().split("\\R")));
        config.set(pos + ".Click.Enable", customPlaceholder.getClick().isEnabled());
        config.set(pos + ".Click.Action", customPlaceholder.getClick().getAction().toString());
        config.set(pos + ".Click.Value", customPlaceholder.getClick().getValue());
        config.set(pos + ".Replace.Enable", customPlaceholder.getReplace().isEnabled());
        config.set(pos + ".Replace.ReplaceText", customPlaceholder.getReplace().getReplaceText());
        textAreaConfigOutput.setText(config.saveToString());

        SwingUtilities.invokeLater(() -> {
            scrollPaneConfigOutput.getHorizontalScrollBar().setValue(0);
            scrollPaneConfigOutput.getVerticalScrollBar().setValue(0);
        });
    }

    public Component processCustomPlaceholder(CustomPlaceholder placeholder, Component component) {
        String replace;
        if (placeholder.getReplace().isEnabled()) {
            replace = ChatColorUtils.translateAlternateColorCodes('&', placeholder.getReplace().getReplaceText(), false, false, Collections.emptyList());
        } else {
            replace = null;
        }

        return ComponentReplacing.replace(component, placeholder.getKeyword().pattern(), true, (result, matchedComponents) -> {
            Component replaceComponent;
            if (placeholder.getReplace().isEnabled()) {
                replaceComponent = LegacyComponentSerializer.legacySection().deserialize(CustomStringUtils.applyReplacementRegex(replace, result, 1));
            } else {
                replaceComponent = Component.empty().children(matchedComponents);
            }
            if (placeholder.getHover().isEnabled()) {
                replaceComponent = replaceComponent.hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT, LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', CustomStringUtils.applyReplacementRegex(placeholder.getHover().getText(), result, 1), false, false, Collections.emptyList()))));
            }
            if (placeholder.getClick().isEnabled()) {
                String clicktext = CustomStringUtils.applyReplacementRegex(placeholder.getClick().getValue(), result, 1);
                replaceComponent = replaceComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(placeholder.getClick().getAction().name()), clicktext));
            }
            return replaceComponent;
        });
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(11, 7, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        label1.setToolTipText("This name is used to identify your placeholder, it must be a non-regex form of your keyword and should satisfy your keyword");
        panel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldName = new JTextField();
        textFieldName.setToolTipText("This name is used to identify your placeholder, it must be a non-regex form of your keyword and should satisfy your keyword");
        panel.add(textFieldName, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Description");
        label2.setToolTipText("This description is used when listing placeholders");
        panel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldDescription = new JTextField();
        textFieldDescription.setToolTipText("This description is used when listing placeholders");
        panel.add(textFieldDescription, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Keyword");
        label3.setToolTipText("The keyword to look for in the chat (in regex)");
        panel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldKeyword = new JTextField();
        textFieldKeyword.setToolTipText("The keyword to look for in the chat (in regex)");
        panel.add(textFieldKeyword, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("ParsePlayer");
        label4.setToolTipText("Should the placeholders be parsed with the message sender or the receiver");
        panel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        boxParsePlayer = new JComboBox();
        boxParsePlayer.setToolTipText("Should the placeholders be parsed with the message sender or the receiver");
        panel.add(boxParsePlayer, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldCooldown = new JTextField();
        textFieldCooldown.setToolTipText("Cooldown of this placeholder in seconds");
        panel.add(textFieldCooldown, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Cooldown");
        label5.setToolTipText("Cooldown of this placeholder in seconds");
        panel.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Text");
        label6.setToolTipText("Text for the hover message");
        panel.add(label6, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        boxClickAction = new JComboBox();
        boxClickAction.setToolTipText("The action to do when clicked");
        panel.add(boxClickAction, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Value");
        label7.setToolTipText("The value for the action above, for example, '/say yellow is her fav color' for the action RUN_COMMAND");
        panel.add(label7, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldClickValue = new JTextField();
        textFieldClickValue.setToolTipText("The value for the action above, for example, '/say yellow is her fav color' for the action RUN_COMMAND");
        panel.add(textFieldClickValue, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("ReplaceText");
        label8.setToolTipText("The text to replace the keyword");
        panel.add(label8, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Action");
        label9.setToolTipText("The action to do when clicked");
        panel.add(label9, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("ParseKeyword");
        label10.setToolTipText("Whether to parse placeholders in the keyword text");
        panel.add(label10, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxParseKeyword = new JCheckBox();
        checkBoxParseKeyword.setSelected(false);
        checkBoxParseKeyword.setText("Enable");
        checkBoxParseKeyword.setToolTipText("Whether to parse placeholders in the keyword text");
        panel.add(checkBoxParseKeyword, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollTestOutput = new JScrollPane();
        panel.add(scrollTestOutput, new GridConstraints(9, 3, 2, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaTestOutput = new JTextArea();
        textAreaTestOutput.setEditable(false);
        scrollTestOutput.setViewportView(textAreaTestOutput);
        scrollReplaceText = new JScrollPane();
        panel.add(scrollReplaceText, new GridConstraints(10, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaReplaceText = new JTextArea();
        textAreaReplaceText.setToolTipText("The text to replace the keyword");
        scrollReplaceText.setViewportView(textAreaReplaceText);
        scrollHoverText = new JScrollPane();
        panel.add(scrollHoverText, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaHover = new JTextArea();
        textAreaHover.setText("");
        textAreaHover.setToolTipText("Text for the hover message");
        scrollHoverText.setViewportView(textAreaHover);
        scrollPaneConfigOutput = new JScrollPane();
        panel.add(scrollPaneConfigOutput, new GridConstraints(1, 3, 7, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaConfigOutput = new JTextArea();
        textAreaConfigOutput.setEditable(false);
        scrollPaneConfigOutput.setViewportView(textAreaConfigOutput);
        checkBoxHover = new JCheckBox();
        checkBoxHover.setSelected(true);
        checkBoxHover.setText("Hover");
        checkBoxHover.setToolTipText("Add a hover message");
        panel.add(checkBoxHover, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxClick = new JCheckBox();
        checkBoxClick.setSelected(true);
        checkBoxClick.setText("Click");
        checkBoxClick.setToolTipText("Add a click action");
        panel.add(checkBoxClick, new GridConstraints(8, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Test Chat");
        label11.setToolTipText("Test your CustomPlaceholder");
        panel.add(label11, new GridConstraints(8, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldTestChat = new JTextField();
        textFieldTestChat.setToolTipText("Test your CustomPlaceholder");
        panel.add(textFieldTestChat, new GridConstraints(8, 4, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        checkBoxReplace = new JCheckBox();
        checkBoxReplace.setSelected(true);
        checkBoxReplace.setText("Replace");
        checkBoxReplace.setToolTipText("Whether the keyword should be replaced with another text");
        panel.add(checkBoxReplace, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Index");
        label12.setToolTipText("Make sure your CustomPlaceholders are indexed correctly starting from 1");
        panel.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerIndex.setToolTipText("Make sure your CustomPlaceholders are indexed correctly starting from 1");
        panel.add(spinnerIndex, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Config Output (Copy this to your actual config)");
        panel.add(label13, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonDefaultExamples = new JButton();
        buttonDefaultExamples.setText("Default Examples");
        panel.add(buttonDefaultExamples, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonImport = new JButton();
        buttonImport.setText("Import from Config");
        panel.add(buttonImport, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

    private static class IntFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            if (test(sb.toString())) {
                super.insertString(fb, offset, string, attr);
            }
        }

        private boolean test(String text) {
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            if (test(sb.toString())) {
                super.replace(fb, offset, length, text, attrs);
            }

        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset + length);

            if (sb.toString().length() == 0) {
                super.replace(fb, offset, length, "", null);
            } else {
                if (test(sb.toString())) {
                    super.remove(fb, offset, length);
                }
            }
        }

    }

    private class ConfigOutputListener implements KeyListener, ActionListener, ChangeListener {

        @Override
        public void keyTyped(KeyEvent e) {
            //do nothing
        }

        @Override
        public void keyPressed(KeyEvent e) {
            //do nothing
        }

        @Override
        public void keyReleased(KeyEvent e) {
            updateConfigOutput(toCustomPlaceholder());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateConfigOutput(toCustomPlaceholder());
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateConfigOutput(toCustomPlaceholder());
        }

    }

}
