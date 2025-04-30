// utils/axiosInstance.js
import axios from 'axios';

// Create an Axios instance with default configuration
const instance = axios.create({
  baseURL: 'https://filtering.insighttimer-api.net/api/v1',  
  timeout: 5000, 
  headers: {
    'Content-Type': 'application/json',  
  },
});

export default instance;
