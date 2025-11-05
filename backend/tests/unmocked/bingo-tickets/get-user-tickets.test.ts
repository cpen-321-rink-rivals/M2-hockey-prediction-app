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
import { connectDB } from '../../../src/database';
import router from '../../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../../src/models/user.model';
import { Ticket } from '../../../src/models/tickets.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../../.env.test') });

// Create Express app for testing (same setup as index.ts)
const app = express();
app.use(express.json());
app.use('/api', router);

describe('Unmocked GET /api/tickets/user/:userId', () => {
  let authToken: string;
  let testUserId: string;
  let testTicketIds: string[] = [];

  // Connect to test database and create test user before all tests
  beforeAll(async () => {
    await connectDB();

    // Create a test user
    const testUser = await userModel.create({
      googleId: 'test-google-id-get-user-tickets',
      email: 'testuser-get@example.com',
      name: 'Test User Get',
    });
    testUserId = testUser._id.toString();

    // Generate a valid JWT token for the test user
    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Create some test tickets for this user
    const ticket1 = await Ticket.create({
      userId: testUserId,
      name: 'Test Ticket 1',
      game: {
        id: 2024020101,
        homeTeam: { abbrev: 'TOR' },
        awayTeam: { abbrev: 'MTL' },
      },
      events: Array.from({ length: 9 }, (_, i) => `Event ${i + 1}`),
    });
    testTicketIds.push(ticket1._id.toString());

    const ticket2 = await Ticket.create({
      userId: testUserId,
      name: 'Test Ticket 2',
      game: {
        id: 2024020102,
        homeTeam: { abbrev: 'VAN' },
        awayTeam: { abbrev: 'EDM' },
      },
      events: Array.from({ length: 9 }, (_, i) => `Event ${i + 10}`),
    });
    testTicketIds.push(ticket2._id.toString());
  }, 30000); // 30 second timeout for setup

  // Cleanup: delete test tickets, user and disconnect after all tests
  afterAll(async () => {
    for (const ticketId of testTicketIds) {
      await Ticket.findByIdAndDelete(ticketId);
    }
    if (testUserId) {
      await userModel.delete(new mongoose.Types.ObjectId(testUserId));
    }
    await mongoose.connection.close();
  });

  // Input: valid userId with tickets
  // Expected status code: 200
  // Expected behavior: returns all tickets for the user sorted by createdAt descending
  // Expected output: array of tickets
  test('Valid userId with tickets', async () => {
    // Act: Make GET request to /api/tickets/user/:userId with auth token
    const response = await request(app)
      .get(`/api/tickets/user/${testUserId}`)
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBe(2);
    expect(response.body[0].name).toBe('Test Ticket 2'); // Most recent first
    expect(response.body[1].name).toBe('Test Ticket 1');
  });

  // Input: valid userId with no tickets
  // Expected status code: 200
  // Expected behavior: returns empty array
  // Expected output: empty array
  test('Valid userId with no tickets', async () => {
    const differentUserId = new mongoose.Types.ObjectId().toString();

    // Act: Make GET request with userId that has no tickets
    const response = await request(app)
      .get(`/api/tickets/user/${differentUserId}`)
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(Array.isArray(response.body)).toBe(true);
    expect(response.body.length).toBe(0);
  });

  // Input: no auth token
  // Expected status code: 401
  // Expected behavior: authentication fails
  // Expected output: error message
  test('Invalid authorization', async () => {
    // Act: Make GET request WITHOUT auth token
    const response = await request(app)
      .get(`/api/tickets/user/${testUserId}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(401);
  });
});
