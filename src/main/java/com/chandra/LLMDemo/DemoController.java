package com.chandra.LLMDemo;

import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@RestController
public class DemoController {

    private final ChatLanguageModel chatLanguageModel;

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private ConversationalChain chain;

    private final ResourceLoader resourceLoader;

    public DemoController(ChatLanguageModel chatLanguageModel, EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel, ResourceLoader resourceLoader) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public String demo() {
        return "Welcome to Build your First LLM Project Demo";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "question") String question) {
        return chatLanguageModel.generate(question);
    }

    @GetMapping("/chat/context")
    public String chatWithMemory(@RequestParam(value = "question") String question) {
        if (chain == null) {
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
            chain = ConversationalChain.builder()
                    .chatLanguageModel(chatLanguageModel)
                    .chatMemory(chatMemory)
                    .build();

        }
        return chain.execute(question);
    }

    @GetMapping("/assistant")
    String assistant(@RequestParam(value = "question") String question) {
        SystemMessage systemMessage = SystemMessage.from("You are a Cooking assistant, Only provide answer for cooking related questions");
        return  chatLanguageModel.generate(systemMessage, UserMessage.from(question)).content().text();
    }

    @GetMapping("/prompt_template")
    String promptTemplate(@RequestParam(value = "language") String language) {
        PromptTemplate promptTemplate = PromptTemplate.from("Say 'hello' in {{it}}.");
        Prompt prompt = promptTemplate.apply(language);
        return chatLanguageModel.generate(prompt.text());
    }

    @GetMapping("/translator")
    String translator(@RequestParam(value = "text") String text, @RequestParam(value = "language") String language) {
        Translator translator = AiServices.create(Translator.class, chatLanguageModel);
        return translator.translate(text, language);
    }

    @GetMapping("/sentiment")
    String sentiment(@RequestParam(value = "text") String text) {
        SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, chatLanguageModel);
        return sentimentAnalyzer.analyseSentiment(text).name();
    }


    @GetMapping("/summarizer")
    String summarizer() {

        String aboutKasparov = """
                Garry Kimovich Kasparov[a] (born Garik Kimovich Weinstein[b] on 13 April 1963) is a Russian chess grandmaster, former World Chess Champion (1985–2000), political activist and writer. 
                His peak FIDE chess rating of 2851,[2] achieved in 1999, was the highest recorded until being surpassed by Magnus Carlsen in 2013. 
                From 1984 until his retirement from regular competitive chess in 2005, Kasparov was ranked world no. 1 for a record 255 months overall. 
                Kasparov also holds records for the most consecutive professional tournament victories (15) and Chess Oscars.     
                Kasparov became the youngest-ever undisputed world champion in 1985 at age 22 by defeating then-champion Anatoly Karpov.
                He defended the title against Karpov three times, in 1986, 1987 and 1990. 
                Kasparov held the official FIDE world title until 1993, when a dispute with FIDE led him to set up a rival organisation, the Professional Chess Association.[4] In 1997, 
                he became the first world champion to lose a match to a computer under standard time controls when he was defeated by the IBM supercomputer Deep Blue in a highly publicised match. He continued to hold the "Classical" 
                world title until his defeat by Vladimir Kramnik in 2000. Despite losing the PCA title, he continued winning tournaments and was the world's highest-rated player at the time of his official retirement. Kasparov coached Carlsen in 2009–10, 
                during which time Carlsen rose to world no. 1. Kasparov stood unsuccessfully for FIDE president in 2013–2014.
            """;

        Summarizer summarizer = AiServices.create(Summarizer.class, chatLanguageModel);
//        return summarizer.summarizeInFormat(aboutKasparov);
        return summarizer.summarizeAsJSON(summarizer.summarizeInFormat(aboutKasparov));

    }

    @GetMapping("/summarizer/object")
    String summarizerObject() {

        String aboutCapablanca = """
                Capablanca was born in 1888 in the Castillo del Príncipe, Havana.[1] 
                He beat Cuban champion Juan Corzo in a match on 17 November 1901, two days before his 13th birthday.
                 His victory over Frank Marshall in a 1909 match earned him an invitation to the 1911 San Sebastián tournament, 
                 which he won ahead of players such as Akiba Rubinstein, Aron Nimzowitsch and Siegbert Tarrasch. 
                 Over the next several years, Capablanca had a strong series of tournament results. 
                 After several unsuccessful attempts to arrange a match with then world champion Emanuel Lasker, 
                 Capablanca finally won the world chess champion title from Lasker in 1921. 
                 Capablanca was undefeated from February 10, 1916 to March 21, 1924, a period that included the world championship match 
                 with Lasker.
            """;

        SummarizerObject summarizer = AiServices.create(SummarizerObject.class, chatLanguageModel);
        PersonSummaryPrompt prompt = new PersonSummaryPrompt("Capablanca", aboutCapablanca);
        return summarizer.summarizeInObject(prompt);

    }


    @GetMapping("/rag/pdf/load")
    String rag() throws URISyntaxException, IOException {
        Resource resource = resourceLoader.getResource("classpath:tech_radar_vol_30_en.pdf");
        Path documentPath = Paths.get(resource.getURI());

        Document document = loadDocument(documentPath, new ApacheTikaDocumentParser());
//        List<Document> documents = loadDocuments(documentPath, new ApacheTikaDocumentParser());

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentTransformer(new HtmlTextExtractor())
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        return "success";
    }


    @GetMapping("/rag/pdf/assistant")
    String ragPdfAssistant(@RequestParam(value = "question") String question) {
        RAGAssistant ragAssistant = AiServices.builder(RAGAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 3))
                .build();

        return ragAssistant.query(question);
    }

    @GetMapping("/rag/url/load")
    String ragWithURL() {
        Document document = UrlDocumentLoader.load("https://www.nbcnews.com/", new TextDocumentParser());

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentTransformer(new HtmlTextExtractor())
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        return "success";
    }


    @GetMapping("/rag/url/assistant")
    String ragAssistant(@RequestParam(value = "question") String question) {
        RAGAssistant ragAssistant = AiServices.builder(RAGAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel, 3))
                .build();

        return ragAssistant.query(question);
    }

}






















