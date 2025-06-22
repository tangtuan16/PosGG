package Views.Statistical;

import Controllers.StatisticController;
import Models.OrderStatistic;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.*;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class StatisticView extends JFrame {
    private StatisticController controller;
    private JTable table;
    private JComboBox<String> cbType;
    private JComboBox<Integer> cbYear; // Thêm JComboBox cho năm
    private JPanel chartPanel;
    private static final DecimalFormat df = new DecimalFormat(
            "#,##0",
            new DecimalFormatSymbols(new Locale.Builder().setLanguage("vi").setRegion("VN").build())
    );

    public StatisticView() {
        setTitle("Thống Kê Doanh Thu");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo danh sách loại thống kê
        String[] types = {"Tuần", "Tháng", "Quý", "Năm"};
        String[] displayNames = {"Tuần", "Tháng", "Quý", "Năm"};
        DefaultComboBoxModel<String> typeModel = new DefaultComboBoxModel<>(displayNames);
        cbType = new JComboBox<>(typeModel) {
            public String getSelectedItem() {
                int index = getSelectedIndex();
                return index >= 0 ? types[index] : null;
            }
        };

        // Tạo danh sách năm (từ năm hiện tại về trước 10 năm)
        Integer[] years = new Integer[11];
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 10; i++) {
            years[i] = currentYear - i;
        }
        DefaultComboBoxModel<Integer> yearModel = new DefaultComboBoxModel<>(years);
        cbYear = new JComboBox<>(yearModel);
        cbYear.setSelectedItem(currentYear); // Chọn năm hiện tại mặc định

        JButton btnLoad = new JButton("Tải Thống Kê");

        btnLoad.addActionListener(e -> {
            String type = cbType.getSelectedItem().toString();
            Integer selectedYear = (Integer) cbYear.getSelectedItem();
            controller.loadStatistics(type, selectedYear); // Truyền thêm tham số năm
        });

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Loại Thống Kê:"));
        topPanel.add(cbType);
        topPanel.add(new JLabel("Năm:"));
        topPanel.add(cbYear);
        topPanel.add(btnLoad);

        table = new JTable(new DefaultTableModel(new Object[]{"Thời gian", "Doanh thu"}, 0));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        JScrollPane tableScroll = new JScrollPane(table);

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(800, 500));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(chartPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void setController(StatisticController controller) {
        this.controller = controller;
    }

    private String formatRevenueForChart(double amount) {
        try {
            if (amount >= 1_000_000_000) {
                return df.format(amount / 1_000_000_000) + "B VND";
            } else if (amount >= 1_000_000) {
                return df.format(amount / 1_000_000) + "M VND";
            } else {
                return df.format(amount) + " VND";
            }
        } catch (IllegalArgumentException e) {
            return "0 VND";
        }
    }

    public void updateTable(List<OrderStatistic> stats) {
        SwingUtilities.invokeLater(() -> {
            if (table == null) {
                System.err.println("Table is not initialized in StatisticView");
                return;
            }
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            if (stats == null || stats.isEmpty()) {
                chartPanel.removeAll();
                chartPanel.revalidate();
                chartPanel.repaint();
                return;
            }

            for (OrderStatistic stat : stats) {
                String timeGroup = stat.getTimeGroup() != null ? stat.getTimeGroup() : "";
                String totalAmount;
                try {
                    totalAmount = df.format(stat.getTotalAmount()) + " VND";
                } catch (IllegalArgumentException e) {
                    totalAmount = "0 VND";
                }
                model.addRow(new Object[]{timeGroup, totalAmount});
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (OrderStatistic stat : stats) {
                String timeGroup = stat.getTimeGroup() != null ? stat.getTimeGroup() : "";
                try {
                    dataset.addValue(stat.getTotalAmount(), "Doanh thu", timeGroup);
                } catch (IllegalArgumentException e) {
                    dataset.addValue(0.0, "Doanh thu", timeGroup);
                }
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Thống Kê Doanh Thu",
                    "Thời gian",
                    "Doanh thu (VND)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            CategoryPlot plot = barChart.getCategoryPlot();
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(new CustomNumberFormat());

            // Tooltips
            plot.getRenderer().setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator() {
                @Override
                public String generateToolTip(org.jfree.data.category.CategoryDataset dataset, int row, int column) {
                    return "Doanh thu: " + formatRevenueForChart(dataset.getValue(row, column).doubleValue()) +
                            " (" + dataset.getColumnKey(column) + ")";
                }
            });

            // Hiện label trên mỗi cột
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new CategoryItemLabelGenerator() {
                @Override
                public String generateLabel(org.jfree.data.category.CategoryDataset dataset, int row, int column) {
                    Number value = dataset.getValue(row, column);
                    return value != null ? formatRevenueForChart(value.doubleValue()) : "";
                }

                @Override
                public String generateRowLabel(org.jfree.data.category.CategoryDataset dataset, int row) {
                    return dataset.getRowKey(row).toString();
                }

                @Override
                public String generateColumnLabel(org.jfree.data.category.CategoryDataset dataset, int column) {
                    return dataset.getColumnKey(column).toString();
                }
            });

            // Styling
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinesVisible(true);
            plot.setRangeGridlinesVisible(true);

            chartPanel.removeAll();
            chartPanel.add(new ChartPanel(barChart), BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    // Format tùy chỉnh cho trục Y
    private class CustomNumberFormat extends NumberFormat {
        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            return new StringBuffer(formatRevenueForChart(number));
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return new StringBuffer(formatRevenueForChart(number));
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            return 0;
        }
    }
}