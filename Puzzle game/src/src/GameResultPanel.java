package src;


import javax.swing.*;
import java.awt.*;

public class GameResultPanel extends JPanel {
    public GameResultPanel(PuzzleGame game, long totalTime) {
        setLayout(new BorderLayout());
        JLabel msg = new JLabel("🎉 恭喜通关！", SwingConstants.CENTER);
        msg.setFont(new Font("微软雅黑", Font.BOLD, 36));
        JLabel timeLabel = new JLabel("总用时：" + totalTime + " 秒", SwingConstants.CENTER);
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 24));

        JButton backButton = new JButton("返回主菜单");
        backButton.addActionListener(e -> game.showPreparationPanel());
        JPanel btnPanel = new JPanel();
        btnPanel.add(backButton);

        add(msg, BorderLayout.NORTH);
        add(timeLabel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}

