package com.fifthlab.springtasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private Summary summary;
    private Distribution distribution;
    private List<ChartPoint> completionHistory;
    private List<ChartPoint> productivityByDay;
    private List<CategoryStat> categoryPerformance;
    private List<Insight> insights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalTasks;
        private long completed;
        private long pending;
        private long overdue;
        private double completionRate;
        private Map<String, String> trends; // e.g. "total" -> "+5%"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        private long completed;
        private long pending;
        private long overdue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartPoint {
        private String label;
        private long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {
        private Long id;
        private String name;
        private String color;
        private long completed;
        private long total;
        private int rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Insight {
        private String type; // e.g. "lightbulb", "lightning-charge"
        private String title;
        private String message;
    }
}
