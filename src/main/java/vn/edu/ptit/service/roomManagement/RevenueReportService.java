package vn.edu.ptit.service.roomManagement;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.RevenueReportResponse;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.RevenueReportRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.edu.ptit.Exception.InvalidRequestException;
import vn.edu.ptit.Exception.UnauthorizedException;

import java.util.List;
import java.util.Comparator;

@Service
@AllArgsConstructor
public class RevenueReportService {
    private final RevenueReportRepository revenueReportRepository;
    private final AuthService authService;

    public RevenueReportResponse getMonthlyReport(Integer year, Integer month) {
        checkYear(year);
        if (month == null || month < 1 || month > 12) {
            throw new InvalidRequestException("Thang thong ke khong hop le");
        }
        String billingMonth = year + "-" + String.format("%02d", month);
        Long landlordId = getLandlordId();
        List<Object[]> reports = revenueReportRepository.reportByMonth(
                billingMonth,
                landlordId,
                RentPayments.Status.PAID
        );
        if (reports.isEmpty()) {
            return emptyReport(year, month);
        }
        return mapToResponse(year, month, reports.get(0));
    }

    public RevenueReportResponse getYearReport(Integer year) {
        checkYear(year);
        String yearText = String.valueOf(year);
        Long landlordId = getLandlordId();
        List<Object[]> reports = revenueReportRepository.reportByYear(
                yearText,
                landlordId,
                RentPayments.Status.PAID
        );
        if (reports.isEmpty()) {
            return emptyReport(year, null);
        }
        return mapToResponse(year, null, reports.get(0));
    }

    public RevenueReportResponse getHighestMonthReport(Integer year) {
        return getExtremeMonthReport(year, true);
    }

    public RevenueReportResponse getLowestMonthReport(Integer year) {
        return getExtremeMonthReport(year, false);
    }

    private RevenueReportResponse getExtremeMonthReport(Integer year, boolean highest) {
        checkYear(year);
        String yearText = String.valueOf(year);
        Long landlordId = getLandlordId();
        List<Object[]> monthlyRevenue = revenueReportRepository.reportMonthlyRevenueByYear(
                yearText,
                landlordId,
                RentPayments.Status.PAID
        );

        if (monthlyRevenue.isEmpty()) {
            return emptyReport(year, null);
        }

        Comparator<Object[]> byRevenue = Comparator.comparing(row -> ((Number) row[1]).doubleValue());
        Object[] target = highest
                ? monthlyRevenue.stream().max(byRevenue).orElse(null)
                : monthlyRevenue.stream().min(byRevenue).orElse(null);

        if (target == null) {
            return emptyReport(year, null);
        }

        int month = Integer.parseInt((String) target[0]);
        return getMonthlyReport(year, month);
    }

    private RevenueReportResponse mapToResponse(Integer year, Integer month, Object[] row) {
        return RevenueReportResponse.builder()
                .year(year)
                .month(month)
                .rentAmount(((Number) row[0]).doubleValue())
                .utilityAmount(((Number) row[1]).doubleValue())
                .totalRevenue(((Number) row[2]).doubleValue())
                .paidCount(((Number) row[3]).longValue())
                .unpaidCount(0L)
                .build();
    }

    private Long getLandlordId() {
        User user = authService.getUser();
        if (user.getRole() == User.Role.ADMIN) {
            return null;
        }
        if (user.getRole() != User.Role.LANDLORD) {
            throw new UnauthorizedException("Ban khong co quyen xem bao cao doanh thu");
        }
        return user.getId();
    }

    private void checkYear(Integer year) {
        if (year == null || year < 2000 || year > 2100) {
            throw new InvalidRequestException("Nam thong ke khong hop le");
        }
    }

    private RevenueReportResponse emptyReport(Integer year, Integer month) {
        return RevenueReportResponse.builder()
                .year(year)
                .month(month)
                .rentAmount(0.0)
                .utilityAmount(0.0)
                .totalRevenue(0.0)
                .paidCount(0L)
                .unpaidCount(0L)
                .build();
    }
}
