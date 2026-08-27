package com.sunrisedental.controller;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import java.time.LocalDate;
import java.util.List;

public class ReportController {
    private AppointmentDAO apptDAO = new AppointmentDAOImpl();
    private BillDAO billDAO = new BillDAOImpl();

    public List<Appointment> getDailyAppointments(LocalDate date) {
        return apptDAO.findByDate(date);
    }

    public List<Bill> getRevenueReport(LocalDate start, LocalDate end) {
        return billDAO.findBillsByDateRange(start, end);
    }
}