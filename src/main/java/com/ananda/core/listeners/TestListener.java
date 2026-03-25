package com.ananda.core.listeners;

import org.testng.ITestContext;
import org.testng.IExecutionListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.ananda.core.reports.ExtentManager;
import com.ananda.core.reports.ExtentTestManager;
import com.aventstack.extentreports.ExtentTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestListener implements ITestListener, IExecutionListener {

    private static class ModuleStats {
        int positive;
        int negative;
        int passed;
        int failed;
        int skipped;
    }

    private static final Map<String, ModuleStats> STATS = new ConcurrentHashMap<>();
    private static final List<String> SUMMARY_MODULE_ORDER = Arrays.asList(
            "AdminLoginTest",
            "ProgramsTest",
            "AssessmentTest",
            "ActivityTest",
            "RecipeCollectionTest",
            "GuestUserTest",
            "DownloadsTest",
            "TeamMembersTest",
            "AccessManagementTest",
            "AnnouncementTest",
            "UserRoleTest",
            "DeleteRequestsTest",
            "ArticlesTest",
            "ProgramFeedbackTest",
            "TestimonialsTest",
            "QMSScorecardTest",
            "FeedbackReportTest",
            "GuestCommentsTest",
            "ConsultantRatingsTest",
            "TherapistSessionsTest",
            "AnalyticalReportTest"
    );

    private ModuleStats getStats(ITestResult result) {
        String module = result.getTestClass().getRealClass().getSimpleName();
        return STATS.computeIfAbsent(module, k -> new ModuleStats());
    }

    private void captureGroupCounts(ITestResult result, ModuleStats stats) {
        String[] groups = result.getMethod().getGroups();
        if (groups == null) {
            return;
        }
        for (String g : groups) {
            if ("Positive".equalsIgnoreCase(g)) {
                stats.positive++;
            } else if ("Negative".equalsIgnoreCase(g)) {
                stats.negative++;
            }
        }
    }

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {

        ExtentTest test = ExtentManager.getInstance()
                .createTest(result.getMethod().getMethodName());

        ExtentTestManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ModuleStats stats = getStats(result);
        stats.passed++;
        captureGroupCounts(result, stats);
        ExtentTest test = ExtentTestManager.getTest();
        if (test == null) {
            test = ExtentManager.getInstance().createTest(result.getMethod().getMethodName());
            ExtentTestManager.setTest(test);
        }
        test.pass("Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ModuleStats stats = getStats(result);
        stats.failed++;
        captureGroupCounts(result, stats);
        ExtentTest test = ExtentTestManager.getTest();
        if (test == null) {
            test = ExtentManager.getInstance().createTest(result.getMethod().getMethodName());
            ExtentTestManager.setTest(test);
        }
        test.fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ModuleStats stats = getStats(result);
        stats.skipped++;
        captureGroupCounts(result, stats);
        ExtentTest test = ExtentTestManager.getTest();
        if (test == null) {
            test = ExtentManager.getInstance().createTest(result.getMethod().getMethodName());
            ExtentTestManager.setTest(test);
        }
        test.skip("Test Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getInstance().flush();
    }

    @Override
    public void onExecutionFinish() {
        if (STATS.isEmpty()) {
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' width='90%' style='border-collapse: collapse'>");
        html.append("<tr style='background:#f2f2f2'>")
                .append("<th>Name</th><th>Positive</th><th>Negative</th><th>Passed</th>")
                .append("<th>Failed</th><th>Skipped</th><th>Others</th><th>Passed %</th>")
                .append("</tr>");

        List<String> orderedModules = new ArrayList<>();
        for (String module : SUMMARY_MODULE_ORDER) {
            if (STATS.containsKey(module)) {
                orderedModules.add(module);
            }
        }
        for (String module : STATS.keySet()) {
            if (!orderedModules.contains(module)) {
                orderedModules.add(module);
            }
        }

        for (String module : orderedModules) {
            ModuleStats s = STATS.get(module);
            int total = s.passed + s.failed + s.skipped;
            int passPct = total == 0 ? 0 : (s.passed * 100) / total;

            html.append("<tr>")
                    .append("<td>").append(module).append("</td>")
                    .append("<td>").append(s.positive).append("</td>")
                    .append("<td>").append(s.negative).append("</td>")
                    .append("<td>").append(s.passed).append("</td>")
                    .append("<td>").append(s.failed).append("</td>")
                    .append("<td>").append(s.skipped).append("</td>")
                    .append("<td>0</td>")
                    .append("<td>").append(passPct).append("%</td>")
                    .append("</tr>");
        }

        int totalPositive = 0;
        int totalNegative = 0;
        int totalPassed = 0;
        int totalFailed = 0;
        int totalSkipped = 0;
        int totalOthers = 0;

        for (String module : orderedModules) {
            ModuleStats s = STATS.get(module);
            if (s == null) {
                continue;
            }
            totalPositive += s.positive;
            totalNegative += s.negative;
            totalPassed += s.passed;
            totalFailed += s.failed;
            totalSkipped += s.skipped;
        }

        int grandTotal = totalPassed + totalFailed + totalSkipped + totalOthers;
        int totalPassPct = grandTotal == 0 ? 0 : (totalPassed * 100) / grandTotal;

        html.append("<tr style='background:#f2f2f2;font-weight:bold'>")
                .append("<td>Total</td>")
                .append("<td>").append(totalPositive).append("</td>")
                .append("<td>").append(totalNegative).append("</td>")
                .append("<td>").append(totalPassed).append("</td>")
                .append("<td>").append(totalFailed).append("</td>")
                .append("<td>").append(totalSkipped).append("</td>")
                .append("<td>").append(totalOthers).append("</td>")
                .append("<td>").append(totalPassPct).append("%</td>")
                .append("</tr>");

        html.append("</table>");

        ExtentTest summary = ExtentManager.getInstance().createTest("Overall Summary");
        summary.info(html.toString());
        ExtentManager.getInstance().flush();
    }
}
