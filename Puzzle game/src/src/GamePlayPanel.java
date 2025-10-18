package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GamePlayPanel extends JPanel {
    private PuzzleGame game;
    private BufferedImage displayImage;
    private JLabel errorLabel;
    private JPanel completedPanel; // 已拼部分
    private JPanel piecesPanel;    // 待选择的拼图容器
    private JScrollPane piecesScrollPane; // 支持滑动的容器
    private JPanel piecesContainer; // 实际存放拼图块的面板
    private JMenuBar menuBar;
    private JMenu gameMenu;
    private JMenuItem undoItem;
    private JMenuItem autoCompleteItem;
    private JMenuItem tipsItem;
    private int difficulty; // 当前难度级别
    private int rows, cols; // 拼图行列数
    private String imageName; // 图片名称，用于标识拼图块
    private JDialog previewDialog; // 长按预览对话框
    private Timer scrollTimer; // 用于滑动惯性效果
    private int scrollSpeed; // 滑动速度

    // 核心交互变量
    // selectedPiece 现在存储的是 PuzzlePiecePanel 实例
    private JPanel selectedPiece = null; // 当前选中的拼图块（两次点击用）
    private int selectedPieceIndex = -1; // 当前选中的拼图块索引
    private JPanel[][] completedGrid; // 已完成区域的网格
    // availablePieces 现在存储的是 PuzzlePiecePanel 实例
    private List<JPanel> availablePieces = new ArrayList<>(); // 可用拼图块列表
    private boolean isDraggingPiece = false; // 是否正在拖动拼图块
    private JLabel draggedPieceLabel = null; // 拖动时显示的临时标签

    // === 新增功能变量 ===
    private Timer gameTimer;         // 游戏计时器
    private long startTime;          // 记录游戏开始时间
    private JLabel timerLabel;       // 显示计时
    private int placedCount = 0;     // 当前已放置的拼图数量
    private BufferedImage[][] puzzlePieces; // 切割后的图像块


    public GamePlayPanel(PuzzleGame game) {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 从主类获取难度信息
        this.difficulty = game.getCurrentDifficulty();
        int[] dims = game.getDifficultySettings(difficulty);
        this.rows = dims[0];
        this.cols = dims[1];
        this.imageName = "未知图片";

        // 初始化工具栏与支持组件
        initMenuBar();
        initPreviewDialog();
        initScrollTimer();

        // === 新增功能：顶部计时栏 ===
        JPanel topContainer = new JPanel(new BorderLayout());

        // 中间标题标签（原来的标题）
        JLabel titleLabel = new JLabel("拼图游戏 - 游玩中 (" + rows + "x" + cols + ")");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 右侧计时器显示
        timerLabel = new JLabel("用时: 0 秒", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // 左侧菜单栏（原本就存在）
        topContainer.add(menuBar, BorderLayout.WEST);
        topContainer.add(titleLabel, BorderLayout.CENTER);
        topContainer.add(timerLabel, BorderLayout.EAST);

        // 将整个顶部容器放入窗口上方
        add(topContainer, BorderLayout.NORTH);

        // 错误提示区域
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        errorLabel.setForeground(Color.RED);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        errorLabel.setVisible(false);

        // 主内容区（上方拼图区 + 下方拼图块区）
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(errorLabel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);

        // 上半部分：已完成拼图区
        completedPanel = createCompletedPanel();
        splitPane.setTopComponent(completedPanel);

        // 下半部分：待拼拼图块
        piecesPanel = createPiecesPanel();
        splitPane.setBottomComponent(piecesPanel);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // 底部按钮区
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton backButton = new JButton("返回");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        backButton.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            game.showPreLaunchPanel();
        });

        JButton restartButton = new JButton("重新开始");
        restartButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        restartButton.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();
            game.showPlayPanel();
        });

        buttonPanel.add(restartButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 初始化显示图片
        initDisplayImage();

        // === 新增功能：启动计时器 ===
        startTimer();

        // 拖拽监听器
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handlePieceDragging(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                handlePieceDrop(e);
            }
        });
    }


    // 初始化长按预览对话框
    private void initPreviewDialog() {
        previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "预览", false);
        previewDialog.setUndecorated(true);
        previewDialog.setSize(200, 200);
        previewDialog.setBackground(new Color(255, 255, 255, 240));
        previewDialog.setAlwaysOnTop(true);
    }

    // 初始化滚动计时器，用于实现滑动惯性
    private void initScrollTimer() {
        scrollTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar scrollBar = piecesScrollPane.getHorizontalScrollBar();
                int newValue = scrollBar.getValue() + scrollSpeed;

                // 限制滚动范围
                if (newValue < scrollBar.getMinimum()) {
                    newValue = scrollBar.getMinimum();
                    scrollTimer.stop();
                } else if (newValue > scrollBar.getMaximum() - scrollBar.getVisibleAmount()) {
                    newValue = scrollBar.getMaximum() - scrollBar.getVisibleAmount();
                    scrollTimer.stop();
                }

                scrollBar.setValue(newValue);

                // 逐渐减速
                if (scrollSpeed > 0) {
                    scrollSpeed--;
                    if (scrollSpeed == 0) scrollTimer.stop();
                } else if (scrollSpeed < 0) {
                    scrollSpeed++;
                    if (scrollSpeed == 0) scrollTimer.stop();
                }
            }
        });
    }

    // 创建已拼部分面板 - 关键修改：单元格使用 BorderLayout
    private JPanel createCompletedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "已完成部分",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("微软雅黑", Font.PLAIN, 16)));

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        gridPanel.setBackground(Color.LIGHT_GRAY);

        // 初始化网格引用
        completedGrid = new JPanel[rows][cols];

        // 初始显示空白拼图格
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // *** 核心修改：使用 BorderLayout，以支持组件填充单元格 ***
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBackground(Color.WHITE);
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                gridPanel.add(cell);
                completedGrid[i][j] = cell;

                // 为每个单元格添加点击事件（两次点击放置的第二步）
                int row = i;
                int col = j;
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleCellClick(row, col);
                    }
                });
            }
        }

        panel.add(new JScrollPane(gridPanel), BorderLayout.CENTER);
        return panel;
    }

    // 处理单元格点击（两次点击放置的第二步 - 保留原有逻辑）
    private void handleCellClick(int row, int col) {
        // 如果已有选中的拼图块，则直接放置
        if (selectedPiece != null && selectedPieceIndex != -1) {
            placePiece(row, col, selectedPieceIndex);

            // 清除选中状态
            selectedPiece.setBorder(null);
            selectedPiece = null;
            selectedPieceIndex = -1;
        }
    }

    // 放置拼图块 - 关键修改：移动 PuzzlePiecePanel 实例
    private void placePiece(int targetRow, int targetCol, int pieceIndex) {
        JPanel pieceToMove = availablePieces.get(pieceIndex);
        JPanel targetCell = completedGrid[targetRow][targetCol];
        targetCell.removeAll();

        // === 自适应缩放（如果已加） ===
        Dimension cellSize = targetCell.getSize();
        pieceToMove.setPreferredSize(new Dimension(cellSize.width, cellSize.height));
        pieceToMove.revalidate();

        targetCell.add(pieceToMove, BorderLayout.CENTER);
        targetCell.revalidate();
        targetCell.repaint();

        piecesContainer.revalidate();
        piecesContainer.repaint();

        // === 新增功能：检测通关 ===
        placedCount++;
        if (placedCount == rows * cols) { // 所有拼图都放入格子
            gameTimer.stop(); // 停止计时
            long totalTime = (System.currentTimeMillis() - startTime) / 1000; // 计算用时
            SwingUtilities.invokeLater(() -> game.showResultPanel(totalTime)); // 显示结算界面
        }
    }


    // 创建待选择拼图面板（支持左右滑动）
    private JPanel createPiecesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(235, 235, 235));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "待选择拼图块",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("微软雅黑", Font.PLAIN, 16)));

        // 创建可水平滚动的容器
        piecesScrollPane = new JScrollPane();
        piecesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        piecesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        piecesScrollPane.setBorder(null);

        // 创建存放拼图块的面板
        piecesContainer = new JPanel();
        piecesContainer.setLayout(new BoxLayout(piecesContainer, BoxLayout.X_AXIS));
        piecesContainer.setBackground(new Color(235, 235, 235));
        piecesContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        piecesContainer.add(Box.createHorizontalStrut(5));

        // 添加拼图块
        addPuzzlePieces(piecesContainer);

        // 添加到滚动面板
        piecesScrollPane.setViewportView(piecesContainer);

        // 支持鼠标拖动滚动
        installScrollByDragging();

        panel.add(piecesScrollPane, BorderLayout.CENTER);
        return panel;
    }

    // 实现鼠标拖动滚动功能
    private void installScrollByDragging() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private int pressX;
            private int currentX;
            private boolean isDragging = false;
            private long lastDragTime;

            @Override
            public void mousePressed(MouseEvent e) {
                scrollTimer.stop();
                pressX = e.getX();
                currentX = pressX;
                isDragging = true;
                lastDragTime = System.currentTimeMillis();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDragging) return;

                currentX = e.getX();
                int deltaX = pressX - currentX;
                long currentTime = System.currentTimeMillis();

                long timeDelta = currentTime - lastDragTime;
                if (timeDelta > 0) {
                    scrollSpeed = deltaX / (int)Math.max(1, timeDelta / 10);
                }

                JScrollBar scrollBar = piecesScrollPane.getHorizontalScrollBar();
                int newPosition = scrollBar.getValue() + deltaX;
                scrollBar.setValue(newPosition);

                pressX = currentX;
                lastDragTime = currentTime;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;

                if (Math.abs(scrollSpeed) > 2) {
                    if (scrollSpeed > 15) scrollSpeed = 15;
                    else if (scrollSpeed < -15) scrollSpeed = -15;
                    scrollTimer.start();
                }
            }
        };

        piecesScrollPane.addMouseListener(mouseAdapter);
        piecesScrollPane.addMouseMotionListener(mouseAdapter);
        piecesContainer.addMouseListener(mouseAdapter);
        piecesContainer.addMouseMotionListener(mouseAdapter);
        piecesScrollPane.getViewport().addMouseListener(mouseAdapter);
        piecesScrollPane.getViewport().addMouseMotionListener(mouseAdapter);
    }

    // 添加拼图块 - 关键修改：使用 PuzzlePiecePanel
    private void addPuzzlePieces(JPanel container) {
        // 清除现有内容（保留第一个间距）
        if (container.getComponentCount() > 1) {
            Component[] components = container.getComponents();
            for (int i = components.length - 1; i > 0; i--) {
                container.remove(i);
            }
        }

        availablePieces.clear();

        int totalPieces = rows * cols;
        System.out.println("根据难度生成拼图块数量: " + totalPieces);

        // 添加拼图块（使用自定义的 PuzzlePiecePanel）
        for (int i = 0; i < totalPieces; i++) {
            int pieceIndex = i;
            // *** 核心修改：创建 PuzzlePiecePanel 实例 ***
            PuzzlePiecePanel piece = new PuzzlePiecePanel(pieceIndex);
            piece.setPreferredSize(new Dimension(80, 80));
            piece.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            // 添加长按和点击事件
            PieceMouseListener listener = new PieceMouseListener(piece, pieceIndex);
            piece.addMouseListener(listener);
            piece.addMouseMotionListener(listener);

            container.add(piece);
            container.add(Box.createHorizontalStrut(10));
            availablePieces.add(piece);
        }
    }

    // 处理拼图块拖动
    private void handlePieceDragging(MouseEvent e) {
        if (isDraggingPiece && draggedPieceLabel != null) {
            // 基于屏幕坐标计算，避免偏移
            Point panelLocation = getLocationOnScreen();
            Point mouseLocation = e.getLocationOnScreen();
            int x = mouseLocation.x - panelLocation.x - 35; // 居中显示
            int y = mouseLocation.y - panelLocation.y - 35;
            draggedPieceLabel.setLocation(x, y);
            repaint();
        }
    }

    // 处理拼图块放置（拖动结束）
    private void handlePieceDrop(MouseEvent e) {
        if (isDraggingPiece && selectedPieceIndex != -1) {
            // 移除拖动标签
            if (draggedPieceLabel != null) {
                remove(draggedPieceLabel);
                draggedPieceLabel = null;
            }

            // === 新增功能：自动吸附 ===
            // 当拼图释放时，检测鼠标是否接近某个拼图区格中心，如果接近则自动吸附
            Point dropPoint = e.getPoint(); // 鼠标释放时的位置（相对整个面板）
            boolean snapped = false;

            // 遍历所有拼图区格
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Rectangle cellBounds = completedGrid[r][c].getBounds();
                    // 计算格子中心点
                    Point center = new Point(
                            cellBounds.x + cellBounds.width / 2,
                            cellBounds.y + cellBounds.height / 2
                    );

                    // 判断距离是否小于吸附阈值（60像素，可调）
                    if (dropPoint.distance(center) < 60) {
                        // 自动放入目标格
                        placePiece(r, c, selectedPieceIndex);
                        snapped = true;
                        break;
                    }
                }
                if (snapped) break;
            }

            // 如果没有吸附到格子，执行原有逻辑
            if (!snapped) {
                // 计算目标单元格
                Component centerComponent = completedPanel.getComponent(0);
                if (centerComponent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) centerComponent;
                    Component gridComponent = scrollPane.getViewport().getView();

                    // 转换鼠标坐标到网格面板坐标系
                    Point gridPoint = SwingUtilities.convertPoint(
                            this, e.getPoint(), gridComponent);

                    // 检查鼠标是否在网格范围内
                    if (gridComponent.contains(gridPoint)) {
                        // 计算目标行列
                        int cellWidth = gridComponent.getWidth() / cols;
                        int cellHeight = gridComponent.getHeight() / rows;
                        int targetRow = gridPoint.y / cellHeight;
                        int targetCol = gridPoint.x / cellWidth;

                        // 放置拼图块
                        placePiece(targetRow, targetCol, selectedPieceIndex);
                    }
                }
            }

            // 清除选中状态
            if (selectedPiece != null) {
                selectedPiece.setBorder(null);
            }
            selectedPiece = null;
            selectedPieceIndex = -1;
            isDraggingPiece = false;
            repaint();
        }
    }


    // 拼图块鼠标监听器
    private class PieceMouseListener extends MouseAdapter {
        private JPanel piece;
        private int pieceIndex;
        private Timer longPressTimer;
        private boolean isLongPressed = false;
        private boolean isDragging = false;
        private Point pressLocation;

        public PieceMouseListener(JPanel piece, int pieceIndex) {
            this.piece = piece;
            this.pieceIndex = pieceIndex;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // 检查是否仍在待选区
            if (piece.getParent() != piecesContainer) return;

            if (!isLongPressed && !isDragging && piece != selectedPiece) {
                piece.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
                piece.setBackground(new Color(240, 240, 255));
                piece.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // 检查是否仍在待选区
            if (piece.getParent() != piecesContainer) return;

            if (!isLongPressed && !isDragging && piece != selectedPiece) {
                piece.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                piece.setBackground(new Color(250, 250, 250));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (piece.getParent() != piecesContainer) return;

            pressLocation = e.getPoint();
            // 长按计时器
            longPressTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    showPreview();
                    isLongPressed = true;
                }
            });
            longPressTimer.setRepeats(false);
            longPressTimer.start();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (piece.getParent() != piecesContainer) return;

            // 拖动触发判断
            if (!isDragging && pressLocation != null) {
                int dragDistance = (int) pressLocation.distance(e.getPoint());
                if (dragDistance > 5) {
                    isDragging = true;
                    longPressTimer.stop(); // 取消长按
                    if (isLongPressed) {
                        hidePreview();
                        isLongPressed = false;
                    }
                    startDraggingPiece(); // 启动拖动
                }
            }

            // 拖动过程中更新位置
            if (isDragging && isDraggingPiece && draggedPieceLabel != null) {
                Point panelLocation = getLocationOnScreen();
                Point mouseLocation = e.getLocationOnScreen();
                int x = mouseLocation.x - panelLocation.x - 35;
                int y = mouseLocation.y - panelLocation.y - 35;
                draggedPieceLabel.setLocation(x, y);
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (piece.getParent() != piecesContainer) return;

            // 区分拖动和点击
            if (isDragging) {
                isDragging = false;
                pressLocation = null;
                return;
            }

            // 处理长按和点击
            if (isLongPressed) {
                hidePreview();
                isLongPressed = false;
            } else if (longPressTimer != null) {
                longPressTimer.stop();
                handlePieceSelection(); // 第一次点击选择拼图块
            }

            pressLocation = null;
        }

        // 处理拼图块选择（两次点击放置的第一步）
        private void handlePieceSelection() {
            // 取消之前选中的拼图块状态
            if (selectedPiece != null && selectedPiece != piece) {
                selectedPiece.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                selectedPiece.setBackground(new Color(250, 250, 250));
            }

            // 选中当前拼图块
            selectedPiece = piece;
            selectedPieceIndex = pieceIndex;

            // 显示选中状态（视觉反馈）
            piece.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            piece.setBackground(new Color(255, 240, 240));
        }

        // 开始拖动拼图块 - 关键修改：从 PuzzlePiecePanel 获取文本
        private void startDraggingPiece() {
            selectedPiece = piece;
            selectedPieceIndex = pieceIndex;
            isDraggingPiece = true;

            // *** 核心修改：从 PuzzlePiecePanel 获取文本 ***
            String pieceText = "拼图 " + (selectedPieceIndex + 1);
            if (piece instanceof PuzzlePiecePanel) {
                pieceText = ((PuzzlePiecePanel) piece).getPieceText();
            }

            // 创建拖动时显示的临时标签
            draggedPieceLabel = new JLabel(pieceText);
            draggedPieceLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            draggedPieceLabel.setForeground(new Color(70, 130, 180));
            draggedPieceLabel.setSize(70, 70);
            draggedPieceLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            draggedPieceLabel.setBackground(new Color(255, 255, 255, 200));
            draggedPieceLabel.setHorizontalAlignment(SwingConstants.CENTER);
            draggedPieceLabel.setOpaque(true);


            // 计算初始位置
            Point pieceScreenLoc = piece.getLocationOnScreen();
            Point panelScreenLoc = getLocationOnScreen();
            int x = pieceScreenLoc.x - panelScreenLoc.x + (piece.getWidth() - 70) / 2;
            int y = pieceScreenLoc.y - panelScreenLoc.y + (piece.getHeight() - 70) / 2;
            draggedPieceLabel.setLocation(x, y);

            // 添加到顶层
            add(draggedPieceLabel, 0);
            setComponentZOrder(draggedPieceLabel, 0);
            repaint();
        }

        // 显示预览 - 关键修改：从 PuzzlePiecePanel 获取文本
        private void showPreview() {
            JPanel previewContent = new JPanel(new BorderLayout());
            previewContent.setBackground(Color.WHITE);

            // *** 核心修改：从 PuzzlePiecePanel 获取文本 ***
            String pieceText = "拼图 " + (pieceIndex + 1);
            if (piece instanceof PuzzlePiecePanel) {
                pieceText = ((PuzzlePiecePanel) piece).getPieceText();
            }

            JLabel previewLabel = new JLabel(pieceText);
            previewLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
            previewLabel.setForeground(new Color(70, 130, 180));
            previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
            previewLabel.setVerticalAlignment(SwingConstants.CENTER);
            previewContent.add(previewLabel, BorderLayout.CENTER);
            previewContent.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 3));

            previewDialog.setContentPane(previewContent);
            Point pieceLocation = piece.getLocationOnScreen();
            int x = pieceLocation.x + (piece.getWidth() - previewDialog.getWidth()) / 2;
            int y = pieceLocation.y - previewDialog.getHeight() - 10;

            Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            if (x < 0) x = 0;
            if (y < 0) y = pieceLocation.y + piece.getHeight() + 10;
            if (x + previewDialog.getWidth() > screenBounds.width) {
                x = screenBounds.width - previewDialog.getWidth();
            }

            previewDialog.setLocation(x, y);
            previewDialog.setVisible(true);
        }
    }

    // 隐藏预览
    private void hidePreview() {
        if (previewDialog != null && previewDialog.isVisible()) {
            previewDialog.setVisible(false);
        }
    }

    // 刷新面板内容
    private void refreshPanels() {
        Component splitPaneComp = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (splitPaneComp instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) splitPaneComp;

            completedPanel = createCompletedPanel();
            piecesPanel = createPiecesPanel();

            splitPane.setTopComponent(completedPanel);
            splitPane.setBottomComponent(piecesPanel);

            revalidate();
            repaint();
        }
    }

    // 初始化菜单栏
    private void initMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);

        gameMenu = new JMenu("功能菜单");
        gameMenu.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gameMenu.setForeground(new Color(50, 50, 50));
        gameMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        undoItem = new JMenuItem("撤销上一步");
        autoCompleteItem = new JMenuItem("电脑自动完成");
        tipsItem = new JMenuItem("提示");

        setMenuItemStyle(undoItem);
        setMenuItemStyle(autoCompleteItem);
        setMenuItemStyle(tipsItem);

        gameMenu.add(undoItem);
        gameMenu.add(autoCompleteItem);
        gameMenu.addSeparator();
        gameMenu.add(tipsItem);

        menuBar.add(gameMenu);

        undoItem.addActionListener(e -> showFeatureNotImplemented("撤销上一步"));
        autoCompleteItem.addActionListener(e -> showFeatureNotImplemented("电脑自动完成"));
        tipsItem.addActionListener(e -> showFeatureNotImplemented("提示"));
    }

    private void showFeatureNotImplemented(String featureName) {
        JOptionPane.showMessageDialog(this,
                featureName + "功能尚未实现，敬请期待！",
                "功能开发中",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void setMenuItemStyle(JMenuItem item) {
        item.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        item.setForeground(new Color(50, 50, 50));
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(220, 230, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });
    }

    // 初始化显示图片（图片处理功能待实现）
    private void initDisplayImage() {
        displayImage = null;
        // === 新增功能：调用 ImageSplitter 切割图像 ===
        try {
            String imagePath = game.getImagePath();
            if (imagePath != null) {
                BufferedImage src = javax.imageio.ImageIO.read(new java.io.File(imagePath));
                puzzlePieces = ImageSplitter.splitImage(src, rows, cols);
            }
        } catch (Exception ex) {
            System.err.println("图像切割失败：" + ex.getMessage());
        }

        errorLabel.setVisible(false);

        // 图片处理功能待实现，直接显示占位信息
        showPlaceholder("图片处理功能\n(待实现)");

        // 刷新拼图块
        addPuzzlePieces(piecesContainer);
        piecesContainer.revalidate();
        piecesContainer.repaint();
    }

    private void showPlaceholder(String text) {
        JPanel placeholderPanel = new JPanel(new GridBagLayout());
        placeholderPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel placeholderLabel = new JLabel("<html>" + text.replace("\n", "<br>") + "</html>");
        placeholderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        placeholderLabel.setForeground(Color.GRAY);
        placeholderPanel.add(placeholderLabel, gbc);

        Component centerComponent = completedPanel.getComponent(0);
        if (centerComponent instanceof JScrollPane) {
            ((JScrollPane)centerComponent).setViewportView(placeholderPanel);
        } else {
            // 确保移除旧的CENTER组件
            ((BorderLayout) completedPanel.getLayout()).removeLayoutComponent(centerComponent);
            completedPanel.add(placeholderPanel, BorderLayout.CENTER);
        }
        completedPanel.revalidate();
        completedPanel.repaint();
    }

    // =================================================================
    // 新增内部类：PuzzlePiecePanel，用于自适应绘制内容
    // =================================================================
    /**
     * 内部类：代表一个拼图块，继承 JPanel，
     * 重写 paintComponent 以实现内容（占位符文本或未来的图片）的自适应缩放。
     */
    private class PuzzlePiecePanel extends JPanel {
        private String pieceText;
        // 实际应用中，这里会有一个 BufferedImage pieceImage;

        public PuzzlePiecePanel(int index) {
            // 构造函数，用索引来生成唯一的文本标识
            this.pieceText = "拼图 " + (index + 1);
            // 默认背景和不透明度
            setOpaque(true);
            setBackground(new Color(250, 250, 250)); // 用于待选区的默认背景
        }

        // 用于拖动和预览时获取文本
        public String getPieceText() {
            return pieceText;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 获取当前组件的尺寸
            int w = getWidth();
            int h = getHeight();

            if (w <= 0 || h <= 0) {
                return;
            }

            // ====== 占位符文本绘制逻辑 (图片切割功能待实现) ======

            // 计算字体大小：与单元格尺寸相关，确保在不同大小的单元格中都能显示
            int minDim = Math.min(w, h);
            int fontSize = Math.max(12, minDim / (rows > 0 ? rows : 1) * 3 / 4);
            g.setFont(new Font("微软雅黑", Font.BOLD, fontSize));

            FontMetrics fm = g.getFontMetrics();

            // 计算文本居中位置
            int x = (w - fm.stringWidth(pieceText)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();

            g.setColor(new Color(70, 130, 180)); // 与待选区的 JLabel 颜色保持一致
            g.drawString(pieceText, x, y);
        }

        /**
         * 覆盖 getPreferredSize 以确保在待选区有合适的默认大小
         */
        @Override
        public Dimension getPreferredSize() {
            // 保持与 addPuzzlePieces 中的设置一致
            return new Dimension(80, 80);
        }
    }
    // === 新增方法：计时功能 ===
    private void startTimer() {
        startTime = System.currentTimeMillis();
        gameTimer = new Timer(1000, e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            timerLabel.setText("用时: " + elapsed + " 秒");
        });
        gameTimer.start();
    }

}


