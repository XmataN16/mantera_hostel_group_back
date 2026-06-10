package ru.mantera.hostel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.report.*;
import ru.mantera.hostel.service.ReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/occupancy")
    public OccupancyReportResponse getOccupancy(
            @RequestParam Long hotelId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return reportService.getOccupancy(hotelId, date);
    }

    @GetMapping("/checkins-checkouts")
    public CheckInOutReportResponse getCheckInsCheckOuts(
            @RequestParam Long hotelId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return reportService.getCheckInsCheckOuts(hotelId, date);
    }

    @GetMapping("/revenue")
    public RevenueReportResponse getRevenue(
            @RequestParam Long hotelId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return reportService.getRevenue(hotelId, fromDate, toDate);
    }

    @GetMapping("/debts")
    public DebtReportResponse getDebts(@RequestParam Long hotelId) {
        return reportService.getDebts(hotelId);
    }

    @GetMapping("/room-type-distribution")
    public List<RoomTypeDistributionItemResponse> getRoomTypeDistribution(
            @RequestParam Long hotelId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return reportService.getRoomTypeDistribution(hotelId, fromDate, toDate);
    }
}