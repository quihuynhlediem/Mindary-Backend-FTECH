import mongoose, { Document, Model, Schema } from "mongoose";

export interface IDiaryAnalysisResult extends Document {
    userId: string;
    diaryId: string;
    emotionObjects: {
        emotionLevel: string;
        emotionCategory: string;
        emotionSummary: string;
    }[];
    correlationObjects: {
        name: string;
        description: string;
    }[];
    symptomObjects: {
        name: string;
        risk: string;
        description: string;
        suggestions: string;
    }[];
}

const diarySchema: Schema<IDiaryAnalysisResult> = new Schema(
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

const DiaryAnalysisResult: Model<IDiaryAnalysisResult> =
    mongoose.models.diaries || mongoose.model<IDiaryAnalysisResult>("diaries", diarySchema);

export default DiaryAnalysisResult;