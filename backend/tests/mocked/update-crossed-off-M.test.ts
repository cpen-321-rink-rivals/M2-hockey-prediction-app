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

// Interface PUT /api/tickets/:id/crossedOff
describe('Mocked PUT /api/tickets/:id/crossedOff', () => {
  let authToken: string;
  let testUserId: string;
  let testTicketId: string;

  // For mocked tests we do not connect to a real DB; instead mock user lookup
  beforeAll(() => {
    // Silence console.error during tests
    jest.spyOn(console, 'error').mockImplementation(() => {});

    // create a fake user id and token
    testUserId = new mongoose.Types.ObjectId().toString();
    testTicketId = new mongoose.Types.ObjectId().toString();
    authToken = jwt.sign(
      { id: testUserId },
      process.env.JWT_SECRET || 'test-secret'
    );

    // Ensure userModel.findById returns a fake user so authenticateToken passes
    jest.spyOn(userModel, 'findById').mockImplementation(async (id: any) => {
      // return an object that looks like a user document
      return {
        _id: id,
        googleId: 'mock-google-id',
        email: 'mock@example.com',
        name: 'Mock User',
      } as any;
    });
  });

  // Restore mocks after tests
  afterAll(() => {
    jest.restoreAllMocks();
  });

  // Mocked behavior: Ticket.findByIdAndUpdate throws an error
  // Input: valid ticket id and crossedOff array
  // Expected status code: 500
  // Expected behavior: the error was handled gracefully
  // Expected output: Error object with message
  test('Database throws when Ticket.findByIdAndUpdate fails', async () => {
    // Arrange: mock Ticket.findByIdAndUpdate to throw
    jest.spyOn(Ticket, 'findByIdAndUpdate').mockImplementationOnce(() => {
      throw new Error('Forced DB error');
    });

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

    // Act
    const res = await request(app)
      .put(`/api/tickets/${testTicketId}/crossedOff`)
      .set('Authorization', `Bearer ${authToken}`)
      .send({ crossedOff });

    // Assert: controller should return 500 on DB error
    expect(res.status).toBe(500);
    expect(Ticket.findByIdAndUpdate).toHaveBeenCalledWith(
      testTicketId,
      { crossedOff },
      { new: true }
    );
    expect(res.body).toHaveProperty('error');
  });
});
