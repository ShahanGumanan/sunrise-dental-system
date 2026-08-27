package com.sunrisedental.dao;

import com.sunrisedental.model.Dentist;
import java.util.List;

public interface DentistDAO {
    List<Dentist> findAll();
    Dentist findById(int id);
}