package facu.studer.services.implementation;

import facu.studer.services.AdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

@Service
public class AdminServiceImpl implements AdminService {

    @PersistenceContext private EntityManager em;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Object> getDashboard(String startDate, String endDate, String date, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", cards());
        d.put("charts", charts(startDate, endDate, date, limit));
        return d;
    }

    private Map<String, Object> cards() {
        Map<String, Object> c = new LinkedHashMap<>();

        c.put("totalUsers", q("SELECT COUNT(u) FROM User u WHERE u.isActive = true"));
        c.put("activeUsersToday", q("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :t AND u.isActive = true", d(1)));
        c.put("activeUsersLast7Days", q("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :t AND u.isActive = true", d(7)));
        c.put("activeUsersLast30Days", q("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :t AND u.isActive = true", d(30)));
        c.put("newUsersToday", q("SELECT COUNT(u) FROM User u WHERE u.createdDatetime >= :t AND u.isActive = true", d(1)));
        c.put("newUsersThisMonth", q("SELECT COUNT(u) FROM User u WHERE u.createdDatetime >= :t AND u.isActive = true", d(30)));

        c.put("totalBlocks", q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true"));
        c.put("totalVersions", q("SELECT COUNT(bv) FROM BlockVersion bv WHERE bv.isActive = true"));
        long forks = q("SELECT COUNT(b) FROM Block b WHERE b.parentBlock IS NOT NULL AND b.isActive = true");
        c.put("totalForks", forks);
        long totalBlocks = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true");
        long totalUsers = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true");
        c.put("reuseRate", totalBlocks > 0 ? Math.round((double) forks / totalBlocks * 100) + "%" : "0%");
        c.put("avgBlocksPerUser", totalUsers > 0 ? Math.round((double) totalBlocks / totalUsers * 10) / 10.0 : 0);

        c.put("totalCourses", q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true"));

        c.put("totalPosts", q("SELECT COUNT(p) FROM Post p WHERE p.isActive = true"));
        c.put("totalComments", q("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true"));

        c.put("totalDiscussions", q("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true"));
        c.put("openDiscussions", q("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true"));

        c.put("totalMessages", q("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true"));

        c.put("totalContests", q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true"));
        c.put("activeContests", q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true AND c.status NOT IN ('RESULTS','CANCELLED')"));

        c.put("totalTags", q("SELECT COUNT(t) FROM Tag t WHERE t.isActive = true"));

        c.put("totalLikes", q("SELECT COALESCE(COUNT(bl),0) FROM BlockLike bl WHERE bl.isActive = true"));

        return c;
    }

    private Map<String, Object> charts(String startDate, String endDate, String date, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();

        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(12));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        LocalDate targetDate = parseDate(date, LocalDate.now());
        int lim = limit != null ? limit : 20;

        ch.put("usersOverTime", usersOverTime(sd, ed));
        ch.put("dailyActivity", dailyActivity(sd, ed));
        ch.put("contentDistribution", contentDistribution());
        ch.put("activeUsersByHour", activeUsersByHour(targetDate));
        ch.put("topTags", topTags(lim, sd, ed));
        ch.put("weeklyGrowth", weeklyGrowth(sd, ed));
        ch.put("activityHeatmap", activityHeatmap(targetDate));
        ch.put("activityPeaks", activityPeaks(targetDate));

        return ch;
    }

    private List<Map<String, Object>> usersOverTime(LocalDate start, LocalDate end) {
        try {
            String sql = "SELECT TO_CHAR(created_datetime, 'YYYY-MM') AS month, COUNT(*) AS users FROM users WHERE is_active = true AND created_datetime >= ? AND created_datetime <= ? GROUP BY TO_CHAR(created_datetime, 'YYYY-MM') ORDER BY month";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, start.atStartOfDay()).setParameter(2, end.atTime(23, 59, 59)).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("month", (String) r[0], "usersRegistered", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private List<Map<String, Object>> dailyActivity(LocalDate start, LocalDate end) {
        String sql = "SELECT d.date, COALESCE(p.cnt,0), COALESCE(b.cnt,0), COALESCE(ds.cnt,0), COALESCE(m.cnt,0), COALESCE(c.cnt,0), COALESCE(u.cnt,0) FROM (SELECT DISTINCT CAST(created_datetime AS DATE) AS date FROM posts WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT CAST(created_datetime AS DATE) FROM blocks WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT CAST(created_datetime AS DATE) FROM discussions WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT CAST(created_datetime AS DATE) FROM discussion_messages WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT CAST(created_datetime AS DATE) FROM courses WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT CAST(created_datetime AS DATE) FROM users WHERE is_active=true AND created_datetime>=? AND created_datetime<=?) d LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM posts WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) p ON d.date=p.date LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM blocks WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) b ON d.date=b.date LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM discussions WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) ds ON d.date=ds.date LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM discussion_messages WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) m ON d.date=m.date LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM courses WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) c ON d.date=c.date LEFT JOIN (SELECT CAST(created_datetime AS DATE) AS date, COUNT(*) AS cnt FROM users WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY CAST(created_datetime AS DATE)) u ON d.date=u.date ORDER BY d.date";
        LocalDateTime s = start.atStartOfDay(), e = end.atTime(23, 59, 59);
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class)
            .setParameter(1, s).setParameter(2, e).setParameter(3, s).setParameter(4, e).setParameter(5, s).setParameter(6, e)
            .setParameter(7, s).setParameter(8, e).setParameter(9, s).setParameter(10, e).setParameter(11, s).setParameter(12, e)
            .setParameter(13, s).setParameter(14, e).setParameter(15, s).setParameter(16, e).setParameter(17, s).setParameter(18, e)
            .setParameter(19, s).setParameter(20, e).setParameter(21, s).setParameter(22, e).setParameter(23, s).setParameter(24, e)
            .getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "posts", n(r[1]), "blocks", n(r[2]), "discussions", n(r[3]), "comments", n(r[4]), "courses", n(r[5]), "users", n(r[6])));
        return list;
    }

    private Map<String, Object> contentDistribution() {
        return Map.of(
            "blocks", q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true"),
            "courses", q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true"),
            "posts", q("SELECT COUNT(p) FROM Post p WHERE p.isActive = true"),
            "discussions", q("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true"),
            "contests", q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true"));
    }

    private List<Map<String, Object>> activeUsersByHour(LocalDate date) {
        LocalDateTime s = date.atStartOfDay();
        LocalDateTime e = date.plusDays(1).atStartOfDay();
        List<Object[]> rows = em.createQuery(
            "SELECT HOUR(u.lastConnectionTime), COUNT(u) FROM User u WHERE u.isActive = true AND u.lastConnectionTime >= :s AND u.lastConnectionTime < :e GROUP BY HOUR(u.lastConnectionTime) ORDER BY HOUR(u.lastConnectionTime)",
            Object[].class).setParameter("s", s).setParameter("e", e).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("hour", r[0].toString(), "activeUsers", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> topTags(int limit, LocalDate start, LocalDate end) {
        String sql = "SELECT t.name, CAST(COALESCE(bt.cnt,0)+COALESCE(pt.cnt,0)+COALESCE(ct.cnt,0)+COALESCE(dt.cnt,0)+COALESCE(cot.cnt,0) AS bigint) AS usages FROM tags t LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM block_tags JOIN blocks b ON b.id=block_id AND b.is_active=true WHERE b.created_datetime>=? AND b.created_datetime<=? GROUP BY tag_id) bt ON t.id=bt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM post_tags JOIN posts p ON p.id=post_id AND p.is_active=true WHERE p.created_datetime>=? AND p.created_datetime<=? GROUP BY tag_id) pt ON t.id=pt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM course_tags JOIN courses c ON c.id=course_id AND c.is_active=true WHERE c.created_datetime>=? AND c.created_datetime<=? GROUP BY tag_id) ct ON t.id=ct.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM discussion_tags JOIN discussions d ON d.id=discussion_id AND d.is_active=true WHERE d.created_datetime>=? AND d.created_datetime<=? GROUP BY tag_id) dt ON t.id=dt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM contest_tags JOIN contest c ON c.id=contest_id AND c.is_active=true WHERE c.created_datetime>=? AND c.created_datetime<=? GROUP BY tag_id) cot ON t.id=cot.tag_id WHERE t.is_active=true ORDER BY usages DESC LIMIT ?";
        LocalDateTime s = start.atStartOfDay(), e = end.atTime(23, 59, 59);
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class)
            .setParameter(1, s).setParameter(2, e).setParameter(3, s).setParameter(4, e).setParameter(5, s).setParameter(6, e)
            .setParameter(7, s).setParameter(8, e).setParameter(9, s).setParameter(10, e).setParameter(11, limit)
            .getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("tag", (String) r[0], "usages", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> weeklyGrowth(LocalDate start, LocalDate end) {
        try {
            String sql = "SELECT TO_CHAR(w.week, 'IYYY-IW') AS label, COALESCE(u.cnt,0), COALESCE(b.cnt,0), COALESCE(c.cnt,0), COALESCE(p.cnt,0) FROM (SELECT DISTINCT date_trunc('week', created_datetime) AS week FROM users WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT date_trunc('week', created_datetime) FROM blocks WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT date_trunc('week', created_datetime) FROM courses WHERE is_active=true AND created_datetime>=? AND created_datetime<=? UNION SELECT DISTINCT date_trunc('week', created_datetime) FROM posts WHERE is_active=true AND created_datetime>=? AND created_datetime<=?) w LEFT JOIN (SELECT date_trunc('week', created_datetime) AS week, COUNT(*) AS cnt FROM users WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY date_trunc('week', created_datetime)) u ON w.week=u.week LEFT JOIN (SELECT date_trunc('week', created_datetime) AS week, COUNT(*) AS cnt FROM blocks WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY date_trunc('week', created_datetime)) b ON w.week=b.week LEFT JOIN (SELECT date_trunc('week', created_datetime) AS week, COUNT(*) AS cnt FROM courses WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY date_trunc('week', created_datetime)) c ON w.week=c.week LEFT JOIN (SELECT date_trunc('week', created_datetime) AS week, COUNT(*) AS cnt FROM posts WHERE is_active=true AND created_datetime>=? AND created_datetime<=? GROUP BY date_trunc('week', created_datetime)) p ON w.week=p.week ORDER BY w.week";
        LocalDateTime s = start.atStartOfDay(), e = end.atTime(23, 59, 59);
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class)
            .setParameter(1, s).setParameter(2, e).setParameter(3, s).setParameter(4, e).setParameter(5, s).setParameter(6, e)
            .setParameter(7, s).setParameter(8, e).setParameter(9, s).setParameter(10, e).setParameter(11, s).setParameter(12, e)
            .setParameter(13, s).setParameter(14, e).setParameter(15, s).setParameter(16, e).setParameter(17, s).setParameter(18, e)
            .setParameter(19, s).setParameter(20, e)
            .getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("week", r[0].toString(), "users", n(r[1]), "blocks", n(r[2]), "courses", n(r[3]), "posts", n(r[4])));
        return list;
        } catch (Exception e) { return List.of(); }
    }

    @Override
    public Map<String, Object> getUsersDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", usersCards());
        d.put("charts", usersCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> usersCards() {
        Map<String, Object> c = new LinkedHashMap<>();

        long totalUsers = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true");
        long activeUsers = q("SELECT COUNT(u) FROM User u WHERE u.lastConnectionTime >= :t AND u.isActive = true", d(30));
        c.put("totalUsers", totalUsers);
        c.put("activeUsers", activeUsers);
        c.put("inactiveUsers", totalUsers - activeUsers);
        c.put("admins", q("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.role = 'ADMIN'"));

        try {
            Object avgAge = em.createNativeQuery("SELECT AVG(EXTRACT(YEAR FROM AGE(birth_date))) FROM users WHERE is_active = true AND birth_date IS NOT NULL", Object.class).getSingleResult();
            c.put("averageAge", avgAge instanceof Number n ? Math.round(n.doubleValue() * 10) / 10.0 : 0);
        } catch (Exception e) { c.put("averageAge", 0); }

        Long friendCount = em.createQuery("SELECT COUNT(f) FROM Friend f WHERE f.isActive = true AND f.senderAccept = true AND f.receiverAccept = true", Long.class).getSingleResult();
        c.put("averageFollowers", totalUsers > 0 ? Math.round((double) friendCount / totalUsers * 2 * 10) / 10.0 : 0);

        long totalBlocks = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true");
        long totalCourses = q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true");
        c.put("averageBlocks", totalUsers > 0 ? Math.round((double) totalBlocks / totalUsers * 10) / 10.0 : 0);
        c.put("averageCourses", totalUsers > 0 ? Math.round((double) totalCourses / totalUsers * 10) / 10.0 : 0);

        return c;
    }

    private Map<String, Object> usersCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(12));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;

        ch.put("usersRegistered", usersRegistered(sd, ed));
        ch.put("ageDistribution", ageDistribution());
        ch.put("usersByCountry", List.of());
        ch.put("usersByRole", usersByRole());
        ch.put("mostActiveUsers", mostActiveUsers(lim));
        ch.put("followersDistribution", followersDistribution());
        ch.put("profileCompletion", profileCompletion());

        return ch;
    }

    private List<Map<String, Object>> usersRegistered(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', u.createdDatetime), COUNT(u) FROM User u WHERE u.isActive = true AND u.createdDatetime >= :s AND u.createdDatetime <= :e GROUP BY FUNCTION('DATE', u.createdDatetime) ORDER BY FUNCTION('DATE', u.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "users", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> ageDistribution() {
        try {
            String sql = "SELECT CASE WHEN yrs < 18 THEN 'Under 18' WHEN yrs < 25 THEN '18-24' WHEN yrs < 35 THEN '25-34' WHEN yrs < 45 THEN '35-44' WHEN yrs < 55 THEN '45-54' WHEN yrs < 65 THEN '55-64' ELSE '65+' END AS age_range, COUNT(*) FROM (SELECT EXTRACT(YEAR FROM AGE(birth_date)) AS yrs FROM users WHERE is_active = true AND birth_date IS NOT NULL) t GROUP BY age_range ORDER BY MIN(yrs)";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("ageRange", (String) r[0], "users", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private Map<String, Object> usersByRole() {
        long usr = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.role = 'USER'");
        long adm = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.role = 'ADMIN'");
        return Map.of("USER", usr, "ADMIN", adm);
    }

    private List<Map<String, Object>> mostActiveUsers(int limit) {
        List<Object[]> rows = em.createQuery(
            "SELECT u.username, u.points FROM User u WHERE u.isActive = true ORDER BY u.points DESC",
            Object[].class).setMaxResults(limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("user", (String) r[0], "activityScore", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> followersDistribution() {
        String sql = "SELECT CASE WHEN fcnt = 0 THEN '0' WHEN fcnt <= 5 THEN '1-5' WHEN fcnt <= 10 THEN '6-10' WHEN fcnt <= 25 THEN '11-25' WHEN fcnt <= 50 THEN '26-50' ELSE '50+' END AS frange, COUNT(*) FROM (SELECT u.id, COALESCE(COUNT(f.id), 0) AS fcnt FROM users u LEFT JOIN friends f ON (f.sender_id = u.id OR f.receiver_id = u.id) AND f.is_active = true AND f.sender_accept = true AND f.receiver_accept = true WHERE u.is_active = true GROUP BY u.id) t GROUP BY frange ORDER BY MIN(t.fcnt)";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("followersRange", (String) r[0], "users", ((Number) r[1]).longValue()));
        return list;
    }

    private Map<String, Object> profileCompletion() {
        long complete = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND (u.profilePictureThumbnailUrl IS NOT NULL OR u.profilePictureAvatarUrl IS NOT NULL)");
        long total = q("SELECT COUNT(u) FROM User u WHERE u.isActive = true");
        return Map.of("complete", complete, "incomplete", total - complete);
    }

    @Override
    public Map<String, Object> getBlocksDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", blocksCards());
        d.put("charts", blocksCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> blocksCards() {
        Map<String, Object> c = new LinkedHashMap<>();

        long totalBlocks = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true");
        long totalForks = q("SELECT COUNT(b) FROM Block b WHERE b.parentBlock IS NOT NULL AND b.isActive = true");
        long totalLikes = q("SELECT COUNT(bl) FROM BlockLike bl WHERE bl.block.isActive = true AND bl.isActive = true");
        long totalVersions = q("SELECT COUNT(bv) FROM BlockVersion bv WHERE bv.block.isActive = true AND bv.isActive = true");

        c.put("totalBlocks", totalBlocks);
        c.put("totalVersions", totalVersions);
        c.put("totalForks", totalForks);
        c.put("totalLikes", totalLikes);
        c.put("avgVersions", totalBlocks > 0 ? Math.round((double) totalVersions / totalBlocks * 10) / 10.0 : 0);
        c.put("avgForks", totalBlocks > 0 ? Math.round((double) totalForks / totalBlocks * 10) / 10.0 : 0);
        c.put("avgLikes", totalBlocks > 0 ? Math.round((double) totalLikes / totalBlocks * 10) / 10.0 : 0);

        long publishedBlocks = q("SELECT COUNT(DISTINCT bv.block) FROM BlockVersion bv WHERE bv.published = true AND bv.block.isActive = true AND bv.isActive = true");
        c.put("publishedBlocks", publishedBlocks);
        c.put("draftBlocks", totalBlocks - publishedBlocks);

        return c;
    }

    private Map<String, Object> blocksCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(6));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;

        ch.put("blocksCreated", blocksCreated(sd, ed));
        ch.put("versionsDistribution", versionsDistribution());
        ch.put("forksDistribution", forksDistribution());
        ch.put("difficultyDistribution", difficultyDistribution());
        ch.put("blockTypes", blockTypes());
        ch.put("mostForked", mostForked(lim));
        ch.put("mostLiked", mostLiked(lim));
        ch.put("topAuthors", topAuthors(lim));

        return ch;
    }

    private List<Map<String, Object>> blocksCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', b.createdDatetime), COUNT(b) FROM Block b WHERE b.isActive = true AND b.createdDatetime >= :s AND b.createdDatetime <= :e GROUP BY FUNCTION('DATE', b.createdDatetime) ORDER BY FUNCTION('DATE', b.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "blocks", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> versionsDistribution() {
        String sql = "SELECT CASE WHEN vc = 1 THEN '1' WHEN vc <= 3 THEN '2-3' WHEN vc <= 5 THEN '4-5' WHEN vc <= 10 THEN '6-10' WHEN vc <= 20 THEN '11-20' ELSE '20+' END AS vrange, COUNT(*) FROM (SELECT b.id, COUNT(bv.id) AS vc FROM blocks b LEFT JOIN blocks_versions bv ON bv.block_id = b.id AND bv.is_active = true WHERE b.is_active = true GROUP BY b.id) t GROUP BY vrange ORDER BY MIN(t.vc)";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("versionsRange", (String) r[0], "blocks", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> forksDistribution() {
        String sql = "SELECT CASE WHEN fc = 0 THEN '0' WHEN fc <= 2 THEN '1-2' WHEN fc <= 5 THEN '3-5' WHEN fc <= 10 THEN '6-10' WHEN fc <= 20 THEN '11-20' ELSE '20+' END AS frange, COUNT(*) FROM (SELECT b.id, COALESCE(fc.cnt, 0) AS fc FROM blocks b LEFT JOIN (SELECT parent_block_id, COUNT(*) AS cnt FROM blocks WHERE is_active = true AND parent_block_id IS NOT NULL GROUP BY parent_block_id) fc ON fc.parent_block_id = b.id WHERE b.is_active = true) t GROUP BY frange ORDER BY MIN(t.fc)";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("forksRange", (String) r[0], "blocks", ((Number) r[1]).longValue()));
        return list;
    }

    private Map<String, Object> difficultyDistribution() {
        long easy = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND b.difficulty = facu.studer.entities.blocks.Difficulty.EASY");
        long normal = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND b.difficulty = facu.studer.entities.blocks.Difficulty.NORMAL");
        long hard = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND b.difficulty = facu.studer.entities.blocks.Difficulty.HARD");
        long expert = q("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND b.difficulty = facu.studer.entities.blocks.Difficulty.EXPERT");
        return Map.of("EASY", easy, "NORMAL", normal, "HARD", hard, "EXPERT", expert);
    }

    private List<Map<String, Object>> blockTypes() {
        try {
            String sql = "SELECT COALESCE(bv.content->>'type', 'unknown') AS btype, COUNT(DISTINCT bv.block_id) FROM blocks_versions bv WHERE bv.is_active = true AND bv.content IS NOT NULL GROUP BY bv.content->>'type' ORDER BY COUNT(DISTINCT bv.block_id) DESC";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("type", (String) r[0], "count", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private List<Map<String, Object>> mostForked(int limit) {
        String sql = "SELECT b.name, COALESCE(fc.cnt, 0) FROM blocks b LEFT JOIN (SELECT parent_block_id, COUNT(*) AS cnt FROM blocks WHERE is_active = true AND parent_block_id IS NOT NULL GROUP BY parent_block_id) fc ON fc.parent_block_id = b.id WHERE b.is_active = true AND b.parent_block_id IS NULL ORDER BY COALESCE(fc.cnt, 0) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("block", (String) r[0], "forks", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> mostLiked(int limit) {
        String sql = "SELECT b.name, COUNT(bl.id) FROM blocks b LEFT JOIN block_likes bl ON bl.block_id = b.id AND bl.is_active = true WHERE b.is_active = true GROUP BY b.id, b.name ORDER BY COUNT(bl.id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("block", (String) r[0], "likes", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> topAuthors(int limit) {
        String sql = "SELECT u.username, COUNT(b.id) FROM blocks b JOIN users u ON b.owner_id = u.id WHERE b.is_active = true GROUP BY u.id, u.username ORDER BY COUNT(b.id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("author", (String) r[0], "blocks", ((Number) r[1]).longValue()));
        return list;
    }

    @Override
    public Map<String, Object> getCoursesDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", coursesCards());
        d.put("charts", coursesCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> coursesCards() {
        Map<String, Object> c = new LinkedHashMap<>();

        long total = q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true");
        long published = q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true AND c.published = true");
        c.put("totalCourses", total);
        c.put("publishedCourses", published);
        c.put("draftCourses", total - published);

        Object avgBlocks = em.createQuery("SELECT AVG(CAST((SELECT COUNT(cb) FROM CourseBlock cb WHERE cb.course.id = c.id AND cb.isActive = true) AS double)) FROM Course c WHERE c.isActive = true", Double.class).getSingleResult();
        c.put("avgBlocksPerCourse", avgBlocks != null ? Math.round((Double) avgBlocks * 10) / 10.0 : 0);

        Object avgDur;
        try {
            avgDur = em.createNativeQuery("SELECT AVG(cd.total_dur) FROM (SELECT c.id, COALESCE(SUM(ucb.duration), 0) AS total_dur FROM courses c LEFT JOIN course_blocks cb ON cb.course_id = c.id AND cb.is_active = true LEFT JOIN user_course_blocks ucb ON ucb.course_block_id = cb.id AND ucb.is_active = true WHERE c.is_active = true GROUP BY c.id) cd", Object.class).getSingleResult();
        } catch (Exception e) { avgDur = null; }
        double durVal = avgDur instanceof Number n ? n.doubleValue() : 0;
        c.put("avgCourseDuration", Math.round(durVal / 60 * 10) / 10.0);

        return c;
    }

    private Map<String, Object> coursesCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(6));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;

        ch.put("coursesCreated", coursesCreated(sd, ed));
        ch.put("blocksPerCourse", blocksPerCourse());
        ch.put("courseDifficulty", courseDifficulty());
        ch.put("mostViewed", mostViewed(lim));
        ch.put("mostReused", mostReused(lim));

        return ch;
    }

    private List<Map<String, Object>> coursesCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', c.createdDatetime), COUNT(c) FROM Course c WHERE c.isActive = true AND c.createdDatetime >= :s AND c.createdDatetime <= :e GROUP BY FUNCTION('DATE', c.createdDatetime) ORDER BY FUNCTION('DATE', c.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "courses", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> blocksPerCourse() {
        String sql = "SELECT CASE WHEN bc = 0 THEN '0' WHEN bc <= 3 THEN '1-3' WHEN bc <= 5 THEN '4-5' WHEN bc <= 10 THEN '6-10' WHEN bc <= 20 THEN '11-20' WHEN bc <= 50 THEN '21-50' ELSE '50+' END AS range, COUNT(*) FROM (SELECT c.id, COALESCE(COUNT(cb.id), 0) AS bc FROM courses c LEFT JOIN course_blocks cb ON cb.course_id = c.id AND cb.is_active = true WHERE c.is_active = true GROUP BY c.id) t GROUP BY range ORDER BY MIN(t.bc)";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("blocksRange", (String) r[0], "courses", ((Number) r[1]).longValue()));
        return list;
    }

    private Map<String, Object> courseDifficulty() {
        String sql = "SELECT COALESCE(b.difficulty, 'NONE'), COUNT(DISTINCT cb.course_id) FROM course_blocks cb JOIN blocks b ON b.id = cb.block_id AND b.is_active = true WHERE cb.is_active = true GROUP BY b.difficulty";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("EASY", 0L); map.put("NORMAL", 0L); map.put("HARD", 0L); map.put("EXPERT", 0L);
        for (Object[] r : rows) {
            String key = r[0] != null ? r[0].toString() : "NONE";
            if (map.containsKey(key)) map.put(key, ((Number) r[1]).longValue());
        }
        return map;
    }

    private List<Map<String, Object>> mostViewed(int limit) {
        String sql = "SELECT c.name, COALESCE(COUNT(DISTINCT ucb.user_id), 0) FROM courses c LEFT JOIN course_blocks cb ON cb.course_id = c.id AND cb.is_active = true LEFT JOIN user_course_blocks ucb ON ucb.course_block_id = cb.id AND ucb.is_active = true WHERE c.is_active = true GROUP BY c.id, c.name ORDER BY COUNT(DISTINCT ucb.user_id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("course", (String) r[0], "views", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> mostReused(int limit) {
        String sql = "SELECT c.name, COALESCE(COUNT(f.id), 0) FROM courses c JOIN course_blocks cb ON cb.course_id = c.id AND cb.is_active = true JOIN blocks b ON b.id = cb.block_id AND b.is_active = true LEFT JOIN blocks f ON f.parent_block_id = b.id AND f.is_active = true AND f.parent_block_id IS NOT NULL WHERE c.is_active = true GROUP BY c.id, c.name ORDER BY COUNT(f.id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("course", (String) r[0], "reusedBlocks", ((Number) r[1]).longValue()));
        return list;
    }

    @Override
    public Map<String, Object> getFeedDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", feedCards());
        d.put("charts", feedCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> feedCards() {
        Map<String, Object> c = new LinkedHashMap<>();

        long totalPosts = q("SELECT COUNT(p) FROM Post p WHERE p.isActive = true");
        long totalComments = q("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true");
        long totalLikes = q("SELECT COUNT(pl) FROM PostLike pl WHERE pl.isActive = true");

        c.put("totalPosts", totalPosts);
        c.put("totalComments", totalComments);
        c.put("totalLikes", totalLikes);
        c.put("avgLikesPerPost", totalPosts > 0 ? Math.round((double) totalLikes / totalPosts * 10) / 10.0 : 0);
        c.put("avgCommentsPerPost", totalPosts > 0 ? Math.round((double) totalComments / totalPosts * 10) / 10.0 : 0);

        return c;
    }

    private Map<String, Object> feedCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(3));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;

        ch.put("postsCreated", postsCreated(sd, ed));
        ch.put("commentsOverTime", commentsOverTime(sd, ed));
        ch.put("likesOverTime", likesOverTime(sd, ed));
        ch.put("postTypes", postTypes());
        ch.put("topPosts", topPosts(lim));

        return ch;
    }

    private List<Map<String, Object>> postsCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', p.createdDatetime), COUNT(p) FROM Post p WHERE p.isActive = true AND p.createdDatetime >= :s AND p.createdDatetime <= :e GROUP BY FUNCTION('DATE', p.createdDatetime) ORDER BY FUNCTION('DATE', p.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "posts", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> commentsOverTime(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', m.createdDatetime), COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true AND m.createdDatetime >= :s AND m.createdDatetime <= :e GROUP BY FUNCTION('DATE', m.createdDatetime) ORDER BY FUNCTION('DATE', m.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "comments", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> likesOverTime(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', pl.createdDatetime), COUNT(pl) FROM PostLike pl WHERE pl.isActive = true AND pl.createdDatetime >= :s AND pl.createdDatetime <= :e GROUP BY FUNCTION('DATE', pl.createdDatetime) ORDER BY FUNCTION('DATE', pl.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "likes", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> postTypes() {
        try {
            String sql = "SELECT COALESCE(p.content->>'type', 'text') AS ptype, COUNT(*) FROM posts p WHERE p.is_active = true GROUP BY p.content->>'type' ORDER BY COUNT(*) DESC";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("type", (String) r[0], "count", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private List<Map<String, Object>> topPosts(int limit) {
        String sql = "SELECT p.id, u.username, COUNT(pl.id) FROM posts p JOIN users u ON p.user_id = u.id LEFT JOIN post_likes pl ON pl.post_id = p.id AND pl.is_active = true WHERE p.is_active = true GROUP BY p.id, u.username ORDER BY COUNT(pl.id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("post", "#" + r[0].toString() + " by " + r[1].toString(), "likes", ((Number) r[2]).longValue()));
        return list;
    }

    @Override
    public Map<String, Object> getDiscussionsDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", discussionsCards());
        d.put("charts", discussionsCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> discussionsCards() {
        Map<String, Object> c = new LinkedHashMap<>();
        long total = q("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true");
        long replies = q("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.discussion.isActive = true AND m.isActive = true");
        c.put("totalDiscussions", total);
        c.put("openDiscussions", total);
        c.put("closedDiscussions", 0);
        c.put("totalReplies", replies);
        c.put("avgReplies", total > 0 ? Math.round((double) replies / total * 10) / 10.0 : 0);
        Object avgTime;
        try {
            avgTime = em.createNativeQuery("SELECT AVG(EXTRACT(EPOCH FROM ((SELECT MAX(m.created_datetime) FROM discussion_messages m WHERE m.discussion_id = d.id AND m.is_active = true) - d.created_datetime)) / 3600) FROM discussions d WHERE d.is_active = true AND EXISTS (SELECT 1 FROM discussion_messages m WHERE m.discussion_id = d.id AND m.is_active = true)", Object.class).getSingleResult();
        } catch (Exception e) { avgTime = null; }
        c.put("avgResolutionTime", avgTime instanceof Number n ? Math.round(n.doubleValue() * 10) / 10.0 : 0);
        return c;
    }

    private Map<String, Object> discussionsCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(6));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;
        ch.put("discussionsCreated", discussionsCreated(sd, ed));
        ch.put("repliesOverTime", repliesOverTime(sd, ed));
        ch.put("topDiscussionTags", topDiscussionTags(lim));
        ch.put("mostActiveUsers", mostActiveDiscussers(lim));
        ch.put("resolutionTime", resolutionTime());
        return ch;
    }

    private List<Map<String, Object>> discussionsCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', d.createdDatetime), COUNT(d) FROM Discussion d WHERE d.isActive = true AND d.createdDatetime >= :s AND d.createdDatetime <= :e GROUP BY FUNCTION('DATE', d.createdDatetime) ORDER BY FUNCTION('DATE', d.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "discussions", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> repliesOverTime(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', m.createdDatetime), COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true AND m.createdDatetime >= :s AND m.createdDatetime <= :e GROUP BY FUNCTION('DATE', m.createdDatetime) ORDER BY FUNCTION('DATE', m.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "replies", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> topDiscussionTags(int limit) {
        String sql = "SELECT t.name, COUNT(dt.discussion_id) FROM discussion_tags dt JOIN tags t ON t.id = dt.tag_id AND t.is_active = true JOIN discussions d ON d.id = dt.discussion_id AND d.is_active = true GROUP BY t.name ORDER BY COUNT(dt.discussion_id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("tag", (String) r[0], "usages", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> mostActiveDiscussers(int limit) {
        String sql = "SELECT u.username, COUNT(m.id) FROM discussion_messages m JOIN users u ON m.sender_id = u.id WHERE m.is_active = true GROUP BY u.id, u.username ORDER BY COUNT(m.id) DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("user", (String) r[0], "replies", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> resolutionTime() {
        try {
            String s = "SELECT CASE WHEN hrs = 0 THEN '< 1h' WHEN hrs <= 6 THEN '1-6h' WHEN hrs <= 24 THEN '6-24h' WHEN hrs <= 72 THEN '1-3d' WHEN hrs <= 168 THEN '3-7d' ELSE '> 7d' END AS trange, COUNT(*) FROM (SELECT d.id, COALESCE(EXTRACT(EPOCH FROM ((SELECT MAX(m.created_datetime) FROM discussion_messages m WHERE m.discussion_id = d.id AND m.is_active = true) - d.created_datetime)) / 3600, 0) AS hrs FROM discussions d WHERE d.is_active = true) t GROUP BY trange ORDER BY MIN(t.hrs)";
            List<Object[]> rows = em.createNativeQuery(s, Object[].class).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("hoursRange", (String) r[0], "discussions", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    @Override
    public Map<String, Object> getContestsDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", contestsCards());
        d.put("charts", contestsCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> contestsCards() {
        Map<String, Object> c = new LinkedHashMap<>();
        long total = q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true");
        long active = q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true AND c.status NOT IN ('RESULTS','CANCELLED')");
        long finished = q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true AND c.status = 'RESULTS'");
        long participants = q("SELECT COALESCE(SUM(c.participantCount), 0) FROM Contest c WHERE c.isActive = true");
        long submissions = q("SELECT COUNT(c) FROM Course c WHERE c.isActive = true AND c.contest IS NOT NULL");
        c.put("totalContests", total);
        c.put("activeContests", active);
        c.put("finishedContests", finished);
        c.put("totalParticipants", participants);
        c.put("avgParticipants", total > 0 ? Math.round((double) participants / total * 10) / 10.0 : 0);
        c.put("totalSubmissions", submissions);
        return c;
    }

    private Map<String, Object> contestsCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(12));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 15;
        ch.put("contestsCreated", contestsCreated(sd, ed));
        ch.put("participantsPerContest", participantsPerContest(lim));
        ch.put("submissionsOverTime", submissionsOverTime(sd, ed));
        ch.put("completionRate", completionRate());
        ch.put("mostPopular", mostPopular(lim));
        return ch;
    }

    private List<Map<String, Object>> contestsCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', c.createdDatetime), COUNT(c) FROM Contest c WHERE c.isActive = true AND c.createdDatetime >= :s AND c.createdDatetime <= :e GROUP BY FUNCTION('DATE', c.createdDatetime) ORDER BY FUNCTION('DATE', c.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "contests", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> participantsPerContest(int limit) {
        String sql = "SELECT c.title, c.participant_count FROM contest c WHERE c.is_active = true ORDER BY c.participant_count DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("contest", (String) r[0], "participants", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> submissionsOverTime(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', c.createdDatetime), COUNT(c) FROM Course c WHERE c.isActive = true AND c.contest IS NOT NULL AND c.createdDatetime >= :s AND c.createdDatetime <= :e GROUP BY FUNCTION('DATE', c.createdDatetime) ORDER BY FUNCTION('DATE', c.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "submissions", ((Number) r[1]).longValue()));
        return list;
    }

    private Map<String, Object> completionRate() {
        long completed = q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true AND c.status = 'RESULTS'");
        long cancelled = q("SELECT COUNT(c) FROM Contest c WHERE c.isActive = true AND c.status = 'CANCELLED'");
        return Map.of("completed", completed, "abandoned", cancelled);
    }

    private List<Map<String, Object>> mostPopular(int limit) {
        String sql = "SELECT c.title, c.participant_count FROM contest c WHERE c.is_active = true ORDER BY c.participant_count DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("contest", (String) r[0], "participants", ((Number) r[1]).longValue()));
        return list;
    }

    @Override
    public Map<String, Object> getTagsDashboard(String startDate, String endDate, Integer limit) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("cards", tagsCards());
        d.put("charts", tagsCharts(startDate, endDate, limit));
        return d;
    }

    private Map<String, Object> tagsCards() {
        Map<String, Object> c = new LinkedHashMap<>();
        long total = q("SELECT COUNT(t) FROM Tag t WHERE t.isActive = true");
        c.put("totalTags", total);

        try {
            String sql = "SELECT (SELECT COUNT(*) FROM tags t WHERE t.is_active = true AND (EXISTS (SELECT 1 FROM block_tags bt WHERE bt.tag_id = t.id) OR EXISTS (SELECT 1 FROM course_tags ct WHERE ct.tag_id = t.id) OR EXISTS (SELECT 1 FROM post_tags pt WHERE pt.tag_id = t.id) OR EXISTS (SELECT 1 FROM discussion_tags dt WHERE dt.tag_id = t.id) OR EXISTS (SELECT 1 FROM contest_tags cot WHERE cot.tag_id = t.id))) AS used, (SELECT COUNT(*) FROM tags t WHERE t.is_active = true AND NOT EXISTS (SELECT 1 FROM block_tags bt WHERE bt.tag_id = t.id) AND NOT EXISTS (SELECT 1 FROM course_tags ct WHERE ct.tag_id = t.id) AND NOT EXISTS (SELECT 1 FROM post_tags pt WHERE pt.tag_id = t.id) AND NOT EXISTS (SELECT 1 FROM discussion_tags dt WHERE dt.tag_id = t.id) AND NOT EXISTS (SELECT 1 FROM contest_tags cot WHERE cot.tag_id = t.id)) AS unused";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
            if (!rows.isEmpty()) {
                c.put("usedTags", ((Number) rows.get(0)[0]).longValue());
                c.put("unusedTags", ((Number) rows.get(0)[1]).longValue());
            } else { c.put("usedTags", 0L); c.put("unusedTags", 0L); }
        } catch (Exception e) { c.put("usedTags", 0L); c.put("unusedTags", 0L); }

        long totalBlockTags;
        try {
            totalBlockTags = ((Number) em.createNativeQuery("SELECT COALESCE(COUNT(*), 0) FROM block_tags").getSingleResult()).longValue();
        } catch (Exception e) { totalBlockTags = 0; }
        c.put("avgBlocksPerTag", total > 0 ? Math.round((double) totalBlockTags / total * 10) / 10.0 : 0);

        try {
            String sql = "SELECT t.name FROM tags t JOIN (SELECT tag_id, COUNT(*) AS cnt FROM block_tags GROUP BY tag_id UNION ALL SELECT tag_id, COUNT(*) FROM course_tags UNION ALL SELECT tag_id, COUNT(*) FROM post_tags UNION ALL SELECT tag_id, COUNT(*) FROM discussion_tags UNION ALL SELECT tag_id, COUNT(*) FROM contest_tags) all_tags ON all_tags.tag_id = t.id WHERE t.is_active = true GROUP BY t.name ORDER BY SUM(all_tags.cnt) DESC LIMIT 1";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).getResultList();
            c.put("mostUsedTag", rows.isEmpty() ? "" : rows.get(0)[0].toString());
        } catch (Exception e) { c.put("mostUsedTag", ""); }

        return c;
    }

    private Map<String, Object> tagsCharts(String startDate, String endDate, Integer limit) {
        Map<String, Object> ch = new LinkedHashMap<>();
        LocalDate sd = parseDate(startDate, LocalDate.now().minusMonths(12));
        LocalDate ed = parseDate(endDate, LocalDate.now());
        int lim = limit != null ? limit : 20;
        ch.put("topTags", adminTopTags(lim));
        ch.put("tagsCreated", tagsCreated(sd, ed));
        ch.put("tagsByCategory", List.of());
        ch.put("tagGrowth", tagGrowth(sd, ed));
        ch.put("tagCoOccurrence", tagCoOccurrence(lim));
        return ch;
    }

    private List<Map<String, Object>> adminTopTags(int limit) {
        String sql = "SELECT t.name, CAST(COALESCE(bt.cnt,0)+COALESCE(pt.cnt,0)+COALESCE(ct.cnt,0)+COALESCE(dt.cnt,0)+COALESCE(cot.cnt,0) AS bigint) AS usages FROM tags t LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM block_tags GROUP BY tag_id) bt ON t.id=bt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM post_tags GROUP BY tag_id) pt ON t.id=pt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM course_tags GROUP BY tag_id) ct ON t.id=ct.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM discussion_tags GROUP BY tag_id) dt ON t.id=dt.tag_id LEFT JOIN (SELECT tag_id, COUNT(*) AS cnt FROM contest_tags GROUP BY tag_id) cot ON t.id=cot.tag_id WHERE t.is_active=true ORDER BY usages DESC LIMIT ?";
        List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("tag", (String) r[0], "usages", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> tagsCreated(LocalDate start, LocalDate end) {
        List<Object[]> rows = em.createQuery(
            "SELECT FUNCTION('DATE', t.createdDatetime), COUNT(t) FROM Tag t WHERE t.isActive = true AND t.createdDatetime >= :s AND t.createdDatetime <= :e GROUP BY FUNCTION('DATE', t.createdDatetime) ORDER BY FUNCTION('DATE', t.createdDatetime)",
            Object[].class).setParameter("s", start.atStartOfDay()).setParameter("e", end.atTime(23, 59, 59)).getResultList();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "tags", ((Number) r[1]).longValue()));
        return list;
    }

    private List<Map<String, Object>> tagGrowth(LocalDate start, LocalDate end) {
        String sql = "SELECT CAST(b.created_datetime AS DATE) AS dt, COUNT(bt.block_id) AS usages FROM block_tags bt JOIN blocks b ON b.id = bt.block_id AND b.is_active = true WHERE b.created_datetime >= ? AND b.created_datetime <= ? GROUP BY CAST(b.created_datetime AS DATE) ORDER BY dt";
        try {
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class)
                .setParameter(1, start.atStartOfDay()).setParameter(2, end.atTime(23, 59, 59)).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("date", r[0].toString(), "usages", ((Number) r[1]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private List<Map<String, Object>> tagCoOccurrence(int limit) {
        try {
            String sql = "SELECT t1.name, t2.name, COUNT(*) FROM block_tags bt1 JOIN block_tags bt2 ON bt1.block_id = bt2.block_id AND bt1.tag_id < bt2.tag_id JOIN tags t1 ON t1.id = bt1.tag_id JOIN tags t2 ON t2.id = bt2.tag_id JOIN blocks b ON b.id = bt1.block_id AND b.is_active = true GROUP BY t1.name, t2.name ORDER BY COUNT(*) DESC LIMIT ?";
            List<Object[]> rows = em.createNativeQuery(sql, Object[].class).setParameter(1, limit).getResultList();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object[] r : rows) list.add(Map.of("tag1", (String) r[0], "tag2", (String) r[1], "occurrences", ((Number) r[2]).longValue()));
            return list;
        } catch (Exception e) { return List.of(); }
    }

    private List<Map<String, Object>> activityHeatmap(LocalDate date) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LocalDateTime s = date.atStartOfDay(), e = date.plusDays(1).atStartOfDay();
            for (int h = 0; h < 24; h++) {
                LocalDateTime hs = date.atTime(h, 0), he = date.atTime(h, 59, 59);
                long posts = count("SELECT COUNT(p) FROM Post p WHERE p.isActive = true AND HOUR(p.createdDatetime) = :h AND p.createdDatetime >= :s AND p.createdDatetime < :e", h, s, e);
                long blocks = count("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND HOUR(b.createdDatetime) = :h AND b.createdDatetime >= :s AND b.createdDatetime < :e", h, s, e);
                long courses = count("SELECT COUNT(c) FROM Course c WHERE c.isActive = true AND HOUR(c.createdDatetime) = :h AND c.createdDatetime >= :s AND c.createdDatetime < :e", h, s, e);
                long discussions = count("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true AND HOUR(d.createdDatetime) = :h AND d.createdDatetime >= :s AND d.createdDatetime < :e", h, s, e);
                long messages = count("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true AND HOUR(m.createdDatetime) = :h AND m.createdDatetime >= :s AND m.createdDatetime < :e", h, s, e);
                list.add(Map.of("hour", String.valueOf(h), "posts", posts, "blocks", blocks, "courses", courses, "discussions", discussions, "messages", messages));
            }
        } catch (Exception e) { return list; }
        return list;
    }

    private List<Map<String, Object>> activityPeaks(LocalDate date) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LocalDateTime s = date.atStartOfDay(), e = date.plusDays(1).atStartOfDay();
            for (int h = 0; h < 24; h++) {
                long posts = count("SELECT COUNT(p) FROM Post p WHERE p.isActive = true AND HOUR(p.createdDatetime) = :h AND p.createdDatetime >= :s AND p.createdDatetime < :e", h, s, e);
                long blocks = count("SELECT COUNT(b) FROM Block b WHERE b.isActive = true AND HOUR(b.createdDatetime) = :h AND b.createdDatetime >= :s AND b.createdDatetime < :e", h, s, e);
                long courses = count("SELECT COUNT(c) FROM Course c WHERE c.isActive = true AND HOUR(c.createdDatetime) = :h AND c.createdDatetime >= :s AND c.createdDatetime < :e", h, s, e);
                long discussions = count("SELECT COUNT(d) FROM Discussion d WHERE d.isActive = true AND HOUR(d.createdDatetime) = :h AND d.createdDatetime >= :s AND d.createdDatetime < :e", h, s, e);
                long messages = count("SELECT COUNT(m) FROM DiscussionMessage m WHERE m.isActive = true AND HOUR(m.createdDatetime) = :h AND m.createdDatetime >= :s AND m.createdDatetime < :e", h, s, e);
                long total = posts + blocks + courses + discussions + messages;
                list.add(Map.of("hour", String.valueOf(h), "total", total, "posts", posts, "blocks", blocks, "courses", courses, "discussions", discussions, "messages", messages));
            }
        } catch (Exception e) { return list; }
        return list;
    }

    private long count(String jpql, int hour, LocalDateTime start, LocalDateTime end) {
        try {
            return em.createQuery(jpql, Long.class)
                .setParameter("h", hour).setParameter("s", start).setParameter("e", end)
                .getSingleResult();
        } catch (Exception e) { return 0; }
    }

    private long q(String jpql, Object... p) { var q = em.createQuery(jpql, Long.class); if (p.length > 0 && p[0] instanceof LocalDateTime t) q.setParameter("t", t); return q.getSingleResult(); }
    private long n(Object o) { return o instanceof Number n ? n.longValue() : 0; }
    private LocalDateTime d(int n) { return LocalDateTime.now().minusDays(n); }
    private LocalDate parseDate(String d, LocalDate fallback) { try { return d != null ? LocalDate.parse(d, DF) : fallback; } catch (Exception e) { return fallback; } }
}
