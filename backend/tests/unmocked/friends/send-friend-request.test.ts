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
import { connectDB } from '../../../src/database';
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

// Interface POST /api/friends/request (Integration)
describe('Unmocked POST /api/friends/request', () => {
  let authToken: string;
  let testUserId: string;
  let receiverUserId: string;
  let receiverFriendCode: string;

  beforeAll(async () => {
    await connectDB();

    // Create test sender user
    const testUser = await userModel.create({
      email: 'sender-unmocked@test.com',
      name: 'Test Sender',
      googleId: 'google-sender-123',
    });
    testUserId = testUser._id.toString();

    // Create test receiver user with friend code
    const receiverUser = await userModel.create({
      email: 'receiver-unmocked@test.com',
      name: 'Test Receiver',
      googleId: 'google-receiver-123',
    });
    receiverUserId = receiverUser._id.toString();
    receiverFriendCode = receiverUser.friendCode;

    // Generate auth token
    authToken = jwt.sign({ id: testUserId }, process.env.JWT_SECRET!, {
      expiresIn: '1h',
    });
  });

  afterEach(async () => {
    // Clean up friend requests after each test
    await FriendRequest.deleteMany({});
  });

  afterAll(async () => {
    // Cleanup all test data
    await FriendRequest.deleteMany({});
    await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    await userModel.delete(new mongoose.Types.ObjectId(receiverUserId));
    await mongoose.connection.close();
  });

  // Integration test: Successfully send friend request
  // Input: Valid receiver friend code
  // Expected behavior: Creates friend request in database
  // Expected output: 201 status, friend request object
  test('Successfully sends friend request to real database', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(201);
    expect(response.body).toHaveProperty('message', 'Friend request sent');
    expect(response.body.data).toHaveProperty('sender', testUserId);
    expect(response.body.data).toHaveProperty('receiver', receiverUserId);
    expect(response.body.data).toHaveProperty('status', 'pending');

    // Verify it was actually saved to database
    const savedRequest = await FriendRequest.findById(response.body.data._id);
    expect(savedRequest).not.toBeNull();
    expect(savedRequest!.status).toBe('pending');
  });

  // Integration test: Duplicate friend request
  // Input: Send same request twice
  // Expected behavior: Second request fails
  // Expected output: 500 status with error message
  test('Returns error when sending duplicate friend request', async () => {
    // First request
    const firstResponse = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: receiverFriendCode });

    expect(firstResponse.status).toBe(201);

    // Second request (duplicate) - should fail
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(500);
  });

  // Integration test: Send request with non-existent friend code
  // Input: Invalid friend code
  // Expected behavior: Returns 404 error
  // Expected output: 404 status, error message
  test('Returns 404 when receiver not found in database', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: 'NONEXISTENT123' });

    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('message', 'User not found');
  });

  // Integration test: Send request without authentication
  // Input: No auth token
  // Expected behavior: Returns 401 unauthorized
  // Expected output: 401 status
  test('Returns 401 when not authenticated', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .send({ receiverCode: receiverFriendCode });

    expect(response.status).toBe(401);
  });

  // Integration test: Validation error
  // Input: Missing receiverCode
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status
  test('Returns 400 when receiverCode is missing', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({});

    expect(response.status).toBe(400);
  });

  // Integration test: Empty receiverCode
  // Input: Empty string receiverCode
  // Expected behavior: Returns 400 validation error
  // Expected output: 400 status
  test('Returns 400 when receiverCode is empty string', async () => {
    const response = await request(app)
      .post('/api/friends/request')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ receiverCode: '' });

    expect(response.status).toBe(400);
  });
});
