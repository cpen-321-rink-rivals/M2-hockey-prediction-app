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

describe('Unmocked PUT /api/tickets/:id/crossedOff', () => {
  let authToken: string;
  let testUserId: string;
  let testTicketId: string;

  // Connect to test database and create test user before all tests
  beforeAll(async () => {
    await connectDB();

    // Create a test user
    const testUser = await userModel.create({
      googleId: 'test-google-id-update',
      email: 'testuser-update@example.com',
      name: 'Test User Update',
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
      name: 'Test Ticket for Update',
      game: {
        id: 2024020106,
        homeTeam: { abbrev: 'SEA' },
        awayTeam: { abbrev: 'VGK' },
      },
      events: Array.from({ length: 9 }, (_, i) => `Event ${i + 1}`),
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

  // Input: valid ticket id and crossedOff array (object format)
  // Expected status code: 200
  // Expected behavior: ticket crossedOff is updated in database
  // Expected output: updated ticket object
  test('Valid crossedOff update with object format', async () => {
    const crossedOff = [
      true,
      false,
      true,
      false,
      true,
      false,
      true,
      false,
      true,
    ];

    // Act: Make PUT request to /api/tickets/:id/crossedOff with auth token
    const response = await request(app)
      .put(`/api/tickets/${testTicketId}/crossedOff`)
      .set('Authorization', `Bearer ${authToken}`)
      .send({ crossedOff })
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('_id', testTicketId);
    expect(response.body.crossedOff).toEqual(crossedOff);

    // Verify the update in database
    const updatedTicket = await Ticket.findById(testTicketId);
    expect(updatedTicket).not.toBeNull();
    expect(updatedTicket!.crossedOff).toEqual(crossedOff);
  });

  // Input: valid ticket id and crossedOff array (array format)
  // Expected status code: 200
  // Expected behavior: ticket crossedOff is updated in database
  // Expected output: updated ticket object
  test('Valid crossedOff update with array format', async () => {
    const crossedOff = [
      false,
      true,
      false,
      true,
      false,
      true,
      false,
      true,
      false,
    ];

    // Act: Make PUT request sending array directly
    const response = await request(app)
      .put(`/api/tickets/${testTicketId}/crossedOff`)
      .set('Authorization', `Bearer ${authToken}`)
      .send(crossedOff)
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('_id', testTicketId);
    expect(response.body.crossedOff).toEqual(crossedOff);

    // Verify the update in database
    const updatedTicket = await Ticket.findById(testTicketId);
    expect(updatedTicket).not.toBeNull();
    expect(updatedTicket!.crossedOff).toEqual(crossedOff);
  });

  // Input: non-existent ticket id
  // Expected status code: 404
  // Expected behavior: no changes to database
  // Expected output: error message
  test('Non-existent ticket id', async () => {
    const fakeTicketId = new mongoose.Types.ObjectId().toString();
    const crossedOff = [true, true, true, true, true, true, true, true, true];

    // Act: Make PUT request with non-existent id
    const response = await request(app)
      .put(`/api/tickets/${fakeTicketId}/crossedOff`)
      .set('Authorization', `Bearer ${authToken}`)
      .send({ crossedOff })
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(404);
    expect(response.body).toHaveProperty('message', 'Ticket not found');
  });

  // Input: invalid crossedOff format (not an array)
  // Expected status code: 400
  // Expected behavior: no changes to database
  // Expected output: error message
  test('Invalid crossedOff format', async () => {
    // Act: Make PUT request with invalid data
    const response = await request(app)
      .put(`/api/tickets/${testTicketId}/crossedOff`)
      .set('Authorization', `Bearer ${authToken}`)
      .send({ crossedOff: 'not-an-array' })
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty(
      'message',
      'Invalid crossedOff format'
    );
  });

  // Input: invalid ticket id format
  // Expected status code: 500
  // Expected behavior: error handling
  // Expected output: error message
  test('Invalid ticket id format', async () => {
    const crossedOff = [
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
      false,
    ];

    // Act: Make PUT request with invalid id format
    const response = await request(app)
      .put('/api/tickets/invalid-id-format/crossedOff')
      .set('Authorization', `Bearer ${authToken}`)
      .send({ crossedOff })
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(500);
    expect(response.body).toHaveProperty('error');
  });

  // Input: no auth token
  // Expected status code: 401
  // Expected behavior: authentication fails
  // Expected output: error message
  test('Invalid authorization', async () => {
    const crossedOff = [
      true,
      false,
      true,
      false,
      true,
      false,
      true,
      false,
      true,
    ];

    // Act: Make PUT request WITHOUT auth token
    const response = await request(app)
      .put(`/api/tickets/${testTicketId}/crossedOff`)
      .send({ crossedOff })
      .expect('Content-Type', /json/);

    // Assert: Check response
    expect(response.status).toBe(401);
  });
});
