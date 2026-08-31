package com.litego.config;

import com.litego.constants.SystemConstants;
import com.litego.tools.SearchNearbyPlacesFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {
    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }



    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.SYSTEM_PROMPT_CHAT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient dateChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.SYSTEM_PROMPT_DATE)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }



    @Bean
    public ChatClient recipeChatClient(OpenAiChatModel model, ChatMemory chatMemory
            /*@Qualifier("recipeVectorStore") VectorStore recipeVectorStore*/) {

        /*QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(recipeVectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.1)
                        .topK(5)
                        .build())
                .build();*/

        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.SYSTEM_PROMPT_RECIPE)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public ChatClient mapClient(OpenAiChatModel chatModel,
                                 SearchNearbyPlacesFunction searchNearbyPlacesFunction) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SystemConstants.SYSTEM_PROMPT_MAP)
                .defaultTools(searchNearbyPlacesFunction)
                .build();
    }



}
