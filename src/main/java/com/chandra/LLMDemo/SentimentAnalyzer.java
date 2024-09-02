package com.chandra.LLMDemo;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SentimentAnalyzer {
    @UserMessage("Analyse sentiment of this {{text}}")
    Sentiment analyseSentiment(@V("text") String text);
}