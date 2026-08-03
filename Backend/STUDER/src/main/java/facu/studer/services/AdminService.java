package facu.studer.services;

import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboard(String startDate, String endDate, String date, Integer limit);
    Map<String, Object> getUsersDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getBlocksDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getCoursesDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getFeedDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getDiscussionsDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getContestsDashboard(String startDate, String endDate, Integer limit);
    Map<String, Object> getTagsDashboard(String startDate, String endDate, Integer limit);
}
