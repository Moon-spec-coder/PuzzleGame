package src;
import javax.swing.*;
import java.awt.image.BufferedImage;
public class PuzzleGame {
    // 游戏主窗口
    private JFrame mainFrame;

    // 各个界面面板
    private GamePreparationPanel preparationPanel;
    private GamePreLaunchPanel preLaunchPanel;
    private GamePlayPanel playPanel; // 不提前创建实例
    private TestPanel testPanel;

    // 图片状态变量
    private boolean useRandomImage;
    private BufferedImage randomNetworkImage;
    private String imagePath;

    // 新增：难度管理变量
    private int currentDifficulty; // 当前难度级别 0=简单,1=中等,2=困难

    public PuzzleGame() {
        // 初始化主窗口
        mainFrame = new JFrame("拼图游戏");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        // 初始化固定面板（仅创建一次）
        preparationPanel = new GamePreparationPanel(this);
        preLaunchPanel = new GamePreLaunchPanel(this);
        testPanel = new TestPanel(this);

        // 初始化默认难度
        currentDifficulty = 0; // 默认简单难度

        // 显示初始界面
        showPreparationPanel();
    }

    public void showResultPanel(long totalTime) {
        mainFrame.setContentPane(new GameResultPanel(this, totalTime));
        mainFrame.setVisible(true);
    }





    // 显示游戏准备界面
    public void showPreparationPanel() {
        mainFrame.setContentPane(preparationPanel);
        mainFrame.setVisible(true);
    }

    // 显示游戏预启动界面
    public void showPreLaunchPanel() {
        mainFrame.setContentPane(preLaunchPanel);
        mainFrame.setVisible(true);
    }

    // 显示游戏游玩界面
    public void showPlayPanel() {
        playPanel = new GamePlayPanel(this); // 每次进入都创建新实例
        mainFrame.setContentPane(playPanel);
        mainFrame.revalidate(); // 强制刷新布局
        mainFrame.repaint();    // 强制重绘界面
        mainFrame.setVisible(true);
    }

    // 显示测试界面
    public void showTestPanel() {
        mainFrame.setContentPane(testPanel);
        mainFrame.setVisible(true);
    }

    // 图片状态相关set/get方法（保持不变）
    public void setUseRandomImage(boolean useRandom) {
        this.useRandomImage = useRandom;
        System.out.println("PuzzleGame设置useRandomImage: " + useRandom);
    }

    public void setRandomNetworkImage(BufferedImage image) {
        this.randomNetworkImage = image;
    }

    public BufferedImage getRandomNetworkImage() {
        return randomNetworkImage;
    }

    public void setImagePath(String path) {
        if (path == null) {
            System.out.println("警告：尝试设置空路径，已忽略");
            return;
        }
        this.imagePath = path;
        System.out.println("PuzzleGame已保存图片路径: " + path);
    }

    public String getImagePath() {
        System.out.println("PuzzleGame返回图片路径: " + (imagePath == null ? "null" : imagePath));
        return imagePath;
    }

    public boolean isUseRandomImage() {
        return useRandomImage;
    }

    // 难度相关方法（新增和修改）
    public int getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(int difficulty) {
        // 确保难度值在有效范围内
        if (difficulty >= 0 && difficulty <= 2) {
            currentDifficulty = difficulty;
        }
    }

    public int[] getDifficultySettings(int difficulty) {
        switch(difficulty) {
            case 0: return new int[]{3, 3};
            case 1: return new int[]{5, 5};
            case 2: return new int[]{7, 7};
            default: return new int[]{3, 3};
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PuzzleGame());
    }
}
