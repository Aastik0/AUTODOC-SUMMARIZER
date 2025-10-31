package com.autodoc.service;

import com.autodoc.db.GlossaryDao;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class NlpService {
    private final GlossaryDao glossaryDao;

    // This constructor correctly accepts the GlossaryDao object.
    public NlpService(GlossaryDao glossaryDao) {
        this.glossaryDao = glossaryDao;
    }

    /**
     * Creates a very basic summary of the text (first 3 sentences).
     * @param text The full text from the report.
     * @return A summarized string.
     */
    public String summarize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }
        // A simple summarization logic.
        String[] sentences = text.split("\\.");
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < sentences.length && i < 3; i++) {
            summary.append(sentences[i].trim()).append(". ");
        }
        return summary.toString().trim();
    }

    /**
     * Finds and defines medical terms from the glossary within the given text.
     * @param text The full text from the report.
     * @return A formatted string of terms and their definitions.
     */
    public String defineTerms(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder definitions = new StringBuilder();
        Set<String> foundTerms = new HashSet<>(); // To avoid duplicate definitions
        
        // Clean the text to easily find words
        String cleanedText = text.toLowerCase().replaceAll("[^a-zA-Z ]", "");
        StringTokenizer tokenizer = new StringTokenizer(cleanedText);

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            // Check if we haven't already defined this word
            if (!foundTerms.contains(word)) {
                // This call will now work correctly with the corrected GlossaryDao
                String definition = glossaryDao.findDefinitionByTerm(word);
                if (definition != null) {
                    definitions.append(word.toUpperCase())
                               .append(":\n")
                               .append(definition)
                               .append("\n\n");
                    foundTerms.add(word); // Mark this term as found
                }
            }
        }
        return definitions.toString().trim();
    }
}

