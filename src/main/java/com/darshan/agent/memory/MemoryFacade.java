package com.darshan.agent.memory;

import com.darshan.agent.cognition.MetaThought;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeType;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Unified facade for all memory operations.
 * Provides a single entry point for storing and retrieving memories.
 */
@Component
public class MemoryFacade {
    
    private final EpisodicMemoryEngine episodicMemory;
    private final EpisodicRecallEngine episodicRecall;
    private final SemanticMemoryEngine semanticMemory;
    private final VectorMemoryStore vectorMemory;
    private final MemoryStore persistentStore;
    private final UserProfile userProfile;
    
    public MemoryFacade(
            EpisodicMemoryEngine episodicMemory,
            EpisodicRecallEngine episodicRecall,
            SemanticMemoryEngine semanticMemory,
            VectorMemoryStore vectorMemory,
            MemoryStore persistentStore,
            UserProfile userProfile) {
        this.episodicMemory = episodicMemory;
        this.episodicRecall = episodicRecall;
        this.semanticMemory = semanticMemory;
        this.vectorMemory = vectorMemory;
        this.persistentStore = persistentStore;
        this.userProfile = userProfile;
    }
    
    // ==================== EPISODIC MEMORY ====================
    
    /**
     * Store an episodic memory (conversation experience).
     */
    public void storeEpisode(String input, String response, MetaThought meta) {
        episodicMemory.remember(input, response, meta);
    }
    
    /**
     * Store a raw episode.
     */
    public void storeEpisode(Episode episode) {
        episodicMemory.store(episode);
    }
    
    /**
     * Recall relevant episodic memories.
     */
    public String recallEpisodic(String query) {
        return episodicRecall.recallRelevant(query);
    }
    
    /**
     * Get all episodes.
     */
    public List<Episode> getAllEpisodes() {
        return episodicMemory.all();
    }
    
    // ==================== SEMANTIC MEMORY ====================
    
    /**
     * Learn a new concept with its meaning.
     */
    public void learnConcept(String concept, String meaning) {
        semanticMemory.learn(concept, meaning);
    }
    
    /**
     * Check if a concept is known.
     */
    public boolean knowsConcept(String concept) {
        return semanticMemory.knows(concept);
    }
    
    /**
     * Recall semantic knowledge about a keyword.
     */
    public String recallSemantic(String keyword) {
        return semanticMemory.recall(keyword);
    }
    
    /**
     * Get top concepts by frequency.
     */
    public List<com.darshan.agent.memory.semantic.Concept> getTopConcepts(int limit) {
        return semanticMemory.topConcepts(limit);
    }
    
    // ==================== VECTOR MEMORY ====================
    
    /**
     * Store a vector embedding.
     */
    public void storeVector(String text, double[] embedding) {
        vectorMemory.store(text, embedding);
    }
    
    /**
     * Search for similar vectors.
     */
    public List<String> searchVectors(double[] query, int topK) {
        return vectorMemory.search(query, topK);
    }
    
    /**
     * Get all stored vectors.
     */
    public List<MemoryVector> getAllVectors() {
        return vectorMemory.all();
    }
    
    // ==================== PERSISTENT STORE ====================
    
    /**
     * Save conversation entry to persistent storage.
     */
    public void saveConversationEntry(com.darshan.agent.memory.ConversationEntry entry) {
        persistentStore.addConversation(entry);
    }
    
    /**
     * Load persistent memory file.
     */
    public MemoryFile loadPersistentMemory() {
        return persistentStore.load();
    }
    
    /**
     * Save persistent memory file.
     */
    public void savePersistentMemory(MemoryFile memory) {
        persistentStore.save(memory);
    }
    
    // ==================== USER PROFILE ====================
    
    /**
     * Set user's name.
     */
    public void setUserName(String name) {
        userProfile.setName(name);
    }
    
    /**
     * Get user's name.
     */
    public String getUserName() {
        return userProfile.getName();
    }
    
    /**
     * Check if user profile has a name.
     */
    public boolean hasUserName() {
        return userProfile.getName() != null && !userProfile.getName().isBlank();
    }
    
    // ==================== COMBINED RECALL ====================
    
    /**
     * Recall all relevant memories for a query.
     * Combines episodic and semantic memories.
     */
    public String recallAll(String query) {
        StringBuilder result = new StringBuilder();
        
        // Episodic memories
        String episodic = recallEpisodic(query);
        if (!episodic.isEmpty()) {
            result.append("Past experiences:\n").append(episodic).append("\n\n");
        }
        
        // Semantic knowledge
        String semantic = recallSemantic(query);
        if (!semantic.isEmpty()) {
            result.append("Knowledge:\n").append(semantic).append("\n\n");
        }
        
        return result.toString().trim();
    }
    
    /**
     * Check if any memory contains information about a topic.
     */
    public boolean hasMemoryAbout(String topic) {
        // Check episodic
        List<Episode> episodes = getAllEpisodes();
        for (Episode episode : episodes) {
            if (episode.getUserInput().toLowerCase().contains(topic.toLowerCase()) ||
                episode.getAgentResponse().toLowerCase().contains(topic.toLowerCase())) {
                return true;
            }
        }
        
        // Check semantic
        return knowsConcept(topic);
    }
}