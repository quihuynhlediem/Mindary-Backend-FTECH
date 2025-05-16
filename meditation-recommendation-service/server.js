import express from 'express';
import config from './src/config/config.js';
import { connectDB } from './src/database/connection.js';
import router from './src/routes/meditationRoutes.js';
import swaggerDocs from "./src/swaggerConfig.js";
import cors from "cors";

const app = express();
app.use(express.json());
app.use('/api/v1', router);
app.use(
    cors({
      credentials: true,
    })
);

connectDB();

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(config.PORT, () => {
  console.log(`Server started at http://localhost:${config.PORT}`);
  swaggerDocs(app, config.PORT)
});

