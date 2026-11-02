
package com.swcms.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final AdminSetupService adminSetupService;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminDataInitializer(
            AdminSetupService adminSetupService,
            @Value("${app.admin.name:SWCMS Administrator}") String adminName,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.adminSetupService = adminSetupService;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {

        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("Bootstrap administrator was not created because ADMIN_EMAIL or ADMIN_PASSWORD is not configured");
            return;
        }
        String result=adminSetupService.createAdmin(adminName,adminEmail,adminPassword);
        log.info("{}",result);
    }
}
