package com.darshan.agent.brain;

import com.darshan.agent.autonomy.AutonomousLoop;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.cognition.*;
import com.darshan.agent.cognition.learning.LearningMemory;
import com.darshan.agent.cognition.learning.SelfLearningEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.MemoryEmbedder;
import com.darshan.agent.memory.VectorMemory;
import com.darshan.agent.memory.VectorMemoryStore;
import com.darshan.agent.planner.*;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Component;

@Component
public class CognitiveLoop {

    private final PerceptionEngine perception;
    private final ReasoningEngine reasoning;
    private final DecisionEngine decision;
    private final PlannerBrain planner;
    private final PlanExecutor executor;
    private final SkillRouter router;
    private final ReflectionEngine reflection;
    private final MetaCognitionEngine meta;
    private final ResponseComposer composer;
    private final SelfLearningEngine selfLearning;
    private final VectorMemory vectorMemory;
    private final VectorMemoryStore vectorStore;
    private final MemoryEmbedder embedder;
    private final AutonomousLoop autonomous;
    private final GoalManager goals;




    public CognitiveLoop(
            PerceptionEngine perception,
            ReasoningEngine reasoning,
            DecisionEngine decision,
            PlannerBrain planner,
            PlanExecutor executor,
            SkillRouter router,
            ReflectionEngine reflection,
            MetaCognitionEngine meta,
            ResponseComposer composer,
            SelfLearningEngine selfLearning,
            VectorMemory vectorMemory,
            VectorMemoryStore vectorStore,
            MemoryEmbedder embedder,
            AutonomousLoop autonomous,
            GoalManager goals


    ) {
        this.perception = perception;
        this.reasoning = reasoning;
        this.decision = decision;
        this.planner = planner;
        this.executor = executor;
        this.router = router;
        this.reflection = reflection;
        this.meta = meta;
        this.composer = composer;
        this.selfLearning = selfLearning;
        this.vectorMemory = vectorMemory;
        this.vectorStore = vectorStore;
        this.embedder = embedder;
        this.autonomous = autonomous;
        this.goals = goals;





    }

    public String run(String input,
                      ConversationContext context)
            throws Exception {

        // 🧠 Autonomous execution
        if (goals.hasGoal()) {
            String auto = autonomous.run(context);
            if (auto != null) {
                return auto;
            }
        }


        String response = null;

        for (int attempt = 0; attempt < 3; attempt++) {

            // =========================
            // 1️⃣ PERCEPTION
            // =========================
            String goal = perception.detectGoal(input);

            // =========================
            // 2️⃣ REASONING
            // =========================
            Thought thought = reasoning.think(input, goal);

            // =========================
            // 3️⃣ DECISION
            // =========================
            String action = decision.decide(thought);



            // =========================
            // 4️⃣ EXECUTION
            // =========================
            if ("CREATE_PLAN".equals(action)) {

                Plan plan = planner.createPlan(
                        thought.getIntent(),
                        input,
                        context
                );

                response = executor.execute(plan, context);

            } else {

                Skill skill =
                        router.route(thought.getIntent());

                response =
                        skill.execute(input, context);
            }

            // =========================
            // 5️⃣ REFLECTION (FIXED POSITION)
            // =========================
            ReflectionResult reflectionResult =
                    reflection.reflect(
                            input,
                            response,
                            thought
                    );

            System.out.println(
                    "🔍 Reflection: "
                            + reflectionResult.getFeedback()
            );

            // =========================
            // 6️⃣ META-COGNITION
            // =========================
            meta.observe(thought, reflectionResult);

            String strategy = meta.adjustStrategy();

            if ("CHANGE_STRATEGY".equals(strategy)) {

                System.out.println("🧠 Meta: Changing strategy");

                Skill fallback =
                        router.route("CHAT");

                return fallback.execute(input, context);
            }

            LearningMemory memory =
                    selfLearning.extract(input, response);

            if (memory != null) {
                vectorMemory.store(memory.getLesson());
            }



            // =========================
            // SUCCESS EXIT
            // =========================
            if (!reflectionResult.needsImprovement()) {

                // store conversational memory
                vectorStore.store(
                        input,
                        embedder.embed(input)
                );

                return composer.compose(response);
            }


            System.out.println("🧠 Re-thinking...");
        }

        if (response == null || response.isBlank()) {
            Skill chat = router.route("CHAT");
            return chat.execute(input, context);
        }





        return "Let me think more carefully about that.";
    }


}
