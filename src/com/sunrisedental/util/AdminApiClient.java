package com.sunrisedental.util;

import com.google.gson.JsonObject;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class AdminApiClient {
    private AdminApiClient() { }

    public static List<User> users() {
        try { return JsonUtil.fromJsonList(ApiClient.get("/users"), User.class); }
        catch (Exception ignored) { return Collections.emptyList(); }
    }

    public static boolean createUser(User user) {
        try { return JsonUtil.fromJson(ApiClient.post("/users", user), JsonObject.class).get("success").getAsBoolean(); }
        catch (Exception ignored) { return false; }
    }

    public static boolean updateUserStatus(int id, boolean active, int actingUserId) {
        try { return JsonUtil.fromJson(ApiClient.post("/users", Map.of("id", id, "active", active, "actingUserId", actingUserId)), JsonObject.class).get("success").getAsBoolean(); }
        catch (Exception ignored) { return false; }
    }

    public static List<Treatment> treatments() {
        try { return JsonUtil.fromJsonList(ApiClient.get("/treatments"), Treatment.class); }
        catch (Exception ignored) { return Collections.emptyList(); }
    }

    public static boolean createTreatment(Treatment treatment) {
        try { return JsonUtil.fromJson(ApiClient.post("/treatments", treatment), JsonObject.class).get("success").getAsBoolean(); }
        catch (Exception ignored) { return false; }
    }
}