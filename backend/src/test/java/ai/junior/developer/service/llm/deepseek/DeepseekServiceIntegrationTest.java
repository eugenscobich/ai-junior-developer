package ai.junior.developer.service.llm.deepseek;

import static org.assertj.core.api.Assertions.assertThat;

import ai.junior.developer.service.llm.LlmService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Disabled
@SpringBootTest
@TestPropertySource(properties = {"service.llm.type=deepseek"})
class DeepseekServiceIntegrationTest {

    @Autowired
    private LlmService llmService;

    @Test
    void testExecuteLlmPrompt() {
        String threadId = llmService.startAThread();
        String prompt = """
            Issue key: JKNIG-1
            Task title: Create a project structure for backend java application
            Task:
            <task>
            Context: We are developing a backend application for a books store.
            
            This task is one small part of the implementation flow.
            
            What need to be done:
             - Create a skeleton of the project using java 21, maven 3.8.6 and spring boot 3.4.4.
             - The project must have a service class where list of dummy books will be returned.
             - The project must have an rest endpoint to retrieve list of books.
            
            Technical details:
             - Repo url is git@github.com-eugenscobich:eugenscobich/ai-demo-project.git
             - Maven group id: com.eugenscobich
             - Maven artifact id: ai-demo-project
            
            Java base package: com.eugenscobich.ai.demo.project
            </task>
            """;
        String response = llmService.executeLlmPrompt(prompt, threadId);
        assertThat(response).isNotNull();

    }
}

