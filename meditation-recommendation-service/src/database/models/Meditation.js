import { text } from 'express';
import mongoose from 'mongoose';

const meditationSchema = new mongoose.Schema(
    {
        _id: {
            type: String,
            required: true,
            unique: true,
        },
        slug: { 
            type: String, 
            required: true, 
            trim: true 
        },
        tags: { 
            type: String, 
            required: true, 
            trim: true 
        },
        title: { 
            type: String, 
            required: true, 
            trim: true 
        },
        author: { 
            type: String, 
            required: true, 
            trim: true 
        },
        description: { 
            type: String, 
            required: true, 
            trim: true 
        },
        transcripts: { 
            type: String, 
            required: true, 
            trim: true 
        },
        review_summary: { 
            type: String, 
            required: true, 
            trim: true 
        },
        picture_url:{
            type: String, 
            required: true, 
            trim: true 
        },
        widget_url:{
            type: String, 
            required: true, 
            trim: true 
        },
        embedding: { 
            type: [Number], 
            required: true 
        },
    }, { 
        timestamps: true, 
    }
);

const Meditation = mongoose.model('Meditation', meditationSchema);
export default Meditation;
