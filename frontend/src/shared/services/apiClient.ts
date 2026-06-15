import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/agent',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 120000, // 120s - must be > backend Ollama timeout
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === 'ECONNABORTED') {
      console.error('[API] Request timeout');
    } else if (error.response) {
      console.error(`[API] ${error.response.status}: ${error.response.data?.message || error.response.statusText}`);
    } else if (error.request) {
      console.error('[API] No response received - backend may be offline');
    }
    return Promise.reject(error);
  }
);

export default apiClient;