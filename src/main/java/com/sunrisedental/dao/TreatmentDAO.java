package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;
import java.util.List;

public interface TreatmentDAO {
    List<Treatment> findAll();
    Treatment findById(int id);
    boolean create(Treatment treatment);
    boolean update(Treatment treatment);
    boolean delete(int id);
}