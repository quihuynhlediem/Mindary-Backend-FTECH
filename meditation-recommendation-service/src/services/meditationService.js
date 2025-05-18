import Meditation from "../database/models/Meditation.js";
import { prompt, llm, embeddings } from "../config/llmModelConfig.js";
import { vectorStore } from "../database/connection.js";
import { Document } from "@langchain/core/documents";
import { buildSearchPrompt } from "../utils/prompt.js";
import { instance, urlInstance } from "../utils/axiosInstance.js";
import Analysis from "../database/models/Analysis.js";

// GET
const getAllMeditations = async () => {
	const data = await Meditation.find()
		.select("-embedding -intention -reviews_summary")
		.lean();
	return data;
};

const getMeditationById = async (meditationId) => {
	const meditation = await Meditation.findById(meditationId);
	return meditation;
};

const loadData = async (page, limit) => {
	const offset = (page - 1) * limit;
	const data = await Meditation.find()
		.select("-meditation_id -embedding -intention -reviews_summary")
		// .sort({ createdAt: -1 })
		.skip(offset)
		.limit(limit)
		.lean();
	return data;
};

const getRecommendations = async (userId, diaryId) => {
	const analysis = await getDiaryAnalysis(userId, diaryId);

	const question = await buildSearchPrompt(analysis);

	// const embedding = await embeddings.embedQuery(question);
	// const searchResultWithScore = await vectorStore.similaritySearchVectorWithScore(embedding, 3);
	// const result = searchResultWithScore.map(([meditation, score]) => {
	//     return {
	//         title: meditation.metadata.title,
	//         author: meditation.metadata.author,
	//         media_url: meditation.metadata.media_url,
	//         media_length: meditation.metadata.media_length,
	//         picture_url: meditation.metadata.picture_url,
	//         intention: meditation.metadata.intention,
	//     }
	// })
	const searchResult = await vectorStore.similaritySearch(question, 3);
	const result = searchResult.map((meditation) => {
		return {
			title: meditation.metadata.title,
			author: meditation.metadata.author,
			media_url: meditation.metadata.media_url,
			media_length: meditation.metadata.media_length,
			picture_url: meditation.metadata.picture_url,
			intention: meditation.metadata.intention,
		};
	});

	return result;
};

// CREATE
const createMeditation = async (body) => {
	const meditations = await fetchData(body);
	const resource = await vectorStore.addDocuments(
		createDocumentFromResource(meditations),
		{
			ids: meditations.map((m) => m.meditation_id),
		}
	);
	//console.log('Resource:', resource);
	return resource;
};

// DELETE
const deleteMeditation = async (id) => {
	return await vectorStore.delete({ ids: [id] });
};

// FUNCTION
const fetchAllTrackIds = async (body) => {
	const response = await instance.get("/single-tracks/filter", {
		params: {
			content_langs: "en",
			content_types: "guided,talks",
			device_lang: "en",
			offset: body.offset || 0, // default offset
			size: body.size || 3, // default size
			sort_option: "most_played",
		},
	});
	const trackIds = response.data.map(
		(track) => track.item_summary.library_item_summary.id
	);
	return trackIds;
};

const fetchDataWithTrackId = async (id) => {
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
			reviews_summary: response.data.ai_user_reviews_summary.message.replace(
				/\*\*(.*?)\*\*/g,
				"$1"
			),
			picture_url: `https://libraryitems.insighttimer.com/${response.data.id}/pictures/tiny_rectangle_xlarge.jpeg`,
		};
		return trackData;
	} catch (error) {
		console.error("Error fetching data:", error);
		throw new Error("Failed to fetch data from the API");
	}
};

const fetchData = async (body) => {
	try {
		const trackIds = await fetchAllTrackIds(body);
		const tracks = await Promise.all(
			trackIds.map((id) => fetchDataWithTrackId(id))
		);
		return tracks;
	} catch (error) {
		console.error("Error fetching track data:", error);
		throw new Error("Failed to fetch track data from the API");
	}
};

const createDocumentFromResource = (meditations) => {
	try {
		const result = meditations.map((meditation) => {
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
		});
		return result;
	} catch (error) {
		console.error("Error storing meditation:", error);
		throw new Error("Failed to store meditation in the database");
	}
};

const getDiaryAnalysis = async (userId, diaryId) => {
	try {
		// Parse the date and create a range for the whole day
		// const startDate = new Date(date);
		// startDate.setHours(0, 0, 0, 0); // Start of the day
		// const endDate = new Date(date);
		// endDate.setHours(23, 59, 59, 999); // End of the day

		console.log("Still work until here");
		const analysis = await Analysis.findOne(
			{
				userId: userId,
				diaryId: diaryId,
			},
			{
				emotionObjects: 1,
				symptomObjects: 1,
				correlationObjects: 1,
			}
		).lean(); // Add lean() for better performance
		// .maxTimeMS(30000); // Increase timeout to 30 seconds
		console.log(analysis);

		if (!analysis) {
			console.log(`No analysis found for user ${userId} on ${date}`);
			return null;
		}

		return analysis;
	} catch (error) {
		console.error(
			`Error fetching analysis for user ${userId} on ${date}:`,
			error
		);
		throw new Error("Failed to fetch diary analysis from database");
	}
};

export default {
	createMeditation,
	getAllMeditations,
	getRecommendations,
	loadData,
	getMeditationById,
	deleteMeditation,
};
