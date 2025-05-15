package Utils;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.SimpleDateFormat;
import java.awt.Component;
import javax.swing.JTable;
import java.util.Date;

public class DateRenderer extends DefaultTableCellRenderer {
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Date) {
            value = formatter.format((Date) value);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
