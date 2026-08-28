package com.sunrisedental;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.controller.AuthController;
import com.sunrisedental.controller.BillController;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.model.User;
import com.sunrisedental.util.SessionManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemScenarioReportTest {
    @Test
    void printsCoreSystemScenarioReport() {
        System.out.println();
        System.out.println("========== SUNRISE DENTAL AUTOMATION REPORT ==========");
        report("Active user with correct password", true, login(true, "password"));
        report("Inactive user is rejected", false, login(false, "password"));
        report("Wrong password is rejected", false, login(true, "wrong"));
        report("Complete future appointment is accepted", true, book(true, false));
        report("Past date is rejected", false, book(false, false));
        report("Past time today is rejected", false, book(true, true));
        report("Pending appointment cannot be billed", true, billingRejected("pending"));
        report("Confirmed appointment bill total is correct", 1500.0, confirmedBillTotal());
        report("Treatment duration is stored", 45, treatmentDuration());
        System.out.println("=======================================================");

        assertAll(
                () -> assertTrue(login(true, "password")),
                () -> assertFalse(login(false, "password")),
                () -> assertFalse(login(true, "wrong")),
                () -> assertTrue(book(true, false)),
                () -> assertFalse(book(false, false)),
                () -> assertFalse(book(true, true)),
                () -> assertTrue(billingRejected("pending")),
                () -> assertEquals(1500.0, confirmedBillTotal(), 0.01),
                () -> assertEquals(45, treatmentDuration())
        );
    }

    private void report(String scenario, Object expected, Object actual) {
        TestConsole.report(scenario, expected, actual);
    }

    private boolean login(boolean active, String password) {
        SessionManager.logout();
        User user = new User();
        user.setActive(active);
        user.setPasswordHash(com.sunrisedental.util.PasswordUtil.hash("password"));
        UserDAO dao = mock(UserDAO.class);
        when(dao.findByUsername("user")).thenReturn(user);
        return new AuthController(dao).login("user", password);
    }

    private boolean book(boolean futureDate, boolean pastTime) {
        Appointment appointment = appointment();
        appointment.setAppointmentDate(pastTime ? LocalDate.now()
            : (futureDate ? LocalDate.now().plusDays(1) : LocalDate.now().minusDays(1)));
        if (pastTime) appointment.setAppointmentTime(LocalTime.now().minusMinutes(2));
        AppointmentDAO dao = mock(AppointmentDAO.class);
        when(dao.create(any(Appointment.class))).thenReturn(true);
        return new AppointmentController(dao).bookAppointment(appointment);
    }

    private boolean billingRejected(String status) {
        return new BillController(mock(BillDAO.class)).generateBill(billAppointment(status), "standard") == null;
    }

    private double confirmedBillTotal() {
        BillDAO dao = mock(BillDAO.class);
        when(dao.create(any(com.sunrisedental.model.Bill.class))).thenReturn(true);
        return new BillController(dao).generateBill(billAppointment("confirmed"), "standard").getTotal();
    }

    private int treatmentDuration() {
        Treatment treatment = new Treatment();
        treatment.setDurationMinutes(45);
        return treatment.getDurationMinutes();
    }

    private Appointment billAppointment(String status) {
        Appointment appointment = appointment();
        appointment.setId(1);
        appointment.setStatus(status);
        return appointment;
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber("APT-REPORT-001");
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setAppointmentTime(LocalTime.NOON);
        Patient patient = new Patient();
        patient.setId(1);
        Dentist dentist = new Dentist();
        dentist.setId(1);
        Treatment treatment = new Treatment();
        treatment.setBaseFee(1000);
        treatment.setConsultationFee(500);
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        return appointment;
    }
}