package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.dto.AnalyticsDTO;
import com.fifthlab.springtasks.model.Category;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.repository.CategoryRepository;
import com.fifthlab.springtasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "defaultUser";
    }

    public AnalyticsDTO getAnalyticsData() {
        String username = getCurrentUsername();
        List<Task> allTasks = taskRepository.findByUsernameOrderByCreatedAtDesc(username);
        List<Category> allCategories = categoryRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // 1. Summary & Distribution
        long total = allTasks.size();
        long completed = allTasks.stream().filter(Task::isCompleted).count();
        long pending = allTasks.stream().filter(t -> !t.isCompleted()).count();
        long overdue = allTasks.stream().filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(now)).count();
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;

        Map<String, String> trends = calculateTrends(allTasks, now);

        AnalyticsDTO.Summary summary = AnalyticsDTO.Summary.builder()
                .totalTasks(total)
                .completed(completed)
                .pending(pending)
                .overdue(overdue)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .trends(trends)
                .build();

        AnalyticsDTO.Distribution distribution = AnalyticsDTO.Distribution.builder()
                .completed(completed)
                .pending(pending)
                .overdue(overdue)
                .build();

        // 2. Completion History (Last 7 Days)
        List<AnalyticsDTO.ChartPoint> completionHistory = calculateCompletionHistory(allTasks, now);

        // 3. Productivity By Day
        List<AnalyticsDTO.ChartPoint> productivityByDay = calculateProductivityByDay(allTasks);

        // 4. Category Performance
        List<AnalyticsDTO.CategoryStat> categoryPerformance = calculateCategoryPerformance(allTasks, allCategories);

        // 5. Insights
        List<AnalyticsDTO.Insight> insights = generateInsights(allTasks, productivityByDay);

        return AnalyticsDTO.builder()
                .summary(summary)
                .distribution(distribution)
                .completionHistory(completionHistory)
                .productivityByDay(productivityByDay)
                .categoryPerformance(categoryPerformance)
                .insights(insights)
                .build();
    }

    private Map<String, String> calculateTrends(List<Task> tasks, LocalDateTime now) {
        LocalDateTime last30 = now.minusDays(30);
        LocalDateTime last60 = now.minusDays(60);

        long currentPeriodCount = tasks.stream().filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(last30)).count();
        long previousPeriodCount = tasks.stream().filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(last60) && t.getCreatedAt().isBefore(last30)).count();

        Map<String, String> trends = new HashMap<>();
        trends.put("total", calculateTrendString(currentPeriodCount, previousPeriodCount));
        
        long currentComp = tasks.stream().filter(t -> t.isCompleted() && t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(last30)).count();
        long prevComp = tasks.stream().filter(t -> t.isCompleted() && t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(last60) && t.getUpdatedAt().isBefore(last30)).count();
        trends.put("completed", calculateTrendString(currentComp, prevComp));

        long currentPend = tasks.stream().filter(t -> !t.isCompleted() && t.getCreatedAt() != null && t.getCreatedAt().isAfter(last30)).count();
        long prevPend = tasks.stream().filter(t -> !t.isCompleted() && t.getCreatedAt() != null && t.getCreatedAt().isAfter(last60) && t.getCreatedAt().isBefore(last30)).count();
        trends.put("pending", calculateTrendString(currentPend, prevPend));

        long currentOver = tasks.stream().filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(now) && t.getDueDate().isAfter(last30)).count();
        long prevOver = tasks.stream().filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(last30) && t.getDueDate().isAfter(last60)).count();
        trends.put("overdue", calculateTrendString(currentOver, prevOver));

        return trends;
    }

    private String calculateTrendString(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : "0%";
        double diff = ((double) current - previous) / previous * 100;
        return (diff >= 0 ? "+" : "") + Math.round(diff) + "%";
    }

    private List<AnalyticsDTO.ChartPoint> calculateCompletionHistory(List<Task> tasks, LocalDateTime now) {
        Map<String, Long> historyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            String label = day.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + day.getDayOfMonth();
            historyMap.put(label, 0L);
        }

        tasks.stream()
                .filter(Task::isCompleted)
                .filter(t -> t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(now.minusDays(7)))
                .forEach(t -> {
                    String label = t.getUpdatedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + t.getUpdatedAt().getDayOfMonth();
                    if (historyMap.containsKey(label)) {
                        historyMap.put(label, historyMap.get(label) + 1);
                    }
                });

        List<AnalyticsDTO.ChartPoint> points = new ArrayList<>();
        historyMap.forEach((k, v) -> points.add(new AnalyticsDTO.ChartPoint(k, v)));
        return points;
    }

    private List<AnalyticsDTO.ChartPoint> calculateProductivityByDay(List<Task> tasks) {
        Map<DayOfWeek, Long> dayMap = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) dayMap.put(d, 0L);

        tasks.stream().filter(Task::isCompleted).forEach(t -> {
            if (t.getUpdatedAt() != null) {
                DayOfWeek day = t.getUpdatedAt().getDayOfWeek();
                dayMap.put(day, dayMap.get(day) + 1);
            }
        });

        List<AnalyticsDTO.ChartPoint> points = new ArrayList<>();
        DayOfWeek[] order = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        for (DayOfWeek d : order) {
            points.add(new AnalyticsDTO.ChartPoint(d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), dayMap.get(d)));
        }
        return points;
    }

    private List<AnalyticsDTO.CategoryStat> calculateCategoryPerformance(List<Task> tasks, List<Category> categories) {
        List<AnalyticsDTO.CategoryStat> stats = new ArrayList<>();
        for (Category cat : categories) {
            List<Task> catTasks = tasks.stream().filter(t -> Objects.equals(t.getCategoryId(), cat.getId())).toList();
            if (catTasks.isEmpty()) continue;

            long total = catTasks.size();
            long completed = catTasks.stream().filter(Task::isCompleted).count();
            int rate = (int) Math.round((double) completed / total * 100);

            stats.add(AnalyticsDTO.CategoryStat.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .color(cat.getColor())
                    .completed(completed)
                    .total(total)
                    .rate(rate)
                    .build());
        }
        return stats;
    }

    private List<AnalyticsDTO.Insight> generateInsights(List<Task> tasks, List<AnalyticsDTO.ChartPoint> productivityByDay) {
        List<AnalyticsDTO.Insight> insights = new ArrayList<>();

        AnalyticsDTO.ChartPoint bestDay = productivityByDay.stream().max(Comparator.comparingLong(AnalyticsDTO.ChartPoint::getValue)).orElse(null);
        if (bestDay != null && bestDay.getValue() > 0) {
            insights.add(AnalyticsDTO.Insight.builder()
                    .type("lightbulb")
                    .title("Most Productive Day")
                    .message("You typically complete the most tasks on " + bestDay.getLabel() + "s.")
                    .build());
        }

        Map<Integer, Long> hourMap = new HashMap<>();
        tasks.stream().filter(Task::isCompleted).forEach(t -> {
            if (t.getUpdatedAt() != null) {
                int hour = t.getUpdatedAt().getHour();
                hourMap.put(hour, hourMap.getOrDefault(hour, 0L) + 1);
            }
        });
        
        Integer bestHour = hourMap.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        if (bestHour != null) {
            String ampm = bestHour >= 12 ? "PM" : "AM";
            int h12 = bestHour % 12;
            if (h12 == 0) h12 = 12;
            String timeRange = h12 + " " + ampm;
            insights.add(AnalyticsDTO.Insight.builder()
                    .type("lightning-charge")
                    .title("Peak Activity Time")
                    .message("Your most active completion time is around " + timeRange + ".")
                    .build());
        }

        return insights;
    }
}
