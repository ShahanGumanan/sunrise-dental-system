package com.sunrisedental;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {
    @Test
    void rejectsIncompleteAppointmentsWithoutCallingDao() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        AppointmentController controller = new AppointmentController(dao);

        assertFalse(controller.bookAppointment(new Appointment()));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectsAppointmentsInThePast() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        AppointmentController controller = new AppointmentController(dao);
        Appointment appointment = validAppointment();
        appointment.setAppointmentDate(LocalDate.now().minusDays(1));

        assertFalse(controller.bookAppointment(appointment));
        verifyNoInteractions(dao);
    }

    @Test
    void rejectsPastTimeOnToday() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        AppointmentController controller = new AppointmentController(dao);
        Appointment appointment = validAppointment();
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalDateTime.now().minusMinutes(1).toLocalTime());

        boolean result = controller.bookAppointment(appointment);

        TestConsole.report("Same-day appointment with past time", false, result);
        assertFalse(result);
        verifyNoInteractions(dao);
    }

    @Test
    void rejectsBlankAppointmentNumber() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        AppointmentController controller = new AppointmentController(dao);
        Appointment appointment = validAppointment();
        appointment.setAppointmentNumber("   ");

        boolean result = controller.bookAppointment(appointment);

        TestConsole.report("Blank appointment number", false, result);
        assertFalse(result);
        verifyNoInteractions(dao);
    }

    @Test
    void acceptsFutureAppointmentAndDelegatesToDao() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        when(dao.create(any(Appointment.class))).thenReturn(true);
        Appointment appointment = validAppointment();
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));

        boolean result = new AppointmentController(dao).bookAppointment(appointment);

        TestConsole.report("Complete future appointment", true, result);
        assertTrue(result);
        verify(dao).create(appointment);
    }

    private Appointment validAppointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber("APT-TEST-0001");
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalTime.NOON);
        Patient patient = new Patient();
        patient.setId(1);
        Dentist dentist = new Dentist();
        dentist.setId(1);
        Treatment treatment = new Treatment();
        treatment.setId(1);
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        return appointment;
    }
}