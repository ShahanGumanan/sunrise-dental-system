package com.sunrisedental;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AppointmentWorkflowTest {
    @Test
    public void delegatesConflictCheckWithTreatmentDuration() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        Appointment appointment = validAppointment();
        appointment.getTreatment().setDurationMinutes(60);
        when(dao.existsActiveAppointment(4, appointment.getAppointmentDate(), appointment.getAppointmentTime(), 60, 0))
                .thenReturn(true);
        assertTrue(new AppointmentController(dao).hasAppointmentConflict(appointment));
        verify(dao).existsActiveAppointment(4, appointment.getAppointmentDate(), appointment.getAppointmentTime(), 60, 0);
    }

    @Test
    public void delegatesDentistConfirmationAndCancellation() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        when(dao.updateStatusForDentist(12, "confirmed", 7)).thenReturn(true);
        when(dao.updateStatusForDentist(12, "cancelled", 7)).thenReturn(true);
        AppointmentController controller = new AppointmentController(dao);
        assertTrue(controller.confirmAppointment(12, 7));
        assertTrue(controller.cancelAppointmentByDentist(12, 7));
        verify(dao).updateStatusForDentist(12, "confirmed", 7);
        verify(dao).updateStatusForDentist(12, "cancelled", 7);
    }

    @Test
    public void doesNotReportConflictForIncompleteAppointment() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        boolean result = new AppointmentController(dao).hasAppointmentConflict(new Appointment());
        assertFalse(result);
        verifyNoInteractions(dao);
    }

    @Test
    public void rejectsInvalidUpdateBeforeCallingDao() {
        AppointmentDAO dao = mock(AppointmentDAO.class);
        Appointment invalid = validAppointment();
        invalid.setAppointmentDate(LocalDate.now().minusDays(1));
        assertFalse(new AppointmentController(dao).updateAppointment(invalid));
        verifyNoInteractions(dao);
    }

    private Appointment validAppointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setAppointmentTime(LocalTime.of(23, 0));
        Patient patient = new Patient();
        patient.setId(1);
        Dentist dentist = new Dentist();
        dentist.setId(4);
        Treatment treatment = new Treatment();
        treatment.setId(2);
        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        return appointment;
    }
}