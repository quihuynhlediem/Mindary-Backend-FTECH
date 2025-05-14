import mongoose from 'mongoose';
import config from '../config/config.js';
import { MongoDBAtlasVectorSearch } from "@langchain/mongodb";
import { embeddings } from '../config/llmModelConfig.js';
import { MongoClient } from "mongodb";

const connectDB = async () => {
    if (!config.MONGODB_URI) {
        console.error("MONGODB_URI is undefined. Check your .env file.");
        process.exit(1);
    }
    await mongoose
        .connect(config.MONGODB_URI, { dbName: "test" })
        .then(() => {
            console.log("MongoDB is connected");
        })
        .catch((error) => console.log(`${error}. SERVER IS NOT CONNECTING`));
    ;
};

const client = new MongoClient(config.MONGODB_URI || "");

const collection = client
    .db("test")
    .collection("meditations");

const vectorStore = new MongoDBAtlasVectorSearch(embeddings, {
    collection: collection,
    indexName: "default",
    textKey: "reviews_summary",
    embeddingKey: "embedding",
});

export { connectDB, vectorStore };
