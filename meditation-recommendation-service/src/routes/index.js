import express from 'express';
import meditationRoutes from './meditationRoutes.js';

const router = express.Router();

router.use('/', meditationRoutes);

export default router;