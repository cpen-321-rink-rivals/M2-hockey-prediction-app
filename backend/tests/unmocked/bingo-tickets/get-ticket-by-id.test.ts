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
import { Ticket } from '../../../src/models/tickets.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Helper to create an EventCondition-like object (matches new Ticket schema)
const makeEvent = (i: number) => ({
  id: `event-${i}`,
  category: 'TEAM',
  subject: 'GOAL',
  comparison: 'GREATER_OR_EQUAL',
  threshold: 1,
  teamAbbrev: 'TOR',
});

// Create Express app for testing (same setup as index.ts)
const app = express();
app.use(express.json());
app.use('/api', router);

describe('Unmocked GET /api/tickets/:id', () => {
  let authToken: string;
  let testUserId: string;
  let testTicketId: string;

  // Connect to test database and create test user before all tests
  beforeAll(async () => {
    await connectDB();

    // Create a test user
    const testUser = await userModel.create({
      googleId: 'test-google-id-get-by-id',
      email: 'testuser-getid@example.com',
      name: 'Test User Get By ID',
    });
    testUserId = testUser._id.toString();

    // Generate a valid JWT token for the test user
    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Create a test ticket
    const ticket = await Ticket.create({
      userId: testUserId,
      name: 'Test Ticket for GetById',
      game: {
        id: 2024020103,
        homeTeam: { abbrev: 'BOS' },
        awayTeam: { abbrev: 'NYR' },
      },
      events: Array.from({ length: 9 }, (_, i) => makeEvent(i + 1)),
    });
    testTicketId = ticket._id.toString();
  }, 30000); // 30 second timeout for setup

  // Cleanup: delete test ticket, user and disconnect after all tests
  afterAll(async () => {
    if (testTicketId) {
      await Ticket.findByIdAndDelete(testTicketId);
    }
    if (testUserId) {
      await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    }
    await mongoose.connection.close();
  });

  // Input: valid ticket id
  // Expected status code: 200
  // Expected behavior: returns the ticket
  // Expected output: ticket object
  test('Valid ticket id', async () => {
    // Act: Make GET request to /api/tickets/:id with auth token
    const response = await request(app)
      .get(`/api/tickets/${testTicketId}`)
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('_id', testTicketId);
    expect(response.body.name).toBe('Test Ticket for GetById');
    expect(response.body.userId).toBe(testUserId);
    expect(response.body.events).toHaveLength(9);
  });

  // Input: non-existent ticket id
  // Expected status code: 404
  // Expected behavior: ticket not found
  // Expected output: error message
  test('Non-existent ticket id', async () => {
    const fakeTicketId = new mongoose.Types.ObjectId().toString();

    // Act: Make GET request with non-existent id
    const response = await request(app)
      .get(`/api/tickets/${fakeTicketId}`)
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('message', 'Ticket not found');
  });

  // Input: invalid ticket id format
  // Expected status code: 500
  // Expected behavior: error handling
  // Expected output: error message
  test('Invalid ticket id format', async () => {
    // Act: Make GET request with invalid id format
    const response = await request(app)
      .get('/api/tickets/invalid-id-format')
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(500);
    expect(response.body).toHaveProperty('message', 'Server error');
  });

  // Input: no auth token
  // Expected status code: 401
  // Expected behavior: authentication fails
  // Expected output: error message
  test('Invalid authorization', async () => {
    // Act: Make GET request WITHOUT auth token
    const response = await request(app)
      .get(`/api/tickets/${testTicketId}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(401);
  });
});
