package com.sunrisedental.controller;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import java.time.LocalDate;
import java.util.List;
import com.sunrisedental.util.ApiClient;
import com.sunrisedental.util.JsonUtil;

public class ReportController {
    private BillDAO billDAO;
    private final boolean remote;

    public ReportController() {
        this(new BillDAOImpl());
    }

    public ReportController(BillDAO billDAO) {
        this.billDAO = billDAO;
        this.remote = false;
    }

    public List<Bill> getRevenueReport(LocalDate start, LocalDate end) {
        if (remote) {
            try { return JsonUtil.fromJsonList(ApiClient.get("/reports/revenue?start=" + start + "&end=" + end), Bill.class); }
            catch (Exception ignored) { return java.util.Collections.emptyList(); }
        }
        return billDAO.findBillsByDateRange(start, end);
    }
}