package com.sunrisedental.controller;

import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.model.Patient;
import java.util.List;

public class PatientController {
    private PatientDAO patientDAO;

    public PatientController() {
        this.patientDAO = new PatientDAOImpl();
    }

    public boolean registerPatient(Patient patient) {
        return patientDAO.create(patient);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }
}