package com.sunrisedental;

import com.sunrisedental.controller.BillController;
import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingControllerTest {
    @Test
    void generatesStandardBillForConfirmedAppointment() {
        BillDAO dao = mock(BillDAO.class);
        when(dao.create(any(Bill.class))).thenReturn(true);
        Appointment appointment = appointment("confirmed");

        Bill bill = new BillController(dao).generateBill(appointment, "standard");

        assertNotNull(bill);
        assertEquals(1500.0, bill.getTotal(), 0.01);
        assertEquals(1000.0, bill.getTreatmentFee(), 0.01);
        assertEquals(500.0, bill.getConsultationFee(), 0.01);
        verify(dao).create(any(Bill.class));
    }

    @Test
    void rejectsPendingAndCancelledAppointments() {
        BillDAO dao = mock(BillDAO.class);
        BillController controller = new BillController(dao);

        assertNull(controller.generateBill(appointment("pending"), "standard"));
        assertNull(controller.generateBill(appointment("cancelled"), "standard"));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectsSecondBillForSameAppointment() {
        BillDAO dao = mock(BillDAO.class);
        when(dao.existsForAppointment(10)).thenReturn(true);

        assertNull(new BillController(dao).generateBill(appointment("confirmed"), "standard"));
        verify(dao, never()).create(any(Bill.class));
    }

    @Test
    void rejectsNullIncompleteAndBlankBillRequests() {
        BillDAO dao = mock(BillDAO.class);
        BillController controller = new BillController(dao);

        assertNull(controller.generateBill(null, "standard"));
        assertNull(controller.generateBill(new Appointment(), "standard"));
        assertNull(controller.generateBill(appointment("confirmed"), "   "));
        verifyNoInteractions(dao);
    }

    @Test
    void calculatesEmergencyAndChildTotals() {
        BillDAO dao = mock(BillDAO.class);
        when(dao.create(any(Bill.class))).thenReturn(true);
        BillController controller = new BillController(dao);

        Bill emergency = controller.generateBill(appointment("confirmed"), "emergency");
        Bill child = controller.generateBill(appointment("confirmed"), "child");

        assertEquals(2000.0, emergency.getTotal(), 0.01);
        assertEquals(1300.0, child.getTotal(), 0.01);
    }

    private Appointment appointment(String status) {
        Appointment appointment = new Appointment();
        appointment.setId(10);
        appointment.setStatus(status);
        Treatment treatment = new Treatment();
        treatment.setBaseFee(1000);
        treatment.setConsultationFee(500);
        appointment.setTreatment(treatment);
        return appointment;
    }
}