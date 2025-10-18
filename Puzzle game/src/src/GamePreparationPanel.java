package src;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class GamePreparationPanel extends JPanel {
    private PuzzleGame game;
    private JLabel backgroundLabel;
    private JPanel overlayPanel; // 单独定义覆盖面板，便于控制层级

    public GamePreparationPanel(PuzzleGame game) {
        this.game = game;
        setLayout(null); // 使用绝对布局精确控制组件位置
        setOpaque(true);
        setBackground(Color.BLACK); // 整体面板设为黑色，方便观察问题

        // 初始化背景和覆盖面板
        initBackgroundLabel();
        createOverlayPanel();

        // 确保层级正确：背景在最底层，覆盖面板在上方
        setComponentZOrder(backgroundLabel, 1);
        setComponentZOrder(overlayPanel, 0);
    }

    // 初始化背景标签
    private void initBackgroundLabel() {
        backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 0, 0); // 初始大小为0，后续会调整
        backgroundLabel.setOpaque(false); // 背景标签不透明，确保能看到图片
        backgroundLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundLabel.setVerticalAlignment(SwingConstants.CENTER);

        // 添加到面板
        add(backgroundLabel);

        // 选择并设置背景图片
        selectAndSetBackgroundImage();
    }

    // 选择并设置背景图片
    private void selectAndSetBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择动态背景图片（支持GIF格式）");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".gif") ||
                        f.getName().toLowerCase().endsWith(".jpg") ||
                        f.getName().toLowerCase().endsWith(".jpeg") ||
                        f.getName().toLowerCase().endsWith(".png");
            }
            @Override
            public String getDescription() {
                return "图片文件 (*.gif, *.jpg, *.jpeg, *.png)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            setDefaultBackground();
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        try {
            // 直接尝试加载图片，不显示任何弹窗
            ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
            Image image = icon.getImage();

            // 简单验证图片有效性
            if (image.getWidth(this) > 0 && image.getHeight(this) > 0) {
                backgroundLabel.setIcon(icon);
                backgroundLabel.setOpaque(false);
                System.out.println("背景图片加载成功: " + selectedFile.getAbsolutePath());
            } else {
                // 加载失败仅在控制台输出，不弹窗
                System.out.println("背景图片加载失败: 无效的图片尺寸");
                setDefaultBackground();
            }
        } catch (Exception e) {
            // 异常情况仅在控制台输出，不弹窗
            System.out.println("背景图片处理异常: " + e.getMessage());
            e.printStackTrace();
            setDefaultBackground();
        }
    }

    // 优化默认背景设置方法，避免混淆
    private void setDefaultBackground() {
        backgroundLabel.setIcon(null);
        backgroundLabel.setBackground(new Color(30, 30, 60));
        backgroundLabel.setOpaque(true);
        // 移除可能的"未选择图片"提示，仅在控制台输出以便调试
        System.out.println("使用默认背景（未选择图片或加载失败）");
    }

    // 创建覆盖面板（包含按钮和标题）
    private void createOverlayPanel() {
        overlayPanel = new JPanel(new GridBagLayout());
        overlayPanel.setOpaque(false); // 关键：覆盖面板必须透明
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // 标题
        JLabel titleLabel = new JLabel("拼图游戏");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 50, 0);
        overlayPanel.add(titleLabel, gbc);

        // 按钮
        gbc.gridy = 1;
        overlayPanel.add(createButton("开始游戏", e -> game.showPreLaunchPanel()), gbc);

        gbc.gridy = 2;
        overlayPanel.add(createButton("设置", e -> showSettings()), gbc);

        gbc.gridy = 3;
        overlayPanel.add(createButton("About（如何开发）", e -> showAbout()), gbc);

        add(overlayPanel);
    }

    // 当窗口大小改变时调整组件大小
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        // 调整背景标签大小以填充整个面板
        if (backgroundLabel != null) {
            backgroundLabel.setBounds(0, 0, width, height);

            // 调整图片大小
            Icon icon = backgroundLabel.getIcon();
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image image = imageIcon.getImage().getScaledInstance(
                        width, height, Image.SCALE_DEFAULT);
                backgroundLabel.setIcon(new ImageIcon(image));
            }
        }

        // 调整覆盖面板大小和位置（居中）
        if (overlayPanel != null) {
            overlayPanel.setBounds(0, 0, width, height);
        }
    }

    // 创建按钮
    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 24));
        button.setPreferredSize(new Dimension(250, 60));
        button.setBackground(new Color(70, 130, 180, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 149, 237, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180, 200));
            }
        });

        return button;
    }

    // 显示设置
    private void showSettings() {
        JOptionPane.showMessageDialog(this, "设置功能待实现", "设置", JOptionPane.INFORMATION_MESSAGE);
    }

    // 显示关于
    private void showAbout() {
        String message = "拼图游戏 v1.0\n\n" +
                "开发说明：\n" +
                "1. 使用Java Swing开发\n" +
                "2. 支持自定义动态背景图片\n" +
                "3. 包含多级难度选择\n" +
                "4. 支持随机图片和自定义图片";
        JOptionPane.showMessageDialog(this, message, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
}
