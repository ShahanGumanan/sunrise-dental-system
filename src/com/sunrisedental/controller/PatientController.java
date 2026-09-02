package com.sunrisedental.controller;

import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.model.Patient;
import com.sunrisedental.util.ApiClient;
import com.sunrisedental.util.JsonUtil;
import java.util.List;

public class PatientController {
    private PatientDAO patientDAO;
    private final boolean remote;

    public PatientController() {
        this.patientDAO = null;
        this.remote = true;
    }

    public boolean registerPatient(Patient patient) {
        if (remote) {
            try {
                return JsonUtil.fromJson(ApiClient.post("/patients", patient), com.google.gson.JsonObject.class)
                        .get("success").getAsBoolean();
            } catch (Exception ignored) { return false; }
        }
        return patientDAO.create(patient);
    }

    public List<Patient> getAllPatients() {
        if (remote) {
            try { return JsonUtil.fromJsonList(ApiClient.get("/patients"), Patient.class); }
            catch (Exception ignored) { return java.util.Collections.emptyList(); }
        }
        return patientDAO.findAll();
    }
}