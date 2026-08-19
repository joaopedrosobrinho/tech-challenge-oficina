package br.com.fiap.oficina.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI oficinaOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                .title("API da Oficina Mecânica")
                .description("API REST do Tech Challenge FIAP para gestão de clientes, "
                        + "veículos, serviços, peças, estoque e ordens de serviço.")
                .version("1.0.0")
                .contact(new Contact().name("Tech Challenge FIAP"))
                .license(new License().name("Uso acadêmico")));
    }

    @Bean
    public OperationCustomizer respostasHttpPadrao() {
        return (operation, handlerMethod) -> {
            operation.getResponses()
                    .addApiResponse("400", resposta("Requisição inválida"))
                    .addApiResponse("404", resposta("Recurso não encontrado"))
                    .addApiResponse("409", resposta("Conflito com regra de negócio ou integridade"))
                    .addApiResponse("422", resposta("Regra de domínio não atendida"))
                    .addApiResponse("500", resposta("Erro interno inesperado"));
            return operation;
        };
    }

    private ApiResponse resposta(String descricao) {
        return new ApiResponse().description(descricao);
    }
}
