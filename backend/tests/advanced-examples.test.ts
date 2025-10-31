import {
  describe,
  expect,
  test,
  jest,
  beforeEach,
  afterEach,
} from '@jest/globals';

// Example: Testing Express-like controller functions
interface MockRequest {
  body?: any;
  params?: any;
  query?: any;
  headers?: any;
}

interface MockResponse {
  status: jest.Mock<(code: number) => MockResponse>;
  json: jest.Mock<(data: any) => MockResponse>;
  send: jest.Mock<(data: any) => MockResponse>;
}

// Mock controller function
const createUser = async (req: MockRequest, res: MockResponse) => {
  const { name, email } = req.body;

  if (!name || !email) {
    return res.status(400).json({ error: 'Name and email are required' });
  }

  // Simulate database operation
  const user = {
    id: '123',
    name,
    email,
    createdAt: new Date().toISOString(),
  };

  return res.status(201).json({ data: user });
};

describe('User Controller', () => {
  let mockReq: MockRequest;
  let mockRes: MockResponse;

  beforeEach(() => {
    mockReq = {
      body: {},
      params: {},
      query: {},
    };

    mockRes = {
      status: jest.fn().mockReturnThis() as any,
      json: jest.fn().mockReturnThis() as any,
      send: jest.fn().mockReturnThis() as any,
    };
  });

  describe('createUser', () => {
    test('should create user with valid data', async () => {
      mockReq.body = {
        name: 'John Doe',
        email: 'john@example.com',
      };

      await createUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(201);
      expect(mockRes.json).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({
            name: 'John Doe',
            email: 'john@example.com',
          }),
        })
      );
    });

    test('should return 400 when name is missing', async () => {
      mockReq.body = {
        email: 'john@example.com',
      };

      await createUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: 'Name and email are required',
      });
    });

    test('should return 400 when email is missing', async () => {
      mockReq.body = {
        name: 'John Doe',
      };

      await createUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
      expect(mockRes.json).toHaveBeenCalledWith({
        error: 'Name and email are required',
      });
    });

    test('should return 400 when both fields are missing', async () => {
      mockReq.body = {};

      await createUser(mockReq, mockRes);

      expect(mockRes.status).toHaveBeenCalledWith(400);
    });
  });
});

// Example: Testing utility functions with mocks
interface User {
  id: string;
  name: string;
  email: string;
}

class UserService {
  async getUserById(id: string): Promise<User | null> {
    // This would normally call a database
    throw new Error('Not implemented');
  }

  async createUser(name: string, email: string): Promise<User> {
    throw new Error('Not implemented');
  }
}

describe('UserService with Mocks', () => {
  let userService: UserService;

  beforeEach(() => {
    userService = new UserService();
  });

  test('should get user by id', async () => {
    // Mock the getUserById method
    const mockUser = { id: '1', name: 'Test User', email: 'test@test.com' };
    jest.spyOn(userService, 'getUserById').mockResolvedValue(mockUser);

    const result = await userService.getUserById('1');

    expect(result).toEqual(mockUser);
    expect(userService.getUserById).toHaveBeenCalledWith('1');
  });

  test('should return null for non-existent user', async () => {
    jest.spyOn(userService, 'getUserById').mockResolvedValue(null);

    const result = await userService.getUserById('999');

    expect(result).toBeNull();
  });

  test('should create user', async () => {
    const newUser = { id: '2', name: 'New User', email: 'new@test.com' };
    jest.spyOn(userService, 'createUser').mockResolvedValue(newUser);

    const result = await userService.createUser('New User', 'new@test.com');

    expect(result).toEqual(newUser);
    expect(userService.createUser).toHaveBeenCalledWith(
      'New User',
      'new@test.com'
    );
  });
});

// Example: Testing error handling
describe('Error Handling', () => {
  test('should catch and handle errors', async () => {
    const errorFunction = async () => {
      throw new Error('Something went wrong');
    };

    await expect(errorFunction()).rejects.toThrow('Something went wrong');
  });

  test('should handle specific error types', async () => {
    class CustomError extends Error {
      constructor(message: string) {
        super(message);
        this.name = 'CustomError';
      }
    }

    const throwCustomError = () => {
      throw new CustomError('Custom error occurred');
    };

    expect(throwCustomError).toThrow(CustomError);
    expect(throwCustomError).toThrow('Custom error occurred');
  });
});

// Example: Testing with timers
describe('Timer Functions', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  test('should execute callback after delay', () => {
    const callback = jest.fn();

    setTimeout(callback, 1000);

    expect(callback).not.toHaveBeenCalled();

    jest.advanceTimersByTime(1000);

    expect(callback).toHaveBeenCalled();
  });

  test('should execute multiple timers', () => {
    const callback1 = jest.fn();
    const callback2 = jest.fn();

    setTimeout(callback1, 1000);
    setTimeout(callback2, 2000);

    jest.advanceTimersByTime(1500);
    expect(callback1).toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();

    jest.advanceTimersByTime(500);
    expect(callback2).toHaveBeenCalled();
  });
});
