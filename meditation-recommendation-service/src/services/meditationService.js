import Meditation from '../database/models/Meditation.js';
import { prompt, llm } from '../config/llmModelConfig.js';
import { vectorStore } from '../database/connection.js';
import { Document } from "@langchain/core/documents";
import { buildSearchPrompt } from '../utils/prompt.js';
import { instance, urlInstance } from '../utils/axiosInstance.js';
import { ObjectId } from 'mongodb';

/**
* Method to fetch all track IDs from the API.
* @returns List of track IDs.
*/
const fetchAllTrackIds = async (body) => {
    try {
        const response = await instance.get('/single-tracks/filter', {
            params: {
                content_langs: 'en',
                content_types: 'guided,talks',
                device_lang: 'en',
                offset: body.offset || 0, // default offset
                size: body.size || 3, // default size
                sort_option: 'most_played',
            }
        });
        const trackIds = response.data.map(track => track.item_summary.library_item_summary.id);

        return trackIds;
    } catch (error) {
        console.error('Error fetching data:', error);
        throw new Error('Failed to fetch data from the API');
    }
}

// Fetch track data using the track ID
const fetchTrackDataWithId = async (id) => {
    try {
        const response = await instance.get(`/single-tracks/${id}`);

        const trackData = {
            meditation_id: response.data.id,
            tags: response.data.tags,
            title: response.data.title,
            author: response.data.publisher.name,
            media_url: response.data.media_paths[1],
            media_length: response.data.media_length,
            description: response.data.long_description,
            intention: response.data.ai_intention_summary.summary,
            transcripts: response.data.transcripts.transcript,
            reviews_summary: response.data.ai_user_reviews_summary.message.replace(/\*\*(.*?)\*\*/g, '$1'),
            picture_url: `https://libraryitems.insighttimer.com/${response.data.id}/pictures/tiny_rectangle_xlarge.jpeg`,

        }
        return trackData;
    } catch (error) {
        console.error('Error fetching data:', error);
        throw new Error('Failed to fetch data from the API');
    }
}

// Fetch all track data using the track IDs
const fetchTrackData = async (body) => {
    try {
        const trackIds = await fetchAllTrackIds(body);
        const tracks = await Promise.all(trackIds.map(id => fetchTrackDataWithId(id)));
        return tracks;
    } catch (error) {
        console.error('Error fetching track data:', error);
        throw new Error('Failed to fetch track data from the API');
    }
}

const storeMeditation = (meditations) => {
    try {
        const result = meditations.map(meditation => {
            return new Document({
                pageContent: meditation.reviews_summary,
                metadata: {
                    meditation_id: meditation.meditation_id,
                    tags: meditation.tags,
                    title: meditation.title,
                    author: meditation.author,
                    media_url: meditation.media_url,
                    media_length: meditation.media_length,
                    description: meditation.description,
                    intention: meditation.intention,
                    transcripts: meditation.transcripts,
                    picture_url: meditation.picture_url,
                },
            });
        })
        return result;
    } catch (error) {
        console.error('Error storing meditation:', error);
        throw new Error('Failed to store meditation in the database');
    }
};

const createMeditation = async (body) => {
    try {
        // Create a new meditation
        const meditations = await fetchTrackData(body);
        const resource = await vectorStore.addDocuments(storeMeditation(meditations),
            {
                ids: meditations.map(m => m.meditation_id)
            }
        );
        //console.log('Resource:', resource);
        return resource;
    } catch (error) {
        console.error("Error creating meditation:", error);
        return { success: false, message: "An error occurred while creating the meditation. Please try again later." };
    }
};


const getAllMeditations = async () => {
    try {
        const meditations = await Meditation.find().select('-embedding -intention -reviews_summary').lean();
        return meditations;
    } catch (error) {
        console.error("Error retrieving meditations:", error);
        return null;
    }
};

const getMeditationOnScroll = async (page, limit) => {
    try {
        const offset = (page - 1) * limit;
        const data = await Meditation
            .find()
            .select('-meditation_id -embedding -intention -reviews_summary')
            // .sort({ createdAt: -1 })
            .skip(offset)
            .limit(limit)
            .lean();

        // const totalCount = data.length;
        // console.log("Total count:", totalCount);
        return data;
    } catch (error) {
        console.error("Error retrieving meditations:", error);
        return { error };
    }
}

const getMeditationById = async (meditationId) => {
    try {

        const meditation = await Meditation.findById(meditationId);
        return meditation;
    } catch (error) {
        console.error("Error retrieving meditation by ID:", error);
        return {
            success: false,
            message: "An error occurred while retrieving the meditation.",
            data: null
        };
    }
};

const getRecommendedMeditation = async (analysis) => {
    try {
        //const prompt = buildSearchPrompt({ diaryAnalysis });
        const analysisObject = analysis;

        const retrievedMeditations = await vectorStore.similaritySearch("How to have a good sleep ?", 3);

        const result = retrievedMeditations.map(meditation => {
            return {
                title: meditation.metadata.title,
                intention: meditation.metadata.intention,
            }
        })
        //console.log("Recommended meditation:", result);
        return result;

    } catch (error) {
        console.error("Error retrieving meditation:", error);
        return { success: false, message: `Failed to retrieve meditation.` };
    }
};

const updateMeditation = async (meditationId, newTitle, newContent) => {
    try {
        const doc = new Document({
            pageContent: newContent,
            metadata: { title: newTitle },
        });
        await vectorStore.addDocuments([doc], { ids: [meditationId] });

        return { success: true, message: "Meditation updated successfully!" };
    } catch (error) {
        console.error("Error updating meditation:", error);
        return { success: false, message: "Failed to update meditation." };
    }
};

const deleteMeditation = async (id) => {
    return await vectorStore.delete({ ids: [id] });
};


export default {
    // fetchAllTrackIds,
    fetchTrackDataWithId,
    // fetchTrackData,
    createMeditation,
    getAllMeditations,
    getMeditationOnScroll,
    getMeditationById,
    updateMeditation,
    deleteMeditation,
    getRecommendedMeditation
}