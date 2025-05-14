import mongoose, { Schema } from "mongoose";

const diaryAnalysisSchema = new Schema(
    {
        userId: { type: String },
        diaryId: { type: String },
        emotionObjects: [
            {
                emotionLevel: { type: String },
                emotionCategory: [{ type: String }],
                emotionSummary: { type: String },
            }
        ],
        correlationObjects: [
            {
                name: { type: String },
                description: { type: String },
            }
        ],
        symptomObjects: [
            {
                name: { type: String },
                risk: { type: String },
                description: { type: String },
                suggestions: { type: String },
            }
        ],
    },
    { timestamps: true }
);

const Analysis = mongoose.model("analyses", diaryAnalysisSchema);

export default Analysis;