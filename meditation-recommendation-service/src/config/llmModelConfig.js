import config from './config.js';
import { ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings } from '@langchain/google-genai';
import { TaskType } from '@google/generative-ai';
import { HarmBlockThreshold, HarmCategory } from "@google/generative-ai";
import { ChatPromptTemplate } from "@langchain/core/prompts";

const prompt = ChatPromptTemplate.fromMessages([
	[
		"human",
		"You are an assistant for question-answering tasks. Use the following pieces of retrieved context to answer the question. If you don't know the answer, just say that you don't know. Use three sentences maximum and keep the answer concise. Question: {question} Context: {context} Answer:"
	],
]);

const llm = new ChatGoogleGenerativeAI({
	modelName: "gemini-2.5-pro-preview-05-06",
	temperature: 0.5,
	apiKey: process.env.GEMINI_API_KEY,
	safetySettings: [
		{
			category: HarmCategory.HARM_CATEGORY_HARASSMENT,
			threshold: HarmBlockThreshold.BLOCK_LOW_AND_ABOVE,
		},
	],
	generationConfig: {
		maxOutputTokens: 100,
	},
});

const embeddings = new GoogleGenerativeAIEmbeddings({
	modelName: "gemini-embedding-exp-03-07",
	apiKey: config.GEMINI_API_KEY,
	taskType: TaskType.RETRIEVAL_DOCUMENT,  // TaskType.SEMANTIC_SIMILARITY??
});

export { llm, embeddings, prompt };
