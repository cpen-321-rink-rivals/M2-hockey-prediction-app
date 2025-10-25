import { Server as HttpServer } from 'http';
import { Server as SocketIOServer, Socket } from 'socket.io';

interface AuthenticatedSocket extends Socket {
  userId?: string;
  userEmail?: string;
}

class SocketService {
  private io: SocketIOServer;
  private connectedUsers: Map<string, AuthenticatedSocket> = new Map();

  constructor(server: HttpServer) {
    this.io = new SocketIOServer(server, {
      cors: {
        origin: '*', // Configure this properly for production
        methods: ['GET', 'POST'],
        credentials: false,
      },
      // Allow both polling and websocket
      transports: ['websocket', 'polling'],
      // Path must match client
      path: '/socket.io/',
      // Connection settings
      pingTimeout: 60000,
      pingInterval: 25000,
    });

    this.setupSocketHandlers();
    console.log('🔌 Socket.IO initialized with CORS: *');
    console.log('📡 Available transports: websocket, polling');
  }

  private setupSocketHandlers() {
    this.io.on('connection', (socket: AuthenticatedSocket) => {
      console.log(`🔗 Socket connected: ${socket.id}`);

      // Handle user authentication/identification
      socket.on(
        'authenticate',
        (data: { userId: string; userEmail?: string }) => {
          socket.userId = data.userId;
          socket.userEmail = data.userEmail;
          this.connectedUsers.set(data.userId, socket);

          console.log(
            `👤 User authenticated: ${data.userId} (${data.userEmail || 'no email'})`
          );

          // Join user to their personal room for targeted updates
          socket.join(`user:${data.userId}`);

          // Send confirmation
          socket.emit('authenticated', { userId: data.userId });
        }
      );

      // Handle joining challenge rooms
      socket.on('join_challenge', (challengeId: string) => {
        socket.join(`challenge:${challengeId}`);
        console.log(
          `🏒 User ${socket.userId} joined challenge room: ${challengeId}`
        );
      });

      // Handle leaving challenge rooms
      socket.on('leave_challenge', (challengeId: string) => {
        socket.leave(`challenge:${challengeId}`);
        console.log(
          `🚪 User ${socket.userId} left challenge room: ${challengeId}`
        );
      });

      // Handle disconnect
      socket.on('disconnect', () => {
        if (socket.userId) {
          this.connectedUsers.delete(socket.userId);
          console.log(`👋 User ${socket.userId} disconnected`);
        }
        console.log(`🔌 Socket disconnected: ${socket.id}`);
      });

      // Handle errors
      socket.on('error', error => {
        console.error('Socket error:', error);
      });
    });
  }

  // Broadcast to all connected clients
  public broadcastToAll(event: string, data: any) {
    this.io.emit(event, data);
  }

  // Send to specific user
  public sendToUser(userId: string, event: string, data: any) {
    this.io.to(`user:${userId}`).emit(event, data);
  }

  // Broadcast to challenge participants
  public broadcastToChallenge(challengeId: string, event: string, data: any) {
    this.io.to(`challenge:${challengeId}`).emit(event, data);
  }

  // Send to multiple users
  public sendToUsers(userIds: string[], event: string, data: any) {
    userIds.forEach(userId => {
      this.sendToUser(userId, event, data);
    });
  }

  // Get connected users count
  public getConnectedUsersCount(): number {
    return this.connectedUsers.size;
  }

  // Check if user is online
  public isUserOnline(userId: string): boolean {
    return this.connectedUsers.has(userId);
  }

  // Get socket instance for advanced usage
  public getIO(): SocketIOServer {
    return this.io;
  }
}

export default SocketService;
