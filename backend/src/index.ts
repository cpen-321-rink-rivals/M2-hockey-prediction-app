import dotenv from 'dotenv';
import express from 'express';
import { createServer } from 'http';

import { connectDB } from './database';
import { errorHandler, notFoundHandler } from './errorHandler.middleware';
import router from './routes/routes';
import SocketService from './socket.service';
import path from 'path';

dotenv.config();

const app = express();
const server = createServer(app);
const PORT = process.env.PORT ?? 3000;

// Initialize Socket.IO service
const socketService = new SocketService(server);

// Make socket service available globally for other modules
declare global {
  var socketService: SocketService;
}
global.socketService = socketService;

app.use(express.json());

app.use('/api', router);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
app.use('*', notFoundHandler);
app.use(errorHandler);

connectDB();
server.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`🔗 WebSocket server ready`);
});
