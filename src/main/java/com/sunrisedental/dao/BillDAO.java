package com.sunrisedental.dao;
import com.sunrisedental.model.Bill;
import java.util.List;

public interface BillDAO {
    boolean create(Bill bill);
    List<Bill> findBillsByDateRange(java.time.LocalDate start, java.time.LocalDate end);
}