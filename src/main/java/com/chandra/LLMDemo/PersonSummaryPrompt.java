package com.chandra.LLMDemo;

import dev.langchain4j.model.input.structured.StructuredPrompt;

@StructuredPrompt("Provide content only in text summary of following information: {{info}} ")
public record PersonSummaryPrompt(String name, String info) {
}