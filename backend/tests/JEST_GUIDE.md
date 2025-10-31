# Jest Testing Guide

## 🎯 Why the import wasn't working

The issue was:

1. **Mixed module syntax**: You had `require()` (CommonJS) and `import` (ES modules) mixed
2. **No Jest config**: Jest didn't know how to handle TypeScript files
3. **Fixed with**: `jest.config.js` that uses `ts-jest` preset

## 📚 Test Files Created

### 1. `sum.test.ts` - Basic tests

Simple addition function tests with multiple scenarios

### 2. `calculator.test.ts` - Class testing

Shows how to:

- Test class methods
- Use `beforeEach` and `afterEach` hooks
- Group related tests with nested `describe` blocks
- Test error throwing

### 3. `jest-examples.test.ts` - Comprehensive examples

Covers:

- Async/await testing
- Array and object matchers
- String testing
- Parameterized tests with `test.each`
- Mock functions
- Truthiness checks
- Number comparisons

### 4. `advanced-examples.test.ts` - Real-world patterns

Shows:

- Testing Express-like controllers
- Mocking service classes
- Error handling
- Timer mocking

## 🚀 Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage

# Run specific test file
npm test sum.test.ts

# Run tests matching pattern
npm test -- --testNamePattern="should add"
```

## 📖 Common Jest Matchers

### Equality

```typescript
expect(value).toBe(5); // Exact equality (===)
expect(value).toEqual({ a: 1 }); // Deep equality
expect(value).not.toBe(null); // Negation
```

### Truthiness

```typescript
expect(value).toBeTruthy(); // Truthy check
expect(value).toBeFalsy(); // Falsy check
expect(value).toBeNull(); // null check
expect(value).toBeDefined(); // not undefined
expect(value).toBeUndefined(); // is undefined
```

### Numbers

```typescript
expect(value).toBeGreaterThan(3);
expect(value).toBeGreaterThanOrEqual(3);
expect(value).toBeLessThan(5);
expect(value).toBeLessThanOrEqual(5);
expect(0.1 + 0.2).toBeCloseTo(0.3); // For floats
```

### Strings

```typescript
expect(str).toMatch(/pattern/);
expect(str).toContain('substring');
expect(str).toHaveLength(5);
```

### Arrays

```typescript
expect(arr).toContain(item);
expect(arr).toHaveLength(3);
expect(arr).toEqual([1, 2, 3]);
expect(arr).toEqual(expect.arrayContaining([2, 3]));
```

### Objects

```typescript
expect(obj).toHaveProperty('name');
expect(obj).toHaveProperty('name', 'John');
expect(obj).toMatchObject({ name: 'John' });
expect(obj).toEqual({ name: 'John', age: 30 });
```

### Exceptions

```typescript
expect(() => fn()).toThrow();
expect(() => fn()).toThrow('error message');
expect(() => fn()).toThrow(ErrorClass);
expect(async () => await fn()).rejects.toThrow();
```

### Async

```typescript
// Promise-based
await expect(promise).resolves.toBe(value);
await expect(promise).rejects.toThrow();

// Async/await
const result = await asyncFn();
expect(result).toBe(value);
```

## 🎭 Mocking

### Mock Functions

```typescript
const mockFn = jest.fn();
mockFn('arg');

expect(mockFn).toHaveBeenCalled();
expect(mockFn).toHaveBeenCalledTimes(1);
expect(mockFn).toHaveBeenCalledWith('arg');
```

### Mock Return Values

```typescript
const mock = jest.fn().mockReturnValue('value');
const mock = jest.fn().mockResolvedValue('async value');
const mock = jest.fn().mockRejectedValue(new Error('error'));
```

### Spy on Methods

```typescript
jest.spyOn(object, 'method').mockImplementation(() => 'mocked');
jest.spyOn(object, 'method').mockResolvedValue('value');
```

## 🔄 Lifecycle Hooks

```typescript
describe('Test Suite', () => {
  beforeAll(() => {
    // Runs once before all tests
  });

  afterAll(() => {
    // Runs once after all tests
  });

  beforeEach(() => {
    // Runs before each test
  });

  afterEach(() => {
    // Runs after each test
  });

  test('test case', () => {
    // Test code
  });
});
```

## 📊 Test Organization

```typescript
describe('Feature Name', () => {
  describe('Sub-feature', () => {
    test('should do something', () => {
      // Test
    });

    test('should do another thing', () => {
      // Test
    });
  });

  describe('Another sub-feature', () => {
    // More tests
  });
});
```

## 🎯 Best Practices

1. **One assertion per test** (when possible)
2. **Use descriptive test names**
3. **Arrange, Act, Assert pattern**
4. **Mock external dependencies**
5. **Test edge cases and error conditions**
6. **Keep tests independent**
7. **Use `beforeEach` for setup**
8. **Group related tests with `describe`**

## 📝 Naming Conventions

```typescript
// Good test names
test('should add two numbers correctly');
test('should throw error when dividing by zero');
test('should return null when user not found');

// Bad test names
test('test1');
test('works');
test('check function');
```

## 🔍 Debugging Tests

```typescript
// Add .only to run single test
test.only('should run only this test', () => {});

// Skip tests
test.skip('should skip this test', () => {});

// Use console.log
test('debug test', () => {
  console.log('Debug info:', value);
  expect(value).toBe(5);
});
```

## 🎓 Next Steps

1. Test your actual backend controllers
2. Mock database calls using `jest.spyOn()`
3. Test API endpoints with supertest
4. Add integration tests
5. Set up continuous integration

Happy Testing! 🚀
