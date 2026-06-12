package com.darshan.agent.approval;

import com.darshan.agent.memory.MemoryFile;
import com.darshan.agent.memory.MemoryStore;
import com.darshan.agent.memory.ConversationEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalService {

    private final MemoryStore memoryStore;

    public ApprovalService(MemoryStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    public String handleApproval(boolean approved) {

        MemoryFile memory = memoryStore.load();

        List<ConversationEntry> history =
                memory.getHistory();

        if (!history.isEmpty()) {

            ConversationEntry last =
                    history.get(history.size() - 1);

            last.setStatus(
                    approved ? "APPROVED" : "REJECTED"
            );

            memoryStore.save(memory);
        }

        return approved
                ? "Action approved by user."
                : "Action rejected by user.";
    }
}
