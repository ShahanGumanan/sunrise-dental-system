package com.sunrisedental.util;

import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import java.util.Collections;
import java.util.List;

public final class ReferenceDataClient {
    private ReferenceDataClient() { }

    public static List<Patient> patients() {
        try { return JsonUtil.fromJsonList(ApiClient.get("/reference/patients"), Patient.class); }
        catch (Exception ignored) { return Collections.emptyList(); }
    }

    public static List<Dentist> dentists() {
        try { return JsonUtil.fromJsonList(ApiClient.get("/reference/dentists"), Dentist.class); }
        catch (Exception ignored) { return Collections.emptyList(); }
    }

    public static List<Treatment> treatments() {
        try { return JsonUtil.fromJsonList(ApiClient.get("/reference/treatments"), Treatment.class); }
        catch (Exception ignored) { return Collections.emptyList(); }
    }
}