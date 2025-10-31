```

import { describe, expect, test } from '@jest/globals';
import fs from 'fs';
import app from '../../src/app.js';

// Interface POST /photo
describe('Unmocked: POST /media/upload', () => {
  // Input: icon-32.png is a valid photo
  // Expected status code: 201
  // Expected behavior: photo is added to the database
  // Expected output: id of the uploaded photo
  test('Valid Photo', async () => {
    const photo = fs.readFileSync('tests/res/icon-32.png');
    const res = await app.post('/media/upload').attach('photo', photo);
    expect(res.status).toStrictEqual(201);
    expect(typeof res.body).toBe('number'); // Expect returned id
    const insertedPhoto = database
      .getAllPhotos()
      .find(photo => photo.id === res.body);
    expect(insertedPhoto).toBeDefined();
    expect(insertedPhoto.content).toStrictEqual(photo);
  });

  // Input: no photo attached to request
  // Expected status code: 400
  // Expected behavior: database is unchanged
  // Expected output: None
  test('No Photo', async () => {
    // ...
  });

  // Input: bad_photo.txt is a not a valid photo
  // Expected status code: 400
  // Expected behavior: database is unchanged
  // Expected output: None
  test('Invalid Photo', async () => {
    // ...
  });

  // more tests...
});

```;
