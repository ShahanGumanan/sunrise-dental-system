package com.sunrisedental.controller;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import java.time.LocalDate;
import java.util.List;

public class ReportController {
    private BillDAO billDAO = new BillDAOImpl();

    public List<Bill> getRevenueReport(LocalDate start, LocalDate end) {
        return billDAO.findBillsByDateRange(start, end);
    }
}