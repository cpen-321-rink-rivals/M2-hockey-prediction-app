import {
  describe,
  expect,
  test,
  jest,
  beforeAll,
  afterAll,
} from '@jest/globals';
import dotenv from 'dotenv';
import request from 'supertest';
import express from 'express';
import { connectDB } from '../../../src/config/database';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing (same setup as index.ts)
const app = express();
app.use(express.json());
app.use('/api', router);

// Interface POST /api/tickets
describe('Unmocked POST /api/tickets', () => {
  let authToken: string;
  let testUserId: string;

  // Connect to test database and create test user before all tests
  beforeAll(async () => {
    await connectDB();

    // Create a test user
    const testUser = await userModel.create({
      googleId: 'test-google-id-123',
      email: 'testuser@example.com',
      name: 'Test User',
    });
    testUserId = testUser._id.toString();

    // Generate a valid JWT token for the test user
    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );
  }, 30000); // 30 second timeout for setup

  // Cleanup: delete test user and disconnect after all tests
  afterAll(async () => {
    if (testUserId) {
      await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    }
    await mongoose.connection.close();
  });

  // Input: valid ticket
  // Expected status code: 201
  // Expected behavior: ticket is added to the database
  // Expected output: ticket details with _id
  test('Valid ticket', async () => {
    // Arrange: Create a valid bingo ticket payload
    const validBingoTicket = {
      userId: testUserId,
      name: 'My Test Ticket',
      game: {
        id: 2024020100,
        homeTeam: {
          abbrev: 'TOR',
        },
        awayTeam: {
          abbrev: 'MTL',
        },
      },
      events: [
        'Goal scored',
        'Penalty called',
        'Powerplay goal',
        'Goalie save',
        'Fight',
        'Hat trick',
        'Overtime',
        'Shutout',
        'Game winner',
      ],
    };

    // Act: Make POST request to /api/tickets with auth token
    const response = await request(app)
      .post('/api/tickets')
      .set('Authorization', `Bearer ${authToken}`)
      .send(validBingoTicket)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(201);
    expect(response.body).toHaveProperty('_id');
    expect(response.body.userId).toBe(validBingoTicket.userId);
    expect(response.body.name).toBe(validBingoTicket.name);
    expect(response.body.events).toEqual(validBingoTicket.events);
    expect(response.body.crossedOff).toEqual(Array(9).fill(false));
    expect(response.body).toHaveProperty('createdAt');

    // Verify that the ticket was actually inserted in the database
    const insertedTicket = await mongoose
      .model('Ticket')
      .findById(response.body._id)
      .exec();

    expect(insertedTicket).not.toBeNull();
    expect(insertedTicket!.userId.toString()).toBe(validBingoTicket.userId);
    expect(insertedTicket!.name).toBe(validBingoTicket.name);
    expect(insertedTicket!.events).toEqual(validBingoTicket.events);
    expect(insertedTicket!.crossedOff).toEqual(Array(9).fill(false));

    // Cleanup: Delete the created ticket
    await mongoose.model('Ticket').findByIdAndDelete(response.body._id);
  });

  // Input: no ticket data
  // Expected status code: 400
  // Expected behavior: database remains unchanged
  // Expected output: none
  test('No ticket', async () => {
    // Act: Make empty POST request to /api/tickets with auth token
    const response = await request(app)
      .post('/api/tickets')
      .set('Authorization', `Bearer ${authToken}`)
      .send()
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(400);
  });

  // Input: invalid ticket
  // Expected status code: 400
  // Expected behavior: database remains unchanged
  // Expected output: none
  test('Invalid ticket', async () => {
    // Arrange: Create an invalid bingo ticket payload (missing events)
    const invalidBingoTicket = {
      userId: testUserId,
      name: 'Invalid Ticket',
      // events field is missing
    };

    // Act: Make POST request to /api/tickets with auth token
    const response = await request(app)
      .post('/api/tickets')
      .set('Authorization', `Bearer ${authToken}`)
      .send(invalidBingoTicket)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(400);
  });

  // Input: no auth token
  // Expected status code: 401
  // Expected behavior: database remains unchanged
  // Expected output: none
  test('Invalid authorization', async () => {
    // Arrange: Create a valid bingo ticket payload
    const validBingoTicket = {
      userId: testUserId,
      name: 'My Test Ticket',
      game: {
        id: 2024020100,
        homeTeam: {
          abbrev: 'TOR',
        },
        awayTeam: {
          abbrev: 'MTL',
        },
      },
      events: [
        'Goal scored',
        'Penalty called',
        'Powerplay goal',
        'Goalie save',
        'Fight',
        'Hat trick',
        'Overtime',
        'Shutout',
        'Game winner',
      ],
    };

    // Act: Make POST request to /api/tickets WITHOUT auth token
    const response = await request(app)
      .post('/api/tickets')
      // No Authorization header
      .send(validBingoTicket)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(401);
  });
});
