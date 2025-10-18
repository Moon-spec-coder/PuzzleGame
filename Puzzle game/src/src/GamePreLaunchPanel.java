package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
public class GamePreLaunchPanel extends JPanel {
    private PuzzleGame game;
    private JRadioButton randomImageRadio;
    private JRadioButton customImageRadio;
    private JRadioButton easyRadio;
    private JRadioButton mediumRadio;
    private JRadioButton hardRadio;
    private JButton selectImageButton;
    private JButton startGameButton;
    private File selectedImageFile;
    private BufferedImage randomNetworkImage;
    private JPanel imageSelectionPanel;
    private JRadioButton lastSelectedDifficultyRadio;

    public GamePreLaunchPanel(PuzzleGame game) {
        this.game = game;
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        setBackground(new Color(240, 240, 240));

        // 标题
        JLabel titleLabel = new JLabel("游戏设置");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 主内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(240, 240, 240));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // 图片选择面板
        imageSelectionPanel = createImageSelectionPanel();
        // 难度选择面板
        JPanel difficultyPanel = createDifficultyPanel();

        contentPanel.add(imageSelectionPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(difficultyPanel);
        add(contentPanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // 图片选择按钮交互
        initImageSelectionListeners();
    }

    // 图片选择面板（保持原有功能）
    private JPanel createImageSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "图片选择",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("微软雅黑", Font.PLAIN, 18)));
        panel.setBackground(new Color(240, 240, 240));

        ButtonGroup imageGroup = new ButtonGroup();
        randomImageRadio = new JRadioButton("随机生成图片");
        customImageRadio = new JRadioButton("自行导入图片");
        setRadioButtonStyle(randomImageRadio);
        setRadioButtonStyle(customImageRadio);
        randomImageRadio.setSelected(true);

        selectImageButton = new JButton("选择图片...");
        selectImageButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        selectImageButton.setEnabled(false);
        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (customImageRadio.isSelected()) {
                    selectImageButton.setEnabled(true);
                    selectImageFile();
                }
            }
        });

        JPanel radioPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel1.setBackground(new Color(240, 240, 240));
        radioPanel1.add(randomImageRadio);

        JPanel radioPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel2.setBackground(new Color(240, 240, 240));
        radioPanel2.add(customImageRadio);
        radioPanel2.add(selectImageButton);

        panel.add(radioPanel1);
        panel.add(radioPanel2);
        return panel;
    }

    // 难度选择面板（修复：将选择的难度保存到游戏实例）
    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "难度选择",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("微软雅黑", Font.PLAIN, 18)));
        panel.setBackground(new Color(240, 240, 240));

        ButtonGroup difficultyGroup = new ButtonGroup();
        easyRadio = new JRadioButton("简单 (3x3)");
        mediumRadio = new JRadioButton("中等 (5x5)");
        hardRadio = new JRadioButton("困难 (7x7)");
        setRadioButtonStyle(easyRadio);
        setRadioButtonStyle(mediumRadio);
        setRadioButtonStyle(hardRadio);

        // 初始化默认选中
        easyRadio.setSelected(true);
        lastSelectedDifficultyRadio = easyRadio;
        highlightDifficultyRadio(easyRadio);

        // 关键修复：将选择的难度保存到游戏实例
        easyRadio.addActionListener(e -> {
            if (easyRadio.isSelected() && lastSelectedDifficultyRadio != easyRadio) {
                highlightDifficultyRadio(easyRadio);
                lastSelectedDifficultyRadio = easyRadio;
                game.setCurrentDifficulty(0); // 保存难度选择
                System.out.println("已选择难度：简单 (3x3)");
            }
        });

        mediumRadio.addActionListener(e -> {
            if (mediumRadio.isSelected() && lastSelectedDifficultyRadio != mediumRadio) {
                highlightDifficultyRadio(mediumRadio);
                lastSelectedDifficultyRadio = mediumRadio;
                game.setCurrentDifficulty(1); // 保存难度选择
                System.out.println("已选择难度：中等 (5x5)");
            }
        });

        hardRadio.addActionListener(e -> {
            if (hardRadio.isSelected() && lastSelectedDifficultyRadio != hardRadio) {
                highlightDifficultyRadio(hardRadio);
                lastSelectedDifficultyRadio = hardRadio;
                game.setCurrentDifficulty(2); // 保存难度选择
                System.out.println("已选择难度：困难 (7x7)");
            }
        });

        difficultyGroup.add(easyRadio);
        difficultyGroup.add(mediumRadio);
        difficultyGroup.add(hardRadio);

        JPanel diffPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diffPanel1.setBackground(new Color(240, 240, 240));
        diffPanel1.add(easyRadio);

        JPanel diffPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diffPanel2.setBackground(new Color(240, 240, 240));
        diffPanel2.add(mediumRadio);

        JPanel diffPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diffPanel3.setBackground(new Color(240, 240, 240));
        diffPanel3.add(hardRadio);

        panel.add(diffPanel1);
        panel.add(diffPanel2);
        panel.add(diffPanel3);
        return panel;
    }

    private void highlightDifficultyRadio(JRadioButton selectedRadio) {
        easyRadio.setBackground(new Color(240, 240, 240));
        mediumRadio.setBackground(new Color(240, 240, 240));
        hardRadio.setBackground(new Color(240, 240, 240));
        selectedRadio.setBackground(new Color(220, 230, 245));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(new Color(240, 240, 240));

        JButton backButton = new JButton("返回");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        backButton.addActionListener(e -> game.showPreparationPanel());

        startGameButton = new JButton("开始游戏");
        startGameButton.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        startGameButton.addActionListener(e -> startGame());

        setButtonStyle(backButton);
        setButtonStyle(startGameButton);

        panel.add(backButton);
        panel.add(startGameButton);
        return panel;
    }

    private void initImageSelectionListeners() {
        randomImageRadio.addActionListener(e -> {
            if (randomImageRadio.isSelected()) {
                selectImageButton.setEnabled(false);
                selectedImageFile = null;
                customImageRadio.setSelected(false);
                System.out.println("已切换到：随机生成图片模式");
            }
        });

        customImageRadio.addActionListener(e -> {
            if (customImageRadio.isSelected()) {
                selectImageButton.setEnabled(true);
                selectImageButton.requestFocus();
                randomImageRadio.setSelected(false);
                System.out.println("已切换到：自行导入图片模式，选择按钮已启用");
            }
        });

        selectImageButton.addActionListener(e -> {
            if (!customImageRadio.isSelected()) {
                customImageRadio.setSelected(true);
                randomImageRadio.setSelected(false);
            }
            selectImageFile();
        });
    }

    private void selectImageFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "图片文件 (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));

            if (selectedImageFile != null && selectedImageFile.getParentFile() != null) {
                fileChooser.setCurrentDirectory(selectedImageFile.getParentFile());
            }

            System.out.println("开始选择图片...");
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fileChooser.getSelectedFile();
                validateSelectedImage();
            } else {
                System.out.println("用户取消了图片选择");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "打开图片选择器失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void validateSelectedImage() {
        try {
            ImageIcon tempIcon = new ImageIcon(selectedImageFile.getAbsolutePath());
            Image tempImg = tempIcon.getImage();
            if (tempImg.getWidth(this) <= 0 || tempImg.getHeight(this) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "选择的文件不是有效的图片", "验证失败", JOptionPane.WARNING_MESSAGE);
                selectedImageFile = null;
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "已选择图片: " + selectedImageFile.getName() +
                            "\n尺寸: " + tempImg.getWidth(this) + "x" + tempImg.getHeight(this),
                    "选择成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "图片验证失败: " + e.getMessage(), "验证失败", JOptionPane.WARNING_MESSAGE);
            selectedImageFile = null;
        }
    }

    private void startGame() {
        if (!randomImageRadio.isSelected() && !customImageRadio.isSelected()) {
            JOptionPane.showMessageDialog(this,
                    "请选择图片来源（随机生成或自行导入）", "选择提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (customImageRadio.isSelected() && selectedImageFile == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一张图片", "选择提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int difficulty = getSelectedDifficulty();
        int[] dimensions = game.getDifficultySettings(difficulty);
        String imageSourceDesc = "";

        if (randomImageRadio.isSelected()) {
            imageSourceDesc = "随机网络图片（开发中）";
            game.setUseRandomImage(true);
            game.setRandomNetworkImage(null);
            System.out.println("随机图片模式 - useRandomImage: " + game.isUseRandomImage());
        } else {
            imageSourceDesc = "自定义图片: " + selectedImageFile.getName();
            String absolutePath = selectedImageFile.getAbsolutePath();
            game.setRandomNetworkImage(null);
            game.setUseRandomImage(false);
            game.setImagePath(absolutePath);
            System.out.println("本地图片路径 - 选择: " + absolutePath + ", 保存: " + game.getImagePath());
        }

        showGameInfo(imageSourceDesc, difficulty, dimensions);
    }

    private int getSelectedDifficulty() {
        if (mediumRadio.isSelected()) return 1;
        if (hardRadio.isSelected()) return 2;
        return 0;
    }

    private void showGameInfo(String imageSource, int difficulty, int[] dimensions) {
        String diffName = difficulty == 0 ? "简单" : (difficulty == 1 ? "中等" : "困难");
        String message = String.format(
                "游戏设置:\n图片来源: %s\n难度: %s (%dx%d)\n\n准备开始游戏!",
                imageSource, diffName, dimensions[0], dimensions[1]
        );
        JOptionPane.showMessageDialog(this, message, "准备就绪", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(() -> game.showPlayPanel());
    }

    private void setRadioButtonStyle(JRadioButton radio) {
        radio.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        radio.setBackground(new Color(240, 240, 240));
        radio.setPreferredSize(new Dimension(180, 30));
    }

    private void setButtonStyle(JButton button) {
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
    }

    private JDialog createLoadingDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "加载中", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 240, 240));

        JLabel label = new JLabel("正在获取随机图片...");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        panel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("取消");
        setButtonStyle(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(240, 240, 240));
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        return dialog;
    }
}
