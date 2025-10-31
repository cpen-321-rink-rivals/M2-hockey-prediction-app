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
import { connectDB } from '../../src/database';
import router from '../../src/routes/routes';
import mongoose from 'mongoose';
import jwt from 'jsonwebtoken';
import { userModel } from '../../src/models/user.model';
import { Ticket } from '../../src/models/tickets.model';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '../../.env.test') });

// Create Express app for testing (same setup as index.ts)
const app = express();
app.use(express.json());
app.use('/api', router);

describe('Unmocked DELETE /api/tickets/:id', () => {
  let authToken: string;
  let testUserId: string;
  let testTicketId: string;

  // Connect to test database and create test user before all tests
  beforeAll(async () => {
    await connectDB();

    // Create a test user
    const testUser = await userModel.create({
      googleId: 'test-google-id-delete',
      email: 'testuser-delete@example.com',
      name: 'Test User Delete',
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

  // Input: valid ticket id
  // Expected status code: 200
  // Expected behavior: ticket is deleted from database
  // Expected output: success message
  test('Valid ticket id', async () => {
    // Arrange: Create a test ticket to delete
    const ticket = await Ticket.create({
      userId: testUserId,
      name: 'Test Ticket to Delete',
      game: {
        id: 2024020104,
        homeTeam: { abbrev: 'CHI' },
        awayTeam: { abbrev: 'DET' },
      },
      events: Array.from({ length: 9 }, (_, i) => `Event ${i + 1}`),
    });
    testTicketId = ticket._id.toString();

    // Act: Make DELETE request to /api/tickets/:id with auth token
    const response = await request(app)
      .delete(`/api/tickets/${testTicketId}`)
      .set('Authorization', `Bearer ${authToken}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty(
      'message',
      'Ticket deleted successfully'
    );

    // Verify the ticket was actually deleted from database
    const deletedTicket = await Ticket.findById(testTicketId);
    expect(deletedTicket).toBeNull();
  });

  // Input: non-existent ticket id
  // Expected status code: 404
  // Expected behavior: no changes to database
  // Expected output: error message
  test('Non-existent ticket id', async () => {
    const fakeTicketId = new mongoose.Types.ObjectId().toString();

    // Act: Make DELETE request with non-existent id
    const response = await request(app)
      .delete(`/api/tickets/${fakeTicketId}`)
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
    // Act: Make DELETE request with invalid id format
    const response = await request(app)
      .delete('/api/tickets/invalid-id-format')
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
    // Arrange: Create a ticket
    const ticket = await Ticket.create({
      userId: testUserId,
      name: 'Test Ticket for Auth Test',
      game: {
        id: 2024020105,
        homeTeam: { abbrev: 'LAK' },
        awayTeam: { abbrev: 'ANA' },
      },
      events: Array.from({ length: 9 }, (_, i) => `Event ${i + 1}`),
    });
    const ticketId = ticket._id.toString();

    // Act: Make DELETE request WITHOUT auth token
    const response = await request(app)
      .delete(`/api/tickets/${ticketId}`)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(401);

    // Verify ticket still exists
    const stillExists = await Ticket.findById(ticketId);
    expect(stillExists).not.toBeNull();

    // Cleanup
    await Ticket.findByIdAndDelete(ticketId);
  });
});
