import mongoose, { Schema } from 'mongoose';

const meditationSchema = new mongoose.Schema({
    meditation_id: { type: String, required: true },
    slug: { type: String, required: true, trim: true },
    tags: { type: [String], required: true },
    title: { type: String, required: true, trim: true },
    author: { type: String, required: true, trim: true },
    media_length: { type: Number, required: true },
    description: { type: String, required: true, trim: true },
    intention: { type: String, required: true, trim: true },
    transcripts: { type: String, required: true, trim: true },
    reviews_summary: { type: String, required: true, trim: true },  
    picture_url: { type: String, required: true, trim: true },
    widget_url: { type: String, required: true, trim: true },
    embedding: { type: [Number] },             
}, {
    timestamps: true,
});

const Meditation = mongoose.model('Meditation', meditationSchema);
export default Meditation;
