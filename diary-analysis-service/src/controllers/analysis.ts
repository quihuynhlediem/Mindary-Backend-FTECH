import { Request, Response } from 'express';
import { analyzeDiaryEntry, getDiaryAnalysis, deleteDiaryAnalysis, getEmotionLevelFromAnalysis, getEmotionLevelByPeriod } from "../services/analysis";

export const getAnalysisResult = async (req: Request, res: Response): Promise<void> => {
    try {
        const userId = req.params.userId as string;
        const diaryId = req.params.diaryId as string;
        // if (!date) {
        //     res.status(400).json({ message: "Date is in the future" });
        //     return;
        // }
        const result = await getDiaryAnalysis(userId, diaryId);
        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }

}

export const getEmotionLevelResultByPeriod = async (req: Request, res: Response): Promise<void> => {
    try {
        const userId = req.query.userId as string;
        const filter = req.query.filter as string;
        if (!filter) {
            res.status(400).json({ message: "Filter is required" });
            return;
        }
        const result = await getEmotionLevelByPeriod(filter, userId);
        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }

}

export const diaryAnalysisResult = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userId, diaryId, content } = req.body;
        if (!userId || !content || !diaryId) {
            res.status(400).json({ message: "Missing required fields: userId, diary content, or diaryId." });
            return;
        }

        const result = await analyzeDiaryEntry(userId, diaryId, content, req.file);

        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }
};

export const deleteAnalysisResult = async (req: Request, res: Response): Promise<void> => {
    try {
        const { diaryId } = req.params;

        if (!diaryId) {
            res.status(400).json({ message: "Missing diaryId." });
            return;
        }

        await deleteDiaryAnalysis(diaryId);

        res.status(200).json({ message: "Analysis result deleted successfully." });
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }
};

export const  getEmotionLevel = async (req: Request, res: Response): Promise<void> => {
    try {
        const userId = req.params.userId as string;
        const date = req.params.date as string;
        if (!date) {
            res.status(400).json({ message: "Date is in the future" });
            return;
        }
        const result = await getEmotionLevelFromAnalysis(userId, date);
        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }
};