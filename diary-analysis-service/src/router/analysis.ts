import { upload } from "../lib/multerSetup";
import { diaryAnalysisResult, getAnalysisResult, deleteAnalysisResult, getEmotionLevel, getEmotionLevelResultByPeriod } from "../controllers/analysis";
import express, { Router } from "express";

export default (router: express.Router) => {
  router.get("/diary/emotionlevel/:userId/:date", getEmotionLevel);
  router.get("/diary/emotionlevel/piechart", getEmotionLevelResultByPeriod);
  router.get("/diary/:userId/:date", getAnalysisResult);
  router.post("/diary/analyze", upload.single('image'), diaryAnalysisResult);
  router.delete("/diary/delete/:diaryId", deleteAnalysisResult);
  return router;
};