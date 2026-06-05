package rw.gov.wasac.ubsystem.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Authentication",
            "User Management",
            "Customer Management",
            "Meter Management",
            "Meter Reading Management",
            "Tariff Management",
            "Bill Management",
            "Payment Management",
            "Notifications",
            "Penalty Management"
    );

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Utility Billing System API")
                        .description("""
                                WASAC/REG Utility Billing System — Backend API.

                                Start here: **Authentication** → login → OTP verify → Authorize with JWT.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("WASAC/REG").email("kpntare@gmail.com")))
                .tags(List.of(
                        new Tag().name("Authentication")
                                .description("Start here — login (sends OTP), verify OTP, register, profile, password reset"),
                        new Tag().name("User Management").description("Admin user provisioning"),
                        new Tag().name("Customer Management").description("Customer profiles"),
                        new Tag().name("Meter Management").description("Utility meters"),
                        new Tag().name("Meter Reading Management").description("Operator meter readings"),
                        new Tag().name("Tariff Management").description("Tariffs, VAT, penalties"),
                        new Tag().name("Bill Management").description("Bill generation and approval"),
                        new Tag().name("Payment Management").description("Payments and approval"),
                        new Tag().name("Notifications").description("Customer notification messages"),
                        new Tag().name("Penalty Management").description("Late payment penalties")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            openApi.getTags().sort(Comparator.comparingInt(tag -> {
                int index = TAG_ORDER.indexOf(tag.getName());
                return index >= 0 ? index : Integer.MAX_VALUE;
            }));
        };
    }
}
