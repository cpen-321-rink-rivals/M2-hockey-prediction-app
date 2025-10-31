import { describe, expect, test, jest } from '@jest/globals';

// Example: Testing async functions
async function fetchUserData(
  userId: string
): Promise<{ id: string; name: string }> {
  // Simulate API call
  return new Promise(resolve => {
    setTimeout(() => {
      resolve({ id: userId, name: `User ${userId}` });
    }, 100);
  });
}

async function fetchWithError(): Promise<never> {
  throw new Error('Network error');
}

describe('Async Operations', () => {
  test('should fetch user data', async () => {
    const result = await fetchUserData('123');
    expect(result).toEqual({ id: '123', name: 'User 123' });
  });

  test('should have correct properties', async () => {
    const result = await fetchUserData('456');
    expect(result).toHaveProperty('id');
    expect(result).toHaveProperty('name');
    expect(result.id).toBe('456');
  });

  test('should handle errors', async () => {
    await expect(fetchWithError()).rejects.toThrow('Network error');
  });
});

// Example: Testing arrays and objects
describe('Array Operations', () => {
  const numbers = [1, 2, 3, 4, 5];

  test('should contain specific elements', () => {
    expect(numbers).toContain(3);
    expect(numbers).not.toContain(10);
  });

  test('should have correct length', () => {
    expect(numbers).toHaveLength(5);
  });

  test('should match array', () => {
    expect(numbers).toEqual([1, 2, 3, 4, 5]);
  });

  test('should check if array includes subset', () => {
    expect(numbers).toEqual(expect.arrayContaining([2, 4]));
  });
});

// Example: Testing objects
describe('Object Operations', () => {
  const user = {
    id: 1,
    name: 'John Doe',
    email: 'john@example.com',
    isActive: true,
  };

  test('should have specific properties', () => {
    expect(user).toHaveProperty('name');
    expect(user).toHaveProperty('email', 'john@example.com');
  });

  test('should match object structure', () => {
    expect(user).toMatchObject({
      name: 'John Doe',
      isActive: true,
    });
  });

  test('should equal complete object', () => {
    expect(user).toEqual({
      id: 1,
      name: 'John Doe',
      email: 'john@example.com',
      isActive: true,
    });
  });
});

// Example: Testing strings
describe('String Operations', () => {
  const message = 'Hello, World!';

  test('should contain substring', () => {
    expect(message).toContain('World');
  });

  test('should match regex pattern', () => {
    expect(message).toMatch(/Hello/);
    expect(message).toMatch(/^Hello.*!$/);
  });

  test('should have correct length', () => {
    expect(message).toHaveLength(13);
  });
});

// Example: Using test.each for multiple test cases
describe('Parameterized Tests', () => {
  test.each([
    [1, 1, 2],
    [2, 3, 5],
    [10, 5, 15],
    [-5, 5, 0],
  ])('should add %i + %i = %i', (a, b, expected) => {
    expect(a + b).toBe(expected);
  });

  test.each([
    ['hello', true],
    ['', false],
    ['test', true],
  ])('should check if "%s" is truthy: %s', (value, expected) => {
    expect(!!value).toBe(expected);
  });
});

// Example: Testing with mocks
describe('Mock Functions', () => {
  test('should call mock function', () => {
    const mockFn = jest.fn();
    mockFn('test');
    mockFn('another call');

    expect(mockFn).toHaveBeenCalled();
    expect(mockFn).toHaveBeenCalledTimes(2);
    expect(mockFn).toHaveBeenCalledWith('test');
  });

  test('should mock return value', () => {
    const mockFn = jest.fn().mockReturnValue('mocked value');

    const result = mockFn();
    expect(result).toBe('mocked value');
  });

  test('should mock resolved promise', async () => {
    const mockFn = jest
      .fn<() => Promise<{ success: boolean }>>()
      .mockResolvedValue({ success: true });

    const result = await mockFn();
    expect(result).toEqual({ success: true });
  });
});

// Example: Testing truthiness
describe('Truthiness', () => {
  test('should be truthy', () => {
    expect(true).toBeTruthy();
    expect(1).toBeTruthy();
    expect('hello').toBeTruthy();
  });

  test('should be falsy', () => {
    expect(false).toBeFalsy();
    expect(0).toBeFalsy();
    expect('').toBeFalsy();
    expect(null).toBeFalsy();
    expect(undefined).toBeFalsy();
  });

  test('should be defined', () => {
    const value = 'test';
    expect(value).toBeDefined();
    expect(undefined).not.toBeDefined();
  });

  test('should be null', () => {
    const value = null;
    expect(value).toBeNull();
  });
});

// Example: Testing numbers
describe('Number Comparisons', () => {
  test('should be greater than', () => {
    expect(10).toBeGreaterThan(5);
    expect(10).toBeGreaterThanOrEqual(10);
  });

  test('should be less than', () => {
    expect(5).toBeLessThan(10);
    expect(5).toBeLessThanOrEqual(5);
  });

  test('should be close to (for floating point)', () => {
    expect(0.1 + 0.2).toBeCloseTo(0.3);
  });
});
