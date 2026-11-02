package com.swcms;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowEndToEndTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void citizenWorkerAndAdminCompleteComplaintAndFeedbackWorkflow() throws Exception {
        String suffix=UUID.randomUUID().toString().substring(0,8);
        String citizenEmail="citizen-"+suffix+"@example.com";
        String workerEmail="worker-"+suffix+"@example.com";
        String password="WaterSafe123!";
        String adminToken=login("admin@swcms.com","Admin@12345");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Citizen\",\"email\":\""+citizenEmail+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk());
        String citizenToken=login(citizenEmail,password);

        mvc.perform(post("/api/auth/worker/register").with(bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Worker\",\"email\":\""+workerEmail+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk());
        String workerToken=login(workerEmail,password);
        mvc.perform(get("/api/admin/workers").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].active").value(true));

        MockMultipartFile complaintPhoto=image("photo","citizen.jpg");
        String complaintJson=mvc.perform(multipart("/api/user/complaints").file(complaintPhoto)
                .param("customerName","Test Citizen").param("address","River Road")
                .param("complaintType","Water Leakage").param("priority","High")
                .param("description","Leak beside the main valve").with(bearer(citizenToken)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int complaintId=mapper.readTree(complaintJson).get("complaintId").asInt();

        mvc.perform(put("/api/admin/complaints/{id}/status",complaintId).param("status","Resolved").with(bearer(adminToken)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/user/complaints").with(bearer(citizenToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].complaintId").value(complaintId));
        mvc.perform(get("/api/admin/complaints/page").param("page","0").param("size","1").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].complaintId").value(complaintId));
        mvc.perform(put("/api/admin/complaints/{id}/assign",complaintId).param("workerEmail",workerEmail).with(bearer(adminToken)))
                .andExpect(status().isOk());
        mvc.perform(multipart("/api/worker/complaints/{id}/status",complaintId).param("status","In Progress")
                .with(bearer(workerToken)).with(putMultipart()))
                .andExpect(status().isOk());
        mvc.perform(multipart("/api/worker/complaints/{id}/status",complaintId).file(image("photo","repair.jpg"))
                .param("status","Resolved").param("remarks","Valve repaired")
                .with(bearer(workerToken)).with(putMultipart()))
                .andExpect(status().isOk());

        String workerFeedbackJson=mvc.perform(multipart("/api/worker/complaints/{id}/feedback",complaintId)
                .file(image("photo","worker-proof.jpg")).param("rating","5").param("comment","Repair completed")
                .with(bearer(workerToken)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int workerFeedbackId=mapper.readTree(workerFeedbackJson).get("feedbackId").asInt();
        mvc.perform(multipart("/api/worker/complaints/{id}/feedback",complaintId)
                .param("rating","4").param("comment","Second attempt").with(bearer(workerToken)))
                .andExpect(status().isBadRequest());

        String citizenFeedbackJson=mvc.perform(multipart("/api/user/complaints/{id}/feedback",complaintId)
                .file(image("photo","citizen-feedback.jpg")).param("rating","4").param("comment","Thanks for fixing it")
                .with(bearer(citizenToken)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int citizenFeedbackId=mapper.readTree(citizenFeedbackJson).get("feedbackId").asInt();
        mvc.perform(multipart("/api/user/complaints/{id}/feedback",complaintId)
                .param("rating","5").param("comment","Duplicate").with(bearer(citizenToken)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/user/complaints/{id}/worker-feedback",complaintId).with(bearer(citizenToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.feedbackId").value(workerFeedbackId));
        mvc.perform(get("/api/worker/feedback").with(bearer(workerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].feedbackId").value(citizenFeedbackId));
        mvc.perform(get("/api/admin/feedback").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].feedbackId").value(citizenFeedbackId));
        mvc.perform(get("/api/admin/worker-feedback").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].feedbackId").value(workerFeedbackId));
        mvc.perform(get("/api/feedback/{id}/photo",citizenFeedbackId).with(bearer(workerToken)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
        mvc.perform(get("/api/worker-feedback/{id}/photo",workerFeedbackId).with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
        mvc.perform(get("/api/user/complaints/{id}/worker-feedback/photo",complaintId).with(bearer(citizenToken)))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG));
        mvc.perform(get("/api/admin/worker-feedback").with(bearer(citizenToken)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/user/notifications").with(bearer(citizenToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].newStatus").value("Resolved"));
        mvc.perform(get("/api/worker/stats").with(bearer(workerToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resolved").value(1));

        String oldCitizenToken=citizenToken;
        String citizenProfile=mvc.perform(put("/api/user/profile").with(bearer(citizenToken))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Citizen\",\"email\":\"citizen-new-"+suffix+"@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.profile.name").value("Updated Citizen"))
                .andReturn().getResponse().getContentAsString();
        citizenToken=mapper.readTree(citizenProfile).get("token").asText();
        mvc.perform(get("/api/user/profile").with(bearer(citizenToken))).andExpect(status().isOk());
        mvc.perform(put("/api/user/password").with(bearer(citizenToken)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\""+password+"\",\"newPassword\":\"CitizenNew123!\"}"))
                .andExpect(status().isOk());
        assertNotNull(login("citizen-new-"+suffix+"@example.com","CitizenNew123!"));
        mvc.perform(get("/api/user/profile").with(bearer(oldCitizenToken))).andExpect(status().isUnauthorized());

        String workerProfile=mvc.perform(put("/api/worker/profile").with(bearer(workerToken))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Worker\",\"email\":\""+workerEmail+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.profile.name").value("Updated Worker"))
                .andReturn().getResponse().getContentAsString();
        workerToken=mapper.readTree(workerProfile).get("token").asText();
        mvc.perform(put("/api/worker/password").with(bearer(workerToken)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\""+password+"\",\"newPassword\":\"WorkerNew123!\"}"))
                .andExpect(status().isOk());

        String overdueJson=mvc.perform(multipart("/api/user/complaints").param("customerName","Test Citizen")
                .param("address","Old Town").param("complaintType","No Water Supply").param("priority","Low")
                .param("description","Long-running supply issue").with(bearer(citizenToken)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int overdueId=mapper.readTree(overdueJson).get("complaintId").asInt();
        jdbc.update("UPDATE complaints SET created_at = ? WHERE complaint_id = ?",LocalDateTime.now().minusDays(8),overdueId);
        mvc.perform(get("/api/admin/complaints/overdue").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].complaintId").value(overdueId));
        mvc.perform(post("/api/admin/complaints/escalate-overdue").with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.escalated").value(1));
        mvc.perform(get("/api/admin/complaints/{id}",overdueId).with(bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.priority").value("High"));

        mvc.perform(put("/api/admin/workers/{id}",mapper.readTree(mvc.perform(get("/api/admin/workers").with(bearer(adminToken)))
                .andReturn().getResponse().getContentAsString()).get(0).get("userId").asInt()).with(bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Worker\",\"email\":\""+workerEmail+"\",\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/api/worker/stats").with(bearer(workerToken))).andExpect(status().isUnauthorized());
    }

    private String login(String email,String password) throws Exception {
        String body=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\""+email+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    private RequestPostProcessor bearer(String token) {
        return request -> { request.addHeader("Authorization","Bearer "+token); return request; };
    }

    private RequestPostProcessor putMultipart() {
        return request -> { request.setMethod("PUT"); return request; };
    }

    private MockMultipartFile image(String field,String filename) {
        byte[] jpeg={(byte)0xff,(byte)0xd8,(byte)0xff,0,0,0,0,0,0,0,0,0};
        return new MockMultipartFile(field,filename,"image/jpeg",jpeg);
    }
}
