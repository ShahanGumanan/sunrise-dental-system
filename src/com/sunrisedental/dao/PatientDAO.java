package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;
import java.util.List;

public interface PatientDAO {
    boolean create(Patient patient);
    Patient findById(int id);
    List<Patient> findAll();
    List<Patient> searchByNameOrContact(String keyword);
}