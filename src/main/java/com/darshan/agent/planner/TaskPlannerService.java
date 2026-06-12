package com.darshan.agent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TaskPlannerService {

    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String URL = "https://api.openai.com/v1/chat/completions";

    public List<TaskItem> planTasks(String goal) throws Exception {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", PlannerRules.PLANNER_PROMPT),
                        Map.of("role", "user", "content", goal)
                )
        );

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body),
                        MediaType.parse("application/json")
                ))
                .build();

        Response response = client.newCall(request).execute();
        String json = (String)((Map)((Map)((List)mapper
                .readValue(response.body().string(), Map.class)
                .get("choices")).get(0)).get("message")).get("content");

        return mapper.readValue(
                json,
                mapper.getTypeFactory()
                        .constructCollectionType(List.class, TaskItem.class)
        );
    }
}
