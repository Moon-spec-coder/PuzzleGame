package src;


import javax.swing.*;
import java.awt.*;

public class TestPanel extends JPanel {
    private PuzzleGame game;
    private JTextArea testOutput;

    public TestPanel(PuzzleGame game) {
        this.game = game;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 标题
        JLabel titleLabel = new JLabel("测试界面");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 测试内容区域
        JPanel testContent = new JPanel(new BorderLayout(10, 10));

        // 测试按钮面板
        JPanel testButtons = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton testDifficultyButton = new JButton("测试难度设置");
        testDifficultyButton.addActionListener(e -> testDifficultySettings());

        JButton setEasyButton = new JButton("设置为简单难度");
        setEasyButton.addActionListener(e -> {
            game.setCurrentDifficulty(0);
            testOutput.append("已设置为简单难度\n当前难度: " + game.getCurrentDifficulty() + "\n\n");
        });

        JButton setMediumButton = new JButton("设置为中等难度");
        setMediumButton.addActionListener(e -> {
            game.setCurrentDifficulty(1);
            testOutput.append("已设置为中等难度\n当前难度: " + game.getCurrentDifficulty() + "\n\n");
        });

        JButton setHardButton = new JButton("设置为困难难度");
        setHardButton.addActionListener(e -> {
            game.setCurrentDifficulty(2);
            testOutput.append("已设置为困难难度\n当前难度: " + game.getCurrentDifficulty() + "\n\n");
        });

        testButtons.add(testDifficultyButton);
        testButtons.add(setEasyButton);
        testButtons.add(setMediumButton);
        testButtons.add(setHardButton);

        // 测试输出区域
        testOutput = new JTextArea();
        testOutput.setFont(new Font("Monospaced", Font.PLAIN, 14));
        testOutput.setLineWrap(true);
        testOutput.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(testOutput);
        scrollPane.setBorder(BorderFactory.createTitledBorder("测试结果"));

        testContent.add(testButtons, BorderLayout.WEST);
        testContent.add(scrollPane, BorderLayout.CENTER);

        add(testContent, BorderLayout.CENTER);

        // 返回按钮
        JButton backButton = new JButton("返回准备界面");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        backButton.addActionListener(e -> game.showPreparationPanel());
        add(backButton, BorderLayout.SOUTH);
    }

    // 测试难度设置
    private void testDifficultySettings() {
        testOutput.append("=== 测试难度设置 ===\n");
        testOutput.append("当前选中的难度级别: " + game.getCurrentDifficulty() + "\n");
        testOutput.append("简单难度配置: " + java.util.Arrays.toString(game.getDifficultySettings(0)) + "\n");
        testOutput.append("中等难度配置: " + java.util.Arrays.toString(game.getDifficultySettings(1)) + "\n");
        testOutput.append("困难难度配置: " + java.util.Arrays.toString(game.getDifficultySettings(2)) + "\n");
        testOutput.append("无效难度配置: " + java.util.Arrays.toString(game.getDifficultySettings(3)) + "\n\n");
    }

    // 测试图片处理
    private void testImageProcessing() {
        testOutput.append("测试图片处理功能...\n");
        testOutput.append("图片加载和切割功能待实现\n\n");
    }

    // 测试拼图逻辑
    private void testPuzzleLogic() {
        testOutput.append("测试拼图逻辑...\n");
        testOutput.append("拼图打乱和验证功能待实现\n\n");
    }
}
