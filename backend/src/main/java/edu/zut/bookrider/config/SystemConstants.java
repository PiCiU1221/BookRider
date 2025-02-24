package edu.zut.bookrider.config;

import java.math.BigDecimal;

public class SystemConstants {

    public static final BigDecimal SERVICE_FEE_PERCENTAGE = new BigDecimal("0.20");
    public static final BigDecimal BASE_COST = BigDecimal.valueOf(10.00);
    public static final BigDecimal PER_KM_RATE = BigDecimal.valueOf(0.50);
    public static final BigDecimal ADDITIONAL_ITEM_COST = BigDecimal.valueOf(1.00);
    public static final BigDecimal DAILY_LATE_FEE = BigDecimal.valueOf(1.00);
    public static final int RETURN_DEADLINE_DAYS = 30;
}
