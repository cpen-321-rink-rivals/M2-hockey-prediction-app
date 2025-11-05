import {
  describe,
  expect,
  test,
  beforeAll,
  afterAll,
  afterEach,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { FriendRequest } from '../../../src/models/friends.model';
import { connectDB } from '../../../src/database';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface GET /api/friends/pending (Integration)
describe('Unmocked GET /api/friends/pending', () => {
  let authToken: string;
  let testUserId: string;
  let sender1Id: string;
  let sender2Id: string;
  let acceptedSenderId: string;
  let otherUserId: string;

  beforeAll(async () => {
    await connectDB();

    // Create test user (receiver)
    const testUser = await userModel.create({
      email: 'receiver-pending@test.com',
      name: 'Test Receiver',
      googleId: 'google-receiver-pending',
    });
    testUserId = testUser._id.toString();

    // Create sender 1
    const sender1 = await userModel.create({
      email: 'sender1-pending@test.com',
      name: 'Sender One',
      googleId: 'google-sender1-pending',
    });
    sender1Id = sender1._id.toString();

    // Create sender 2
    const sender2 = await userModel.create({
      email: 'sender2-pending@test.com',
      name: 'Sender Two',
      googleId: 'google-sender2-pending',
    });
    sender2Id = sender2._id.toString();

    // Create an accepted request (should not appear in pending)
    const acceptedSender = await userModel.create({
      email: 'accepted-sender-pending@test.com',
      name: 'Accepted Sender',
      googleId: 'google-accepted-sender-pending',
    });
    acceptedSenderId = acceptedSender._id.toString();

    // Create a request where test user is sender (should not appear)
    const otherUser = await userModel.create({
      email: 'other-user-pending@test.com',
      name: 'Other User',
      googleId: 'google-other-user-pending',
    });
    otherUserId = otherUser._id.toString();

    // Generate auth token
    authToken = jwt.sign({ id: testUserId }, process.env.JWT_SECRET!, {
      expiresIn: '1h',
    });
  });

  beforeEach(async () => {
    // Recreate friend requests before each test
    await FriendRequest.create({
      sender: sender1Id,
      receiver: testUserId,
      status: 'pending',
    });

    await FriendRequest.create({
      sender: sender2Id,
      receiver: testUserId,
      status: 'pending',
    });

    await FriendRequest.create({
      sender: acceptedSenderId,
      receiver: testUserId,
      status: 'accepted',
    });

    await FriendRequest.create({
      sender: testUserId,
      receiver: otherUserId,
      status: 'pending',
    });
  });

  afterEach(async () => {
    // Clean up friend requests after each test
    await FriendRequest.deleteMany({});
  });

  afterAll(async () => {
    // Cleanup all users
    await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    await userModel.delete(new mongoose.Types.ObjectId(sender1Id));
    await userModel.delete(new mongoose.Types.ObjectId(sender2Id));
    await userModel.delete(new mongoose.Types.ObjectId(acceptedSenderId));
    await userModel.delete(new mongoose.Types.ObjectId(otherUserId));
    await mongoose.connection.close();
  });

  // Integration test: Successfully get pending requests
  // Input: Authenticated user
  // Expected behavior: Returns list of pending friend requests where user is receiver
  // Expected output: 200 status, array of 2 pending requests
  test('Successfully retrieves pending friend requests from database', async () => {
    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Pending requests fetched successfully'
    );
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(2);

    // Verify both senders are in the list
    const senderIds = response.body.data.map((req: any) => req.sender._id);
    expect(senderIds).toContain(sender1Id);
    expect(senderIds).toContain(sender2Id);

    // Verify all are pending and user is receiver
    response.body.data.forEach((req: any) => {
      expect(req.status).toBe('pending');
      expect(req.receiver).toBe(testUserId);
    });
  });

  // Integration test: Pending requests have populated sender data
  // Input: Authenticated user
  // Expected behavior: Returns requests with populated sender name and email
  // Expected output: Request objects with sender details
  test('Returns pending requests with populated sender information', async () => {
    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${authToken}`);

    expect(response.status).toBe(200);
    expect(response.body.data).toBeInstanceOf(Array);

    // Check that sender is populated
    response.body.data.forEach((request: any) => {
      expect(request.sender).toHaveProperty('name');
      expect(request.sender).toHaveProperty('email');
      expect(typeof request.sender.name).toBe('string');
      expect(typeof request.sender.email).toBe('string');
    });
  });

  // Integration test: Get pending requests without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app).get('/api/friends/pending');

    expect(response.status).toBe(401);
  });

  // Integration test: User with no pending requests
  // Input: New user with no pending requests
  // Expected behavior: Returns empty array
  // Expected output: 200 status, empty array
  test('Returns empty array when user has no pending requests', async () => {
    // Create new user with no pending requests
    const newUser = await userModel.create({
      email: 'nopending@test.com',
      name: 'No Pending User',
      googleId: 'google-nopending',
    });

    const noPendingToken = jwt.sign(
      { id: newUser._id.toString() },
      process.env.JWT_SECRET!,
      { expiresIn: '1h' }
    );

    const response = await request(app)
      .get('/api/friends/pending')
      .set('Authorization', `Bearer ${noPendingToken}`);

    expect(response.status).toBe(200);
    expect(response.body.data).toBeInstanceOf(Array);
    expect(response.body.data).toHaveLength(0);

    // Cleanup
    await userModel.delete(newUser._id);
  });
});
