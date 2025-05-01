import Meditation from '../database/models/Meditation.js';
import { prompt, llm } from '../config/llmModelConfig.js';
import { vectorStore } from '../database/connection.js';
import { Document } from "@langchain/core/documents";
import { buildSearchPrompt } from '../utils/prompt.js';
import Embedding from '../database/models/EmbeddingModel.js';
import mongoose from 'mongoose';
import { instance, urlInstance } from '../utils/axiosInstance.js';

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
        const url = await urlInstance.get('/generate', {
            params: {
                type: 'SINGLE_TRACK',
                id_slug: response.data.slug,
            }
        });
        const trackData = {
            id: response.data.id,
            slug: response.data.slug,
            tags: response.data.tags,
            title: response.data.title,
            author: response.data.publisher.name,
            description: response.data.long_description,
            transcripts: response.data.transcripts.transcript,
            reviews_summary: response.data.ai_user_reviews_summary.message,
            picture_url: `https://libraryitems.insighttimer.com/${response.data.id}/pictures/tiny_rectangle_xlarge.jpeg`,
            widget_url: url.data.iframe.src,

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
                pageContent: meditation.review_summary,
                metadata: {
                    // id: meditation.id,
                    slug: meditation.slug,
                    tags: meditation.tags,
                    title: meditation.title,
                    author: meditation.author,
                    description: meditation.description,
                    transcripts: meditation.transcripts,
                    picture_url: meditation.picture_url,
                    widget_url: meditation.widget_url,
                },
                // id: meditation.id,
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
        const resource = await vectorStore.addDocuments(storeMeditation(meditations), {
            ids: meditations.map(m => m.id)
        });
        console.log('Resource:', resource);
        return resource;
    } catch (error) {
        console.error("Error creating meditation:", error);
        return { success: false, message: "An error occurred while creating the meditation. Please try again later." };
    }
};

// const createMultipleMeditations = async (meditationsData) => {
//     //return await Meditation.insertMany(meditationsData);
//     const meditationsWithIds = meditationsData.map(
//         meditation => ({
//         ...meditation,
//         meditationId: uuidv4() 
//     }));

//     const documents = meditationsData.map(
//         (meditation, index) => ({
//         pageContent: meditation.content, 
//         metadata: { 
//             title: meditation.title,
//         }
//     }));

//     await vectorStore.addDocuments(
//         documents,
//         { 
//             ids: meditationsWithIds.map(m => m.meditationId) 
//         }
//     );
// };

// const createMultipleMeditations = async (meditationsData) => {
//     try {
//         // Generate a unique ID for each meditation
//         const meditationsWithIds = meditationsData.map(meditation => ({
//             ...meditation,
//             meditationId: uuidv4()
//         }));
//
//         // Prepare documents for vectorStore insertion
//         const documents = meditationsData.map(meditation => ({
//             pageContent: meditation.content,
//             metadata: { title: meditation.title }
//         }));
//
//         // Add the documents to vectorStore using the generated IDs
//         await vectorStore.addDocuments(
//             documents,
//             { ids: meditationsWithIds.map(m => m.meditationId) }
//         );
//
//         return {
//             success: true,
//             message: "Multiple meditations have been created successfully.",
//             ids: meditationsWithIds.map(m => m.meditationId)
//         };
//     } catch (error) {
//         console.error("Error creating multiple meditations:", error);
//         return {
//             success: false,
//             message: "An error occurred while creating multiple meditations. Please try again later."
//         };
//     }
// };



const createMultipleMeditations = async (meditationsData) => {
    try {
        // First create all meditation documents in MongoDB
        const createdMeditations = await Meditation.create(meditationsData);

        // Process all meditations in parallel to get use cases
        const processingResults = await Promise.all(createdMeditations.map(async (meditation) => {
            const messages = await prompt.invoke({
                question: 'Given the following meditation content in the context section. Describe when someone should use this meditation based on their emotional state.',
                context: meditation.content,
            });

            const useCase = await llm.invoke(messages);

            return {
                document: {
                    pageContent: useCase.content,
                    metadata: { _meditationId: meditation._id }
                },
                ids: [meditation._id]
            };
        }));

        // Extract documents and IDs for vectorStore
        const documents = processingResults.map(result => result.document);
        const ids = processingResults.map(result => result.ids);

        // Add all documents to vectorStore using meditation IDs
        await vectorStore.addDocuments(documents, { ids });

        return {
            success: true,
            message: "Multiple meditations have been created successfully.",
            meditations: createdMeditations
        };
    } catch (error) {
        console.error("Error creating multiple meditations:", error);
        return {
            success: false,
            message: "An error occurred while creating multiple meditations. Please try again later."
        };
    }
}

const getAllMeditations = async () => {
    try {
        const meditations = await Meditation.find();
        return {
            success: true,
            message: "All meditations retrieved successfully.",
            data: meditations
        };
    } catch (error) {
        console.error("Error retrieving meditations:", error);
        return {
            success: false,
            message: "An error occurred while retrieving meditations.",
            data: null
        };
    }
};

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

const getRecommendedMeditation = async (diaryAnalysis) => {
    try {
        const prompt = buildSearchPrompt({ diaryAnalysis });

        const retrievedMeditations = await vectorStore.similaritySearch(prompt, 10);
        if (!retrievedMeditations || retrievedMeditations.length === 0) {
            throw new Error("No meditations found matching the criteria.");
        }
        console.log("Retrieved meditations:", retrievedMeditations);

        const meditationsContent = retrievedMeditations
            .map(doc => doc.pageContent);
        if (!meditationsContent) {
            throw new Error("Retrieved meditations contain no content.");
        }
        console.log("Meditations content:", meditationsContent);

        const messages = await prompt.invoke({
            question: prompt,
            context: meditationsContent,
        });

        const answer = await llm.invoke(messages);
        console.log("LLM answer:", answer.content);

        return answer.toJSON().kwargs.content;
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

const deleteMeditation = async (meditationId) => {
    return await vectorStore.delete({ ids: [meditationId] });
};


export default {
    fetchAllTrackIds,
    fetchTrackDataWithId,
    fetchTrackData,
    createMeditation,
    createMultipleMeditations,
    getAllMeditations,
    getMeditationById,
    updateMeditation,
    deleteMeditation,
    getRecommendedMeditation
}