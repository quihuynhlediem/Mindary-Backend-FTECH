import { response } from 'express';
import MeditationService from '../services/meditationService.js';


const createMeditation = async (req, res) => {
    try {
        const response = await MeditationService.createMeditation(req.body);
        return res.status(201).json(response);
    } catch (error) {
        if (error.name === 'ValidationError') {
            return res.status(400).json({ message: error.message });
        }

        console.error('Error creating meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};


const getAllMeditations = async (req, res) => {
    try {
        const meditations = await MeditationService.getAllMeditations();
        return res.status(200).json(meditations);
    } catch (error) {
        console.error('Error fetching meditations:', error);
        return res.status(500).json({ message: 'An unexpected error occurred while fetching meditations.' });
    }
};

const loadData = async (req, res) => {
    try {
        const { page = 1, limit = 10} = req.query;
        if (typeof page !== 'number' || typeof limit !== 'number') {
            return res.status(400).json({ message: 'Offset and limit must be numbers.' });
        }

        const response = await MeditationService.getMeditationOnScroll(page, limit);

        if (!response) {
            return res.status(404).json({ message: 'No meditations found.' });
        }
        //console.log(response);
        return res.json(response);
    } catch (error) {
        console.error('Error fetching meditations on scroll:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
}

const getMeditationById = async (req, res) => {
    try {
        const meditation = await MeditationService.getMeditationById(req.params.id);
        if (!meditation) {
            return res.status(404).json({ message: 'Meditation not found' });
        }
        return res.status(200).json(meditation);
    } catch (error) {
        console.error('Error fetching meditation by ID:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const getRecommendations = async (req, res) => {
    try {
        const { userId, date } = req.query;
        if (typeof userId !== 'string' || typeof date !== 'string') {
            return res.status(400).json({ message: 'User ID and date must be strings.' });
        }

        const recommendations = await MeditationService.getRecommendations(userId, date);
        if (!recommendations) {
            return res.status(404).json({ message: 'Recommended meditation not found' });
        }

        return res.status(200).json(recommendations);
    } catch (error) {
        console.error('Error fetching recommended meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};


const deleteMeditation = async (req, res) => {
    try {
        await MeditationService.deleteMeditation(req.params.id);
        return res.status(204).send();
    } catch (error) {
        console.error('Error deleting meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};


export default {
    createMeditation,
    getAllMeditations,
    loadData,
    getMeditationById,
    deleteMeditation,
    getRecommendations
};
