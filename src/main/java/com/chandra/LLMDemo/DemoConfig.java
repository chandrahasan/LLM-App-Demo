package com.chandra.LLMDemo;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@Configuration
public class DemoConfig {

    private static final String COLLECTION_NAME = "demo-index";

    @Bean
    ChatLanguageModel gemma2ChatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("gemma2:latest")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    ChatLanguageModel llama3ChatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("llama3.1:latest")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

     /*
    ChatLanguageModel azureOpenAIChatLanguageModel() {
        return AzureOpenAiChatModel.builder()
                .endpoint(azureOpenAiEndpoint)
                .tokenCredential(new DefaultAzureCredentialBuilder().build())
                .deploymentName("gpt-4o")
                .logRequestsAndResponses(true)
                .build();
    }
    */


    // EmbeddingModel
    @Bean
    EmbeddingModel ollamaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("nomic-embed-text")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    //EmbeddingStore
    @Bean
    EmbeddingStore<TextSegment> embeddingStore() throws ExecutionException, InterruptedException {
        QdrantClient client =
                new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build());

        if (!client.listCollectionsAsync().get().contains(COLLECTION_NAME)) {
            client.createCollectionAsync(COLLECTION_NAME,
                            Collections.VectorParams.newBuilder()
                                    .setDistance(Collections.Distance.Cosine)
                                    .setSize(768)
                                    .build())
                    .get();
        }

        return QdrantEmbeddingStore.builder()
                .collectionName(COLLECTION_NAME)
                .host("localhost")
                .port(6334)
                .build();
    }


}
