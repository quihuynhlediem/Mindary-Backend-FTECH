import express from 'express';
import MeditationController from '../controllers/meditationController.js';

const router = express.Router();

/**
 * @openapi
 * /meditation/create:
 *   post:
 *     summary: Create a new meditation
 *     description: Creates a new meditation record.
 *     tags:
 *       - Meditations
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               title:
 *                 type: string
 *                 description: Title of the meditation.
 *               content:
 *                 type: string
 *                 description: Content of the meditation.
 *             example:
 *               title: "Mindfulness Meditation"
 *               content: "Focus on your breath..."
 *     responses:
 *       201:
 *         description: Meditation created successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object # Define your Meditation object schema in a real app
 *               example:
 *                 id: "uuid-example"
 *                 title: "Mindfulness Meditation"
 *                 content: "Focus on your breath..."
 *       400:
 *         description: Bad request - Input validation failed or other client error.
 *       500:
 *         description: Internal server error.
 */


router.get('/meditations/get-all-meditation', MeditationController.getAllMeditations);
router.get('/meditations/load-data', MeditationController.loadData);
router.get('/meditations/:id', MeditationController.getMeditationById);
router.post("/meditations/get-recommendations", MeditationController.getRecommendations);
router.post('/meditations/create', MeditationController.createMeditation);
// router.post('/meditation/recommended', MeditationController.getRecommendedMeditation);
router.delete('/meditations/delete/:id', MeditationController.deleteMeditation);

//router.use('/', router);

export default router;