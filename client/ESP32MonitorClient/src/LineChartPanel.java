import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineChartPanel extends JPanel {
    private List<Integer> dataPoints = new ArrayList<>();
    private final int maxPoints;
    private final String title;
    private final Color lineColor;

    private static final int MARGIN_LEFT = 40;
    private static final int MARGIN_BOTTOM = 30;
    private static final int MARGIN_TOP = 30;
    private static final int MARGIN_RIGHT = 10;

    private static final Color AXIS_COLOR = Color.GRAY;
    private static final Color TEXT_COLOR = Color.BLACK;

    public LineChartPanel(String title, int maxPoints, int chartWidth, int chartHeight, Color lineColor) {
        this.title = title;
        this.maxPoints = maxPoints;
        this.lineColor = lineColor;
        setPreferredSize(new Dimension(chartWidth, chartHeight));
        setBorder(BorderFactory.createBevelBorder(1)); // Beveled border
    }

    public void addDataPoint(int valuePercent) {
        if (dataPoints.size() >= maxPoints) {
            dataPoints.remove(0);
        }
        dataPoints.add(valuePercent);
        repaint();
    }
    public List<Integer> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<Integer> l) { this.dataPoints = l; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Title
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        g2.drawString(title, MARGIN_LEFT, MARGIN_TOP - 10);

        int height = getHeight() - MARGIN_BOTTOM - MARGIN_TOP;
        int width = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        int originX = MARGIN_LEFT;
        int originY = getHeight() - MARGIN_BOTTOM;

        // Axes
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(originX, MARGIN_TOP, originX, originY); // Y-axis
        g2.drawLine(originX, originY, getWidth() - MARGIN_RIGHT, originY); // X-axis

        // Grid lines
        g2.setColor(new Color(220, 220, 220));
        for (int i = 20; i < 100; i += 20) {
            int y = originY - i * height / 100;
            g2.drawLine(originX, y, getWidth() - MARGIN_RIGHT, y);
        }

        // Y-axis labels
        g2.setColor(TEXT_COLOR);
        g2.setFont(g2.getFont().deriveFont(12f));
        for (int i = 0; i <= 100; i += 20) {
            int y = originY - i * height / 100;
            g2.drawString(i + "%", 5, y + 5);
        }

        // Draw line
        if (dataPoints.isEmpty()) return;

        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2.5f)); // Thicker line
        int xStep = width / maxPoints;
        int prevX = originX;
        int prevY = originY - dataPoints.get(0) * height / 100;

        for (int i = 1; i < dataPoints.size(); i++) {
            int x = originX + i * xStep;
            int y = originY - dataPoints.get(i) * height / 100;
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }

        g2.dispose();
    }
}
