package com.chandra.LLMDemo;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Summarizer {

    @UserMessage("Give a summary of {{name}} in 3 bullet points using the following information:\n\n {{info}}")
    String summarize(@V("name") String name, @V("info") String info);

    //

    @UserMessage("""
            Give a person summary in the following format:<br><br>
             Name: ...<br><br>
             Date of Birth: ...<br><br>
             Profession: ...<br><br>
             Highest Rating: ...<br><br>
             About: ...<br><br>
            
            Use the following information:
            {{info}}
            """)
    String summarizeInFormat(@V("info") String info);

    //

    @UserMessage("""
            Summarize the the following information in JSON format with no other prefix or suffix:
            {{info}}
            """)
    String summarizeAsJSON(@V("info") String info);

}


