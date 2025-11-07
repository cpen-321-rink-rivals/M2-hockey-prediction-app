import {
  describe,
  expect,
  test,
  beforeAll,
  afterAll,
  beforeEach,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import { connectDB } from '../../../src/config/database';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { FriendRequest } from '../../../src/models/friends.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface POST /api/friends/accept (Integration)
describe('Unmocked POST /api/friends/accept', () => {
  let authToken: string;
  let testUserId: string;
  let senderUserId: string;
  let pendingRequestId: string;

  beforeAll(async () => {
    await connectDB();

    // Create test receiver user (who will accept)
    const testUser = await userModel.create({
      email: 'receiver-accept@test.com',
      name: 'Test Receiver',
      googleId: 'google-receiver-accept',
    });
    testUserId = testUser._id.toString();

    // Create test sender user
    const senderUser = await userModel.create({
      email: 'sender-accept@test.com',
      name: 'Test Sender',
      googleId: 'google-sender-accept',
    });
    senderUserId = senderUser._id.toString();

    // Generate auth token
    authToken = jwt.sign({ id: testUserId }, process.env.JWT_SECRET!, {
      expiresIn: '1h',
    });
  });

  beforeEach(async () => {
    // Create a fresh pending request before each test
    const friendRequest = await FriendRequest.create({
      sender: senderUserId,
      receiver: testUserId,
      status: 'pending',
    });
    pendingRequestId = (
      friendRequest._id as mongoose.Types.ObjectId
    ).toString();
  });

  afterAll(async () => {
    // Cleanup
    await FriendRequest.deleteMany({});
    await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    await userModel.delete(new mongoose.Types.ObjectId(senderUserId));
    await mongoose.connection.close();
  });

  // Integration test: Successfully accept friend request
  // Input: Valid pending request ID
  // Expected behavior: Updates request status to accepted in database
  // Expected output: 200 status, updated request
  test('Successfully accepts friend request in database', async () => {
    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('message', 'Friend request accepted');
    expect(response.body.data).toHaveProperty('status', 'accepted');
    expect(response.body.data._id).toBe(pendingRequestId);

    // Verify it was actually updated in database
    const updatedRequest = await FriendRequest.findById(pendingRequestId);
    expect(updatedRequest).not.toBeNull();
    expect(updatedRequest!.status).toBe('accepted');
  });

  // Integration test: Accept request without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app)
      .post('/api/friends/accept')
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(401);

    // Verify status wasn't changed in database
    const unchangedRequest = await FriendRequest.findById(pendingRequestId);
    expect(unchangedRequest!.status).toBe('pending');
  });

  // Integration test: Accept with missing requestId
  // Input: Empty body
  // Expected behavior: Returns 400 error
  // Expected output: 400 status
  test('Returns 400 when requestId is missing', async () => {
    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({});

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('message', 'Request ID is required');
  });

  // Integration test: Accept non-existent request
  // Input: Invalid request ID
  // Expected behavior: Returns 200 with null data
  // Expected output: 200 status
  test('Returns 200 with null when request does not exist', async () => {
    const fakeId = new mongoose.Types.ObjectId().toString();

    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: fakeId });

    expect(response.status).toBe(200);
    expect(response.body.data).toBeNull();
  });

  // Integration test: Accept already accepted request
  // Input: Request ID that's already accepted
  // Expected behavior: Updates and returns accepted status
  // Expected output: 200 status
  test('Handles accepting already accepted request', async () => {
    // First acceptance
    await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    // Second acceptance (idempotent)
    const response = await request(app)
      .post('/api/friends/accept')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ requestId: pendingRequestId });

    expect(response.status).toBe(200);
    expect(response.body.data.status).toBe('accepted');
  });
});
