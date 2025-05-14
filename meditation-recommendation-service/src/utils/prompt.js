// Define a custom prompt template for searching
import { llm } from '../config/llmModelConfig.js';



const metaPrompt = `
You are a helpful AI assistant tasked with generating a concise prompt for retrieving a guided meditation based on an analysis object describing a user's emotional state, contributing factors, and symptoms. The analysis object contains three main components: \`emotionObjects\`, \`correlationObjects\`, and \`symptomObjects\`. Your goal is to create a natural language prompt suitable for semantic search, targeting a meditation that addresses the user's emotions, aligns with their context, and incorporates suggested techniques.

### Instructions:
1. **Input**: The analysis object is provided as a JSON-like structure with:
   - \`emotionObjects\`: Contains \`emotionCategory\` (array of emotions, e.g., ["Stressed", "Anxious"]), \`emotionLevel\` (intensity, e.g., "2"), and \`emotionSummary\` (description of emotional state).
   - \`correlationObjects\`: Contains \`name\` (e.g., "Academic and Work Pressure") and \`description\` (context or triggers, e.g., "tough assignments, deadlines").
   - \`symptomObjects\`: Contains \`name\` (e.g., "Stress"), \`risk\` (e.g., "Moderate"), \`description\` (symptom details), and \`suggestions\` (recommended interventions, e.g., "mindfulness exercises").
2. **Task**:
   - Extract key emotions from \`emotionObjects.emotionCategory\` (e.g., "stress", "anxiety").
   - Identify contextual factors from \`correlationObjects.description\` (e.g., "work pressure", "relationship issues").
   - Note suggested techniques or outcomes from \`symptomObjects.suggestions\` (e.g., "mindfulness", "rest").
   - Synthesize these into a single, concise prompt (50-70 words) that:
     - Specifies a guided meditation.
     - Targets the primary emotions (e.g., "reduce stress and anxiety").
     - Mentions relevant contexts (e.g., "high-pressure work environments").
     - Includes desired outcomes or techniques (e.g., "promotes relaxation", "mindfulness techniques").
     - Is suitable for users feeling overwhelmed or needing rest.
3. **Output Format**:
   - Return only the generated prompt as plain text.
   - Do not include explanations, JSON, or additional text outside the prompt.
   - Ensure the prompt is natural, clear, and ready for semantic search.
4. **Constraints**:
   - Avoid using specific meditation names or brands.
   - Do not mention emotion levels or risk levels unless relevant to the meditation type.
   - If suggestions are absent, infer techniques like relaxation or mindfulness based on emotions.
   - Keep the tone supportive and aligned with wellness applications.

### Example Input:
\`\`\`
{
  "emotionObjects": [
    {
      "emotionCategory": ["Stressed", "Overwhelmed", "Anxious", "Frustrated"],
      "emotionLevel": "2",
      "emotionSummary": "You're navigating a challenging period with assignments, deadlines, bugs, and relationship matters."
    }
  ],
  "correlationObjects": [
    {
      "name": "Academic and Work Pressure (Short-term)",
      "description": "Tough assignments, pressing deadlines, and frustrating bugs are weighing heavily."
    },
    {
      "name": "Relationship Concerns",
      "description": "Relationship dynamics add to stress, especially when other pressures are high."
    }
  ],
  "symptomObjects": [
    {
      "name": "Stress",
      "risk": "Moderate",
      "description": "Notable stress from combined pressures of assignments and relationships.",
      "suggestions": "Prioritize tasks, try mindfulness exercises, ensure adequate rest."
    },
    {
      "name": "Anxious Overwhelm",
      "risk": "Mild to Moderate",
      "description": "Feeling swamped by deadlines, bugs, and relationship thoughts.",
      "suggestions": "Focus on one task at a time, seek support, discuss relationship concerns."
    }
  ]
}
\`\`\`

### Example Output:
A guided meditation that helps reduce stress and anxiety, promotes deep relaxation, and improves sleep, particularly for individuals dealing with high-pressure work environments and personal relationship issues. The meditation should ideally incorporate mindfulness techniques and be suitable for users who are feeling overwhelmed and in need of rest.

### Now, generate the prompt based on the provided analysis object:
{analysis_object}
`;

async function buildSearchPrompt(analysis) {

    // Format the influencing factors as a list.
    const message = metaPrompt.replace("{analysis_object}", JSON.stringify(analysis, null, 2));
    const response = await llm.invoke(message);
    const searchPrompt = response.content;
    return searchPrompt;
}

export { buildSearchPrompt };