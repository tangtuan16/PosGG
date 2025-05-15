package Utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class FormatVND {

    public static String format(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(amount);
    }
}